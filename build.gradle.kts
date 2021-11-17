import net.minecraftforge.gradle.common.util.RunConfig
import java.time.LocalDateTime

plugins {
    kotlin("jvm")
    id("net.minecraftforge.gradle") version "5.1.+"
    id("wtf.gofancy.fancygradle") version "1.1.0-0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

evaluationDependsOn(":koremods-script")

val scriptProj = project(":koremods-script")
val coremodPath = "dev.su5ed.koremods.launchwrapper.KoremodsLoadingPlugin"
val repackPackagePath: String by project
val relocatePackages: ((String, String) -> Unit) -> Unit by scriptProj.extra

minecraft {
    mappings("stable", "39-1.12")

    runs {
        val config = Action<RunConfig> {
            properties(mapOf(
                "forge.logging.markers" to "SCAN,REGISTRIES",
                "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            jvmArgs.add("-Dfml.coreMods.load=$coremodPath")
        }

        create("client", config)
        create("server", config)
    }
}

fancyGradle {
    patches {
        resources
        coremods
        asm
    }
}

configurations.mavenRuntime {
    extendsFrom(scriptProj.configurations.mavenRuntime.get())
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.12.2-14.23.5.2855")
    
    compileOnly(scriptProj)
    compileOnly(scriptProj.sourceSets["splash"].output)
}

license {
    excludes.add("dev/su5ed/koremods/launchwrapper/transform/ClassHierarchyManager.java")
    excludes.add("dev/su5ed/koremods/launchwrapper/KoremodsClassWriter.kt")
}

val manifestAttributes = mapOf(
    "Specification-Title" to "Koremods-LaunchWrapper",
    "Specification-Vendor" to "Su5eD",
    "Specification-Version" to 1,
    "Implementation-Title" to "Koremods-LaunchWrapper",
    "Implementation-Version" to project.version,
    "Implementation-Vendor" to "Su5eD",
    "Implementation-Timestamp" to LocalDateTime.now(),
    "FMLCorePlugin" to coremodPath,
    "FMLCorePluginContainsFMLMod" to true
)

tasks {
    jar {
        dependsOn("fullJar")
        isEnabled = false
    }
    
    shadowJar {
        configurations = emptyList()
        
        manifest.attributes(manifestAttributes)
        relocatePackages(::relocate)
        
        archiveClassifier.set("shade")
    }
    
    register<Jar>("fullJar") {
        val shadowJar = scriptProj.tasks.getByName<Jar>("shadowJar")
        val lwjglDepsJar = scriptProj.tasks.getByName<Jar>("lwjglDepsJar")
        val kotlinDepsJar = scriptProj.tasks.getByName<Jar>("kotlinDepsJar")
        dependsOn(project.tasks.shadowJar, shadowJar, lwjglDepsJar, kotlinDepsJar)
        
        from(zipTree(project.tasks.shadowJar.get().archiveFile))
        from(zipTree(shadowJar.archiveFile))
        from(lwjglDepsJar.archiveFile)
        from(kotlinDepsJar.archiveFile)
        
        manifest {
            attributes(manifestAttributes)
            attributes(
                "Additional-Dependencies-LWJGL" to lwjglDepsJar.archiveFile.get().asFile.name,
                "Additional-Dependencies-Kotlin" to kotlinDepsJar.archiveFile.get().asFile.name
            )
        }
    }
    
    processResources {
        inputs.property("version", project.version)
        
        filesMatching("mcmod.info") {
            expand("version" to project.version)
        }
    }
}

configurations.all { 
    outgoing.artifacts.removeIf { 
        it.buildDependencies.getDependencies(null).contains(tasks["jar"])
    }
    outgoing.artifact(tasks["fullJar"])
}
