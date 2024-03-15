package com.mineinabyss.pack_obfuscator

import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.idofront.messaging.logWarn
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.ProcessBuilder.Redirect

object GenericPackSquash : PackSquash {
    private val settingToml: File get() {
        val settingsPath = obfuscator.config.generic.packSquash.settingsPath
        return File(settingsPath).takeIf { it.exists() } ?: obfuscator.plugin.dataFolder.resolve(settingsPath)
    }
    override val inputDir: File
        get() = settingToml.readText().substringBetween("pack_directory = '", "'").let(::File)
    override val outputZip: File
        get() = settingToml.readText().substringBetween("output_file_path = '", "'").let(::File)
}

interface PackSquash {

    val inputDir: File
    val outputZip: File
    fun extractPackSquashConfig(packsquash: ObfuscatorConfig.PackSquash) {
        val packDir = inputDir.absolutePath.correctWindowsPath()
        val outputPack = outputZip.absolutePath.correctWindowsPath()
        val toml = File(packsquash.settingsPath).takeIf { it.exists() } ?: obfuscator.plugin.dataFolder.resolve(packsquash.settingsPath)
        if (!toml.exists()) {
            logInfo("Extracting PackSquash settings...")
            obfuscator.plugin.saveResource("packsquash.toml", false)
        }

        val tomlContent = toml.readText()
            .replace("pack_directory = .*".toRegex(), "pack_directory = \'${packDir}\'")
            .replace("output_file_path = .*".toRegex(), "output_file_path = \'${outputPack}\'")
        toml.writeText(tomlContent)
    }

    fun squashPack(packsquash: ObfuscatorConfig.PackSquash): Boolean {
        if (!packsquash.validateExecutable()) {
            logSquashError("""
                |PackSquash executable not found, skipping process...
                |Please set the correct path in the config, or download it from https://github.com/ComunidadAylas/PackSquash-action/releases
            """.trimMargin())
            return false
        }

        return runCatching {
            val processBuilder = ProcessBuilder(packsquash.executablePath, packsquash.settingsPath)
            processBuilder.directory(obfuscator.plugin.dataFolder)
            processBuilder.redirectInput(Redirect.PIPE)
            processBuilder.redirectOutput(Redirect.PIPE)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            // Read the output of the command
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var currentLine: String?
            while (reader.readLine().also { currentLine = it } != null) {
                val line = currentLine.takeUnless { it.isNullOrEmpty() } ?: continue
                when {
                    line.startsWith("!") -> logSquashError(line)
                    line.startsWith("*") -> logSquashWarning(line)
                    else -> logSquashInfo(line)
                }
            }

            process.waitFor()
        }.onFailure {
            it.printStackTrace()
        }.isSuccess
    }

    fun logSquashError(line: String) {
        logError("Error while squashing pack: $line")
    }

    fun logSquashWarning(line: String) {
        logWarn("Warning while squashing pack: $line")
    }

    fun logSquashInfo(line: String) {
        if (obfuscator.config.generic.packSquash.debug) logInfo("Info while squashing pack: $line")
    }
}
