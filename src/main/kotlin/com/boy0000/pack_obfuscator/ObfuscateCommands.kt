package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.commands.arguments.genericArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.idofront.plugin.Plugins
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs.logSuccess
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.io.File

class ObfuscateCommands : IdofrontCommandExecutor(), TabCompleter {

    override val commands = commands(obfuscator.plugin) {
        ("obfuscate" / "obf") {
            "reload" {
                action {
                    obfuscator.plugin.createContext()
                    logSuccess("Successfully Reloaded OraxenPackObfuscator!")
                }
            }
            "squash" {
                action {
                    logInfo("Attemping to squash pack via PackSquash...")
                    GenericPackSquash.squashPack()
                    logSuccess("Successfully Squashed pack!")
                }
            }
            "creative" {
                fun file(string: String): File? {
                    return when (string.lowercase()) {
                        "oraxen" -> if (Plugins.isEnabled("Oraxen")) OraxenPack.getPack() else null
                        "itemsadder" -> sender.error("ItemsAdder is not supported yet!").let { null }
                        else -> File(string.replace("\\", "/")).takeIf { it.exists() }
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
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return if (command.name == "obfuscate") {
            when (args.size) {
                1 -> listOf("reload", "squash", "creative")
                2 -> when (args[0]) {
                    "creative" -> listOf("oraxen", "itemsadder")
                    else -> emptyList()
                }

                else -> emptyList()
            }
        } else emptyList()
    }
}
