package com.mineinabyss.pack_obfuscator

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.pack_obfuscator.GenericPackSquash.logSquashError
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ObfuscatorConfig(
    val generic: Generic = Generic(),
    @YamlComment("Automatically obfuscates packs from supported plugins")
    val oraxen: Oraxen = Oraxen(),
    val crucible: Crucible = Crucible(),
    val modelEngine: ModelEngine = ModelEngine(),
) {

    @Serializable
    data class Generic(val packSquash: PackSquash = PackSquash())

    @Serializable
    data class ModelEngine(val obfuscate: Boolean = true, val packSquash: PackSquash = PackSquash())

    @Serializable
    data class Oraxen(val obfuscate: Boolean = true, val packSquash: PackSquash = PackSquash())

    @Serializable
    data class Crucible(val obfuscate: Boolean = true, val packSquash: PackSquash = PackSquash())

    @Serializable
    data class PackSquash(
        val enabled: Boolean = true,
        val executablePath: String = if (operatingSystem == "windows") "packsquash.exe" else "packsquash",
        val settingsPath: String = "packsquash.toml",
        @YamlComment("If true, all squash-info will be output, not just errors and warnings")
        val debug: Boolean = false,
    ) {
        fun validateExecutable() =
            !File(executablePath).exists() && !obfuscator.plugin.dataFolder.resolve(executablePath).exists()
    }
}