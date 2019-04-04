package com.lxyx.hellplugin.dir

import com.lxyx.hellplugin.common.HellPageMethodConstant
import com.lxyx.hellplugin.dir.activity.HellActivityExecMethodVisitor
import com.lxyx.hellplugin.dir.activity.HellActivityMethodVisitor
import com.lxyx.hellplugin.common.HellConstant
import com.lxyx.hellplugin.dir.activity.HellActivityStubMethod
import com.lxyx.hellplugin.dir.fragment.HellFragmentMethodVisitor
import com.lxyx.hellplugin.dir.view.HelViewMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/3/5.
 *
 * bug：v4包中的Fragment注入callback失败 ！！！！尝试注入android.app.Activity是否也是这种crash栈？？？？？
 * 来推断是不是v4包中的Fragment不能注入？？？？？？？？？
 * 解决方案：
 * 这个棘手问题：监控页面生命周期(Activity/Fragment)时，我们不能对android.jar中的代码进行注入插桩，且继承这
 * 些类的页面类(XxxActivity/XxxFragment)，很大可能也不会override所有我们需要注入插桩的方法:
 * 注入页面行为函数的策略：
 * 1、继承android.app.Activity的页面，下同fragment，无法字节码注入，可以从另外一个角度解决：
 * 扫描当前Project的工程目录文件(注意不是jar文件)，然后有两个具体方案：
 * 【方案1】、扫描当前业务Activity，父类是直接继承android.app.Activity，遍历是否存在需要注入的方法，如果已经存在，
 * 则直接在目标方法中注入插桩；反之，在ClassVistor.visitEnd()中，也就是整个class文件中插入缺失的目标方法，同
 * 时注入插桩，选取在class文件结尾注入的原因是，不改变原先代码行号。--------> 方案1已经实现
 * 【方案2】：自己实现一个BaseActivity，继承android.app.Activity，override目标方法，然后扫描Project的目录，
 * 替换系统Activity为自己生成的BaseActivity即可。 --------> todo：方案2暂未实现
 * 2、继承v4包中的activity和fragment，无需1中方案，直接扫描jar中class文件：FragmentActivity和Fragment，在
 * 对应的目标方法中注入插桩即可。
 *
 * debug调试 Gradle Plugin步骤:
 * 1、【run】->【Edit configures】，针对自己的需要调试的plugin，新建remote调试选项，起个名字+选择对应需要调试的plugin
 * 2、在Terminal中输入命令：./gradlew assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true, 然后
 *    会等待attach对应的plugin，选择【run】->【Edit configures】中对应需要debug的remote选项中的plugin，ok即可。
 * 3、再次进入【Run】->【Debug "选择要debug的插件名"】，即可breakpoint debug。
 * 注意: debug之前，最好./gradlew clean一次工程，因为增量编译的话，可能会跳过一些编译过程，导致brakpoint不能执行到.
 */
class HellDirClassVisitor extends ClassVisitor {
    private String className
    private String superClassName
    private String[] interfaceArray

    HellDirClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name
        superClassName = superName
        interfaceArray = interfaces

        super.visit(version, access, name, signature, superClassName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        MethodVisitor activityEmthodMv = null
        if ('android/app/Activity' == superClassName) {
            println('HellDirClassVisitor, visitMethod: ' + className + ' | ' + name + ' | ' + desc)
            activityEmthodMv = injectActivityMethod(mv, name, desc)
            // 剩余的，没有override的目标方法，则需要在class文件的末尾注入在当前类中，之后再注入插桩，具体位置在visitEnd()中注入
        } else if ('android/app/Fragment' == superClassName) {
            println('HellDirClassVisitor, android/app/Fragment: ' + name + " | " + desc)
            activityEmthodMv = injectFragmentMethod(mv, name, desc)
        }

        MethodVisitor activityExecMv = new HellActivityExecMethodVisitor(
                activityEmthodMv == null ? mv : activityEmthodMv,
                className, interfaceArray)

        // 这里劫持工程代码中所有出现的onClick()方法，为后面的注入做准备
        if (interfaceArray != null && interfaceArray.length > 0) {
            // 点击
            if (interfaceArray.contains('android/view/View$OnClickListener')) {
                if ('onClick' == name && '(Landroid/view/View;)V' == desc) {
                    println('HellDirClassVisitor OnClickListener: inject ok: ' + className)
                    return new HelViewMethodVisitor(activityExecMv, HellConstant.CLICK)
                }
            }

            // 长按
            if (interfaceArray.contains('android/view/View$OnLongClickListener')) {
                if ('onLongClick' == name && '(Landroid/view/View;)Z' == desc) {
                    println('HellDirClassVisitor OnLongClickListener: inject ok: ' + className)
                    return new HelViewMethodVisitor(activityExecMv, HellConstant.LONG_CLICK)
                }
            }

            // ListView中Item点击
            if (interfaceArray.contains('android/widget/AdapterView$OnItemClickListener')) {
                // void onItemClick(AdapterView<?> parent, View view, int position, long id);
                if ('onItemClick' == name &&
                        '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V' == desc) {
                    println('HellDirClassVisitor onItemClick: inject ok: ' + className)
                    return new HelViewMethodVisitor(activityExecMv, HellConstant.LISTVIEW_ITEM_CLICK)
                }
            }
        }

        // 其他行为类似，实际上最常用的是点击、列表item点击

        return activityExecMv
    }

    @Override
    void visitEnd() {
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~ 这是方案1：类文件尾部插入方法(不改变原代码行号) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //  在一个class文件的结尾插入方法，完整方案应该是：输入若干需要监控的方法name+desc+class名，
        //  然后查看当前合法类中是否存在该方法，不存在则在class末尾最后插入该方法，存在的话，直接注入插桩。
        if ('android/app/Activity' == superClassName) {
            println('HellDirClassVisitor, visitEnd: ' + className)
            injectActivityMethodClassTail()
        } else if ('android/app/Fragment' == superClassName) {
            injectFragmentMethodClassTail()
        }

        super.visitEnd()
    }

    private MethodVisitor injectActivityMethod(MethodVisitor mv, String name, String desc) {
        MethodVisitor activityEmthodMv = null
        if ('onCreate' == name && '(Landroid/os/Bundle;)V' == desc) {
            activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onCreate', '(Landroid/os/Bundle;)V')
            // 这里设置状态，表示有了类中已经ovveride这个方法
            HellPageMethodConstant.setMethodState(className, 'onCreate', '(Landroid/os/Bundle;)V', true)
        } else if ('onNewIntent' == name && '(Landroid/content/Intent;)V' == desc) {
            activityEmthodMv = new HellActivityMethodVisitor(mv, className,
                    'onNewIntent', '(Landroid/content/Intent;)V')
            // 这里设置状态，表示有了类中已经ovveride这个方法
            HellPageMethodConstant.setMethodState(className, 'onNewIntent', '(Landroid/content/Intent;)V', true)
        } else if ('onResume' == name && '()V' == desc) {
            activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onResume', '()V')
            // 这里设置状态，表示有了类中已经ovveride这个方法
            HellPageMethodConstant.setMethodState(className, 'onResume', '()V', true)
        } else if ('onPause' == name && '()V' == desc) {
            activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onPause', '()V')
            // 这里设置状态，表示有了类中已经ovveride这个方法
            HellPageMethodConstant.setMethodState(className, 'onPause', '()V', true)
        } else if ('onStop' == name && '()V' == desc) {
            activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onStop', '()V')
            // 这里设置状态，表示有了类中已经ovveride这个方法
            HellPageMethodConstant.setMethodState(className, 'onStop', '()V', true)
        } else if ('onDestroy' == name && '()V' == desc) {
            activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onDestroy', '()V')
            // 这里设置状态，表示有了类中已经ovveride这个方法
            HellPageMethodConstant.setMethodState(className, 'onDestroy', '()V', true)
        }
        return activityEmthodMv
    }

    private MethodVisitor injectFragmentMethod(MethodVisitor mv, String name, String desc) {
        MethodVisitor activityEmthodMv = null
        if ('onCreate' == name && '(Landroid/os/Bundle;)V' == desc) { // void onCreate(android/os/Bundle)
            activityEmthodMv = new HellFragmentMethodVisitor(mv, className, HellConstant.Page_Event_OnCreate)
            HellPageMethodConstant.setMethodState(className, 'onCreate', '(Landroid/os/Bundle;)V', true)
        } else if ('onResume' == name && '()V' == desc) {
            activityEmthodMv = new HellFragmentMethodVisitor(mv, className, HellConstant.Page_Event_OnResume)
            HellPageMethodConstant.setMethodState(className, 'onResume', '()V', true)
        } else if ('onPause' == name && '()V' == desc) {
            activityEmthodMv = new HellFragmentMethodVisitor(mv, className, HellConstant.Page_Event_OnPause)
            HellPageMethodConstant.setMethodState(className, 'onPause', '()V', true)
        } else if ('onStop' == name && '()V' == desc) {
            activityEmthodMv = new HellFragmentMethodVisitor(mv, className, HellConstant.Page_Event_OnStop)
            HellPageMethodConstant.setMethodState(className, 'onStop', '()V', true)
        } else if ('onDestroy' == name && '()V' == desc) {
            activityEmthodMv = new HellFragmentMethodVisitor(mv, className, HellConstant.Page_Event_OnDestroy)
            HellPageMethodConstant.setMethodState(className, 'onDestroy', '()V', true)
        }

        return activityEmthodMv
    }

    private void injectActivityMethodClassTail() {
        boolean existOnCreate = HellPageMethodConstant.getMethodState(
                className, 'onCreate', '(Landroid/os/Bundle;)V')
        if (!existOnCreate) {
            HellActivityStubMethod.injectMethod(cv,
                    'onCreate', '(Landroid/os/Bundle;)V',
                    HellConstant.Page_Event_OnCreate)
            HellPageMethodConstant.setMethodState(className, 'onCreate', '(Landroid/os/Bundle;)V', true)
        }

        boolean existOnNewIntent = HellPageMethodConstant.getMethodState(className,
                'onNewIntent', '(Landroid/content/Intent;)V')
        if (!existOnNewIntent) {
            HellActivityStubMethod.injectMethod(cv,
                    'onNewIntent', '(Landroid/content/Intent;)V',
                    HellConstant.Page_Event_OnNewIntent)
            HellPageMethodConstant.setMethodState(className, 'onNewIntent', '(Landroid/content/Intent;)V', true)
        }

        boolean existOnResume = HellPageMethodConstant.getMethodState(className, 'onResume', '()V')
        if (!existOnResume) {
            HellActivityStubMethod.injectMethod(cv, 'onResume', '()V', HellConstant.Page_Event_OnResume)
            HellPageMethodConstant.setMethodState(className, 'onResume', '()V', true)
        }

        boolean existOnPause = HellPageMethodConstant.getMethodState(className, 'onPause', '()V')
        if (!existOnPause) {
            HellActivityStubMethod.injectMethod(cv, 'onPause', '()V', HellConstant.Page_Event_OnPause)
            HellPageMethodConstant.setMethodState(className, 'onPause', '()V', true)
        }

        boolean existOnStop = HellPageMethodConstant.getMethodState(className, 'onStop', '()V')
        if (!existOnStop) {
            HellActivityStubMethod.injectMethod(cv, 'onStop', '()V', HellConstant.Page_Event_OnStop)
            HellPageMethodConstant.setMethodState(className, 'onStop', '()V', true)
        }

        boolean existOnDestroy = HellPageMethodConstant.getMethodState(className, 'onDestroy', '()V')
        if (!existOnDestroy) {
            HellActivityStubMethod.injectMethod(cv, 'onDestroy', '()V', HellConstant.Page_Event_OnDestroy)
            HellPageMethodConstant.setMethodState(className, 'onDestroy', '()V', true)
        }
    }

    private void injectFragmentMethodClassTail() {
        boolean existOnCreate = HellPageMethodConstant.getMethodState(
                className, 'onCreate', '(Landroid/os/Bundle;)V')
        if (!existOnCreate) {
            HellActivityStubMethod.injectMethod(cv,
                    'onCreate', '(Landroid/os/Bundle;)V',
                    HellConstant.Page_Event_OnCreate)
            HellPageMethodConstant.setMethodState(classNasme, 'onCreate', '(Landroid/os/Bundle;)V', true)
        }

        boolean existOnResume = HellPageMethodConstant.getMethodState(className, 'onResume', '()V')
        if (!existOnResume) {
            HellActivityStubMethod.injectMethod(cv, 'onResume', '()V', HellConstant.Page_Event_OnResume)
            HellPageMethodConstant.setMethodState(className, 'onResume', '()V', true)
        }

        boolean existOnPause = HellPageMethodConstant.getMethodState(className, 'onPause', '()V')
        if (!existOnPause) {
            HellActivityStubMethod.injectMethod(cv, 'onPause', '()V', HellConstant.Page_Event_OnPause)
            HellPageMethodConstant.setMethodState(className, 'onPause', '()V', true)
        }

        boolean existOnStop = HellPageMethodConstant.getMethodState(className, 'onStop', '()V')
        if (!existOnStop) {
            HellActivityStubMethod.injectMethod(cv, 'onStop', '()V', HellConstant.Page_Event_OnStop)
            HellPageMethodConstant.setMethodState(className, 'onStop', '()V', true)
        }

        boolean existOnDestroy = HellPageMethodConstant.getMethodState(className, 'onDestroy', '()V')
        if (!existOnDestroy) {
            HellActivityStubMethod.injectMethod(cv, 'onDestroy', '()V', HellConstant.Page_Event_OnDestroy)
            HellPageMethodConstant.setMethodState(className, 'onDestroy', '()V', true)
        }
    }
}
