package com.boy0000.pack_obfuscator.oraxen

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.boy0000.pack_obfuscator.unzip
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs

object OraxenPackSquash: PackSquash {
    override fun extractExecutable() {
        super.extractExecutable()
        val toml = obfuscator.plugin.dataFolder.resolve("packsquash.toml")
        val packDir = obfuscator.plugin.dataFolder.resolve("oraxen/pack").absolutePath.replace("\\", "/")
        val outputPack = obfuscator.plugin.dataFolder.resolve("oraxen/pack.zip").absolutePath.replace("\\", "/")
        val tomlContent = toml.readText()
            .replace("pack_directory = .*".toRegex(), "pack_directory = \'${packDir}\'")
            .replace("output_file_path = .*".toRegex(), "output_file_path = \'${outputPack}\'")
        toml.writeText(tomlContent)
    }

    fun squashOraxenPack() {
        val destination = obfuscator.plugin.dataFolder.resolve("oraxenPack/")
        val cache = obfuscator.plugin.dataFolder.resolve("oraxenPack.zip")
        unzip(OraxenPack.getPack(), destination)

        squashPack()

        destination.deleteRecursively()
        cache.copyTo(OraxenPack.getPack(), true)
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