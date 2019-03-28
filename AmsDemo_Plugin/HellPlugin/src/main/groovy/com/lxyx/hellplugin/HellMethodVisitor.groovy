package com.lxyx.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 */
@PackageScope
class HellMethodVisitor extends MethodVisitor {

    // 以单击、长按为例，其他的手势，以及自定义手势都可以按照这个思路，进行劫持并插桩
    static final int CLICK = 0
    static final int LONG_CLICK = 1

    private int type = CLICK

    HellMethodVisitor(MethodVisitor mv, int type) {
        super(Opcodes.ASM5, mv)
        this.type = type
    }

    @Override
    void visitCode() {
        // 开始访问该method的Code属性
        super.visitCode()

//        println('HellMethodVisitor visitCode: START')

        // method运行之前插桩: 注入一个日志
        log("HABBYGE-MALI, I love my family, let us begin !!!!")

        callback(type, true) // 方法执行前，callback

//        println('HellMethodVisitor visitCode: END')
    }

    @Override
    void visitInsn(int opcode) {
        // 这个函数return之前注入jvm指令
        if (opcode == Opcodes.RETURN || opcode == Opcodes.IRETURN) {
            // onClick() return || boolean onLongClick() return
            log("I love my family, visit onClick/onLongClick Over !!!!")
            callback(type, false) // 方法执行后，callback
        }

        mv.visitInsn(opcode)
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        mv.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    @Override
    void visitEnd() {
//        println('HellMethodVisitor, visitEnd')
        mv.visitEnd()
    }

    private log(String message) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitLdcInsn(message)
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false)
    }

    /**
     * @param clickType click or long click
     * @param beforeOrAfter before or after method execute
     * @param View the view on clicked
     */
    private callback(int clickType, boolean beforeOrAfter) { // todo 这里继续，修改这个callback方法体
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/lxyx/helllib/HellMonitor",
                "getInstance",
                "()Lcom/lxyx/helllib/HellMonitor;",
                false)

        mv.visitLdcInsn(clickType)
        mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1中取出View引用，入栈

        if (beforeOrAfter) { // 之前
            // void callListenerBefore(int clickType, View view)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callListenerBefore",
                    "(ILandroid/view/View;)V",
                    false)
        } else { // 之后
            // void callListenerAfter(int clickType, View view)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callListenerAfter",
                    '(ILandroid/view/View;)V',
                    false)
        }
    }
}
