pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
    }
    plugins {
        id("fabric-loom") version "1.15.5"
        id("dev.kikugie.stonecutter") version "0.5.1"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5.1"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions("1.21.9", "1.21.10", "1.21.11", "26.1", "26.2")
        vcsVersion = "26.2"  // PRIMARY VERSION - always latest
    }
}
