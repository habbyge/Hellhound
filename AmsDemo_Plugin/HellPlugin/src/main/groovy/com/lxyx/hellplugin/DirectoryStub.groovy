package com.lxyx.hellplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformOutputProvider
import groovy.transform.PackageScope
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

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

        hookDir(inputDir)

        // 将修改过的字节码copy到dest，作为下一个Transform的输入
        FileUtils.copyDirectory(inputDir, destDir)
    }

    private static void doStubForFile(File srcFile, File destFile) {
        hookFile(srcFile)

        // 将修改过的字节码copy到dest，作为下一个Transform的输入
        FileUtils.copyFile(srcFile, destFile)

        println('HellTransform, doStubForFile: END')
    }

    /**
     * @param inputDir 这是一个目录，把该目录中符合hook的类修改，并重写到这个文件中
     */
    private static boolean hookDir(File inputDir) {
        if (inputDir == null || !inputDir.exists() || !inputDir.canWrite()) {
            return false
        }
        if (inputDir.isDirectory()) {
            List<File> fileList = new ArrayList<>()
            getAllFiles(inputDir, fileList)
            println('fileList.size = ' + fileList.size())
            for (File file : fileList) {
                doStub(file.bytes, file.getAbsolutePath())
            }
        } else if (inputDir.isFile()) {
            doStub(inputDir.bytes, inputDir.getAbsolutePath())
        }

        return true
    }

    private static boolean hookFile(File inputFile) {
        if (inputFile == null || !inputFile.exists() || !inputFile.canWrite()) {
            return false
        }
        doStub(inputFile.bytes, inputFile.getAbsolutePath())
    }

    private static boolean doStub(byte[] bytes, String filePath) {
        ClassReader cr
        try {
            cr = new ClassReader(bytes)
        } catch (IOException ioe) {
            ioe.printStackTrace()
            return false
        }

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        HellDirectoryClassVisitor hellCV = new HellDirectoryClassVisitor(cw)

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
        for (File subDir : subDirArray) {
            if (subDir.isFile()) {
                results.add(subDir)
            } else if (subDir.isDirectory()) {
                getAllFiles(subDir, results)
            }
        }
    }
}
