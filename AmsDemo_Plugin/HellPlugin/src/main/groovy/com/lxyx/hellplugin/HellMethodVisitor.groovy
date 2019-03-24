package com.lxyx.hellplugin

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

        callback(true, 17, "HABBYGE-MALI, callback before method !!!")

        println('HellMethodVisitor visitCode: END')
    }

    @Override
    void visitInsn(int opcode) {
        // 这个函数return之前注入jvm指令
        if (opcode == Opcodes.RETURN) {
            log("I love my family, visit code Over !!!!")
            callback(false, 19, "HABBYGE-MALI, callback after method !!!")
        }

        super.visitInsn(opcode)
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // todo 这里可以根据对应的类名、方法名、方法中的指令，来定位自己想要注入的唯一位置，来劫持代码.
        //      这里以劫持调用startActivity/finish方法为例，获取Bundle参数，并注入callback

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
                "com/lxyx/helllib/HellViewMonitor",
                "getInstance",
                "()Lcom/lxyx/helllib/HellViewMonitor;",
                false)

        mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1中取出View引用，入栈
        mv.visitLdcInsn(eventType)
        mv.visitLdcInsn(params)

        if (action) { // 之前
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellViewMonitor",
                    "callListenerBefore",
                    "(Landroid/view/View;ILjava/lang/Object;)V",
                    false)
        } else { // 之后
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellViewMonitor",
                    "callListenerAfter",
                    "(Landroid/view/View;ILjava/lang/Object;)V",
                    false)
        }
    }
}
