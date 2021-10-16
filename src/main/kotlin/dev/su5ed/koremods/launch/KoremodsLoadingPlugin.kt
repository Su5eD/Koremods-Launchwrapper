package dev.su5ed.koremods.launch

import dev.su5ed.koremods.preloadScriptHost
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.*
import org.apache.logging.log4j.LogManager

internal val koremodLogger = LogManager.getLogger("Koremods")

@Name("Koremods Loading Plugin")
@MCVersion("1.12.2")
@SortingIndex(1001)
@TransformerExclusions("dev.su5ed.koremods", "kotlin", "org.jetbrains.kotlin")
class KoremodsLoadingPlugin : IFMLLoadingPlugin {
    
    init {
        preloadScriptHost(koremodLogger)
    }
    
    override fun getASMTransformerClass(): Array<String> = arrayOf(KoremodsTransformer::class.java.name, ClassHierarchyManager::class.java.name)

    override fun getModContainerClass(): String = KoremodsModContainer::class.java.name

    override fun getSetupClass(): String = KoremodsSetup::class.java.name

    override fun injectData(data: Map<String, Any>) {}
   
    override fun getAccessTransformerClass(): String? = null
}
