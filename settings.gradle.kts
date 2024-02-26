pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
    }

    versionCatalogs {
        create("idofrontLibs") {
            from("com.mineinabyss:catalog:$idofrontVersion")
            version("oraxen", "1.170.0-SNAPSHOT")
        }
    }
}

rootProject.name = "PackObfuscator"
