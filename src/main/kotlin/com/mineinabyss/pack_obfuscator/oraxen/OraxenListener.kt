package com.mineinabyss.pack_obfuscator.oraxen

import com.mineinabyss.pack_obfuscator.obfuscator
import io.th0rgal.oraxen.api.events.OraxenPackGeneratedEvent
import io.th0rgal.oraxen.api.events.OraxenPackPreUploadEvent
import io.th0rgal.oraxen.utils.logs.Logs
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class OraxenListener : Listener {

    @EventHandler
    fun OraxenPackPreUploadEvent.onPreUpload() {
        val oraxenSquash = obfuscator.config.oraxen.packSquash
        if (!oraxenSquash.enabled) return
        OraxenPackSquash.extractPackSquashConfig(oraxenSquash)
        OraxenPackSquash.squashOraxenPack()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun OraxenPackGeneratedEvent.onPackGenerated() {
        if (!obfuscator.config.oraxen.obfuscate) return
        Logs.logInfo("Attempting to Obfuscate OraxenPack...")
        if (OraxenPackObfuscator.obfuscate(output))
            Logs.logSuccess("Successfully Obfuscated OraxenPack!")
    }
}

