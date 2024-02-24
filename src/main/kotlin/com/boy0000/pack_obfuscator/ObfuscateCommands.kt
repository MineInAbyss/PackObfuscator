package com.boy0000.pack_obfuscator

import com.boy0000.pack_obfuscator.crucible.CruciblePackSquash
import com.boy0000.pack_obfuscator.modelengine.ModelEnginePackSquash
import com.boy0000.pack_obfuscator.oraxen.OraxenPackSquash
import com.mineinabyss.idofront.commands.arguments.genericArg
import com.mineinabyss.idofront.commands.entrypoint.CommandDSLEntrypoint
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.idofront.plugin.Plugins
import io.lumine.mythiccrucible.MythicCrucible
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs.logSuccess
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.command.TabCompleter
import java.io.File

class ObfuscateCommands : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(obfuscator.plugin) {
        "packobfuscator" {
            "reload" {
                action {
                    obfuscator.plugin.createContext()
                    logSuccess("PackObfuscator reloaded successfully!")
                }
            }
            "squash" { squashCommand() }
            ("obfuscate" / "obf") { obfuscateCommand() }
        }
        "squash" { squashCommand() }
        ("obfuscate" / "obf") { obfuscateCommand() }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (command.name) {
            "packobfuscator" -> {
                when (args.size) {
                    1 -> listOf("reload", "squash", "obfuscate")
                    2 -> when (args[0]) {
                        "obfuscate" -> listOf("oraxen", "itemsadder", "crucible", "modelengine", "<path>")
                        "squash" -> listOf("oraxen", "crucible", "modelengine")
                        else -> emptyList()
                    }

                    else -> emptyList()
                }
            }
            "squash" -> listOf("oraxen", "crucible", "modelengine")
            "obfuscate" -> listOf("oraxen", "itemsadder", "crucible", "modelengine", "<path>")
            else -> emptyList()
        }
    }

    private fun com.mineinabyss.idofront.commands.Command.squashCommand() {
        val squasher: PackSquash? by genericArg {
            when(it.lowercase()) {
                "oraxen" -> if (Plugins.isEnabled("Oraxen")) OraxenPackSquash else return@genericArg null
                "crucible" -> if (Plugins.isEnabled("MythicCrucible")) CruciblePackSquash else return@genericArg null
                "modelengine" -> if (Plugins.isEnabled("ModelEngine")) ModelEnginePackSquash else return@genericArg null
                "generic" -> GenericPackSquash
                else -> null
            }
        }
        action {
            val packSquash = when (squasher) {
                is OraxenPackSquash -> obfuscator.config.oraxen.packSquash
                is CruciblePackSquash -> obfuscator.config.crucible.packSquash
                is ModelEnginePackSquash -> obfuscator.config.modelEngine.packSquash
                else -> obfuscator.config.generic.packSquash
            }
            squasher?.let {
                logInfo("Attemping to squash pack via ${it::class.simpleName}...")
                it.squashPack(packSquash)
                logSuccess("Successfully Squashed pack!")
            } ?: sender.error("Invalid pack!")
        }
    }
    private fun com.mineinabyss.idofront.commands.Command.obfuscateCommand() {
        fun file(string: String): File? {
            return when (string.lowercase()) {
                "oraxen" -> if (Plugins.isEnabled("Oraxen")) OraxenPack.getPack() else null
                "itemsadder" -> sender.error("ItemsAdder is not supported yet!").let { null }
                "crucible" -> if (Plugins.isEnabled("MythicCrucible")) MythicCrucible.inst().packGenerationManager.zippedOutput else null
                "modelengine" -> if (Plugins.isEnabled("ModelEngine")) ModelEnginePackSquash.outputZip else null
                else -> File(string.correctWindowsPath()).takeIf { it.exists() }
            }
        }
        val input: File? by genericArg { file(it)}
        val output: File? by genericArg { file(it) }
        action {
            if (input == null) return@action sender.error("Invalid input-path!")
            if (output == null) return@action sender.error("Invalid output-path!")

            logInfo("Attempting to Obfuscate pack...")
            CreativeObfuscator.obfuscate(input!!, output!!.toPath())
            logSuccess("Successfully Obfuscated pack!")
            OraxenPack.uploadPack()
        }
    }
}
