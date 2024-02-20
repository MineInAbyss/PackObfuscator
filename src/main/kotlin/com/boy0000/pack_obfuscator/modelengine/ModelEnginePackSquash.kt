package com.boy0000.pack_obfuscator.modelengine

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.ticxo.modelengine.api.ModelEngineAPI
import io.th0rgal.oraxen.utils.logs.Logs
import java.io.File
import java.util.logging.Logger

object ModelEnginePackSquash : PackSquash {

    private val megZipped = obfuscator.plugin.dataFolder.parentFile.resolve("ModelEngine/resource pack.zip")
    private val inputPackDir = obfuscator.plugin.dataFolder.parentFile.resolve("ModelEngine/resource pack")

    override fun extractExecutable() {
        super.extractExecutable()
        val settingsPath = obfuscator.config.packSquash.settingsPath
        val toml = File(settingsPath).takeIf { it.exists() } ?: obfuscator.plugin.dataFolder.resolve(settingsPath)
        val packDir = inputPackDir.absolutePath.replace("\\", "/")
        val outputPack = megZipped.absolutePath.replace("\\", "/")
        val tomlContent = toml.readText()
            .replace("pack_directory = .*".toRegex(), "pack_directory = \'${packDir}\'")
            .replace("output_file_path = .*".toRegex(), "output_file_path = \'${outputPack}\'")
        toml.writeText(tomlContent)
    }

    fun squashModelEnginePack() {
        super.squashPack()
    }

    private val logger = ModelEngineAPI.getAPI().logger
    override fun logSquashError(line: String) {
        logger.severe("Error while squashing OraxenPack: $line")
    }

    override fun logSquashWarning(line: String) {
        logger.warning("Warning while squashing OraxenPack: $line")
    }

    override fun logSquashInfo(line: String) {
        if (obfuscator.config.packSquash.debug) logger.info("Info while squashing OraxenPack: $line")
    }
}