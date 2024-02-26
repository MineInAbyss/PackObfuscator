import io.papermc.paperweight.util.configureTask

plugins {
    alias(idofrontLibs.plugins.kotlin.jvm)
    alias(idofrontLibs.plugins.kotlinx.serialization)
    alias(idofrontLibs.plugins.mia.papermc)
    alias(idofrontLibs.plugins.shadowjar)
    alias(idofrontLibs.plugins.mia.copyjar)
    alias(idofrontLibs.plugins.mia.autoversion)
    alias(idofrontLibs.plugins.mia.publication)
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates plugin.yml
    id("xyz.jpenilla.run-paper") version "2.0.1" // Adds runServer and runMojangMappedServer tasks for testing
    //id("net.minecrell.plugin-yml.paper") version "0.6.0" // Generates plugin.yml
}

group = "com.boy0000"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://repo.oraxen.com/releases")
    maven("https://repo.oraxen.com/snapshots")
    maven("https://repo.unnamed.team/repository/unnamed-public/")
    maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } }// MythicMobs
}

dependencies {
    compileOnly(idofrontLibs.minecraft.plugin.oraxen)
    compileOnly(idofrontLibs.minecraft.plugin.modelengine)
    compileOnly(idofrontLibs.minecraft.plugin.mythic.crucible)
    compileOnly(idofrontLibs.minecraft.plugin.mythic.dist)

    implementation(idofrontLibs.idofront.commands)
    implementation(idofrontLibs.idofront.config)
    implementation(idofrontLibs.idofront.di)
    implementation(idofrontLibs.idofront.util)
    implementation(idofrontLibs.idofront.text.components)
    implementation(idofrontLibs.idofront.logging)
    implementation(idofrontLibs.kotlinx.serialization.kaml)
    implementation(idofrontLibs.kotlinx.serialization.json)
    //implementation(idofrontLibs.kotlinx.coroutines)
    implementation(idofrontLibs.creative.api)
    implementation(idofrontLibs.creative.serializer.minecraft)
}

copyJar {
    excludePlatformDependencies.set(true)
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.18.2")
    }

    shadowJar {
        archiveFileName.set("PackObfuscator.jar")
        relocate("kotlin", "com.mineinabyss.shaded.kotlin")
        relocate("kotlinx", "com.mineinabyss.shaded.kotlinx")
    }

    build {
        dependsOn(copyJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}


//paper {
//    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
//    main = "com.mineinabyss.pack_obfuscator.PackObfuscator"
//    version = "${project.version}"
//    apiVersion = "1.18"
//    authors = listOf("boy0000")
//    foliaSupported = true
//
//    serverDependencies {
//        register("Oraxen") {
//            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
//            required = false
//        }
//        register("MythicCrucible") {
//            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
//            required = false
//        }
//        register("ModelEngine") {
//            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
//            required = false
//        }
//    }
//}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "com.mineinabyss.pack_obfuscator.PackObfuscator"
    version = "${project.version}"
    apiVersion = "1.18"
    authors = listOf("boy0000")
    foliaSupported = true
    softDepend = listOf("Oraxen", "MythicCrucible", "ModelEngine")
}
