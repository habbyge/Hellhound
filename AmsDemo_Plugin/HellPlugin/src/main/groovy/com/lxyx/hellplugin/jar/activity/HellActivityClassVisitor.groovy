package com.lxyx.hellplugin.jar.activity


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge on 2019/03/15.
 */
class HellActivityClassVisitor extends ClassVisitor {
    private String className
    private String superName
    private String[] interfaceArray

    private static final String METHOD_onCreate_NAME = 'onCreate'
    private static final String METHOD_onCreate_DESC = '(Landroid/os/Bundle;)V'

    private static final String METHOD_onNewIntent_NAME = 'onNewIntent'
    private static final String METHOD_onNewIntent_DESC = '(Landroid/content/Intent;)V'

    private static final String METHOD_onResume_NAME = 'onResume'
    private static final String METHOD_onResume_DESC = '()V'

    private static final String METHOD_onPause_NAME = 'onPause'
    private static final String METHOD_onPause_DESC = '()V'

    private static final String METHOD_onStop_NAME = 'onStop'
    private static final String METHOD_onStop_DESC = '()V'

    private static final String METHOD_onDestroy_NAME = 'onDestroy'
    private static final String METHOD_onDestroy_DESC = '()V'

    HellActivityClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
        println("HellActivityClassVisitor <init>")
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name
        this.superName = superName
        this.interfaceArray = interfaces

        println("HellActivityClassVisitor visit")

        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)

        // 在页面的生命周期方法中注入callback方法，用于监控页面生命周期，与startActivity、finish、
        // moveTaskToBack三个方法组成Activity完整执行链路

        if (METHOD_onCreate_NAME == name && METHOD_onCreate_DESC == desc) {
            println('HellActivityClassVisitor: visitMethod: onCreate')
            return new HellFragmentActivityMethodVisitor(mv, name, desc)
        }
        if (METHOD_onNewIntent_NAME == name && METHOD_onNewIntent_DESC == desc) {
            println('HellActivityClassVisitor: visitMethod: onNewIntent')
            return new HellFragmentActivityMethodVisitor(mv, name, desc)
        }
        if (METHOD_onResume_NAME == name && METHOD_onResume_DESC == desc) {
            println('HellActivityClassVisitor: visitMethod: onResume')
            return new HellFragmentActivityMethodVisitor(mv, name, desc)
        }
        if (METHOD_onPause_NAME == name && METHOD_onPause_DESC == desc) {
            println('HellActivityClassVisitor: visitMethod: onPause')
            return new HellFragmentActivityMethodVisitor(mv, name, desc)
        }
        if (METHOD_onStop_NAME == name && METHOD_onStop_DESC == desc) {
            println('HellActivityClassVisitor: visitMethod: onStop')
            return new HellFragmentActivityMethodVisitor(mv, name, desc)
        }
        if (METHOD_onDestroy_NAME == name && METHOD_onDestroy_DESC == desc) {
            println('HellActivityClassVisitor: visitMethod: onDestroy')
            return new HellFragmentActivityMethodVisitor(mv, name, desc)
        }

        return mv
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }
}
