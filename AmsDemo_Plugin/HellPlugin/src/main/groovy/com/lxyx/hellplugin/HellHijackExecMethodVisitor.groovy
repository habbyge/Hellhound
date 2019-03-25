package com.lxyx.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

@PackageScope
class HellHijackExecMethodVisitor extends MethodVisitor {
    private String mClassName
    private String[] mInterfaces

    HellHijackExecMethodVisitor(MethodVisitor mv, String className, String[] interfaces) {
        super(Opcodes.ASM5, mv)
        mClassName = className
        mInterfaces = interfaces
    }

    @Override
    void visitLocalVariable(String name, String desc,
            String signature, Label start, Label end, int index) {

//        println('habbyge-mali, visitLocalVariable: ' + name + "_" + desc + "_" + index)

        super.visitLocalVariable(name, desc, signature, start, end, index)
    }

    @Override
    void visitCode() {
//        println('HellHijackExecMethodVisitor, visitCode: ' + mClassName)
        super.visitCode()
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 这里是调用方法的指令的切面，所以适合我们：
        // 这里可以根据对应的调用方法的指令(Opcode.INVOKEVIRTUAL/INVOKESTATIIC)、调用者owner、
        // 方法name/desc，来定位自己想要注入到的方法执行的前、后时机，这样就来劫持和注入成功了。
        // eg: 这里以劫持调用startActivity/finish方法为例，获取Bundle参数，并注入callback。
        if (opcode == Opcodes.INVOKEVIRTUAL) {
            if ('startActivity'.equals(name) && '(Landroid/content/Intent;)V'.equals(desc)) {
                // src: owner; dest: intent
                println('HABBYGE-MALI, exec: startActivity, ' + owner)
                callbackStartActivity(owner)
            } else if ('finish'.equals(name) && '()V'.equals(desc)) {
                // src: owner
                println('HABBYGE-MALI, exec: finish, ' + owner)
                callbackFinish(owner)
            }
        }

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    @Override
    void visitInsn(int opcode) {
        super.visitInsn(opcode)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }

    private void callbackStartActivity(String owner) {
        // 由于是在startActivity()之前介入的，所以此时栈顶一定是startActivity()的参数Intent的引用，这个信息很重要

        // 赋值一份用于callback的参数，避免影响栈顶，从而影响startActivity()的执行
        mv.visitInsn(Opcodes.DUP)

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellViewMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellViewMonitor;',
                false)

        // 交换栈顶与次栈顶元素
        mv.visitInsn(Opcodes.SWAP)
        mv.visitLdcInsn(owner)
        mv.visitInsn(Opcodes.SWAP) // 这样调用者以及参数顺序就对了
//
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                'com/lxyx/helllib/HellViewMonitor',
                'callbackStartActivity',
                "(Ljava/lang/String;Landroid/content/Intent;)V",
                false)
    }

    private void callbackFinish(String owner) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellViewMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellViewMonitor;',
                false)

        mv.visitLdcInsn(owner)

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                'com/lxyx/helllib/HellViewMonitor',
                'callbackFinish',
                "(Ljava/lang/String;)V",
                false)
    }
}
