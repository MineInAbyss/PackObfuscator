package com.boy0000.pack_obfuscator.oraxen

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.boy0000.pack_obfuscator.unzip
import io.th0rgal.oraxen.api.OraxenPack
import io.th0rgal.oraxen.utils.logs.Logs
import java.io.File
import kotlin.io.path.absolute

object OraxenPackSquash: PackSquash {

    override val inputDir = obfuscator.plugin.dataFolder.resolve("oraxen/pack/")
    override val outputZip: File = OraxenPack.getPack()

    fun squashOraxenPack() {
        unzip(OraxenPack.getPack(), inputDir)

        val oraxenSquash = obfuscator.config.oraxen.packSquash
        super.squashPack(oraxenSquash)

        inputDir.deleteRecursively()
    }

    override fun logSquashError(line: String) {
        Logs.logError("Error while squashing OraxenPack: $line")
    }

    override fun logSquashWarning(line: String) {
        Logs.logWarning("Warning while squashing OraxenPack: $line")
    }

    override fun logSquashInfo(line: String) {
        if (obfuscator.config.oraxen.packSquash.debug) Logs.logInfo("Info while squashing OraxenPack: $line")
    }
}