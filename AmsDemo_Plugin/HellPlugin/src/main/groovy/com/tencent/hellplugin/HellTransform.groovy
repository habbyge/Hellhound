package com.tencent.hellplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.transform.PackageScope
import org.gradle.api.Project

/**
 * Created by habbyge 2019/03/15.
 * todo 现在还剩下一个问题，如何遍历到Android sdk中的class文件
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

        //当前是否是增量编译
//        boolean isIncremental = transformInvocation.isIncremental()
        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        //引用型输入，无需输出。
//        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()
        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        for (TransformInput input : inputs) {

            // 普通文件夹
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                DirectoryStub.startStub(directoryInput, outputProvider)
            }

            // jar包
            Collection<JarInput> jarInputs = input.getJarInputs()
            println('jarInput: ' + jarInputs.size())
            for (JarInput jarInput : jarInputs) {
                JarStub.startStub(jarInput, outputProvider)
            }
        }
    }
}
