package com.boy0000.pack_obfuscator

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

fun String.substringBetween(start: String, end: String) = substringAfter(start).substringBefore(end)

fun unzip(zipFile: File, destDir: File) {
    runCatching {
        destDir.deleteRecursively()
        destDir.mkdirs() // Create the destination directory

        ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
            var zipEntry = zipInputStream.nextEntry

            while (zipEntry != null) {
                val entryFile = File(destDir, zipEntry.name)

                // Create parent directories if they don't exist
                entryFile.parentFile?.mkdirs()

                if (!zipEntry.isDirectory) {
                    FileOutputStream(entryFile).use { outputStream ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (zipInputStream.read(buffer).also { len = it } > 0) {
                            outputStream.write(buffer, 0, len)
                        }
                    }
                }
                zipEntry = zipInputStream.nextEntry
            }
        }
    }.onFailure {
        it.printStackTrace()
    }
}