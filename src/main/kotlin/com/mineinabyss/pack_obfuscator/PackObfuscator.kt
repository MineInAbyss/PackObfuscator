package com.mineinabyss.pack_obfuscator

import com.mineinabyss.pack_obfuscator.crucible.CrucibleListener
import com.mineinabyss.pack_obfuscator.modelengine.ModelEngineListener
import com.mineinabyss.pack_obfuscator.oraxen.OraxenListener
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.plugin.java.JavaPlugin

class PackObfuscator : JavaPlugin() {
    override fun onEnable() {
        createContext()
        ObfuscateCommands()
        if (Plugins.isEnabled("Oraxen")) {
            logSuccess("Oraxen detected! Registering OraxenListener...")
            listeners(OraxenListener())
        }

        if (Plugins.isEnabled("MythicCrucible")) {
            logSuccess("MythicCrucible detected! Registering CrucibleListener...")
            listeners(CrucibleListener())
        }

        if (Plugins.isEnabled("ModelEngine")) {
            logSuccess("ModelEngine detected! Registering ModelEngineListener...")
            listeners(ModelEngineListener())
        }
    }
}

fun PackObfuscator.createContext() {
    DI.remove<ObfuscatorContext>()
    DI.add<ObfuscatorContext>(object : ObfuscatorContext {
        override val plugin = this@createContext
        override val config by config("config", dataFolder.toPath(), ObfuscatorConfig())
    })
}
