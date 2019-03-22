package com.tencent.hellplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.TransformOutputProvider
import com.android.utils.FileUtils

/**
 * Created by habbyge 2019/03/15.
 */
class DirectoryStub {

    private DirectoryStub() {
    }

    static void startStub(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {

        File dest = outputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY)

        println('directoryInput.file: ' + directoryInput.file.getAbsolutePath())
        println('directoryInput dest: ' + dest.getAbsolutePath())

        ViewHook.hook(directoryInput.file)

        println('HellTransform, ViewHook.hook() END')

        // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了：这个dest就是下一个Transform的输入
        FileUtils.copyDirectory(directoryInput.getFile(), dest)

        println('HellTransform, directoryInput copyDirectory END')
    }
}