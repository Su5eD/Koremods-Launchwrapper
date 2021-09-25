import java.time.LocalDateTime
import net.minecraftforge.gradle.common.util.RunConfig

plugins {
    kotlin("jvm")
    id("net.minecraftforge.gradle") version "5.1.+"
    id("wtf.gofancy.fancygradle") version "1.1.0-0"
}

val coremodPath = "dev.su5ed.koremods.launch.KoremodsLoadingPlugin"

evaluationDependsOn(":script")

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

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.12.2-14.23.5.2855")
    
    implementation(project(":script"))
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
        manifest.attributes(manifestAttributes)
    }
    
    register<Jar>("fullJar") {
        from(zipTree(jar.get().archiveFile))
        from(zipTree(project(":script").tasks.getByName<Jar>("shadowJar").archiveFile))
        
        manifest.attributes(manifestAttributes)
        
        archiveClassifier.set("full")
    }
    
    processResources {
        inputs.property("version", project.version)
        
        filesMatching("koremods.info") {
            expand("version" to project.version)
        }
    }
}

publishing {
    publications { 
        named<MavenPublication>(project.name) {
            artifact(tasks.getByName("fullJar"))
        }
    }
}

artifacts { 
    archives(tasks.getByName("fullJar"))
}
