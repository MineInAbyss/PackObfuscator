package com.boy0000.pack_obfuscator

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val obfuscator: OraxenPackObfuscator by lazy { Bukkit.getPluginManager().getPlugin("OraxenPackObfuscator") as OraxenPackObfuscator }
class OraxenPackObfuscator : JavaPlugin() {
    override fun onEnable() {
        ObfuscateCommands()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        runCatching {
            ObfuscatePack.tempPackDir.deleteRecursively()
            ObfuscatePack.originalPackDir.deleteRecursively()
        }
    }
}
