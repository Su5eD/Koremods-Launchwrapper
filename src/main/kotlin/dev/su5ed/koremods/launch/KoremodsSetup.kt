package dev.su5ed.koremods.launch

import dev.su5ed.koremods.KoremodDiscoverer
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.relauncher.IFMLCallHook
import org.apache.logging.log4j.LogManager
import java.io.File

class KoremodsSetup : IFMLCallHook {
    companion object {
        internal var runtimeDeobfuscationEnabled: Boolean = false
    }
    
    private val logger = LogManager.getLogger()

    private lateinit var gameDir: File
    private lateinit var classLoader: LaunchClassLoader

    override fun call(): Void? {
        logger.info("Setting up Koremods")
        val modsDir = gameDir.toPath().resolve("mods")
        val classpath = classLoader.urLs
        
        KoremodDiscoverer.discoverKoremods(modsDir, classpath)
        
        return null
    }

    override fun injectData(data: Map<String, Any>) {
        logger.debug("Injecting data into setup class")
        
        gameDir = data["mcLocation"] as File
        classLoader = data["classLoader"] as LaunchClassLoader
        runtimeDeobfuscationEnabled = data["runtimeDeobfuscationEnabled"] as Boolean
    }
}
