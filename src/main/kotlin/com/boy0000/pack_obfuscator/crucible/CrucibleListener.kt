package com.boy0000.pack_obfuscator.crucible

import com.boy0000.pack_obfuscator.CreativeObfuscator
import com.boy0000.pack_obfuscator.obfuscator
import io.lumine.mythiccrucible.MythicCrucible
import io.lumine.mythiccrucible.events.MythicCrucibleGeneratePackEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class CrucibleListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun MythicCrucibleGeneratePackEvent.onCruciblePack() {
        val crucible = MythicCrucible.inst()
        if (obfuscator.config.crucible.obfuscate) {
            crucible.logger.info("Attempting to Obfuscate CruciblePack...")
            CreativeObfuscator.obfuscate(zippedPack, zippedPack.toPath())
            crucible.logger.info("Successfully Obfuscated CruciblePack!")
        }

        if (obfuscator.config.packSquash.enabled) {
            crucible.logger.info("Running CruciblePack through PackSquash...")
            CruciblePackSquash.extractExecutable()
            CruciblePackSquash.squashCruciblePack()
            crucible.logger.info("Successfully Squashed CruciblePack!")
        }
    }
}