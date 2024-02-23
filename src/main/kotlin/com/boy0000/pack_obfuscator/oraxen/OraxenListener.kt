package com.boy0000.pack_obfuscator.oraxen

import com.boy0000.pack_obfuscator.obfuscator
import io.th0rgal.oraxen.api.events.OraxenPackGeneratedEvent
import io.th0rgal.oraxen.api.events.OraxenPackPreUploadEvent
import io.th0rgal.oraxen.utils.logs.Logs
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class OraxenListener : Listener {

    @EventHandler
    fun OraxenPackPreUploadEvent.onPreUpload() {
        runBlocking {
            val oraxenSquash = obfuscator.config.oraxen.packSquash
            if (oraxenSquash.enabled) {
                Logs.logInfo("Running OraxenPack through PackSquash...")
                OraxenPackSquash.extractPackSquashFiles(oraxenSquash.settingsPath)
                OraxenPackSquash.squashOraxenPack()
                Logs.logSuccess("Successfully Squashed OraxenPack!")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun OraxenPackGeneratedEvent.onPackGenerated() {
        if (!obfuscator.config.oraxen.obfuscate) return
        Logs.logInfo("Attempting to Obfuscate OraxenPack...")
        OraxenPackObfuscator.obfuscate(output)
        Logs.logSuccess("Successfully Obfuscated OraxenPack!")
    }
}

