package com.lxyx.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 */
@PackageScope
class HellClickMethodVisitor extends MethodVisitor {

    // 以单击、长按为例，其他的手势，以及自定义手势都可以按照这个思路，进行劫持并插桩
    static final int CLICK = 0
    static final int LONG_CLICK = 1
    static final int LISTVIEW_ITEM_CLICK = 2

    private int type = CLICK

    HellClickMethodVisitor(MethodVisitor mv, int type) {
        super(Opcodes.ASM5, mv)
        this.type = type
    }

    @Override
    void visitCode() {
        // 开始访问该method的Code属性
        super.visitCode()

//        println('HellClickMethodVisitor visitCode: START')

        // method运行之前插桩: 注入一个日志
        log("HABBYGE-MALI, stub before click: $type")

        callback(type, true) // 方法执行前，callback

//        println('HellClickMethodVisitor visitCode: END')
    }

    @Override
    void visitInsn(int opcode) {
        // 这个函数return之前注入jvm指令
        if (opcode == Opcodes.RETURN || opcode == Opcodes.IRETURN) {
            // void onClick() return || boolean onLongClick() return || void onItemClick()
            log("HABBYGE-MALI, stub after click: $type")
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
//        println('HellClickMethodVisitor, visitEnd')
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
    private callback(int clickType, boolean beforeOrAfter) {

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "com/lxyx/helllib/HellMonitor",
                "getInstance",
                "()Lcom/lxyx/helllib/HellMonitor;",
                false) // 单例调用者

        if (clickType == CLICK || clickType == LONG_CLICK) { // view click or long click
            mv.visitLdcInsn(clickType)
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1中取出View引用，入栈

            if (beforeOrAfter) { // 之前
                // void callClickListenerBefore(int clickType, View view)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "com/lxyx/helllib/HellMonitor",
                        "callClickListenerBefore",
                        "(ILandroid/view/View;)V",
                        false)
            } else { // 之后
                // void callClickListenerAfter(int clickType, View view)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "com/lxyx/helllib/HellMonitor",
                        "callClickListenerAfter",
                        '(ILandroid/view/View;)V',
                        false)
            }
        } else if (clickType == LISTVIEW_ITEM_CLICK) { // listview item click
            // void onItemClick(AdapterView<?> parent, View view, int position, long id);
            // callbackItemClickBefore
            // callbackItemClickAfter

            // 从局部变量表中load出参数到栈顶，按照参数顺序
            mv.visitVarInsn(Opcodes.ALOAD, 1) // AdapterView<?> parent
            mv.visitVarInsn(Opcodes.ALOAD, 2) // View view
            mv.visitVarInsn(Opcodes.ILOAD, 3) // int position
            mv.visitVarInsn(Opcodes.LLOAD, 4) // long id

            if (beforeOrAfter) { // 之前插桩
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        'com/lxyx/helllib/HellMonitor',
                        'callbackItemClickBefore',
                        '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                        false)
            } else { // 之后插桩
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        'com/lxyx/helllib/HellMonitor',
                        'callbackItemClickAfter',
                        '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                        false)
            }
        }
    }
}
