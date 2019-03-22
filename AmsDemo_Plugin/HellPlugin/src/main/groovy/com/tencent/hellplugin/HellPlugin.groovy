package com.tencent.hellplugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by habbyge 2019/03/15.
 */
class HellPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        System.out.println("Hello, HellPlugin, Start!")

        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class)

        // groovy版本
        appExtension.registerTransform(new HellTransform(project))

        System.out.println("Hello, HellPlugin, End!")
    }
}
