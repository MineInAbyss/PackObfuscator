package com.boy0000.pack_obfuscator

import io.th0rgal.oraxen.api.events.OraxenPackGeneratedEvent
import io.th0rgal.oraxen.utils.logs.Logs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class OraxenListener : Listener {

    @EventHandler
    fun OraxenPackGeneratedEvent.on() {
        Logs.logInfo("Attempting to Obfuscate OraxenPack...")
        ObfuscatePack.obfuscate(output)
        Logs.logSuccess("Successfully Obfuscated OraxenPack!")
    }
}

