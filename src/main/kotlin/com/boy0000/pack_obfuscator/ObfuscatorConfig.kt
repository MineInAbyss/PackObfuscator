package com.boy0000.pack_obfuscator

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class ObfuscatorConfig(
    @YamlComment("Automatically obfuscates packs from supported plugins")
    val oraxen: Oraxen = Oraxen(),
    val crucible: Crucible = Crucible(),
    val packSquash: PackSquash = PackSquash(),
) {

    @Serializable
    data class Oraxen(val obfuscate: Boolean = true, )

    @Serializable
    data class Crucible(val obfuscate: Boolean = true)

    @Serializable
    data class PackSquash(
        val enabled: Boolean = true,
        val executablePath: String = "packsquash",
        val settingsPath: String = "packsquash.toml",
    )
}