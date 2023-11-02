package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs

class ObfuscateCommands : IdofrontCommandExecutor() {

    override val commands = commands(obfuscator) {
        ("oraxen_obf") {
            "main" {
                action {
                    Logs.logInfo("Attempting to Obfuscate OraxenPack...")
                    ObfuscatePack.obfuscate(OraxenPack.getPack())
                    Logs.logSuccess("Successfully Obfuscated OraxenPack!")
                    OraxenPack.uploadPack()
                }
            }
            "creative" {
                action {
                    Logs.logInfo("Attempting to Obfuscate OraxenPack via Creative...")
                    CreativeObfuscator.obfuscate()
                    Logs.logSuccess("Successfully Obfuscated OraxenPack via Creative!")
                    OraxenPack.uploadPack()
                }
            }
        }
    }
}
