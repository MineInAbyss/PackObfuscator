package com.boy0000.pack_obfuscator

import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

val obfuscator: OraxenPackObfuscator by lazy { Bukkit.getPluginManager().getPlugin("OraxenPackObfuscator") as OraxenPackObfuscator }
class OraxenPackObfuscator : JavaPlugin() {
    override fun onEnable() {
        ObfuscateCommands()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
