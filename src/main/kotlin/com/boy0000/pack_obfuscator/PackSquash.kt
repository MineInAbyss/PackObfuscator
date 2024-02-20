package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.idofront.messaging.logWarn
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ProcessBuilder.Redirect
import java.util.logging.Logger

object GenericPackSquash : PackSquash {

}

interface PackSquash {
    fun extractExecutable() {
        if (!obfuscator.plugin.dataFolder.resolve("packsquash.exe").exists()) {
            logInfo("Extracting PackSquash executable...")
            obfuscator.plugin.saveResource("packsquash.exe", false)
        }

        val toml = obfuscator.plugin.dataFolder.resolve("packsquash.toml")
        if (!toml.exists()) {
            logInfo("Extracting PackSquash settings...")
            obfuscator.plugin.saveResource("packsquash.toml", false)
        }
    }
    fun squashPack() {
        val packSquashExecutablePath = obfuscator.config.packSquash.executablePath
        val partialPackSquashSettingsPath = obfuscator.config.packSquash.settingsPath

        runCatching {
            val processBuilder = ProcessBuilder(packSquashExecutablePath, partialPackSquashSettingsPath)
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
        }
    }

    fun logSquashError(line: String) {
        logError("Error while squashing pack: $line")
    }

    fun logSquashWarning(line: String) {
        logWarn("Warning while squashing pack: $line")
    }

    fun logSquashInfo(line: String) {
        if (obfuscator.config.packSquash.debug) logInfo("Info while squashing pack: $line")
    }
}
