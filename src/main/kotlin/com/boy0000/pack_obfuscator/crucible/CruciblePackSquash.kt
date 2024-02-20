package com.boy0000.pack_obfuscator.crucible

import com.boy0000.pack_obfuscator.PackSquash
import com.boy0000.pack_obfuscator.obfuscator
import com.boy0000.pack_obfuscator.oraxen.OraxenPackSquash
import com.boy0000.pack_obfuscator.unzip
import io.lumine.mythiccrucible.MythicCrucible
import java.io.File

object CruciblePackSquash : PackSquash {

    override val inputDir = obfuscator.plugin.dataFolder.resolve("crucible/pack/")
    override val outputZip = obfuscator.plugin.dataFolder.resolve("crucible/pack.zip")

    fun squashCruciblePack() {
        unzip(MythicCrucible.inst().packGenerationManager.zippedOutput, inputDir)

        super.squashPack()

        inputDir.deleteRecursively()
        outputZip.copyTo(MythicCrucible.inst().packGenerationManager.zippedOutput, true)
    }
}