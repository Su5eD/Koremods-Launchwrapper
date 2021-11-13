/*
 * This file is part of Koremods, licensed under the MIT License
 *
 * Copyright (c) 2021 Garden of Fancy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.su5ed.koremods.launch

import dev.su5ed.koremods.KoremodDiscoverer
import dev.su5ed.koremods.KoremodsBlackboard
import dev.su5ed.koremods.api.SplashScreen
import dev.su5ed.koremods.parseMainConfig
import dev.su5ed.koremods.prelaunch.DependencyClassLoader
import dev.su5ed.koremods.prelaunch.KoremodsSetup
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.File
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.div

private val LWJGL_DEP_PACKAGES = listOf(
    "org.lwjgl.",
    "dev.su5ed.koremods.splash."
)

class KoremodsLaunch {
    
    @Suppress("unused")
    fun launch(setup: KoremodsSetup, cacheDir: File, classLoader: ClassLoader, configDir: Path, modsDir: Path, discoveryUrls: Array<URL>) {
        KoremodsBlackboard.cacheDir = cacheDir
        KoremodsBlackboard.scriptContextClassLoader = classLoader
        
        val configPath = configDir / KoremodsBlackboard.CONFIG_FILE
        val config = parseMainConfig(configPath)
        var splash: SplashScreen? = null
        
        if (config.enableSplashScreen && FMLLaunchHandler.side() == Side.CLIENT) {
            val lwjglDep = setup.extractDependency("LWJGL")
            val splashClassLoader = DependencyClassLoader(arrayOf(setup.modJarUrl, lwjglDep), KoremodsSetup.dependencyClassLoader, LWJGL_DEP_PACKAGES)
            val splashClass = splashClassLoader.loadClass("dev.su5ed.koremods.splash.KoremodsSplashKt")
            val initSplashScreen = splashClass.getDeclaredMethod("initSplashScreen")
            splash = initSplashScreen.invoke(null) as SplashScreen
            
            listOf(
                getLoggerContext(KoremodsSetup::class.java.classLoader),
                getLoggerContext(classLoader),
                getLoggerContext(splashClassLoader),
            )
                .forEach(splash::injectSplashLogger)
            
            splash.awaitInit()
            System.clearProperty("org.lwjgl.librarypath")
        }
        
        try {
            KoremodDiscoverer.discoverKoremods(modsDir, discoveryUrls)
            
            splash?.close()
        } catch (t: Throwable) {
            splash?.forceClose()
            throw t
        }
    }
    
    private fun getLoggerContext(classLoader: ClassLoader): LoggerContext {
        return LogManager.getContext(classLoader, false) as LoggerContext
    }
}
