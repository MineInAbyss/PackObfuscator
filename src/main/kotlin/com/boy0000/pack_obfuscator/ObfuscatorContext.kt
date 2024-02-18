package com.boy0000.pack_obfuscator

import com.mineinabyss.idofront.di.DI

val obfuscator by DI.observe<ObfuscatorContext>()
interface ObfuscatorContext {
    val plugin: OraxenPackObfuscator
    val config: ObfuscatorConfig
}