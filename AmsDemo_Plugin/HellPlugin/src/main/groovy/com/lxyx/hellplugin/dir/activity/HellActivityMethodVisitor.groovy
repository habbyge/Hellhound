package com.lxyx.hellplugin.dir.activity

import com.lxyx.hellplugin.common.HellConstant
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/4/2.
 *
 * 这里实现方案1
 * 扫描当前业务Activity，父类是直接继承android.app.Activity，遍历是否存在需要注入的方法，如果已经存在，
 * 则直接在目标方法中注入插桩；反之，在ClassVistor.visitEnd()中，也就是整个class文件中插入缺失的目标方法，同
 * 时注入插桩，选取在class文件结尾注入的原因是，不改变原先代码行号。
 */
class HellActivityMethodVisitor extends MethodVisitor {
    private String mClassName
    private String mMethodName
    private String mMethodDesc

    HellActivityMethodVisitor(MethodVisitor mv, String className, String methodName, String methodDesc) {
        super(Opcodes.ASM5, mv)
        mClassName = className
        mMethodName = methodName
        mMethodDesc = methodDesc

        println('HellActivityMethodVisitor: ' + mClassName + " | " + mMethodName + " | " + mMethodDesc)
    }

    @Override
    void visitCode() {
        super.visitCode()

        // 在需目标方法中注入callback方法，实现监控
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellMonitor;',
                false)
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 当前Activity引用入栈

        int eventType = HellConstant.Page_Event_Invalidate
        if ('onCreate' == mMethodName && '(Landroid/os/Bundle;)V' == mMethodDesc) {
            eventType = HellConstant.Page_Event_OnCreate
        } else if ('onNewIntent' == mMethodName && '(Landroid/content/Intent;)V' == mMethodDesc) {
            eventType = HellConstant.Page_Event_OnNewIntent
        } else if ('onResume' == mMethodName && '()V' == mMethodDesc) {
            eventType = HellConstant.Page_Event_OnResume
        } else if ('onPause' == mMethodName && '()V' == mMethodDesc) {
            eventType = HellConstant.Page_Event_OnPause
        } else if ('onStop' == mMethodName && '()V' == mMethodDesc) {
            eventType = HellConstant.Page_Event_OnStop
        } else if ('onDestroy' == mMethodName && '()V' == mMethodDesc) {
            eventType = HellConstant.Page_Event_OnDestroy
        }

        if (eventType == HellConstant.Page_Event_OnNewIntent) {
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表中slot-1位置加载Intent参数到栈顶
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackActivityOnNewIntentListener",
                    "(Landroid/app/Activity;Landroid/content/Intent;)V",
                    false)
        } else {
            mv.visitLdcInsn(eventType) // 事件类型: onCreate/onResume/onPause...
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    'callbackActivityListener',
                    '(Landroid/app/Activity;I)V',
                    false)
        }

        println('HellActivityMethodVisitor, visitCode: ' + mClassName + " | " + mMethodName)
    }

    @Override
    void visitInsn(int opcode) {
        super.visitInsn(opcode)
    }

    // 这里是验证代码(注释勿删除)：
    // (1) 验证注入的jvm汇编指令是否对原有的源文件行号有影响：没有影响
    // (2) 验证时再打开，发布时关闭。
    /*@Override
    void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start)
        println('HellActivityMethodVisitor, visitLineNumber: ' +
                mClassName + " | " + mMethodName + " | " + line)
    }*/

    @Override
    void visitEnd() {
        super.visitEnd()

        println('HellActivityMethodVisitor, visitEnd: ' + mClassName + " | " + mMethodName)
    }
}