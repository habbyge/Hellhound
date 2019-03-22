package com.tencent.hellplugin

import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream

/**
 * Created by habbyge 2019/03/15.
 */
class PageStub {

    private PageStub() {
    }

    static void startStub(File jarFile) {
        def jarFilePath = jarFile.getAbsolutePath()
        println('HABBYGE-MALI, startStub, jarFile: ' + jarFilePath)

        // 读取原始jar文件
        def file = new JarFile(jarFile)
        // 设置输出到的jar
//        def haxName = ''

        Enumeration<JarEntry> enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream is = file.getInputStream(jarEntry)

            println('sourceClassBytes start')
            byte[] sourceClassBytes = IOUtils.toByteArray(is)
            println('sourceClassBytes end')

            if (jarEntry.name.endsWith('.class')) {
                String className = jarEntry.name.replace('.class', '')

                ClassReader cr
                try {
                    cr = new ClassReader(sourceClassBytes)
                    println('HABBYGE-MALI, startStub, ClassReader END')
                } catch (IOException ioe) {
                    ioe.printStackTrace()
                    return
                }

                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
                HellClassVisitor hellCV = new HellClassVisitor(Opcodes.ASM5, cw)

                cr.accept(hellCV, 0)
            }
        }

        println('HABBYGE-MALI, startStub, accept END')
    }

    private static byte[] toByteArray(String filename) throws IOException {
        File f = new File(filename)
        if (!f.exists()) {
            throw new FileNotFoundException(filename)
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        JarInputStream jis = null
        try {
            jis = new JarInputStream(new FileInputStream(f))
            def buf_size = 1024I
            byte[] buffer = new byte[buf_size]
            def len = 0I
            while ((len = jis.read(buffer, 0, buf_size)) != -1) {
                bos.write(buffer, 0, len)
            }
            return bos.toByteArray()
        } catch (IOException e) {
            e.printStackTrace()
            throw e
        } finally {
            try {
                jis.close()
            } catch (IOException e) {
                e.printStackTrace()
            }
            bos.close()
        }
    }
}
