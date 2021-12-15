package wtf.gofancy.koremods.launchwrapper

import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import org.apache.logging.log4j.Logger
import wtf.gofancy.koremods.api.SplashScreen
import wtf.gofancy.koremods.launch.KoremodsLaunchPlugin
import wtf.gofancy.koremods.prelaunch.DependencyClassLoader
import wtf.gofancy.koremods.prelaunch.KoremodsBlackboard
import wtf.gofancy.koremods.prelaunch.KoremodsPrelaunch

class KoremodsPlugin : KoremodsLaunchPlugin {
    companion object {
        private val LWJGL_DEP_PACKAGES = listOf(
            "org.lwjgl.",
            "wtf.gofancy.koremods.splash."
        )
    }

    override val enableSplashScreen: Boolean
        get() = FMLLaunchHandler.side().isClient

    override fun appendLogMessage(message: String) {}

    override fun createSplashScreen(prelaunch: KoremodsPrelaunch): SplashScreen? {
        return try {
            val lwjglDep = prelaunch.extractDependency("LWJGL")
            val splashClassLoader = DependencyClassLoader(
                arrayOf(prelaunch.modJarUrl, lwjglDep),
                KoremodsPrelaunch.dependencyClassLoader,
                LWJGL_DEP_PACKAGES
            )
            val splashClass = splashClassLoader.loadClass("wtf.gofancy.koremods.splash.KoremodsSplashScreen")
            val logger = KoremodsBlackboard.createLogger("Splash")
            val splash = splashClass.getConstructor(Logger::class.java).newInstance(logger) as SplashScreen
            
            splash.terminateOnClose = true
            splash.startOnThread()
            
            System.clearProperty("org.lwjgl.librarypath")
            splash
        } catch (e: Exception) {
            KoremodsSetup.LOGGER.catching(e)
            null
        }
    }
}
