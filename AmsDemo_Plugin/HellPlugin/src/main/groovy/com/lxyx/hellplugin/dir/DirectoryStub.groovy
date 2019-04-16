package com.lxyx.hellplugin.dir

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformOutputProvider
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

/**
 * Created by habbyge on 2019/03/15.
 */
final class DirectoryStub {

    private DirectoryStub() {
    }

    static void startStub(DirectoryInput dirInput, TransformOutputProvider output, boolean isIncremental) {
        String inputDirPath = dirInput.file.getAbsolutePath() // 一般是目录
        println('DirectoryStub, startStub, inputDirPath: ' + inputDirPath)

        File destDir = output.getContentLocation(
                dirInput.getName(),
                dirInput.getContentTypes(),
                dirInput.getScopes(),
                Format.DIRECTORY)
        String destDirPath = destDir.getAbsolutePath()
        println('DirectoryStub, startStub, destDirPath: ' + destDirPath)

        if (isIncremental) { // 如果是增量编译
            println('DirectoryStub, isIncremental TRUE') // 增量编译

            def fileStatusMap = dirInput.getChangedFiles()
            if (fileStatusMap == null) {
                return
            }
            def inputFile
            def status
            def destFilePath
            fileStatusMap.each {
                inputFile = it.key
                status = it.value

                destFilePath = inputFile.getAbsolutePath().replace(inputDirPath, destDirPath)
                File destFile = new File(destFilePath)

                switch (status) {
                case Status.NOTCHANGED:
                    break

                case Status.ADDED:
                case Status.CHANGED:
                    println('DirectoryStub, isIncremental: ADDED || CHANGED')
                    doStubForFile(inputFile, destFile, false)
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
            doStubForDir(dirInput.file, destDir, true)
        }
    }

    private static void doStubForDir(File inputDir, File destDir, boolean fullCompile) {
        println('doStubForDir, inputDir: ' + inputDir.path)
        println('doStubForDir, destDir: ' + destDir.path)

        hookDir(inputDir, fullCompile)

        // 将修改过的字节码copy到dest，作为下一个Transform的输入
        FileUtils.copyDirectory(inputDir, destDir)
    }

    private static void doStubForFile(File srcFile, File destFile, boolean fullCompile) {
        hookFile(srcFile, fullCompile)

        // 将修改过的字节码copy到dest，作为下一个Transform的输入
        FileUtils.copyFile(srcFile, destFile)

        println('HellTransform, doStubForFile: END')
    }

    /**
     * @param inputDir 这是一个目录，把该目录中符合hook的类修改，并重写到这个文件中
     */
    private static boolean hookDir(File inputDir, boolean fullCompile) {
        if (inputDir == null || !inputDir.exists() || !inputDir.canWrite()) {
            return false
        }
        if (inputDir.isDirectory()) {
            List<File> fileList = new ArrayList<>()
            getAllFiles(inputDir, fileList)
            println('fileList.size = ' + fileList.size())
            fileList.each {
                doStub(it.bytes, it.getAbsolutePath(), fullCompile)
            }
        } else if (inputDir.isFile()) {
            doStub(inputDir.bytes, inputDir.getAbsolutePath(), fullCompile)
        }

        return true
    }

    private static boolean hookFile(File inputFile, boolean fullCompile) {
        if (inputFile == null || !inputFile.exists() || !inputFile.canWrite()) {
            return false
        }
        doStub(inputFile.bytes, inputFile.getAbsolutePath(), fullCompile)
    }

    private static boolean doStub(byte[] bytes, String filePath, boolean fullCompile) {
        ClassReader cr
        try {
            cr = new ClassReader(bytes)
        } catch (IOException ioe) {
            ioe.printStackTrace()
            return false
        }

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        HellDirClassVisitor hellCV = new HellDirClassVisitor(cw, fullCompile)
        cr.accept(hellCV, 0)

        byte[] data = cw.toByteArray()
        File file = new File(filePath)
        FileOutputStream fos
        try {
            fos = new FileOutputStream(file)
        } catch (FileNotFoundException e) {
            e.printStackTrace()
            return null
        }
        try {
            fos.write(data)
        } catch (IOException e) {
            e.printStackTrace()
        }

        return true
    }

    private static void getAllFiles(File directory, List<File> results) {
        File[] subDirArray = directory.listFiles()
        if (subDirArray == null) {
            return
        }
        subDirArray.each {
            if (it.isFile()) {
                results.add(it)
            } else if (it.isDirectory()) {
                getAllFiles(it, results)
            }
        }
    }
}
