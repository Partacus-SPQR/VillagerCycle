plugins {
    id("fabric-loom")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

repositories {
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

val isUnobfuscated = stonecutter.current.version == "26.1"

// When disableObfuscation=true, Loom doesn't register mod* configurations or remap tasks.
// Use standard Gradle configurations for unobfuscated versions.
val modImpl = if (isUnobfuscated) "implementation" else "modImplementation"
val modRunOnly = if (isUnobfuscated) "runtimeOnly" else "modRuntimeOnly"
val modCompOnly = if (isUnobfuscated) "compileOnly" else "modCompileOnly"

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    if (!isUnobfuscated) {
        add("mappings", loom.officialMojangMappings())
    }
    add(modImpl, "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    add(modImpl, "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // Optional dependencies - Cloth Config
    val clothConfigVersion = findProperty("deps.cloth_config") as String?
    if (clothConfigVersion != null) {
        add(modCompOnly, dependencies.create("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion").also {
            (it as org.gradle.api.artifacts.ExternalModuleDependency).exclude(group = "net.fabricmc.fabric-api")
        })
        add(modRunOnly, dependencies.create("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion").also {
            (it as org.gradle.api.artifacts.ExternalModuleDependency).exclude(group = "net.fabricmc.fabric-api")
        })
    }

    // Optional dependencies - ModMenu
    val modmenuVersion = findProperty("deps.modmenu") as String?
    if (modmenuVersion != null) {
        add(modRunOnly, dependencies.create("com.terraformersmc:modmenu:$modmenuVersion").also {
            (it as org.gradle.api.artifacts.ExternalModuleDependency).exclude(group = "eu.pb4")
        })
        add(modCompOnly, dependencies.create("com.terraformersmc:modmenu:$modmenuVersion").also {
            (it as org.gradle.api.artifacts.ExternalModuleDependency).exclude(group = "eu.pb4")
        })
    }
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"  // Shared run directory for all versions
    }
}

val javaVersion = if (isUnobfuscated) 25 else 21

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        if (isUnobfuscated) {
            from(named<Jar>("jar").map { it.archiveFile })
            from(named<Jar>("sourcesJar").map { it.archiveFile })
        } else {
            from(named("remapJar").map { (it as Jar).archiveFile })
            from(named("remapSourcesJar").map { (it as Jar).archiveFile })
        }
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }

    compileJava {
        options.release = javaVersion
    }
}
