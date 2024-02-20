package com.boy0000.pack_obfuscator.oraxen

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.boy0000.pack_obfuscator.unzip
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs
import java.io.File

object OraxenPackSquash: PackSquash {

    val inputPackDir = obfuscator.plugin.dataFolder.resolve("oraxen/pack/")
    val outputZip = obfuscator.plugin.dataFolder.resolve("oraxen/pack.zip")
    override fun extractExecutable() {
        super.extractExecutable()
        val settingsPath = obfuscator.config.packSquash.settingsPath
        val toml = File(settingsPath).takeIf { it.exists() } ?: obfuscator.plugin.dataFolder.resolve(settingsPath)
        val packDir = inputPackDir.absolutePath.replace("\\", "/")
        val outputPack = outputZip.absolutePath.replace("\\", "/")
        val tomlContent = toml.readText()
            .replace("pack_directory = .*".toRegex(), "pack_directory = \'${packDir}\'")
            .replace("output_file_path = .*".toRegex(), "output_file_path = \'${outputPack}\'")
        toml.writeText(tomlContent)
    }

    fun squashOraxenPack() {
        unzip(OraxenPack.getPack(), inputPackDir)

        super.squashPack()

        inputPackDir.deleteRecursively()
        outputZip.copyTo(OraxenPack.getPack(), true)
    }

    override fun logSquashError(line: String) {
        Logs.logError("Error while squashing OraxenPack: $line")
    }

    override fun logSquashWarning(line: String) {
        Logs.logWarning("Warning while squashing OraxenPack: $line")
    }

    override fun logSquashInfo(line: String) {
        Logs.debug("Info while squashing OraxenPack: $line")
    }
}