package com.lxyx.hellplugin.jar.activity

import com.lxyx.hellplugin.common.HellConstant
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/03/24.
 */
class HellFragmentActivityMethodVisitor extends MethodVisitor {
    private String mMethodName
    private String mMethodDesc

    HellFragmentActivityMethodVisitor(MethodVisitor mv, String methodName, String methodDesc) {
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
            int eventType = HellConstant.Page_Event_Invalidate
            if ("onCreate" == mMethodName && "(Landroid/os/Bundle;)V" == mMethodDesc) {
                eventType = HellConstant.Page_Event_OnCreate
            } else if ('onNewIntent' == mMethodName && '(Landroid/os/Bundle;)V' == mMethodDesc) {
                eventType = HellConstant.Page_Event_OnNewIntent
            } else if ("onResume" == mMethodName && "()V" == mMethodDesc) {
                eventType = HellConstant.Page_Event_OnResume
            } else if ("onPause" == mMethodName && "()V" == mMethodDesc) {
                eventType = HellConstant.Page_Event_OnPause
            } else if ("onStop" == mMethodName && "()V" == mMethodDesc) {
                eventType = HellConstant.Page_Event_OnStop
            } else if ("onDestroy" == mMethodName && "()V" == mMethodDesc) {
                eventType = HellConstant.Page_Event_OnDestroy
            }

            injectCallback(eventType)
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
    private void injectCallback(int eventType) {
        if (eventType < 0) {
            return // 非法
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/lxyx/helllib/HellMonitor",
                "getInstance",
                "()Lcom/lxyx/helllib/HellMonitor;",
                false) // 调用者入栈
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 从局部变量表slot-0位置，加载this指针，即当前Activity引用 入栈

        if (eventType == HellConstant.Page_Event_OnNewIntent) { // onNewIntent
            // void callbackActivityOnNewIntentListener(Activity activity, Intent intent)
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表中slot-1位置加载Intent参数到栈顶
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackActivityOnNewIntentListener",
                    "(Landroid/app/Activity;Landroid/content/Intent;)V",
                    false)
        } else {
            mv.visitLdcInsn(eventType) // 事件类型入栈
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, // 调用callback方法
                    "com/lxyx/helllib/HellMonitor",
                    "callActivityListener",
                    "(Landroid/app/Activity;I)V",
                    false)
        }
    }
}
