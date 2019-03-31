package com.lxyx.hellplugin.fragment;

import com.lxyx.hellplugin.common.HellConstant;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by habbyge on 2019/3/31.
 */
public final class HellFragmentClassVisitor extends ClassVisitor {
    private String mClassName;
    private String mSuperName;
    private String[] mInterfaces;

    public HellFragmentClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name,
            String signature, String superName, String[] interfaces) {

        mClassName = name;
        mSuperName = superName;
        mInterfaces = interfaces;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
            String desc, String signature, String[] exceptions) {

        int eventType = HellConstant.FRAGMENT_EVENT_INVALIDATE;
        if ("onCreate".equals(name) && "(Landroid/os/Bundle;)V".equals(desc)) {
            eventType = HellConstant.FRAGMENT_EVENT_OnCreate;
        } else if ("onResume".equals(name) && "()V".equals(desc)) {
            eventType = HellConstant.FRAGMENT_EVENT_OnResume;
        } else if ("onPause".equals(name) && "()V".equals(desc)) {
            eventType = HellConstant.FRAGMENT_EVENT_OnPause;
        } else if ("onStop".equals(name) && "()V".equals(desc)) {
            eventType = HellConstant.FRAGMENT_EVENT_OnStop;
        } else if ("onDestroy".equals(name) && "()V".equals(desc)) {
            eventType = HellConstant.FRAGMENT_EVENT_OnDestroy;
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (eventType != HellConstant.FRAGMENT_EVENT_INVALIDATE) {
            return new HellFragmentMethodVisitor(mv);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
