package com.lxyx.hellplugin.dir

import com.lxyx.hellplugin.common.HellPageMethodConstant
import com.lxyx.hellplugin.dir.activity.HellActivityExecMethodVisitor
import com.lxyx.hellplugin.dir.activity.HellActivityMethodVisitor
import com.lxyx.hellplugin.common.HellConstant
import com.lxyx.hellplugin.dir.activity.HellActivityStubMethod
import com.lxyx.hellplugin.dir.view.HelViewMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 *
 * todo ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
 * 注入页面行为函数的策略：
 * 1、继承android.app.Activity的页面，下同fragment，无法字节码注入，可以从另外一个角度解决：
 * 扫描当前Project的工程目录文件(注意不是jar文件)，然后有两个具体方案：
 * 【方案1】、扫描当前业务Activity，父类是直接继承android.app.Activity，遍历是否存在需要注入的方法，如果已经存在，
 * 则直接在目标方法中注入插桩；反之，在ClassVistor.visitEnd()中，也就是整个class文件中插入缺失的目标方法，同
 * 时注入插桩，选取在class文件结尾注入的原因是，不改变原先代码行号。
 * 【方案2】：自己实现一个BaseActivity，继承android.app.Activity，override目标方法，然后扫描Project的目录，
 * 替换系统Activity为自己生成的BaseActivity即可。
 * 2、继承v4包中的activity和fragment，无需1中方案，直接扫描jar中class文件：FragmentActivity和Fragment，在
 * 对应的目标方法中注入插桩即可。
 *
 * // todo 攻破的技术难点：？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
 * // bug：v4包中的Fragment注入callback失败 ！！！！尝试注入android.app.Activity是否也是这种crash栈？？？？？
 * //      来推断是不是v4包中的Fragment不能注入？？？？？？？？？
 * //
 * // 解决这个棘手问题：监控页面生命周期(Activity/Fragment)时，我们不能对android.jar中
 * // 的代码进行注入插桩，且继承这些类的页面类(XxxActivity/XxxFragment)，很大可能也不会override所有我们需
 * // 要注入插桩的方法，因此，如果要解决这个问题，我的想法是：
 * //
 * // 【想法1】
 * // 还是只扫描工程中的类文件(继承或实现android.app.Activity/androiod.app.Fragment)，
 * // 如果遇到没有override的方法，我们在该类文件的最后，写入这些需要方法，并注入插桩。这样的好处是：既不影响原始
 * // 代码的行号，而且可以监控想要的方法。
 * //
 * // 【想法2】
 * // 编译期生成一个HellBaseActivity，扫描所有继承Activity的子类，替换Activity为HellBaseActivity.
 * // todo ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
 *
 * // todo ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
 * // debug调试 Gradle Plugin步骤:
 * 1、【run】->【Edit configures】，针对自己的需要调试的plugin，新建remote调试选项，起个名字+选择对应需要调试的plugin
 * 2、在Terminal中输入命令：./gradlew assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true, 然后
 *    会等待attach对应的plugin，选择【run】->【Edit configures】中对应需要debug的remote选项中的plugin，ok即可。
 * 注意: debug之前，最好./gradlew clean一次工程，因为增量编译的话，可能会跳过一些编译过程，导致brakpoint不能执行到.
 * // todo ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
 */
class HellDirectoryClassVisitor extends ClassVisitor {
    private String className
    private String superClassName
    private String[] interfaceArray

    HellDirectoryClassVisitor(final ClassVisitor cv) {
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
            println('HellDirectoryClassVisitor, visitMethod(android/app/Activity): '
                    + className + ' | ' + name + ' | ' + desc)

            if ('onCreate' == name && '(Landroid/os/Bundle;)V' == desc) {
                activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onCreate', '(Landroid/os/Bundle;)V')
                // 这里设置状态，表示有了类中已经ovveride这个方法
                HellPageMethodConstant.setMethodState(className, 'onCreate', '(Landroid/os/Bundle;)V', true)
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
            // todo 剩余的，没有override的目标方法，则需要在class文件的末尾注入在当前类中，之后再注入插桩
        }

        MethodVisitor activityExecMv = new HellActivityExecMethodVisitor(
                activityEmthodMv == null ? mv : activityEmthodMv,
                className, interfaceArray)

        // 这里劫持工程代码中所有出现的onClick()方法，为后面的注入做准备
        if (interfaceArray != null && interfaceArray.length > 0) {
            // 点击
            if (interfaceArray.contains('android/view/View$OnClickListener')) {
                if ('onClick' == name && '(Landroid/view/View;)V' == desc) {
                    println('HellDirectoryClassVisitor OnClickListener: inject ok: ' + className)
                    return new HelViewMethodVisitor(activityExecMv, HellConstant.CLICK)
                }
            }

            // 长按
            if (interfaceArray.contains('android/view/View$OnLongClickListener')) {
                if ('onLongClick' == name && '(Landroid/view/View;)Z' == desc) {
                    println('HellDirectoryClassVisitor OnLongClickListener: inject ok: ' + className)
                    return new HelViewMethodVisitor(activityExecMv, HellConstant.LONG_CLICK)
                }
            }

            // ListView中Item点击
            if (interfaceArray.contains('android/widget/AdapterView$OnItemClickListener')) {
                // void onItemClick(AdapterView<?> parent, View view, int position, long id);
                if ('onItemClick' == name &&
                        '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V' == desc) {
                    println('HellDirectoryClassVisitor onItemClick: inject ok: ' + className)
                    return new HelViewMethodVisitor(activityExecMv, HellConstant.LISTVIEW_ITEM_CLICK)
                }
            }
        }

        // 其他行为类似，实际上最常用的是点击、列表item点击

        return activityExecMv
    }

    @Override
    void visitEnd() {
        // todo ~~~~~~~~~~~~~~~~~~~~~~~ 这是方案1：类文件尾部插入方法 ~~~~~~~~~~~~~~~~~~~~~~~
        //  在一个class文件的结尾插入方法，完整方案应该是：输入若干需要监控的方法name+desc+class名，
        //  然后查看当前合法类中是否存在该方法，不存在则在class末尾最后插入该方法，存在的话，直接注入插桩。
        if ('android/app/Activity' == superClassName) {
            println('HellDirectoryClassVisitor, visitEnd: ' + className)
            boolean existOnCreate = HellPageMethodConstant.getMethodState( // todo 这里继续 ！！！！！！！
                    className, 'onCreate', '(Landroid/os/Bundle;)V')
            if (!existOnCreate) {
                HellActivityStubMethod.doInjectMethod(cv, 'onCreate', '(Landroid/os/Bundle;)V')
                HellPageMethodConstant.setMethodState(className, 'onCreate', '(Landroid/os/Bundle;)V', true)
            }

            boolean existOnResume = HellPageMethodConstant.getMethodState(className, 'onResume', '()V')
            if (!existOnResume) {
                HellActivityStubMethod.doInjectMethod(cv, 'onResume', '()V')
                HellPageMethodConstant.setMethodState(className, 'onResume', '()V', true)
            }

            // todo 这里继续 !!!!
            boolean existOnPause = HellPageMethodConstant.getMethodState(className, 'onPause', '()V')
            if (!existOnPause) {
                HellActivityStubMethod.doInjectMethod(cv, 'onPause', '()V')
                HellPageMethodConstant.setMethodState(className, 'onPause', '()V', true)
            }
        }

        super.visitEnd()
    }
}
