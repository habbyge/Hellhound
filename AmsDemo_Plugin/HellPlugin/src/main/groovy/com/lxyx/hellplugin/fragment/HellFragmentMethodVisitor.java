package com.lxyx.hellplugin.fragment;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by habbyge on 2019/3/31.
 * 劫持Fragment生命周期方法
 */
final class HellFragmentMethodVisitor extends MethodVisitor {

    HellFragmentMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitCode() {
        // TODO: 2019-03-31 这里继续

        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        // TODO: 2019-03-31 这里继续

        super.visitInsn(opcode);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    //    injectCallback(eventType); // 注入callback

    private void injectCallback(int eventType) {
        // TODO: 2019-03-31
    }
}
