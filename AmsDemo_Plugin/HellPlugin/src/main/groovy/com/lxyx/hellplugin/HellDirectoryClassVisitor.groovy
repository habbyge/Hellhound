package com.lxyx.hellplugin

import com.lxyx.hellplugin.activity.HellActivityExecMethodVisitor
import com.lxyx.hellplugin.activity.HellActivityMethodVisitor
import com.lxyx.hellplugin.common.HellConstant
import com.lxyx.hellplugin.view.HelViewMethodVisitor
import groovy.transform.PackageScope
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
 */
@PackageScope
class HellDirectoryClassVisitor extends ClassVisitor {
    private String className
    private String superClassName
    private String[] interfaceArray

    HellDirectoryClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    void visit(int version, int access, String name,
            String signature, String superName,
            String[] interfaces) {

        className = name
        superClassName = superName
        interfaceArray = interfaces
6
        // todo ~~~~~~~~~~~~~~~~~~~~ 这是方案2：修改super类 ~~~~~~~~~~~~~~~~~~~~~~~
        if (superClassName == 'com.lxyx.') {
            superClassName = ''
        }

        super.visit(version, access, name, signature, superClassName, interfaces)
    }

    /*@Override
    FieldVisitor visitField(int access, String name,
            String desc, String signature, Object value) {

        println('HellDirectoryClassVisitor visitField: ' + name + ', ' + desc)
        return super.visitField(access, name, desc, signature, value)
    }*/

    @Override
    MethodVisitor visitMethod(int access, String name,
            String desc, String signature, String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)

        MethodVisitor activityEmthodMv = null
        if ('android/app/Activity' == superClassName) {
            println('HellDirectoryClassVisitor, visitMethod(android/app/Activity): '
                   + className + ' | ' + name + ' | ' + desc)

            if ('onCreate' == name && '(Landroid/os/Bundle;)V' == desc) {
                activityEmthodMv = new HellActivityMethodVisitor(mv,
                        className, 'onCreate', '(Landroid/os/Bundle;)V')
                // todo 这里设置状态，表示有了类中已经ovveride这个方法
            } else if ('onResume' == name && '()V' == desc) {
                activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onResume', '()V')
                // todo 这里设置状态，表示有了类中已经ovveride这个方法
            } else if ('onPause' == name && '()V' == desc) {
                activityEmthodMv = new HellActivityMethodVisitor(mv, className, 'onPause', '()V')
                // todo 这里设置状态，表示有了类中已经ovveride这个方法
            } // todo 这里继续

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
        injectOnStopMethod()

        super.visitEnd()
    }

    /**
     * 在这个class中，注入一个onStop()方法
     */
    private void injectOnStopMethod() {
        if ('android/support/v7/app/AppCompatActivity' == superClassName) {
            println('injectOnStopMethod start')

            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, 'onStop', '()V', null, null)
            mv.visitCode()

            // 调用父类onStop ()V方法
            // ALOAD 0
            // INVOKESPECIAL android/support/v7/app/AppCompatActivity.onResume ()V
            mv.visitVarInsn(Opcodes.ALOAD, 0) // 当前指针对象引用
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, // 调用父类方法，使用invokespecial指令
                    'android/support/v7/app/AppCompatActivity',
                    'onStop', '()V', false)

            mv.visitMaxs(1, 1)
            mv.visitInsn(Opcodes.RETURN)
            mv.visitEnd()

            println('injectOnStopMethod End')
        }
    }
}
