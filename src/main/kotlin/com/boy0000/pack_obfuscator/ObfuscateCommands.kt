package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import io.th0rgal.oraxen.OraxenPlugin
import io.th0rgal.oraxen.utils.logs.Logs

class ObfuscateCommands : IdofrontCommandExecutor() {

    override val commands = commands(obfuscator) {
        command("oraxen_obf") {
            action {
                Logs.logInfo("Attempting to Obfuscate OraxenPack...")
                ObfuscatePack.obfuscate(OraxenPlugin.get().resourcePack.file)
                Logs.logSuccess("Successfully Obfuscated OraxenPack!")
            }
        }
    }
}
