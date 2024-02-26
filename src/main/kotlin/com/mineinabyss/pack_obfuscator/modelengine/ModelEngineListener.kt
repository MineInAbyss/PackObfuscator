package com.mineinabyss.pack_obfuscator.modelengine

import com.mineinabyss.pack_obfuscator.CreativeObfuscator
import com.mineinabyss.pack_obfuscator.obfuscator
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.idofront.messaging.logSuccess
import com.ticxo.modelengine.api.events.ModelRegistrationEvent
import com.ticxo.modelengine.api.generator.ModelGenerator
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ModelEngineListener : Listener {

    private val megZipped = obfuscator.plugin.dataFolder.parentFile.resolve("ModelEngine/resource pack.zip")

    @EventHandler(priority = EventPriority.HIGHEST)
    fun ModelRegistrationEvent.onModelEnginePack() {
        if (phase != ModelGenerator.Phase.FINISHED) return
        if (obfuscator.config.modelEngine.obfuscate) {
            logInfo("Attempting to Obfuscate ModelEnginePack...")
            CreativeObfuscator.obfuscate(megZipped, megZipped.toPath())
            logSuccess("Successfully Obfuscated ModelEnginePack!")
        }

        val megSquash = obfuscator.config.modelEngine.packSquash
        if (megSquash.enabled) {
            logInfo("Running ModelEnginePack through PackSquash...")
            ModelEnginePackSquash.extractPackSquashConfig(megSquash)
            ModelEnginePackSquash.squashPack(megSquash)
            logSuccess("Successfully Squashed ModelEnginePack!")
        }
    }
}