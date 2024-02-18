package com.boy0000.pack_obfuscator

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import kotlin.io.path.absolutePathString

@Serializable
data class ObfuscatorConfig(
    val autoObfuscate: Boolean = true,
    val packSquash: PackSquash = PackSquash(),
) {
    @Serializable
    data class PackSquash(
        val enabled: Boolean = true,
        val executablePath: String = "packsquash",
        val partialPackSquashSettingsPath: String = "packsquash.toml",
    )
}