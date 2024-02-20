package com.boy0000.pack_obfuscator.crucible

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.boy0000.pack_obfuscator.oraxen.OraxenPackSquash
import com.boy0000.pack_obfuscator.unzip
import com.mineinabyss.idofront.messaging.logInfo
import io.lumine.mythiccrucible.MythicCrucible
import java.io.File
import java.util.logging.Logger

object CruciblePackSquash : PackSquash {

    private val inputPackDir = obfuscator.plugin.dataFolder.resolve("crucible/pack/")
    private val outputZip = obfuscator.plugin.dataFolder.resolve("crucible/pack.zip")

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