package com.lxyx.hellplugin.dir.fragment

import com.lxyx.hellplugin.common.HellConstant
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class HellFragmentMethodVisitor extends MethodVisitor {
    private String mClassName
    private String mMethodName
    private final int mEventType

    HellFragmentMethodVisitor(MethodVisitor mv, String className, String methodName, int eventType) {
        super(Opcodes.ASM5, mv)
        mClassName = className
        mMethodName = methodName
        mEventType = eventType

        println('HellFragmentMethodVisitor, <init>: ' + mClassName + ' | ' + mMethodName + ' | ' + eventType)
    }

    @Override
    void visitCode() {
        super.visitCode()
        println('HellFragmentMethodVisitor, visitCode: ' + mClassName +  ' | ' + mMethodName)

        injectCallback() // 这里可以在方法执行最后注入callback
    }

    @Override
    void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            // 这里注入方法执行结束之前的插桩
        }
        super.visitInsn(opcode)
    }

    // 这里是验证代码(注释勿删除)：
    // (1) 验证注入的jvm汇编指令是否对原有的源文件行号有影响：没有影响
    // (2) 验证时再打开，发布时关闭。
    /*@Override
    void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start)
        println('HellFragmentMethodVisitor, visitLineNumber: ' + mClassName +  ' | ' + mMethodName + ' | ' + line)
    }*/

    @Override
    void visitEnd() {
        super.visitEnd()
        println('HellFragmentMethodVisitor, visitEnd: ' + mClassName +  ' | ' + mMethodName)
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
        case HellConstant.Page_Event_OnCreate: // 0
            // void callbackFragment(Fragment fragment, int eventType, Bundle savedInstanceState)
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1位置，加载onCreate形参Bundle到栈顶

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackFragment",
                    "(Landroid/app/Fragment;ILandroid/os/Bundle;)V",
                    false)
            println('HellFragmentMethodVisitor, OnCreate')
            break

        case HellConstant.Page_Event_OnResume:      // 1
        case HellConstant.Page_Event_OnPause:       // 2
        case HellConstant.Page_Event_OnStop:        // 3
        case HellConstant.Page_Event_OnDestroy:     // 4
            // void callbackFragment(Fragment fragment, int eventType)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackFragment",
                    "(Landroid/app/Fragment;I)V",
                    false)
            break

        default:
            break
        }
    }
}