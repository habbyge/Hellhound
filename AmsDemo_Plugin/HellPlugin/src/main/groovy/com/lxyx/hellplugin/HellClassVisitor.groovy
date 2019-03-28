package com.lxyx.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 */
@PackageScope
class HellClassVisitor extends ClassVisitor implements Opcodes {
    private String className
    private String[] interfaceArray

    HellClassVisitor(final int api, final ClassVisitor cv) {
        super(api, cv)
    }

    @Override
    void visit(int version, int access, String name,
            String signature, String superName,
            String[] interfaces) {

//        println('HellClassVisitor visit: ' + name + ', ' + superName)
        className = name
        interfaceArray = interfaces

        super.visit(version, access, name, signature, superName, interfaces)
    }

    /*@Override
    FieldVisitor visitField(int access, String name,
            String desc, String signature, Object value) {

        println('HellClassVisitor visitField: ' + name + ', ' + desc)
        return super.visitField(access, name, desc, signature, value)
    }*/

    @Override
    MethodVisitor visitMethod(int access, String name,
            String desc, String signature, String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        MethodVisitor hellMv = new HellHijackExecMethodVisitor(mv, className, interfaceArray)

        // 这里劫持工程代码中所有出现的onClick()方法，为后面的注入做准备
        if (interfaceArray != null && interfaceArray.length > 0) {
            if (interfaceArray.contains('android/view/View$OnClickListener')) { // 类名
                if ('onClick' == name && '(Landroid/view/View;)V' == desc) {
                    println('HellClassVisitor OnClickListener: inject ok: ' + className)
                    return new HellMethodVisitor(hellMv, HellMethodVisitor.CLICK)
                }
            } else if (interfaceArray.contains('android/view/View$OnLongClickListener')) { // 类名
                if ('onLongClick' == name && '(Landroid/view/View;)Z' == desc) {
                    println('HellClassVisitor OnLongClickListener: inject ok: ' + className)
                    return new HellMethodVisitor(hellMv, HellMethodVisitor.LONG_CLICK)
                }
            } else {
                // TODO 有待增加的是：ListView item点击
            }
        }

        // 其他行为类似，实际上最常用的是点击、列表item点击

        return hellMv
    }

    @Override
    void visitEnd() {
        /*println('HellClassVisitor, visitEnd !!!')*/
        super.visitEnd()
    }
}
