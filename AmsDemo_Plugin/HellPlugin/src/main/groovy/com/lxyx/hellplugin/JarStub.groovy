package com.lxyx.hellplugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformOutputProvider
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by habbyge 2019/03/15.
 */
class JarStub {

    private JarStub() {
    }

    static void startStub(JarInput jarInput, TransformOutputProvider output, boolean isIncremental) {
        String jarFilePath = jarInput.file.getAbsolutePath()
        if (!jarFilePath.endsWith('.jar')) {
            return // 过滤非jar文件
        }

        println('JarStub startStub: ' + jarFilePath)

        // 重名名输出文件，因为可能同名，会覆盖
        def jarName = jarInput.name
        def md5Name = DigestUtils.md5(jarFilePath)
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }
        File destJar = output.getContentLocation(
                jarName + md5Name,
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR)

        println('JarStub, startStub, jarInput dest: ' + destJar.getAbsolutePath())
        println('JarStub, startStub, jarInput.file: ' + jarName)

        Status status = jarInput.getStatus()
        if (isIncremental) {
            switch (status) {
            case Status.NOTCHANGED:
                break

            case Status.CHANGED:
            case Status.ADDED:
                doStub(jarInput.getFile(), destJar)
                break

            case Status.REMOVED:
                if (destJar.exists()) {
                    FileUtils.forceDelete(destJar)
                }
                break

            default:
                break
            }
        } else {
            doStub(jarInput.getFile(), destJar)
        }
    }

    private static void doStub(File srcFile, File jarDest) {
        File tmpJarFile = new File(srcFile.getParent() + File.separator + "classes_tmp.jar")
        if (tmpJarFile.exists()) {
            tmpJarFile.delete()
        }
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tmpJarFile))

        JarFile jarFile = new JarFile(srcFile) // JarFile
        Enumeration<JarEntry> enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) { // 遍历JarFile中每一个JarEntry
            JarEntry jarEntry = enumeration.nextElement()
            String jarEntryName = jarEntry.getName()
            ZipEntry zipEntry = new ZipEntry(jarEntryName) // 实际上JarFile是一个Zip压缩文件
            InputStream zipEntryIs = jarFile.getInputStream(zipEntry)

            // 插桩
            if ('android/support/v4/app/FragmentActivity.class' == jarEntryName) {
                println('JarStub Legal: START')

                jos.putNextEntry(zipEntry)

                ClassReader classReader = new ClassReader(IOUtils.toByteArray(zipEntryIs))
                ClassWriter classWriter = new ClassWriter(classReader,
                        ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
                ClassVisitor classVisitor = new HellJarClassVisitor(classWriter)
                classReader.accept(classVisitor, 0)

                byte[] codeBytes = classWriter.toByteArray()
                jos.write(codeBytes)

                println('JarStub Legal: END')
            } else {
                jos.putNextEntry(zipEntry)
                jos.write(IOUtils.toByteArray(zipEntryIs))
            }

            jos.closeEntry()
        }

        jos.close()
        jarFile.close()

        println('JarStub jarFile.close()')

        // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyFile(tmpJarFile, jarDest)
        tmpJarFile.delete() // 删除临时文件
    }
}