package dev.su5ed.koremods.launch

import dev.su5ed.koremods.KoremodBlackboard
import dev.su5ed.koremods.KoremodDiscoverer
import java.io.File
import java.net.URL
import java.nio.file.Path

class KoremodsLaunch {
    
    fun launch(cacheDir: File, classLoader: ClassLoader?, modsDir: Path, urls: Array<URL>) {
        KoremodBlackboard.cacheDir = cacheDir
        KoremodBlackboard.scriptContextClassLoader = classLoader
        KoremodDiscoverer.discoverKoremods(modsDir, urls)
    }
}
