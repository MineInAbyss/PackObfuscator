package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.messaging.logInfo
import io.th0rgal.oraxen.OraxenPlugin
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.config.Settings
import io.th0rgal.oraxen.utils.logs.Logs
import java.io.*
import java.lang.ProcessBuilder.Redirect
import java.nio.charset.StandardCharsets
import kotlin.io.path.absolutePathString


object PackSquash {
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
        val packDir = obfuscator.plugin.dataFolder.resolve("oraxenPack").absolutePath.replace("\\", "/")
        val outputPack = obfuscator.plugin.dataFolder.resolve("oraxenPack.zip").absolutePath.replace("\\", "/")
        val tomlContent = toml.readText()
            .replace("pack_directory = .*".toRegex(), "pack_directory = \'${packDir}\'")
            .replace("output_file_path = .*".toRegex(), "output_file_path = \'${outputPack}\'")
        toml.writeText(tomlContent)
    }
    fun squashOraxenPack() {
        val destination = obfuscator.plugin.dataFolder.resolve("oraxenPack/")
        val cache = obfuscator.plugin.dataFolder.resolve("oraxenPack.zip")
        unzip(OraxenPack.getPack(), destination)

        val packSquashExecutablePath = obfuscator.config.packSquash.executablePath
        val partialPackSquashSettingsPath = obfuscator.config.packSquash.partialPackSquashSettingsPath

        runCatching {
            val processBuilder = ProcessBuilder(packSquashExecutablePath, partialPackSquashSettingsPath)
            processBuilder.directory(obfuscator.plugin.dataFolder)
            processBuilder.redirectInput(Redirect.PIPE)
            processBuilder.redirectOutput(Redirect.PIPE)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            // Read the output of the command
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line?.startsWith("!") == true) Logs.logError(line)
                else if (line?.startsWith("*") == true) Logs.logWarning(line)
                else Logs.debug(line)
            }

            process.waitFor()
        }.onFailure {
            it.printStackTrace()
        }

        destination.deleteRecursively()
        cache.copyTo(OraxenPack.getPack(), true)
    }
}
