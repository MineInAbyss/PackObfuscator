package com.boy0000.pack_obfuscator.crucible

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.boy0000.pack_obfuscator.oraxen.OraxenPackSquash
import com.boy0000.pack_obfuscator.unzip
import io.lumine.mythiccrucible.MythicCrucible
import io.th0rgal.oraxen.api.OraxenPack
import java.io.File

class CruciblePackSquash : PackSquash {

    val inputPackDir = obfuscator.plugin.dataFolder.resolve("crucible/pack/")
    val outputZip = obfuscator.plugin.dataFolder.resolve("crucible/pack.zip")

    override fun extractExecutable() {
        super.extractExecutable()
        val settingsPath = obfuscator.config.packSquash.settingsPath
        val toml = File(settingsPath).takeIf { it.exists() } ?: obfuscator.plugin.dataFolder.resolve(settingsPath)
        val packDir = OraxenPackSquash.inputPackDir.absolutePath.replace("\\", "/")
        val outputPack = OraxenPackSquash.outputZip.absolutePath.replace("\\", "/")
        val tomlContent = toml.readText()
            .replace("pack_directory = .*".toRegex(), "pack_directory = \'${packDir}\'")
            .replace("output_file_path = .*".toRegex(), "output_file_path = \'${outputPack}\'")
        toml.writeText(tomlContent)
    }

    fun squashCruciblePack() {
        unzip(MythicCrucible.inst().packGenerationManager.zippedOutput, inputPackDir)

        super.squashPack()

        inputPackDir.deleteRecursively()
        outputZip.copyTo(MythicCrucible.inst().packGenerationManager.zippedOutput, true)
    }
}