package com.lxyx.hellplugin

import groovy.transform.PackageScope
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

@PackageScope
class HellHijackExecMethodVisitor extends MethodVisitor {
    private String mClassName
    private String[] mInterfaces

    HellHijackExecMethodVisitor(MethodVisitor mv, String className, String[] interfaces) {
        super(Opcodes.ASM5, mv)
        mClassName = className
        mInterfaces = interfaces
    }

    @Override
    void visitLocalVariable(String name, String desc,
            String signature, Label start, Label end, int index) {

//        println('habbyge-mali, visitLocalVariable: ' + name + "_" + desc + "_" + index)

        super.visitLocalVariable(name, desc, signature, start, end, index)
    }

    @Override
    void visitCode() {
//        println('HellHijackExecMethodVisitor, visitCode: ' + mClassName)
        super.visitCode()
    }

    /**
     * 劫持调用方法指令，注入callback函数
     */
    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 这里是调用方法的指令的切面，所以适合我们：
        // 这里可以根据对应的调用方法的指令(Opcode.INVOKEVIRTUAL/INVOKESTATIIC)、调用者owner、
        // 方法name/desc，来定位自己想要注入到的方法执行的前、后时机，这样就来劫持和注入成功了。
        // eg: 这里以劫持调用startActivity/finish方法为例，获取Bundle参数，并注入callback。
        if (opcode == Opcodes.INVOKEVIRTUAL) {
            if ('startActivity'.equals(name) && '(Landroid/content/Intent;)V'.equals(desc)) {
                // src: owner; dest: intent
                println('HABBYGE-MALI, exec: startActivity, ' + owner)
                callbackStartActivity(owner)
            } else if ('finish'.equals(name) && '()V'.equals(desc)) {
                // src: owner
                println('HABBYGE-MALI, exec: finish, ' + owner)
                callbackFinish(owner)
            } else if ('moveTaskToBack'.equals(name) && '(Z)Z'.equals(desc)) {
                println('HABBYGE-MALI, exec: moveTaskToBack, ' + owner)
                callbackMoveTaskToBack(owner)
            }
        }

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    @Override
    void visitInsn(int opcode) {
        super.visitInsn(opcode)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }

    // void callbackStartActivity(Object srcActivity, String srcActivityName, Intent intent)
    private void callbackStartActivity(String owner) {
        // 由于是在startActivity()之前介入的，所以此时操作数栈顶一定是startActivity()
        // 方法的参数Intent的引用，这个信息很重要：只有这样才能从栈顶获取Intent参数。

        // 复制一份栈顶前两个元素，这里是startActivity()方法的调用者Activity或Context引用 和 要启动的Intent引用。
        // 用于callback的参数，避免影响栈顶，从而影响startActivity()的执行，一定要dup，如下：

        mv.visitInsn(Opcodes.DUP2) // 复制栈顶两个元素：调用者(Activity/Context)引用->Intent参数

//        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                'com/lxyx/helllib/HellMonitor',
//                'callbackStartActivity',
//                // Object srcActivity, Intent intent
//                '(Ljava/lang/Object;Landroid/content/Intent;)V',
//                false)

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellMonitor;',
                false) // 此时栈中情况是: Activity->Intent->HellMonitor单例

        // 复制栈顶(HellMonitor单例)，然后向下塞3个位置
        mv.visitInsn(Opcodes.DUP_X2) // 此时栈中情况是: HellMonitor单例->Activity->Intent->HellMonitor单例
        // 栈顶出栈：去掉重复的HellMonitor单例
        mv.visitInsn(Opcodes.POP) // 此时栈中情况是: HellMonitor单例->Activity->Intent

        mv.visitLdcInsn(owner) // 此时栈中情况是: HellMonitor单例->Activity->Intent->owner
        // 交换栈顶和次栈顶
        mv.visitInsn(Opcodes.SWAP) // 此时栈中情况是: HellMonitor单例->Activity->owner->Intent
        // 这样调用者以及参数顺序就对了, ok, Let's invoke callback method.

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                'com/lxyx/helllib/HellMonitor',
                'callbackStartActivity',
                '(Ljava/lang/Object;Ljava/lang/String;Landroid/content/Intent;)V',
                false)

        // 总结一下:
        // 其实方法调用，就是构造操作数栈中的调用顺序，调用者先入栈，再按照形参顺序入栈，之后invoke方法即可。
        // 需要小心的是，控制栈的dup指令系列、pop、swap指令等，实时脑补或写出来现场操作数栈情况，有助于写
        // jvm指令。
        // 另外，一定记住、记住、记住，学习文档尽量看英文原版的，我就是看了某个博客，误导我，导致DUP指令理解
        // 错误，浪费了点时间；后来我去读oracle的官网doc的Chapter-6，才知道，正确的意思，例如：
        // dup_x2: Duplicate the top operand stack value and insert two or three values down.
        // 赋值栈顶元素，并将其向操作数栈下方插到2个或3个位置处。
    }

    // void callbackFinish(Activity srcActivity, String srcActivityName)
    private void callbackFinish(String owner) {
        mv.visitInsn(Opcodes.DUP) // 复制
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellMonitor;',
                false) // 此时栈顶前2个元素是：HellMonitor单例引用<-调用者对象Activity引用
        mv.visitInsn(Opcodes.SWAP)

        mv.visitLdcInsn(owner) // 此时栈顶前3个元素是：owner<-调用者Activity引用<-HellMonitor单例引用

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                'com/lxyx/helllib/HellMonitor',
                'callbackFinish',
                "(Landroid/app/Activity;Ljava/lang/String;)V",
                false)
    }

    // void callbackMoveTaskToBack(Activity srcActivity, String srcActivityName, boolean nonRoot)
    private void callbackMoveTaskToBack(String owner) {

        // 复制栈顶两个元素：该方法的调用者Activity引用->boolean参数
        mv.visitInsn(Opcodes.DUP2)

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellMonitor;',
                false) // 此时栈顶前3个元素是：调用者对象Activity引用->boolean参数->HellMonitor单例引用
        // 此时栈顶前3个元素是：HellMonitor单例引用->调用者对象Activity引用->boolean参数->HellMonitor单例引用
        mv.visitInsn(Opcodes.DUP_X2)
        // pop处栈顶: 多余的元素，此时栈是：HellMonitor单例引用->调用者对象Activity引用->boolean参数
        mv.visitInsn(Opcodes.POP)
        mv.visitLdcInsn(owner)
        // 此时栈是：HellMonitor单例引用->调用者对象Activity引用-owner->boolean参数
        mv.visitInsn(Opcodes.SWAP)
        // 这样调用者以及参数顺序就对了, ok, Let's invoke callback method.

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                'com/lxyx/helllib/HellMonitor',
                'callbackMoveTaskToBack',
                "(Landroid/app/Activity;Ljava/lang/String;Z)V",
                false)
    }
}
