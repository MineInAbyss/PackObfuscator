package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs

class ObfuscateCommands : IdofrontCommandExecutor() {

    override val commands = commands(obfuscator) {
        ("oraxen_obf") {
            "creative" {
                action {
                    Logs.logInfo("Attempting to Obfuscate OraxenPack via Creative...")
                    CreativeObfuscator.obfuscate(OraxenPack.getPack(), OraxenPack.getPack().toPath().parent.resolve("pack.zip"))
                    Logs.logSuccess("Successfully Obfuscated OraxenPack via Creative!")
                    OraxenPack.uploadPack()
                }
            }
        }
    }
}
