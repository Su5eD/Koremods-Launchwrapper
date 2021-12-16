package wtf.gofancy.koremods.launchwrapper

import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import org.apache.logging.log4j.Logger
import wtf.gofancy.koremods.KoremodsDiscoverer
import wtf.gofancy.koremods.api.KoremodsLaunchPlugin
import wtf.gofancy.koremods.api.SplashScreen
import wtf.gofancy.koremods.prelaunch.DependencyClassLoader
import wtf.gofancy.koremods.prelaunch.KoremodsBlackboard
import wtf.gofancy.koremods.prelaunch.KoremodsPrelaunch
import java.nio.file.Path

class KoremodsPlugin : KoremodsLaunchPlugin {
    companion object {
        private val LWJGL_DEP_PACKAGES = listOf(
            "org.lwjgl.",
            "wtf.gofancy.koremods.splash."
        )
    }

    override fun shouldEnableSplashScreen(): Boolean = FMLLaunchHandler.side().isClient

    override fun appendLogMessage(message: String) {}

    override fun createSplashScreen(prelaunch: KoremodsPrelaunch): SplashScreen? {
        return try {
            val lwjglDep = prelaunch.extractDependency("LWJGL")
            val splashClassLoader = DependencyClassLoader(
                arrayOf(prelaunch.modJarUrl, lwjglDep),
                prelaunch.dependencyClassLoader,
                LWJGL_DEP_PACKAGES
            )
            val splashClass = splashClassLoader.loadClass("wtf.gofancy.koremods.splash.KoremodsSplashScreen")
            val logger = KoremodsBlackboard.createLogger("Splash")
            val splash = splashClass.getConstructor(Logger::class.java).newInstance(logger) as SplashScreen
            
            splash.setTerminateOnClose(true)
            splash.startOnThread()
            
            System.clearProperty("org.lwjgl.librarypath")
            splash
        } catch (e: Exception) {
            KoremodsSetup.LOGGER.catching(e)
            null
        }
    }

    override fun verifyScriptPacks(mods: Map<String, Path>) {
        KoremodsSetup.LOGGER.info("Verifying script packs")
        
        KoremodsDiscoverer.transformers.forEach { pack -> 
            mods.forEach { (modid, source) ->
                if (pack.namespace == modid && pack.path != source) {
                    KoremodsSetup.LOGGER.error("Source location of namespace ${pack.namespace} doesn't match the location of its mod")
                }
                else if (pack.path == source && pack.namespace != modid) {
                    KoremodsSetup.LOGGER.error("Namespace ${pack.namespace} doesn't match the modid $modid found within at the same location")
                }
            }
        }
    }
}
