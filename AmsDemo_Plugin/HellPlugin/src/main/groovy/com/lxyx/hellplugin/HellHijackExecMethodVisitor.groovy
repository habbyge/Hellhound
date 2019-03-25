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
//        mv.visitInsn(Opcodes.DUP)
//        mv.visitInsn()
//
//        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                'com/lxyx/helllib/HellViewMonitor',
//                'getInstance',
//                '()Lcom/lxyx/helllib/HellViewMonitor;',
//                false)
//
//        mv.visitLdcInsn(owner)
//
//        // todo 从slot-2中取出Intent引用，加载到栈中，这里有问题，这里的坑是，startActivity之前的参数Intent，
//        // todo 实现的时机是不定的，所以不能通过获取局部变量表的位置获取，因为索引具有不确定性.
//        mv.visitVarInsn(Opcodes.ALOAD, 2)
//
//        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                'com/lxyx/helllib/HellViewMonitor',
//                'callbackStartActivity',
//                "(Ljava/lang/String;Landroid/content/Intent;)V",
//                false)
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
