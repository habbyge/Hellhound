package com.lxyx.hellplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.transform.PackageScope
import org.gradle.api.Project

/**
 * Created by habbyge 2019/03/15.
 */
@PackageScope
class HellTransform extends Transform {
    Project project

    HellTransform(Project project) {
        super()
        this.project = project
    }

    @Override
    String getName() {
        return "HellTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws
            TransformException, InterruptedException, IOException {

        super.transform(transformInvocation)

        // 消费型输入： 获取jar、class路径。需要输出给下一个Transform
        Collection<TransformInput> inputs = transformInvocation.getInputs()

//        // 引用型输入：无需输出给下一个Transform。
//        Collection<TransformInput> rInputs = transformInvocation.getReferencedInputs()

        // OutputProvider是当前Transfrom的输出，亦是下一个Transform的输入：
        // 负责把当前Transform输出给下一个Transform的输入，
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        // 支持增量编译
        boolean isIncremental = transformInvocation.isIncremental()
        if (!isIncremental) { // 非增量编译，删除旧的输出
            outputProvider.deleteAll()
        }

        for (TransformInput input : inputs) {

            // 普通文件夹
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                DirectoryStub.startStub(directoryInput, outputProvider, isIncremental)
            }

            // jar包
            Collection<JarInput> jarInputs = input.getJarInputs()
            println('jarInput: ' + jarInputs.size())
            for (JarInput jarInput : jarInputs) {
                JarStub.startStub(jarInput, outputProvider, isIncremental)
            }
        }
    }
}
