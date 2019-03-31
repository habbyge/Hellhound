package com.lxyx.hellplugin.fragment

import com.lxyx.hellplugin.common.HellConstant
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/3/31.
 * 劫持Fragment生命周期方法
 */
final class HellFragmentMethodVisitor extends MethodVisitor {
    private final int mEventType

    HellFragmentMethodVisitor(MethodVisitor mv, int eventType) {
        super(Opcodes.ASM5, mv)

        mEventType = eventType
        println('HellFragmentMethodVisitor, mEventType: ' + eventType)
    }

    @Override
    void visitCode() {
        injectCallback() // 注入callback
        super.visitCode()
    }

    @Override
    void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            // 这里可以在方法执行最后注入callback，目前不需要
        }

        super.visitInsn(opcode)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }

    private void injectCallback() {
        println('injectCallback: mEventType: ' + mEventType)

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/lxyx/helllib/HellMonitor",
                "getInstance",
                "()Lcom/lxyx/helllib/HellMonitor;",
                false) // 调用者入栈

        // 从局部变量表slot-0位置，加载this指针，即当前Fragment引用 入栈
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        // 事件类型入栈
        mv.visitLdcInsn(mEventType)

        switch (mEventType) {
        case HellConstant.FRAGMENT_EVENT_OnCreate: // 0
            // void callbackFragment(Fragment fragment, int eventType, Bundle savedInstanceState)
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1位置，加载onCreate形参到栈顶

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackFragment",
                    "(Landroid/support/v4/app/Fragment;ILandroid/os/Bundle;)V", false)
            break

        case HellConstant.FRAGMENT_EVENT_OnResume:      // 1
        case HellConstant.FRAGMENT_EVENT_OnPause:       // 2
        case HellConstant.FRAGMENT_EVENT_OnStop:        // 3
        case HellConstant.FRAGMENT_EVENT_OnDestroy:     // 4
            // void callbackFragment(Fragment fragment, int eventType)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackFragment",
                    "(Landroid/support/v4/app/Fragment;I)V", false)
            break

        default:
            break
        }
    }
}
