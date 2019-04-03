package com.lxyx.hellplugin.dir.activity

import com.android.utils.Pair
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 这里是注入继承android/app/Activity的子类中缺失的、需要注入的、目标方法.
 */
class HellActivityStubMethod {

    private HellActivityStubMethod() {
    }

    /**
     * 注入方法
     */
    static void injectMethod(ClassVisitor cv, List<Pair<String, String>> nameDescList) {
        if (nameDescList == null || nameDescList.isEmpty()) {
            return
        }

        for (Pair<String, String> pair : nameDescList) {
            if (pair == null) {
                continue
            }
            doInjectMethod(cv, pair.first, pair.second)
        }
    }

    /**
     * 在这个class中，注入一个onStop()方法
     */
    static void doInjectMethod(ClassVisitor cv, String name, String desc) {
        println('injectOnStopMethod start')

        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null)
        mv.visitCode()

        // 调用父类方法: super.name/desc
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 当前指针对象引用this
        // 调用父类方法，使用invokespecial指令
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 'android/app/Activity', name, desc, false)

//        mv.visitMaxs(1, 1) 已经设置ClassVisitor为自动计算帧栈大小、局部变量表大小了，这里无需手工设置.
        mv.visitInsn(Opcodes.RETURN)
        mv.visitEnd()

        println('injectOnStopMethod End')
    }
}