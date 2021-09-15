package dev.su5ed.koremods.launch

import dev.su5ed.koremods.KoremodDiscoverer
import net.minecraftforge.fml.relauncher.IFMLCallHook
import org.apache.logging.log4j.LogManager
import java.io.File

class KoremodsSetup : IFMLCallHook {
    private val logger = LogManager.getLogger("Koremods")

    private lateinit var gameDir: File

    override fun call(): Void? {
        logger.info("Setting up Koremods")
        val modsDir = gameDir.toPath().resolve("mods")
        
        KoremodDiscoverer(logger).discoverKoremods(modsDir)
        
        return null
    }

    override fun injectData(data: Map<String, Any>) {
        logger.info("Injecting data into setup class")
        
        gameDir = data["mcLocation"] as File
    }
}
