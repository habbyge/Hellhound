package com.lxyx.hellplugin.dir.activity

import org.objectweb.asm.MethodVisitor

/**
 * Created by habbyge 2019/4/2.
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

    HellActivityMethodVisitor(int api, String className, String methodName, String methodDesc) {
        super(api)
        mClassName = className
        mMethodName = methodName
        mMethodDesc = methodDesc

        println('HellActivityMethodVisitor: '
                + mClassName + " | "
                + mMethodName + " | "
                + mMethodDesc)
    }

    @Override
    void visitCode() {
        super.visitCode()
    }

    @Override
    void visitInsn(int opcode) {
        super.visitInsn(opcode)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }
}