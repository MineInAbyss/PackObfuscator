package com.boy0000.pack_obfuscator

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class ObfuscatorConfig(
    @YamlComment("Automatically obfuscates packs from supported plugins")
    val autoObfuscate: Boolean = true,
    val packSquash: PackSquash = PackSquash(),
) {
    @Serializable
    data class PackSquash(
        val enabled: Boolean = true,
        val executablePath: String = "packsquash",
        val settingsPath: String = "packsquash.toml",
    )
}