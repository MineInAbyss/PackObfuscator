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
            if (!obfuscator.config.packSquash.enabled) {
                Logs.logError("Skipping PackSquash as it is disabled in the config.")
            } else {
                Logs.logInfo("Running OraxenPack through PackSquash...")
                OraxenPackSquash.extractPackSquashFiles()
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

