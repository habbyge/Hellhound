package com.lxyx.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/03/24.
 */
@PackageScope
class HellJarMethodVisitor extends MethodVisitor {

    private String mMethodName
    private String mMethodDesc

    HellJarMethodVisitor(MethodVisitor mv, String methodName, String methodDesc) {
        super(Opcodes.ASM5, mv)

        mMethodName = methodName
        mMethodDesc = methodDesc
    }

    @Override
    void visitCode() {
        super.visitCode()
    }

    @Override
    void visitInsn(int opcode) {
        if (Opcodes.RETURN == opcode) {
            int eventType = -1
            if ("onCreate".equals(mMethodName) && "(Landroid/os/Bundle;)V".equals(mMethodDesc)) {
                eventType = 0
            } else if ("onResume".equals(mMethodName) && "()V".equals(mMethodDesc)) {
                eventType = 1
            } else if ("onPause".equals(mMethodName) && "()V".equals(mMethodDesc)) {
                eventType = 2
            } else if ("onStop".equals(mMethodName) && "()V".equals(mMethodDesc)) {
                eventType = 3
            } else if ("onDestroy".equals(mMethodName) && "()V".equals(mMethodDesc)) {
                eventType = 4
            }

            // TODO: 2019-03-28 有待完成，继续劫持其他Activity方法 ！！！

            callback(eventType)
        }
        super.visitInsn(opcode)
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 拦截调用方法指令的地方
        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }

    /**
     * 注入callback方法
     */
    private void callback(int eventType) {
        if (eventType < 0) {
            return
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/lxyx/helllib/HellMonitor",
                "getInstance",
                "()Lcom/lxyx/helllib/HellMonitor;",
                false) // 调用者入栈
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 加载this指针，即当前Activity引用 入栈
        mv.visitLdcInsn(eventType) // 事件类型入栈
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, // 调用callback方法
                "com/lxyx/helllib/HellMonitor",
                "callActivityListener",
                "(Landroid/app/Activity;I)V",
                false)
    }
}
