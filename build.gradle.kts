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
                "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP,COREMODLOG",
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

tasks {
    jar {
        manifest { 
            attributes(
                "FMLCorePlugin" to coremodPath
            )
        }
    }
    
    register<Jar>("fullJar") {
        from(zipTree(jar.get().archiveFile))
        from(zipTree(project(":script").tasks.getByName<Jar>("shadowJar").archiveFile))
        
        archiveClassifier.set("full")
    }
    
    processResources {
        inputs.property("version", rootProject.version)
        
        filesMatching("koremods.info") {
            expand("version" to rootProject.version)
        }
    }
}

artifacts { 
    archives(tasks.getByName("fullJar"))
}
