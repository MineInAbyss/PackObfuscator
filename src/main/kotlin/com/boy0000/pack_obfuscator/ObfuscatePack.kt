package com.boy0000.pack_obfuscator

import com.boy0000.pack_obfuscator.ObfuscatePack.substringBetween
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mineinabyss.idofront.messaging.logWarn
import io.th0rgal.oraxen.OraxenPlugin
import org.bukkit.Material
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class ObfuscatedModel(val modelPath: String, val obfuscatedModelName: String) {
    val resourcePackModelPack: String get() {
        val namespace = modelPath.substringBetween("assets/", "/models")
        val path = modelPath.substringAfter("$namespace/models/").replace(".json", "")
        return if (namespace == "minecraft") path else "$namespace:$path"
    }
}
data class ObfuscatedTexture(val texturePath: String, val obfuscatedTextureName: String)

object ObfuscatePack {

    val tempPackDir: File = OraxenPlugin.get().dataFolder.resolve("pack/obfuscatedPack")
    val originalPackDir: File = OraxenPlugin.get().dataFolder.resolve("pack/originalPack.zip")
    private val tempTextureDir = tempPackDir.resolve("assets/minecraft/textures")
    private val tempModelDir = tempPackDir.resolve("assets/minecraft/models")
    private val tempFontDir = tempPackDir.resolve("assets/minecraft/font")
    private val obfuscatedMap = mutableMapOf<ObfuscatedModel, MutableSet<ObfuscatedTexture>>()
    private val obfuscatedFont = mutableMapOf<String, String>()

    fun obfuscate(pack: File) {
        if (!tempPackDir.exists()) tempPackDir.deleteRecursively()
        if (!pack.exists()) logWarn("Could not find pack at ${pack.absolutePath}")
        val resourcePack = if (originalPackDir.exists()) originalPackDir else pack
        if (!originalPackDir.exists()) pack.copyTo(originalPackDir)
        unzip(resourcePack, tempPackDir)

        val packFiles = tempPackDir.listFilesRecursively()
        obfuscateModels(packFiles)
        obfuscateParentModels(packFiles.filter { it.isTexture })
        obfuscateBlockStateFiles()
        obfuscateFonts(packFiles)
        obfuscateAtlas()

        copyAndCleanup()
    }

    private fun obfuscateFonts(packFiles: List<File>) {
        val defaultFont = packFiles.find { it.packPath == "assets/minecraft/font/default.json" } ?: return
        val defaultJson = JsonParser.parseString(defaultFont.readText()).asJsonObject
        val providers = defaultJson.getAsJsonArray("providers") ?: return

        providers.map { it.asJsonObject }.forEach {
            val texture = it.get("file").asString ?: return
            if (texture.startsWith("required/")) return@forEach

            val obfuscatedTextureName = obfuscatedFont.computeIfAbsent(texture) { UUID.randomUUID().toString().replace("-", "") }
            it.addProperty("file", obfuscatedTextureName)
            providers.add(it)

            // If file doesn't exist don't attempt to rename
            // It has either already been renamed or doesn't exist
            val textureFile = packFiles.find { t -> t.isTexture && t.texturePath == texture } ?: return@forEach
            textureFile.renameTo(File(tempFontDir, textureFile.name))
        }
        defaultJson.add("providers", providers)
        defaultFont.writeText(defaultJson.toString())
    }

    private fun copyAndCleanup() {
        // Delete empty subfolders
        obfuscatedMap.clear()
        OraxenPlugin.get().resourcePack.file.deleteRecursively()
        zipDirectory(tempPackDir, OraxenPlugin.get().resourcePack.file)
        tempPackDir.deleteRecursively()
    }

    private fun obfuscateBlockStateFiles() {
        tempPackDir.resolve("assets/minecraft/blockstates").listFiles()?.forEach { blockstate ->
            val blockstateJson = JsonParser.parseString(blockstate.readText()).asJsonObject
            val variants = blockstateJson.getAsJsonObject("variants") ?: return@forEach
            variants.entrySet().forEach variant@{ variant ->
                val model = variant.value.asJsonObject.getAsJsonPrimitive("model").asString
                val obfuscatedModel = obfuscatedMap.keys.find { it.resourcePackModelPack == model }?.obfuscatedModelName ?: return@variant
                variant.value.asJsonObject.addProperty("model", obfuscatedModel)
                variants.add(variant.key, variant.value)
            }
            blockstateJson.add("variants", variants)
            blockstate.writeText(blockstateJson.toString())
        }
    }

    private fun obfuscateAtlas() {
        val obfuscatedTextures = obfuscatedMap.values.flatten().map { ObfuscatedTexture(it.texturePath.substringBetween("textures/", ".png"), it.obfuscatedTextureName) }
        val newAtlasJson = JsonObject().apply {
            add("sources", JsonArray().apply {
                obfuscatedTextures.forEach {
                    add(JsonObject().apply {
                        addProperty("type", "single")
                        addProperty("resource", it.obfuscatedTextureName)
                        addProperty("sprite", it.obfuscatedTextureName)
                    })
                }
            })
        }

        File(tempPackDir, "assets/minecraft/atlases/blocks.json").writeText(newAtlasJson.toString())
    }

    private fun obfuscateParentModels(packFiles: List<File>) {
        val obfuscatedModelKeys = obfuscatedMap.keys.map {
            val namespace = it.modelPath .substringBetween("assets/", "/models")
            val path = it.modelPath.substringBetween("/models/", ".json")
            ObfuscatedModel(if (namespace == "minecraft") path else "$namespace:$path", it.obfuscatedModelName)
        }
        packFiles.filter { it.isModel && it.isVanillaBaseModel }.forEach baseModel@{ vanillaBaseModel ->
            val baseModelJson = JsonParser.parseString(vanillaBaseModel.readText()).asJsonObject
            val overrides = baseModelJson.getAsJsonArray("overrides") ?: return@baseModel
            overrides.forEach overrides@{ override ->
                val overrideModel = override.asJsonObject.getAsJsonPrimitive("model").asString
                val obfuscatedModel = obfuscatedModelKeys.find { it.modelPath == overrideModel.replace("\"", "") }?.obfuscatedModelName ?: return@overrides
                override.asJsonObject.addProperty("model", obfuscatedModel)
            }
            baseModelJson.add("overrides", overrides)
            vanillaBaseModel.writeText(baseModelJson.toString())
        }
    }

    private fun obfuscateModels(packFiles: List<File>) {
        val models = packFiles.filter { it.isModel && !it.isVanillaBaseModel }
        val textures = packFiles.filter { it.isTexture }
        models.forEach models@{ model ->
            if (!model.exists()) return@models
            val modelJson = JsonParser.parseString(model.readText()).asJsonObject
            val modelTextures = modelJson.getAsJsonObject("textures")?.asMap()?.values?.map { it.asString } ?: return@models
            val obfuscatedModelName = obfuscatedMap.keys.find { it.modelPath == model.modelPath }?.obfuscatedModelName ?: UUID.randomUUID().toString().replace("-", "")

            modelTextures.forEach textures@{ texture ->
                val obfuscatedTextureName = obfuscatedMap.values.flatten().find { it.texturePath.substringBetween("textures/", ".png") == texture }?.obfuscatedTextureName?: UUID.randomUUID().toString().replace("-", "")
                val textureFile = textures.find { texture in it.texturePath } ?: return@textures
                textureFile.renameTo(File(tempTextureDir, "$obfuscatedTextureName.png"))
                File(textureFile.path.replace(".png", ".png.mcmeta")).let {
                    if (it.exists()) it.renameTo(File(tempTextureDir, "$obfuscatedTextureName.png.mcmeta"))
                }
                modelJson.getAsJsonObject("textures").let { it.addProperty(it.entrySet().find { it.value.asString == texture }!!.key, obfuscatedTextureName) }
                obfuscatedMap.computeIfAbsent(ObfuscatedModel(model.packPath, obfuscatedModelName)) { mutableSetOf() } += ObfuscatedTexture(textureFile.packPath, obfuscatedTextureName)
            }

            model.writeText(modelJson.toString())
            model.renameTo(File(tempModelDir, "$obfuscatedModelName.json"))
        }
    }

    private val File.packPath get() = this.absolutePath.removePrefix(tempPackDir.absolutePath).drop(1).replace("\\", "/").replace(".png", "")
    private val File.modelPath get(): String {
        val namespace = this.packPath.substringAfter("assets/").substringBefore("/")
        val path = this.packPath.substringAfter("$namespace/models/")
        return if (namespace == "minecraft") path else "$namespace:$path"
    }
    private val File.texturePath get(): String {
        val namespace = this.packPath.substringAfter("assets/").substringBefore("/")
        val path = this.packPath.substringAfter("$namespace/textures/")
        return if (namespace == "minecraft") path else "$namespace:$path"
    }
    private val File.isModel get() = this.extension == "json"
    private val File.isTexture get() = this.extension == "png" || this.extension == "mcmeta"
    private val File.isVanillaBaseModel get(): Boolean {
        val isBase = this.isModel && "assets\\minecraft\\models\\item" in this.path || "assets\\minecraft\\models\\block" in this.path
        return isBase && Material.matchMaterial(this.nameWithoutExtension) != null
    }

    internal fun String.substringBetween(start: String, end: String) = this.substringAfter(start).substringBefore(end)

    private fun File.listFilesRecursively() = mutableListOf<File>().apply {
        walkTopDown().forEach { this += it }
    }

    private fun unzip(zippedPack: File, destinationDirectory: File) {
        val zipFile = ZipFile(zippedPack)

        if (!destinationDirectory.exists()) destinationDirectory.mkdirs()

        val entries = zipFile.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryDestination = File(destinationDirectory, entry.name)

            if (entry.isDirectory) {
                entryDestination.mkdirs()
            } else {
                entryDestination.parentFile.mkdirs()

                zipFile.getInputStream(entry).use { input ->
                    FileOutputStream(entryDestination).use { output ->
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
        }
    }

    fun zipDirectory(directoryToZip: File, zipFile: File) {
        runCatching {
            val fos = FileOutputStream(zipFile)
            val zos = ZipOutputStream(fos)
            zip(directoryToZip, directoryToZip, zos)
            zos.close()
            fos.close()
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun zip(directory: File, base: File, zos: ZipOutputStream) {
        val files = directory.listFiles() ?: return logWarn("Could not find any files in $directory")
        val buffer = ByteArray(1024)

        for (file in files) {
            if (file.isDirectory) zip(file, base, zos)
            else {
                val entry = ZipEntry(file.relativeTo(base).path)
                zos.putNextEntry(entry)
                val fis = file.inputStream()

                var length: Int
                while (fis.read(buffer).also { length = it } > 0) {
                    zos.write(buffer, 0, length)
                }

                fis.close()
                zos.closeEntry()
            }
        }
    }

}
