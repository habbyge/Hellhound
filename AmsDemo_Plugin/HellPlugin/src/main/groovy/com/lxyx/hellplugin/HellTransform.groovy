package com.lxyx.hellplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.lxyx.hellplugin.dir.DirectoryStub
import com.lxyx.hellplugin.jar.JarStub
import groovy.transform.PackageScope
import org.gradle.api.Project

/**
 * Created by habbyge on 2019/03/15.
 * 该方案至少比其他方案优秀的地方是：
 * 1、startActivity/finish/moveTaskToBack()等方法指令级别的注入；
 * 2、解决了android.jar不能注入的bug
 * 3、在javac之后、transfrom之前就注入，需要考虑到是否影响方法内联、方法数增量、是否影响原有代码的行号(
 *    只有方法才有行号属性LineNumberTable)，
 * 4、项目中已经自带验证方法的行号是否被改变的验证代码。
 *
 * 问：字节码注入为何不影响源代码行号的原理 ？
 * 答：LineNumberTable包括了start_pc和line_number两个u2类型的数据项，前者是字节码行号(偏移量)，后者是Java源码行号。
 * 每个LineNumberTable中的line_number_table部分， 可以看做是一个数组， 数组的每项是一个line_number_info，
 * 每个line_number_info 结构描述了一条字节码和源码行号的对应关系。 其中start_pc是这个line_number_info 描述的
 * 字节码指令的偏移量， line_number是这个line_number_info 描述的字节码指令对应的源码中的行号。可以看出， 方法
 * 中的每条字节码都对应一个line_number_info ， 这些line_number_info 中的line_number可以指向相同的行号， 因为
 * 一行源码可以编译出多条字节码。
 */
@PackageScope
class HellTransform extends Transform {
    Project project
    File androidJar

    HellTransform(Project project, File androidJar) {
        super()
        this.project = project
        this.androidJar = androidJar
        println('HellTransform, project: ' + project.rootDir.absolutePath)
        println('HellTransform, androidJar: ' + androidJar.absolutePath)
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

        println('HellTransform, transform: Begin !!')
        // todo 任务1：这里对 androidJar 这个android sdk的jar来文件解析，注入 ？？？？
        // todo 任务2：写一个组件，独立子线程用于接收callback事件和参数，注意这里一定要保证时序
        // 任务3：验证注入前后，java源代码行号没有改变. --done

        // 消费型输入： 获取jar、class路径。需要输出给下一个Transform
        Collection<TransformInput> inputs = transformInvocation.getInputs()

        /*// 引用型输入：无需输出给下一个Transform。
        Collection<TransformInput> rInputs = transformInvocation.getReferencedInputs()*/

        // OutputProvider是当前Transfrom的输出，亦是下一个Transform的输入：
        // 负责把当前Transform输出给下一个Transform的输入，
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        /*String baseDir = project.project(':helllib')
        println('baseDir: ' + baseDir)
        HellBaseActivityGenerator.create('com/lxyx/habbyge/HellBaseActivity', baseDir)*/

        // 支持增量编译
        boolean isIncremental = transformInvocation.isIncremental()
        if (!isIncremental) { // 非增量编译，删除旧的输出
            outputProvider.deleteAll()
        }

        inputs.each {
            // 工程中的普通文件夹
            it.getDirectoryInputs().each {
                DirectoryStub.startStub(it, outputProvider, isIncremental)
            }

            // 第三方jar包
            it.getJarInputs().each {
                JarStub.startStub(it, outputProvider, isIncremental)
            }
        }
        /*for (TransformInput input : inputs) {
            // 工程中的普通文件夹
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                DirectoryStub.startStub(directoryInput, outputProvider, isIncremental)
            }

            // 第三方jar包
            Collection<JarInput> jarInputs = input.getJarInputs()
            println('jarInput: ' + jarInputs.size())
            for (JarInput jarInput : jarInputs) {
                JarStub.startStub(jarInput, outputProvider, isIncremental)
            }
        }*/
    }
}
