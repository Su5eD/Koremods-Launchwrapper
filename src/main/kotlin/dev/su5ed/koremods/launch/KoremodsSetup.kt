package dev.su5ed.koremods.launch

import dev.su5ed.koremods.KoremodBlackboard
import dev.su5ed.koremods.KoremodDiscoverer
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.relauncher.IFMLCallHook
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.io.path.div

class KoremodsSetup : IFMLCallHook {
    companion object {
        internal var runtimeDeobfuscationEnabled: Boolean = false
    }
    
    private val logger = LogManager.getLogger()

    private lateinit var gameDir: File
    private lateinit var classLoader: LaunchClassLoader

    override fun call(): Void? {
        logger.info("Setting up Koremods")
        
        val modsDir = gameDir.toPath() / "mods"
        val cacheDir = (modsDir / ForgeVersion.mcVersion / "koremods" / "cache").toFile()
        
        cacheDir.mkdir()
        KoremodBlackboard.cacheDir = cacheDir
        KoremodDiscoverer.discoverKoremods(modsDir, classLoader.urLs)
        
        return null
    }

    override fun injectData(data: Map<String, Any>) {
        logger.debug("Injecting data into setup class")
        
        gameDir = data["mcLocation"] as File
        classLoader = data["classLoader"] as LaunchClassLoader
        runtimeDeobfuscationEnabled = data["runtimeDeobfuscationEnabled"] as Boolean
    }
}
