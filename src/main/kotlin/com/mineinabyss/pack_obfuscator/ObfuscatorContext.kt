package com.mineinabyss.pack_obfuscator

import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger

val obfuscator by DI.observe<ObfuscatorContext>()
interface ObfuscatorContext {
    val plugin: PackObfuscator
    val config: ObfuscatorConfig
    val logger: ComponentLogger
}