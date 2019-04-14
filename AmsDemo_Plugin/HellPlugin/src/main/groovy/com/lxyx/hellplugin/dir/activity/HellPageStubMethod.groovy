package com.lxyx.hellplugin.dir.activity

import com.lxyx.hellplugin.common.HellConstant
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 这里是注入继承android/app/Activity的子类中缺失的、需要注入的、目标方法.
 */
final class HellPageStubMethod {
    private static final int TYPE_ACTIVITY = 0
    private static final int TYPE_FRAGMENT = 1

    private HellPageStubMethod() {
    }

    /**
     * 在这个class中，注入一个onStop()方法
     */
    static void injectActivityMethod(ClassVisitor cv, String name, String desc, int eventType) {
        _injectMethod(cv, name, desc, eventType, TYPE_ACTIVITY)
    }

    static void injectFragmentMethod(ClassVisitor cv, String name, String desc, int eventType) {
        _injectMethod(cv, name, desc, eventType, TYPE_FRAGMENT)
    }

    private static void _injectMethod(ClassVisitor cv, String name, String desc, int eventType, int type) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null)
        mv.visitCode() // 开始注入code属性

        // 调用父类方法: super.name/desc，用于调用父类方法
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 当前指针对象引用this

        // 注入callback
        if (eventType == HellConstant.Page_Event_OnNewIntent) {
            // public void onNewIntent(Intent var1)

            mv.visitVarInsn(Opcodes.ALOAD, 1) // 参数Intent从局部变量表slot-1中取出入栈
            // 调用父类方法，使用invokespecial指令
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Activity", name, desc, false)

            // 注入callback
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/lxyx/helllib/HellMonitor",
                    "getInstance",
                    "()Lcom/lxyx/helllib/HellMonitor;",
                    false)
            mv.visitVarInsn(Opcodes.ALOAD, 0) // 当前Activity引用入栈
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表中slot-1位置加载Intent参数到栈顶
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackActivityOnNewIntentListener",
                    "(Landroid/app/Activity;Landroid/content/Intent;)V",
                    false)
        } else {
            // 调用super方法，使用invokespecial指令
            String owner
            String callbackMethod
            if (type == TYPE_ACTIVITY) {
                owner = "android/app/Activity"
                callbackMethod = "callbackActivityListener"
            } else if (type == TYPE_FRAGMENT) {
                owner = "android/app/Fragment"
                callbackMethod = "callbackFragment"
            } else {
                return
            }
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false)

            // 注入callback
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/lxyx/helllib/HellMonitor",
                    "getInstance",
                    "()Lcom/lxyx/helllib/HellMonitor;",
                    false)
            mv.visitVarInsn(Opcodes.ALOAD, 0) // 当前Activity引用入栈
            mv.visitLdcInsn(eventType) // 事件类型: onCreate/onResume/onPause...
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    callbackMethod,
                    "(Landroid/app/Activity;I)V",
                    false)
        }

        // 已经设置ClassVisitor为自动计算帧栈大小、局部变量表大小了，这里无需手工设置.
        /*mv.visitMaxs(1, 1)*/
        mv.visitInsn(Opcodes.RETURN) // return
        mv.visitEnd()
    }
}
