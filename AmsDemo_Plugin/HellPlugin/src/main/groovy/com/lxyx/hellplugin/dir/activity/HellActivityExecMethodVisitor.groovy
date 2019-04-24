package com.lxyx.hellplugin.dir.activity

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/3/15.
 */
class HellActivityExecMethodVisitor extends MethodVisitor {
    private String mClassName
    private String[] mInterfaces
    private String mMethodName

    HellActivityExecMethodVisitor(MethodVisitor mv, String className, String[] interfaces, String methodName) {
        super(Opcodes.ASM5, mv)
        mClassName = className
        mInterfaces = interfaces
        mMethodName = methodName
    }

    @Override
    void visitCode() {
        super.visitCode()
    }

    /**
     * 劫持调用方法指令，注入callback函数
     * /*
     * todo: Activity.startActivity()相关注入: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * public void startActivityForResult(Intent intent, int requestCode)
     * public void startActivityForResult(Intent intent, int requestCode, Bundle options)
     * public void startActivity(Intent intent, Bundle options)
     * public void startActivity(Intent intent)
     * public boolean startActivityIfNeeded(Intent intent, int requestCode)
     * public boolean startActivityIfNeeded(Intent intent, int requestCode, Bundle options)
     */
    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 这里是调用方法的指令的切面，所以适合我们：
        // 这里可以根据对应的调用方法的指令(Opcode.INVOKEVIRTUAL/INVOKESTATIIC)、调用者owner、
        // 方法name/desc，来定位自己想要注入到的方法执行的前、后时机，这样就来劫持和注入成功了。
        // eg: 这里以劫持调用startActivity/finish方法为例，获取Bundle参数，并注入callback。
        if (opcode == Opcodes.INVOKEVIRTUAL) {
            if ('startActivity' == name) {
                if ('(Landroid/content/Intent;)V' == desc) {
                    println('LXYX_ HellActivityExecMethodVisitor, startActivity, ' + owner)
                    injectCallbackStartActivity(owner)
                } else if ('(Landroid/content/Intent;Landroid/os/Bundle;)V' == desc) {
                    injectCallbackStartActivity2(owner)
                }
            } else if ('finish' == name && '()V' == desc) {
                // src: owner
                println('LXYX_ HellActivityExecMethodVisitor, exec: finish, ' + owner)
                injectCallbackFinish(owner)

                // todo finish相关：~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // public void finish()
                // public void finishActivity(int requestCode)
            } else if ('moveTaskToBack' == name && '(Z)Z' == desc) {
                println('LXYX_ HellActivityExecMethodVisitor, moveTaskToBack, ' + owner)
                injectCallbackMoveTaskToBack(owner)
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf)

        // todo 这里还需要优化的点是：这里的方法调用，必须加上"调用者对象"的类型的识别，才能确认是否是我们想要的方法调用：
        // todo 目前主要的调用者对象是：Context(ContextWrapper)/Activity及其子类
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
        println('HellActivityExecMethodVisitor, visitLineNumber: '
                + mClassName + " | " + mMethodName + " | " + line)
    }*/

    @Override
    void visitEnd() {
        super.visitEnd()
    }

    // void callbackStartActivity(Object srcActivity, String srcActivityName, Intent intent)
    private void injectCallbackStartActivity(String owner) {
        // 由于是在startActivity()之前介入的，所以此时操作数栈顶一定是startActivity()的调用者对象引用和
        // 该方法的参数Intent的引用，这个信息很重要：只有这样才能从操作数栈中获取次栈顶调用者对象的引用和栈
        // 顶Intent参数。这是整个callback执行的现场.

        // 复制一份栈顶前两个元素，这里是startActivity()方法的调用者Activity或Context引用 和 要启动的Intent引用。
        // 用于callback的参数，避免影响原有的栈顶(这样就不影响原有方法调用)，从而影响startActivity()的执行，所以一
        // 定要dup，然后编排自己的操作数栈，如下：

        mv.visitInsn(Opcodes.DUP2) // 复制栈顶两个元素：调用者(Activity/Context)引用->Intent参数

        // 取次栈顶，调用者对象，判定是否是Context/Activity及其子类，避免注入有误
        mv.visitInsn(Opcodes.SWAP) // 调用者对象次栈顶->栈顶: Intent参数->Activity/Context

        Label LABEL_ILGAL = new Label() // 非法路径标签

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitorUtil',
                'isActivityOrContentType', '(Ljava/lang/Object;)Z',
                false) // 返回的结果：是否是Activity及其子类？？ Intent参数 -> bool结果
        // 栈：Intent参数
        mv.visitJumpInsn(Opcodes.IFEQ, LABEL_ILGAL) // 如果是false，则跳转到 LABEL_ILGAL

        println("__LXYX__ HellActivityExecMethodVisitor inject: $mClassName, $mMethodName")
        // 下面是true情况：
        mv.visitInsn(Opcodes.POP) // 弹出自己复制的栈顶Intent
        mv.visitInsn(Opcodes.DUP2) // 复制栈顶两个元素：调用者(Activity/Context)引用->Intent参数
//        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                'com/lxyx/helllib/HellMonitor',
//                'callbackStartActivity',
//                // Object srcActivity, Intent intent
//                '(Ljava/lang/Object;Landroid/content/Intent;)V',
//                false)

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance', '()Lcom/lxyx/helllib/HellMonitor;',
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
        // jvm指令，另外一个小技巧是，如果对某些指令实在拿不准或忘记了，可以使用"ASM Bytecode Viewer" 插
        // 件来协助。
        // 另外，一定记住、记住、记住，学习文档尽量看英文原版的，我就是看了某个博客，误导我，导致DUP指令理解
        // 错误，浪费了点时间；后来我去读oracle的官网doc的Chapter-6，才知道，正确的意思，例如：
        // dup_x2: Duplicate the top operand stack value and insert two or three values down.
        // 赋值栈顶元素，并将其向操作数栈下方插到2个或3个位置处。
        Label LABEL_RETURN = new Label()
        mv.visitJumpInsn(Opcodes.GOTO, LABEL_RETURN) // 直接到退出标签

        // 开始这个非法Lable分支:
        mv.visitLabel(LABEL_ILGAL)
        println("__LXYX__ HellActivityExecMethodVisitor NOT inject: $mClassName, $mMethodName")
        mv.visitInsn(Opcodes.POP) // 弹出栈顶Intent，保证对原始栈无影响

        mv.visitLabel(LABEL_RETURN)

        //  what the fuck, 本质就是使用jvm汇编指令来玩操作数栈......
    }

    // public void startActivity(Intent intent, Bundle options)
    private void injectCallbackStartActivity2(String owner) {
        // 此时栈：调用者->Intent->Bundle

        // 获取 "调用者"，判断其是否是Activity/Content子类，否者不合法，拒绝注入
        mv.visitInsn(Opcodes.DUP2_X1) // Intent->Bundle->调用者->Intent->Bundle
        mv.visitInsn(Opcodes.POP2) // Intent->Bundle->调用者
        mv.visitInsn(Opcodes.DUP_X2) // 调用者->Intent->Bundle->调用者
        // what the fuck, 终于还原了以前的栈，并成功把调用者引用的复制，搞到栈顶了

        Label LABEL_ILGAL = new Label() // 非法路径标签

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitorUtil',
                'isActivityOrContentType', '(Ljava/lang/Object;)Z',
                false) // 返回的结果：是否是Activity及其子类？？ Intent参数 -> bool结果
        // 栈：Intent参数
        mv.visitJumpInsn(Opcodes.IFEQ, LABEL_ILGAL) // 如果是false，则跳转到 LABEL_ILGAL

        // 该路径是合法路径，而且与之前栈情况一样: 调用者->Intent->Bundle
        mv.visitInsn(Opcodes.DUP2_X1) // Intent->Bundle->调用者->Intent->Bundle
        mv.visitInsn(Opcodes.POP) // Intent->Bundle->调用者->Intent
        mv.visitInsn(Opcodes.DUP2_X2) // 调用者->Intent->Intent->Bundle->调用者->Intent
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance', '()Lcom/lxyx/helllib/HellMonitor;',
                false) // // 此时栈：调用者->Intent->Intent->Bundle->调用者->Intent->HellMonitor单例
        mv.visitInsn(Opcodes.DUP_X2) // 调用者->Intent->Intent->Bundle->HellMonitor单例->调用者->Intent->HellMonitor单例
        mv.visitInsn(Opcodes.POP) // 调用者->Intent->Intent->Bundle->HellMonitor单例->调用者->Intent
        mv.visitLdcInsn(owner) // 调用者名称常量
        mv.visitInsn(Opcodes.SWAP) // 调用者->Intent->Intent->Bundle->HellMonitor单例->调用者->owner->Intent
        // 测试栈顶四元素已经满足了callback函数
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                'com/lxyx/helllib/HellMonitor',
                'callbackStartActivity',
                '(Ljava/lang/Object;Ljava/lang/String;Landroid/content/Intent;)V',
                false)
        // 此时栈：调用者->Intent->Intent->Bundle
        mv.visitInsn(Opcodes.SWAP)
        mv.visitInsn(Opcodes.POP) // ok: 调用者->Intent->Bundle

        Label LABEL_RETURN = new Label()
        mv.visitJumpInsn(Opcodes.GOTO, LABEL_RETURN) // 直接到退出标签，搞定收工！！！

        // 开始这个非法Lable分支:
        mv.visitLabel(LABEL_ILGAL)
        println("__LXYX__ injectCallbackStartActivity2 NOT inject: $mClassName, $mMethodName")
        // 此时栈情况，与之前没有改变，无需有啥操作了

        mv.visitLabel(LABEL_RETURN)
    }

    // void callbackFinish(Activity srcActivity, String srcActivityName)
    private void injectCallbackFinish(String owner) {
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
    private void injectCallbackMoveTaskToBack(String owner) {

        // 复制栈顶两个元素：该方法的调用者Activity引用->boolean参数
        mv.visitInsn(Opcodes.DUP2)

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                'com/lxyx/helllib/HellMonitor',
                'getInstance',
                '()Lcom/lxyx/helllib/HellMonitor;',
                false) // 此时栈顶前3个元素是：调用者对象Activity引用->boolean参数->HellMonitor单例引用
        // 此时栈顶前3个元素是：HellMonitor单例引用->调用者对象Activity引用->boolean参数->HellMonitor单例引用
        mv.visitInsn(Opcodes.DUP_X2) // 复制栈顶，并向下插入3个位置
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
