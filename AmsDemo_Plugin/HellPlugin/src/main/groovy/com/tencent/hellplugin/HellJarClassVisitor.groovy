package com.tencent.hellplugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/03/15.
 */
class HellJarClassVisitor extends ClassVisitor {
    private String className
    private String superName
    private String[] interfaceArray

    HellJarClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    void visit(int version, int access, String name,
            String signature, String superName,
            String[] interfaces) {

        this.className = name
        this.superName = superName
        this.interfaceArray = interfaces

        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name,
            String desc, String signature, String[] exceptions) {

        println('HellJarClassVisitor visitMethod: ' + name + ", " + desc)
        return super.visitMethod(access, name, desc, signature, exceptions)
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }
}
