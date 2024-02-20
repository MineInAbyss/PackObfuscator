package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.messaging.logWarn
import net.kyori.adventure.key.Key
import org.bukkit.Material
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.atlas.AtlasSource
import team.unnamed.creative.atlas.SingleAtlasSource
import team.unnamed.creative.blockstate.MultiVariant
import team.unnamed.creative.blockstate.Selector
import team.unnamed.creative.blockstate.Variant
import team.unnamed.creative.font.BitMapFontProvider
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.texture.Texture
import java.io.File
import java.nio.file.Path
import java.util.*

object CreativeObfuscator {

    private val defaultItemKeys = Material.entries.filterNot { it.isLegacy }.map { Key.key("minecraft", "item/" + it.key().value()) }
    private val defaultBlockKeys = Material.entries.filterNot { it.isLegacy }.map { Key.key("minecraft", "block/" + it.key().value()) }
    private lateinit var resourcePack: ResourcePack

    fun obfuscate(file: File, output: Path) {
        obfuscatedModels.clear()
        obfuscatedTextures.clear()
        resourcePack = MinecraftResourcePackReader.minecraft().readFromZipFile(file)
        obfuscateTextures()
        obfuscateModels()
        obfuscateFonts()
        obfuscateBlockStates()
        obfuscateAtlas()
        MinecraftResourcePackWriter.minecraft().writeToZipFile(output, resourcePack)
    }

    private class ObfuscatedModel(val originalModel: Model, val obfuscatedModel: Model)
    private class ObfuscatedTexture(val originalTexture: Texture, val obfuscatedTexture: Texture)
    private val obfuscatedModels = mutableSetOf<ObfuscatedModel>()
    private val obfuscatedTextures = mutableSetOf<ObfuscatedTexture>()

    private fun obfuscateAtlas() {
        resourcePack.atlases().filterNotNull().forEach { atlas ->
            resourcePack.atlas(atlas.toBuilder().sources(atlas.sources().mapNotNull { it as? SingleAtlasSource }.map { source ->
                AtlasSource.single(Key.key(obfuscatedTextures.find { it.originalTexture.keyNoPng == source.resourceNoPng }?.obfuscatedTexture?.keyNoPng ?: source.resourceNoPng))
            }).build())
        }
    }

    private fun obfuscateFonts() {
        resourcePack.fonts().mapNotNull { it }.forEach { font ->
            resourcePack.font(font.toBuilder().providers(font.providers().filterNotNull().mapNotNull { it as? BitMapFontProvider }.map { bitmapProvider ->
                bitmapProvider.file(obfuscatedTextures.find { it.originalTexture.keyNoPng == bitmapProvider.fileNoPng }?.obfuscatedTexture?.key() ?: bitmapProvider.file())
            }).build())
        }
    }

    private fun obfuscateBlockStates() {
        resourcePack.blockStates().filterNotNull().forEach { blockState ->
            val multiparts = blockState.multipart().map {
                Selector.of(it.condition(), MultiVariant.of(it.variant().variants().map { v -> v.obfuscateVariant() }))
            }
            blockState.multipart().clear()
            blockState.multipart().addAll(multiparts)

            val variants = blockState.variants().map {
                it.key to MultiVariant.of(it.value.variants().map { v -> v.obfuscateVariant() })
            }
            blockState.variants().clear()
            blockState.variants().putAll(variants)
            resourcePack.blockState(blockState)
        }
    }

    private fun Variant.obfuscateVariant(): Variant {
        return Variant.builder().model(obfuscatedModels.find { it.originalModel.key() == model() }?.obfuscatedModel?.key() ?: model())
            .uvLock(uvLock()).weight(weight()).x(x()).y(y()).build()
    }

    private fun obfuscateTextures() {
        resourcePack.textures().filterNotNull().forEach { texture ->
            val obfuscatedTexture = texture.rename(Key.key(texture.key().namespace(), UUID.randomUUID().toString() + ".png"))
            obfuscatedTextures += ObfuscatedTexture(texture, obfuscatedTexture)
        }

        obfuscatedTextures.forEach {
            resourcePack.removeTexture(it.originalTexture.key())
            resourcePack.texture(it.obfuscatedTexture)
        }

    }
    private fun obfuscateModels() {
        resourcePack.models().filterNotNull().forEach models@{ model ->
            val obfuscatedModel = obfuscateModel(model)
            obfuscatedModel.overrides().filterNotNull().map { override ->
                if (override.model() in defaultItemKeys || override.model() in defaultBlockKeys) return@models
                val obfuscatedOverride = resourcePack.models().find { it.key() == override.model() }?.let { obfuscateModel(it) } ?: override.model()
                ItemOverride.of(obfuscatedOverride.key(), override.predicate())
            }.let {
                obfuscatedModel.overrides().clear()
                obfuscatedModel.overrides().addAll(it)
            }
        }

        obfuscatedModels.map { it.originalModel.key() }.forEach(resourcePack::removeModel)
        obfuscatedModels.map { it.obfuscatedModel }.toSet().forEach(resourcePack::model)
    }

    private fun obfuscateModel(model: Model): Model {
        if (model.key() in defaultItemKeys || model.key() in defaultBlockKeys) return model
        if (model.key() in obfuscatedModels.map { it.obfuscatedModel.key() }) return model

        val obfuscatedModel = model.obfuscateModelTextures().rename(Key.key(model.key().namespace(), UUID.randomUUID().toString()))
        obfuscatedModels += ObfuscatedModel(model, obfuscatedModel)
        return obfuscatedModel
    }

    private fun Model.obfuscateModelTextures() : Model {
        val layers = textures().layers().filter { it?.key() != null }.map { modelTexture ->
            if (modelTexture.key()?.asString()!!.contains("storm_sword")) logError(modelTexture?.key()?.asString() ?: "nuller")
            obfuscatedTextures.find { it.originalTexture.keyNoPng == modelTexture.keyNoPng }?.obfuscatedTexture
                ?.let { ModelTexture.ofKey(it.key()) } ?: modelTexture
        }
        val variables = textures().variables().map { variable ->
            variable.key to (obfuscatedTextures.find { it.originalTexture.keyNoPng == variable.value.keyNoPng }?.obfuscatedTexture
                ?.let { ModelTexture.ofKey(it.key()) } ?: variable.value)
        }.toMap()

        val particle = textures().particle()?.let { p -> obfuscatedTextures.find { it.originalTexture.keyNoPng == p.keyNoPng }?.obfuscatedTexture?.key() ?: p.key() }?.let { ModelTexture.ofKey(it) }

        return Model.model().key(key())
            .textures(ModelTextures.builder().layers(layers).variables(variables).particle(particle).build())
            .elements(elements())
            .ambientOcclusion(ambientOcclusion())
            .overrides(overrides())
            .guiLight(guiLight())
            .display(display())
            .parent(parent())
            .build()
    }

    private val Texture.keyNoPng get() = key().asString().removeSuffix(".png")
    private val ModelTexture.keyNoPng get() = key()?.asString()?.removeSuffix(".png")
    private val BitMapFontProvider.fileNoPng get() = file().asString().removeSuffix(".png")
    private val SingleAtlasSource.resourceNoPng get() = resource().asString().removeSuffix(".png")


    private fun Model.rename(renameKey: Key): Model {

        return Model.model().key(renameKey)
            .parent(parent())
            .elements(elements())
            .ambientOcclusion(ambientOcclusion())
            .overrides(overrides())
            .guiLight(guiLight())
            .display(display())
            .textures(textures())
            .build()
    }

    private fun Texture.rename(renameKey: Key): Texture {
        return Texture.texture(renameKey, data()).meta(meta())
    }
}
