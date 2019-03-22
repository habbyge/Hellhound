package com.lxyx.hellplugin

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * Created by habbyge 2019/3/5.
 */
class ViewHook {

    private ViewHook() {
    }

    /**
     * @param inputDir 这是一个目录
     */
    static boolean hook(File inputDir) {
        if (inputDir == null || !inputDir.exists() || !inputDir.canRead()) {
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

    private static boolean doStub(byte[] bytes, String filePath) {
        println("habbyge-mali, doStub: $filePath")

        ClassReader cr
        try {
            cr = new ClassReader(bytes)
        } catch (IOException ioe) {
            ioe.printStackTrace()
            return false
        }

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        HellClassVisitor hellCV = new HellClassVisitor(Opcodes.ASM5, cw)

        cr.accept(hellCV, 0)

        // todo：这里只对被修改注入的class文件进行重写，这里需要修改，为改变的无需重写 ！！！！
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

        println('habbyge-mali, doHookFile END')

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
