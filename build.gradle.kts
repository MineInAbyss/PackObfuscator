plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.0.1" // Adds runServer and runMojangMappedServer tasks for testing
    //id("net.minecrell.plugin-yml.paper") version "0.6.0" // Generates plugin.yml
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates plugin.yml
    alias(libs.plugins.mia.copyjar)
    alias(libs.plugins.kotlinx.serialization)
}

group = "com.boy0000"
version = "0.1"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.mineinabyss.com/releases")
    maven("https://repo.oraxen.com/releases")
    maven("https://repo.oraxen.com/snapshots")
    maven("https://repo.unnamed.team/repository/unnamed-public/")
    maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } }// MythicMobs
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("io.th0rgal:oraxen:1.170.0-SNAPSHOT")
    compileOnly("io.lumine:MythicCrucible:2.0.0-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.6.0")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.4")

    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.idofront.commands)
    implementation(libs.idofront.config)
    implementation(libs.idofront.di)
    implementation(libs.idofront.util)
    implementation(libs.idofront.text.components)
    implementation(libs.idofront.logging)
    implementation(libs.kotlinx.serialization.kaml)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.creative.api)
    implementation(libs.creative.serializer.minecraft)
}

copyJar {
    destPath.set(project.property("oraxen_plugin_path") as String)
    jarName.set("PackObfuscator-$version.jar")
    excludePlatformDependencies.set(false)
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}

tasks {

    /*assemble {
        dependsOn(reobfJar)
    }*/

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
        minecraftVersion("1.20.1")
    }

    shadowJar {
        archiveFileName.set("PackObfuscator.jar")
        relocate("kotlin", "com.boy0000.shaded.kotlin")
        relocate("kotlinx", "com.boy0000.shaded.kotlinx")
        val pluginLoc = project.property("oraxen_plugin_path") as String + "\\PackObfuscator.jar"
        //archiveFile.get().asFile.copyTo(layout.projectDirectory.file("run/plugins/ModernLightApi.jar").asFile, true)
        println("Copied to $pluginLoc")
    }

    copyJar.get().dependsOn(shadowJar)

    build {
        dependsOn(copyJar)
    }

    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
    }
     */
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}


//paper {
//    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
//    main = "com.boy0000.pack_obfuscator.PackObfuscator"
//    version = "${project.version}"
//    apiVersion = "1.20"
//    authors = listOf("boy0000")
//    foliaSupported = true
//
//    serverDependencies {
//        register("Oraxen") {
//            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
//            required = false
//        }
//    }
//}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "com.boy0000.pack_obfuscator.PackObfuscator"
    version = "${project.version}"
    apiVersion = "1.20"
    authors = listOf("boy0000")
    foliaSupported = true
    softDepend = listOf("Oraxen", "MythicCrucible")
}
