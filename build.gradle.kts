import net.minecraftforge.gradle.common.util.RunConfig
import java.time.LocalDateTime

plugins {
    kotlin("jvm")
    id("net.minecraftforge.gradle") version "5.1.+"
    id("wtf.gofancy.fancygradle") version "1.1.0-0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

evaluationDependsOn(":script")

val scriptProj = project(":script")
val coremodPath = "dev.su5ed.koremods.prelaunch.KoremodsLoadingPlugin"
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
            source(sourceSets.main.get())
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

configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.12.2-14.23.5.2855")
    
    compileOnly(scriptProj)
    compileOnly(scriptProj.sourceSets["splash"].output)
}

val manifestAttributes = mapOf(
    "Specification-Title" to "Koremods-LaunchWrapper",
    "Specification-Vendor" to "Su5eD",
    "Specification-Version" to 1,
    "Implementation-Title" to "Koremods-LaunchWrapper",
    "Implementation-Version" to project.version,
    "Implementation-Vendor" to "Su5eD",
    "Implementation-Timestamp" to LocalDateTime.now(),
    "FMLCorePlugin" to coremodPath
)

tasks {
    jar {
        finalizedBy("fullJar")
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
        
        filesMatching("koremods.info") {
            expand("version" to project.version)
        }
    }
}

reobf {
    create("jar") {
        dependsOn("fullJar")
    }
}

publishing {
    publications { 
        named<MavenPublication>(project.name) {
            artifact(tasks.getByName("fullJar"))
        }
    }
}
