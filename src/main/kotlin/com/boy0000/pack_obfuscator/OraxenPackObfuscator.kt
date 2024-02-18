package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class OraxenPackObfuscator : JavaPlugin() {
    override fun onEnable() {
        createContext()
        ObfuscateCommands()
        listeners(OraxenListener())
    }
}

fun OraxenPackObfuscator.createContext() {
    DI.remove<ObfuscatorContext>()
    DI.add<ObfuscatorContext>(object : ObfuscatorContext {
        override val plugin = this@createContext
        override val config by config("config", dataFolder.toPath(), ObfuscatorConfig())
    })
}
