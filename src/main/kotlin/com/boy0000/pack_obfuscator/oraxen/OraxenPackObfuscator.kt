package com.boy0000.pack_obfuscator.oraxen

import com.boy0000.pack_obfuscator.substringBetween
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.th0rgal.oraxen.utils.VirtualFile
import org.bukkit.Material
import java.util.*

private typealias TexturePath = String
private typealias ModelPath = String

object OraxenPackObfuscator {
    fun obfuscate(output: MutableList<VirtualFile>) {
        obfuscatedMap.clear()
        obfuscatedFont.clear()
        obfuscateModels(output)
        obfuscateParentModels(output)
        obfuscateBlockStateFiles(output)
        obfuscateFonts(output)
        obfuscateAtlas(output)
    }

    private data class ObfuscatedModel(val packPath: String, val obfPackPath: String)
    private data class ObfuscatedTexture(val packPath: String, val obfPackPath: String)
    private val obfuscatedMap = mutableMapOf<ObfuscatedModel, MutableSet<ObfuscatedTexture>>()
    private val obfuscatedFont = mutableMapOf<String, String>()
    private val VirtualFile.modelPath get() = this.path.modelPath
    private val ObfuscatedModel.modelPath get() = this.packPath.modelPath
    private val ModelPath.modelPath get(): String {
        val namespace = substringBetween("assets/", "/")
        val path = substringBetween("$namespace/models/", ".json")
        return if (namespace == "minecraft") path else "$namespace:$path"
    }
    private val VirtualFile.texturePath get() = this.path.texturePath
    private val ObfuscatedTexture.texturePath get() = this.packPath.texturePath
    private val TexturePath.texturePath get(): String {
        val namespace = this.substringBetween("assets/", "/")
        val path = this.substringBetween("$namespace/textures/", ".png")
        return if (namespace == "minecraft") path else "$namespace:$path"
    }
    private val modelRegex = "assets/.*/models/.*\\.json".toRegex()
    private fun VirtualFile.isModel() = path.matches(modelRegex) && !isVanillaBaseModel()
    private val baseModelRegex = "assets/minecraft/models/(item|block)/.*.json".toRegex()
    private fun VirtualFile.isVanillaBaseModel() = path.matches(baseModelRegex) && Material.matchMaterial(this.path.substringAfterLast("/").substringBeforeLast(".json"), false) != null
    private val textureRegex = "assets/.*/textures/.*\\.png".toRegex()
    private fun VirtualFile.isTexture() = path.matches(textureRegex)
    private val fontRegex: Regex = "assets/.*/font/.*.json".toRegex()
    private fun VirtualFile.isFont() = path.matches(fontRegex)
    private fun obfuscateModels(output: MutableList<VirtualFile>) {
        val models = output.filter { it.isModel() }
        val textures = output.filter { it.isTexture() }
        models.forEach models@{ virtualModel ->
            val virtualModelJson = virtualModel.toJsonElement()?.takeIf { it.isJsonObject }?.asJsonObject ?: return@models
            val obfModel = obfuscatedMap.keys.find { it.packPath == virtualModel.modelPath } ?: ObfuscatedModel(virtualModel.modelPath, UUID.randomUUID().toString().replace("-", ""))
            virtualModelJson.getAsJsonObject("textures")?.entrySet()?.toSet()?.mapNotNull textures@{ (key, json) ->
                val texture = json.asString
                val obfTexture = obfuscatedMap.values.flatten().find { it.packPath == texture }?.obfPackPath?: UUID.randomUUID().toString().replace("-", "")
                obfuscatedMap.computeIfAbsent(obfModel) { mutableSetOf() } += ObfuscatedTexture(texture, obfTexture)
                // Attempt to find the texture in the output. If there is no result, it has likely been obfuscated already
                textures.find { texture == it.texturePath }?.let {  virtualFile ->
                    output.find { it.path == virtualFile.texturePath + ".mcmeta" }?.path = "assets/minecraft/textures/$obfTexture.png.mcmeta"
                    virtualFile.path = "assets/minecraft/textures/$obfTexture.png"
                }
                key to obfTexture
            }?.forEach { (key, obf) ->
                virtualModelJson.add("textures", virtualModelJson.getAsJsonObject("textures").apply { addProperty(key, obf) })
            } ?: run { obfuscatedMap.putIfAbsent(obfModel, mutableSetOf()) }
            virtualModel.inputStream = virtualModelJson.toString().byteInputStream()
            virtualModel.path = "assets/minecraft/models/${obfModel.obfPackPath}.json"
        }
    }
    private fun obfuscateParentModels(output: List<VirtualFile>) {
        output.filter { it.isVanillaBaseModel() }.forEach baseModel@{ vanillaBaseModel ->
            val baseModelJson = vanillaBaseModel.toJsonElement()?.asJsonObject ?: return@baseModel
            val overrides = baseModelJson.getAsJsonArray("overrides") ?: return@baseModel
            val obfOverrides: List<JsonElement> = overrides.mapNotNull overrides@{ override ->
                val overrideModel = override.asJsonObject.getAsJsonPrimitive("model").asString
                val obfuscatedModel = obfuscatedMap.keys.find { it.packPath == overrideModel }?.obfPackPath ?: return@overrides null
                override.asJsonObject.addProperty("model", obfuscatedModel)
                return@overrides override
            }
            baseModelJson.remove("overrides")
            baseModelJson.add("overrides", JsonArray().apply { obfOverrides.forEach { add(it) } })
            vanillaBaseModel.inputStream = baseModelJson.toString().byteInputStream()
        }
    }
    private fun obfuscateBlockStateFiles(output: MutableList<VirtualFile>) {
        output.filter { it.path.startsWith("assets/minecraft/blockstates/") }.forEach { virtualFile ->
            val blockstateJson = virtualFile.toJsonElement()?.asJsonObject ?: return@forEach
            val variants = blockstateJson.getAsJsonObject("variants") ?: return@forEach
            variants.entrySet().forEach variant@{ variant ->
                if (variant.value.isJsonObject) {
                    val model = variant.value.asJsonObject.getAsJsonPrimitive("model").asString.replace("minecraft:", "")
                    val obfuscatedModel = obfuscatedMap.keys.find { it.packPath == model }?.obfPackPath ?: return@variant
                    variant.value.asJsonObject.addProperty("model", obfuscatedModel)
                    variants.add(variant.key, variant.value)
                } else if (variant.value.isJsonArray) {
                    variant.value.asJsonArray.forEach { element ->
                        val model = element.asJsonObject.getAsJsonPrimitive("model").asString.replace("minecraft:", "")
                        val obfuscatedModel = obfuscatedMap.keys.find { it.packPath == model }?.obfPackPath ?: return@variant
                        element.asJsonObject.addProperty("model", obfuscatedModel)
                    }
                    variants.add(variant.key, variant.value)
                }
            }
            blockstateJson.add("variants", variants)
            virtualFile.inputStream = blockstateJson.toString().byteInputStream()
        }
    }
    private fun obfuscateAtlas(output: MutableList<VirtualFile>) {
        val newAtlasJson = JsonObject().apply {
            add("sources", JsonArray().apply {
                obfuscatedMap.values.flatten().map(ObfuscatedTexture::obfPackPath).forEach {
                    add(JsonObject().apply {
                        addProperty("type", "single")
                        addProperty("resource", it)
                        addProperty("sprite", it)
                    })
                }
            })
        }
        output.find { it.path == "assets/minecraft/atlases/blocks.json" }?.let {
            it.inputStream = newAtlasJson.toString().byteInputStream()
        } ?: output.add(VirtualFile("assets/minecraft/atlases/", "blocks.json", newAtlasJson.toString().byteInputStream()))
    }
    private fun obfuscateFonts(output: List<VirtualFile>) {
        val flattenedObfTextures = obfuscatedMap.values.flatten()
        output.filter { it.isFont() }.forEach { virtualFont ->
            val virtualJson = virtualFont.toJsonElement()?.takeIf { it.isJsonObject }?.asJsonObject ?: return
            val providers = virtualJson.getAsJsonArray("providers") ?: return
            providers.map { it.asJsonObject }.forEach providers@{
                val texture = it.get("file").asString.replace("minecraft:", "").replace(".png", "")
                if (texture in obfuscatedFont.values) return@providers
                val obfTextureName = obfuscatedFont.putIfAbsent(texture, flattenedObfTextures.find { it.packPath == texture || it.obfPackPath == texture }?.obfPackPath ?: UUID.randomUUID().toString().replace("-", "")) ?: obfuscatedFont[texture] ?: return@providers
                providers.remove(it)
                it.addProperty("file", "$obfTextureName.png")
                providers.add(it)
            }
            virtualJson.add("providers", providers)
            virtualFont.inputStream = virtualJson.toString().byteInputStream()
        }
        output.filter { it.isTexture() && it.texturePath in obfuscatedFont.keys }.forEach { it.path = "assets/minecraft/textures/${obfuscatedFont[it.texturePath]}.png" }
    }
}