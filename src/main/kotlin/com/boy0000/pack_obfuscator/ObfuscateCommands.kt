package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.commands.entrypoint.CommandDSLEntrypoint
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs
import java.io.File

class ObfuscateCommands : IdofrontCommandExecutor() {

    override val commands = commands(obfuscator) {
        command("oraxen_obf") {
            action {
                Logs.logInfo("Attempting to Obfuscate OraxenPack...")
                ObfuscatePack.obfuscate(OraxenPack.getPack())
                Logs.logSuccess("Successfully Obfuscated OraxenPack!")
            }
        }
    }
}
