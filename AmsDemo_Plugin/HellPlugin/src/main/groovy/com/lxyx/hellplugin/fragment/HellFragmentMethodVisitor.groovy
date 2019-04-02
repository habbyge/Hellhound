package com.lxyx.hellplugin.fragment

import com.lxyx.hellplugin.common.HellConstant
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/3/31.
 * 劫持Fragment生命周期方法
 */
// todo 攻破的技术难点：？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
// bug：v4包中的Fragment注入callback失败 ！！！！尝试注入android.app.Activity是否也是这种crash栈？？？？？
//      来推断是不是v4包中的Fragment不能注入？？？？？？？？？
//
// 解决这个棘手问题：监控页面生命周期(Activity/Fragment)时，我们不能对android.jar中
// 的代码进行注入插桩，且继承这些类的页面类(XxxActivity/XxxFragment)，很大可能也不会override所有我们需
// 要注入插桩的方法，因此，如果要解决这个问题，我的想法是：
//
// 【想法1】
// 还是只扫描工程中的类文件(继承或实现android.app.Activity/androiod.app.Fragment)，
// 如果遇到没有override的方法，我们在该类文件的最后，写入这些需要方法，并注入插桩。这样的好处是：既不影响原始
// 代码的行号，而且可以监控想要的方法。
//
// 【想法2】
// 编译期生成一个HellBaseActivity，扫描所有继承Activity的子类，替换Activity为HellBaseActivity.
// todo ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
final class HellFragmentMethodVisitor extends MethodVisitor {
    private String mClassName
    private final int mEventType

    HellFragmentMethodVisitor(MethodVisitor mv, String className, int eventType) {
        super(Opcodes.ASM5, mv)
        mClassName = className
        mEventType = eventType
        println('HellFragmentMethodVisitor <init>: ' + mClassName + ' | ' + eventType)
    }

    @Override
    void visitCode() {
        super.visitCode()
        injectCallback() // 这里可以在方法执行最后注入callback
    }

    @Override
    void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            // 这里注入方法执行结束之前的插桩
        }

        super.visitInsn(opcode)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
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

        // todo，这里继续，先关闭，debug代码 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        switch (mEventType) {
        case HellConstant.FRAGMENT_EVENT_OnCreate: // 0
            // void callbackFragment(Fragment fragment, int eventType, Bundle savedInstanceState)
            mv.visitVarInsn(Opcodes.ALOAD, 1) // 从局部变量表slot-1位置，加载onCreate形参Bundle到栈顶

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "com/lxyx/helllib/HellMonitor",
                    "callbackFragment",
                    "(Landroid/support/v4/app/Fragment;ILandroid/os/Bundle;)V",
                    false)
            break
//
//        case HellConstant.FRAGMENT_EVENT_OnResume:      // 1
//        case HellConstant.FRAGMENT_EVENT_OnPause:       // 2
//        case HellConstant.FRAGMENT_EVENT_OnStop:        // 3
//        case HellConstant.FRAGMENT_EVENT_OnDestroy:     // 4
//            // void callbackFragment(Fragment fragment, int eventType)
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                    "com/lxyx/helllib/HellMonitor",
//                    "callbackFragment",
//                    "(Landroid/support/v4/app/Fragment;I)V", false)
//            break
//
        default:
            break
        }
    }
}
