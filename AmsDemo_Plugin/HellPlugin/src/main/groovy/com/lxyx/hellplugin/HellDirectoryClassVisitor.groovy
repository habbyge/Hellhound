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
class HellDirectoryClassVisitor extends ClassVisitor implements Opcodes {
    private String className
    private String[] interfaceArray

    HellDirectoryClassVisitor(final int api, final ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    void visit(int version, int access, String name,
            String signature, String superName,
            String[] interfaces) {

//        println('HellDirectoryClassVisitor visit: ' + name + ', ' + superName)
        className = name
        interfaceArray = interfaces

        super.visit(version, access, name, signature, superName, interfaces)
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
        /*println('HellDirectoryClassVisitor, visitEnd !!!')*/
        super.visitEnd()
    }
}
