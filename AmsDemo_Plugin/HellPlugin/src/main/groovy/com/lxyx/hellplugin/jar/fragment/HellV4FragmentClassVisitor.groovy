package com.lxyx.hellplugin.jar.fragment

import com.lxyx.hellplugin.common.HellConstant

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/3/31.
 */
final class HellV4FragmentClassVisitor extends ClassVisitor {
    private String mClassName
    private String mSuperName
    private String[] mInterfaces

    HellV4FragmentClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mClassName = name
        mSuperName = superName
        mInterfaces = interfaces

        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        int eventType = HellConstant.Page_Event_Invalidate
        if ("onCreate" == name && "(Landroid/os/Bundle;)V" == desc) {
            eventType = HellConstant.Page_Event_OnCreate
        } else if ("onResume" == name && "()V" == desc) {
            eventType = HellConstant.Page_Event_OnResume
        } else if ("onPause" == name && "()V" == desc) {
            eventType = HellConstant.Page_Event_OnPause
        } else if ("onStop" == name && "()V" == desc) {
            eventType = HellConstant.Page_Event_OnStop
        } else if ("onDestroy" == name && "()V" == desc) {
            eventType = HellConstant.Page_Event_OnDestroy
        }

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        if (eventType != HellConstant.Page_Event_Invalidate) {
            return new HellV4FragmentMethodVisitor(mv, mClassName, eventType)
        } else {
            return mv
        }
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }
}
