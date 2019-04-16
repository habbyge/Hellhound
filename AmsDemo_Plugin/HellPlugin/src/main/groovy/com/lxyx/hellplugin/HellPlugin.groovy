package com.lxyx.hellplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by habbyge on 2019/03/15.
 */
class HellPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.task("hellplugin-task", dependsOn: ["compileJava"]) {
            // 这里需要注意gradle task中执行时机的区别，除了dependsOn这个依赖数组之外，还有个需要注意的：
            // 在task的时候有doFirst{}/doLast{}执行时机，这两个与不写有啥区别呐？
            // 不写的话，是在构建的配置阶段就执行；doFirst/doLast表示实在该task执行时的前/后才执行；
            doApply(project)
        }
    }

    private static void doApply(Project project) {
        System.out.println("Hello, HellPlugin, Start!")

        // 获取android.jar(即：Android sdk)的路径，以及文件对象
        def appPlugin = project.getPlugins().getPlugin(AppPlugin.class)
        def sdkDirectory = appPlugin.extension.sdkDirectory
        def androidJarPath = "platforms/${appPlugin.extension.compileSdkVersion}/android.jar"
        println('HellPlugin, androidJarPath: ' + androidJarPath)
        def androidJar = new File(sdkDirectory, androidJarPath)
        println('HellPlugin, androidJar: ' + androidJar.absolutePath)

        def appExtension = project.getExtensions().findByType(AppExtension.class)
        appExtension.registerTransform(new HellTransform(project, androidJar))

        System.out.println("Hello, HellPlugin, End!")
    }
}
