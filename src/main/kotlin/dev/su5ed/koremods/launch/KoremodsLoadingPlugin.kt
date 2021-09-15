package dev.su5ed.koremods.launch

import dev.su5ed.koremods.initScriptEngine
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.*
import org.apache.logging.log4j.LogManager

@Name("Koremods Loading Plugin")
@MCVersion("1.12.2")
@SortingIndex(1001)
@TransformerExclusions("dev.su5ed.koremods.script", "dev.su5ed.koremods.launch", "kotlin", "org.jetbrains.kotlin")
class KoremodsLoadingPlugin : IFMLLoadingPlugin {
    private val logger = LogManager.getLogger()
    
    init {
        initScriptEngine(logger)
    }
    
    override fun getASMTransformerClass(): Array<String> = emptyArray() // TODO

    override fun getModContainerClass(): String = KoremodsModContainer::class.java.name

    override fun getSetupClass(): String = KoremodsSetup::class.java.name

    override fun injectData(data: Map<String, Any>) {}
   
    override fun getAccessTransformerClass(): String? = null
}
