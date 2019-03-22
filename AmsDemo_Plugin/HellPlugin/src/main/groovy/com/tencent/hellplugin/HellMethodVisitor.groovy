package com.tencent.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 */
@PackageScope
class HellMethodVisitor extends MethodVisitor {

    HellMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv)
    }

    @Override
    void visitCode() {
        // 开始访问该method的Code属性
        super.visitCode()

        println('HellMethodVisitor visitCode: START')

        // method运行之前插桩: 注入一个日志
        log("HABBYGE-MALI, I love my family, let us begin !!!!")

//        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
////        mv.visitLdcInsn("HABBYGE-MALI, I have 4 family members !!!")
//        mv.visitVarInsn(Opcodes.ALOAD, 1)
//        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
//                false)

        // 注入一个单例调用的callback方法
//        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                "com/tencent/habbyge/HellViewMonitor",
//                "getInstance", "()Lcom/tencent/habbyge/HellViewMonitor;", false)
//        mv.visitLdcInsn(17)
//        mv.visitLdcInsn("HABBYGE-MLAI, I love Mali")
////        mv.visitVarInsn(Opcodes.ALOAD, 1) // load view对象 到栈顶
//        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                "com/tencent/habbyge/HellViewMonitor",
//                "callListener",
//                "(ILjava/lang/Object;)V",
//                false)

        callback(true, 17, "HABBYGE-MALI, callback before method !!!")

        // 注入一个static的callback方法
//        mv.visitLdcInsn(17)
////        mv.visitLdcInsn("I love Mali")
//        mv.visitVarInsn(Opcodes.ALOAD, 1) // load onClick的参数view对象到栈顶
//        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                "com/tencent/habbyge/HellViewMonitor",
//                "callListenerStatic",
//                "(ILjava/lang/Object;)V",
//                false)

        println('HellMethodVisitor visitCode: END')
    }

    @Override
    void visitInsn(int opcode) {
        // 这个函数return之前注入jvm指令
        if (opcode == Opcodes.RETURN) {
//            mv.visitLdcInsn(1000)
//            mv.visitLdcInsn("invoke-after")
//            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                    "com/tencent/habbyge/TestAccessibilityCallback",
//                    "justCallback", "(ILjava/lang/Object;)V",
//                    false)
            log("I love my family, visit code Over !!!!")
            callback(false, 19, "HABBYGE-MALI, callback after method !!!")

//            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
//            mv.visitLdcInsn("HABBYGE-MALI, AFTER !!!")
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                    "java/io/PrintStream",
//                    "println", "(Ljava/lang/String;)V",
//                    false)
        }

        super.visitInsn(opcode)
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // todo 这里可以根据对应的类名、方法名、方法中的指令，来定位自己想要注入的唯一位置，来劫持代码.

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    @Override
    void visitEnd() {
        println('HellMethodVisitor, visitEnd')
        super.visitEnd()
    }

    private log(String message) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitLdcInsn(message)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
                false)
    }

    private callback(boolean action, int eventType, Object params) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/tencent/habbyge/HellViewMonitor",
                "getInstance",
                "()Lcom/tencent/habbyge/HellViewMonitor;",
                false)

        mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1中取出View引用，入栈
        mv.visitLdcInsn(eventType)
        mv.visitLdcInsn(params)

        if (action) { // 之前
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/tencent/habbyge/HellViewMonitor",
                    "callListenerBefore", // todo
                    "(Landroid/view/View;ILjava/lang/Object;)V",
                    false)
        } else { // 之后
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/tencent/habbyge/HellViewMonitor",
                    "callListenerAfter", // todo
                    "(Landroid/view/View;ILjava/lang/Object;)V",
                    false)
        }
    }
}
