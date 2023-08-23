package com.boy0000.pack_obfuscator

import com.google.gson.JsonParser
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.logError
import org.bukkit.Material
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ObfuscatePack {

    private val tempPackDir: File = Files.createTempDirectory("tempPack").toFile()
    fun obfuscate(pack: File) {
        tempPackDir.deleteOnExit()
        unzip(pack.absolutePath, tempPackDir.absolutePath)

        val packFiles = tempPackDir.listFilesRecursively()
        val models = packFiles.filter { it.isModel && !it.isVanillaBaseModel }
        val textures = packFiles.filter { it.isTexture }
        broadcast("Obfuscating ${textures.size} textures and ${models.size} models")

        models.forEach models@{ model ->
            val modelJson = JsonParser.parseString(model.readText()).asJsonObject
            val modelTextures = modelJson.getAsJsonObject("textures")?.asMap()?.values?.map { it.toString() } ?: return@models

            modelTextures.forEach textures@{ texture ->
                broadcast("Obfuscating $texture")
                val textureFile = packFiles.find { it.packPath.endsWith(texture) } ?: return@textures
                broadcast("Obfuscating ${textureFile.path}")
            }
        }
    }

    private val File.packPath get() = this.path.removePrefix(tempPackDir.path).replace("\\", "/").replace(".png", "")
    private val File.isModel get() = this.extension == "json"
    private val File.isTexture get() = this.extension == "png" || this.extension == "mcmeta"
    private val File.isVanillaBaseModel get(): Boolean {
        val isBase = this.isModel && "assets\\minecraft\\models\\item" in this.path || "assets\\minecraft\\models\\block" in this.path
        return isBase && Material.matchMaterial(this.nameWithoutExtension) != null
    }

    private fun File.listFilesRecursively() = mutableListOf<File>().apply {
        walkTopDown().forEach { this += it }
    }

    fun main() {
        val zipFilePath = "path/to/your/pack.zip"
        val tempDir = Files.createTempDirectory("tempPack").toFile()

        // Extract the ZIP archive to the temporary directory
        unzip(zipFilePath, tempDir.absolutePath)

        // Perform your edits in the temporary directory
        val fileToEdit = File(tempDir, "file_inside_pack.txt")
        // Modify the file content as needed

        // Recreate the ZIP archive from the temporary directory
        val updatedZipFilePath = "path/to/save/updated_pack.zip"
        zip(tempDir, updatedZipFilePath)

        // Clean up the temporary directory if needed
        tempDir.deleteRecursively()

        println("ZIP archive edited and saved as: $updatedZipFilePath")
    }

    fun unzip(zipFilePath: String, destinationFolderPath: String) {
        val zipFile = ZipFile(zipFilePath)
        val destinationFolder = File(destinationFolderPath)

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs()
        }

        val entries = zipFile.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryDestination = File(destinationFolderPath, entry.name)

            if (entry.isDirectory) {
                entryDestination.mkdirs()
            } else {
                entryDestination.parentFile.mkdirs()

                zipFile.getInputStream(entry).use { input ->
                    Files.copy(input, entryDestination.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    fun zip(sourceDirectory: File, zipFilePath: String) {
        val outputStream = ZipOutputStream(FileOutputStream(zipFilePath))

        sourceDirectory.walkTopDown().forEach { file ->
            val entryName = sourceDirectory.toPath().relativize(file.toPath()).toString()
            val entry = ZipEntry(entryName)
            outputStream.putNextEntry(entry)

            if (file.isFile) {
                Files.copy(file.toPath(), outputStream)
            }

            outputStream.closeEntry()
        }

        outputStream.close()
    }

}
