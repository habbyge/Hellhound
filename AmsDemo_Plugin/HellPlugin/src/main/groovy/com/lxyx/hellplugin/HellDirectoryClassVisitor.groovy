package com.lxyx.hellplugin

import com.lxyx.hellplugin.activity.HellActivityExecMethodVisitor
import com.lxyx.hellplugin.common.HellConstant
import com.lxyx.hellplugin.view.HelViewMethodVisitor
import groovy.transform.PackageScope
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 */
@PackageScope
class HellDirectoryClassVisitor extends ClassVisitor {
    private String className
    private String superClassName
    private String[] interfaceArray

    HellDirectoryClassVisitor(final int api, final ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    void visit(int version, int access, String name,
            String signature, String superName,
            String[] interfaces) {

        className = name
        superClassName = superName
        interfaceArray = interfaces

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
        MethodVisitor activityExecMv = new HellActivityExecMethodVisitor(mv, className, interfaceArray)

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
        println('injectOnStopMethod, superClassName = ' + superClassName)

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
