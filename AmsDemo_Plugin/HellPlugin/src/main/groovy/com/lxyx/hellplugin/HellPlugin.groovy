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
