package com.lxyx.hellplugin.fragment

import com.lxyx.hellplugin.common.HellConstant

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/3/31.
 */
final class HellFragmentClassVisitor extends ClassVisitor {
    private String mClassName
    private String mSuperName
    private String[] mInterfaces

    HellFragmentClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    void visit(int version, int access, String name,
            String signature, String superName, String[] interfaces) {

        mClassName = name
        mSuperName = superName
        mInterfaces = interfaces

        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name,
            String desc, String signature, String[] exceptions) {

        int eventType = HellConstant.FRAGMENT_EVENT_INVALIDATE
        if ("onCreate" == name && "(Landroid/os/Bundle;)V" == desc) {
            eventType = HellConstant.FRAGMENT_EVENT_OnCreate
        } else if ("onResume" == name && "()V" == desc) {
            eventType = HellConstant.FRAGMENT_EVENT_OnResume
        } else if ("onPause" == name && "()V" == desc) {
            eventType = HellConstant.FRAGMENT_EVENT_OnPause
        } else if ("onStop" == name && "()V" == desc) {
            eventType = HellConstant.FRAGMENT_EVENT_OnStop
        } else if ("onDestroy" == name && "()V" == desc) {
            eventType = HellConstant.FRAGMENT_EVENT_OnDestroy
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        if (eventType != HellConstant.FRAGMENT_EVENT_INVALIDATE) {
            return new HellFragmentMethodVisitor(mv, eventType)
        } else {
            return mv
        }
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }
}
