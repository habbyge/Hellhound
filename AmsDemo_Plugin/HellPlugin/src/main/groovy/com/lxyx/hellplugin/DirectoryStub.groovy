package com.lxyx.hellplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformOutputProvider
import groovy.transform.PackageScope
import org.apache.commons.io.FileUtils

/**
 * Created by habbyge 2019/03/15.
 */
@PackageScope
class DirectoryStub {

    private DirectoryStub() {
    }

    static void startStub(DirectoryInput directoryInput,
            TransformOutputProvider output, boolean isIncremental) {

        String inputDirPath = directoryInput.file.getAbsolutePath() // 一般是目录
        println('DirectoryStub, startStub, inputDirPath: ' + inputDirPath)

        File destDir = output.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY)
        String destDirPath = destDir.getAbsolutePath()
        println('DirectoryStub, startStub, destDirPath: ' + destDirPath)

        if (isIncremental) { // 如果是增量编译
            println('DirectoryStub, isIncremental TRUE') // 增量编译

            Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
            if (fileStatusMap == null) {
                return
            }
            File inputFile
            Status status
            String destFilePath
            for (Map.Entry<File, Status> entry : fileStatusMap.entrySet()) {
                inputFile = entry.getKey()
                status = entry.getValue()

                destFilePath = inputFile.getAbsolutePath().replace(inputDirPath, destDirPath)
                File destFile = new File(destFilePath)

                switch (status) {
                case Status.NOTCHANGED:
                    break

                case Status.ADDED:
                case Status.CHANGED:
                    println('DirectoryStub, isIncremental: ADDED || CHANGED')
                    doStubForFile(inputFile, destFile)
                    break

                case Status.REMOVED:
                    println('DirectoryStub, isIncremental: REMOVED')
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destDir)
                    }
                    break

                default:
                    break
                }
            }
        } else { // 全量编译
            println('DirectoryStub, isIncremental FALSE')
            doStubForDir(directoryInput.file, destDir)
        }
    }

    private static void doStubForDir(File inputDir, File destDir) {
        println('doStubForDir, inputDir: ' + inputDir.path)
        println('doStubForDir, destDir: ' + destDir.path)

        ViewHook.hookDir(inputDir)

        // 将修改过的字节码copy到dest，作为下一个Transform的输入
        FileUtils.copyDirectory(inputDir, destDir)
    }

    private static void doStubForFile(File srcFile, File destFile) {
        ViewHook.hookFile(srcFile)

        // 将修改过的字节码copy到dest，作为下一个Transform的输入
        FileUtils.copyFile(srcFile, destFile)

        println('HellTransform, doStubForFile: END')
    }
}