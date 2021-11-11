package dev.su5ed.koremods.launch

import dev.su5ed.koremods.KoremodDiscoverer
import dev.su5ed.koremods.KoremodsBlackboard
import java.io.File
import java.net.URL
import java.nio.file.Path

class KoremodsLaunch {
    
    fun launch(cacheDir: File, classLoader: ClassLoader?, modsDir: Path, urls: Array<URL>) {
        KoremodsBlackboard.cacheDir = cacheDir
        KoremodsBlackboard.scriptContextClassLoader = classLoader
        KoremodDiscoverer.discoverKoremods(modsDir, urls)
    }
}
