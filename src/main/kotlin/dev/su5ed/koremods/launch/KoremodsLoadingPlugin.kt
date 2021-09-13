package dev.su5ed.koremods.launch

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions

@Name("Koremods Loading Plugin")
@MCVersion("1.12.2")
@SortingIndex(1001)
@TransformerExclusions("dev.su5ed.koremods.script", "dev.su5ed.koremods.launch")
class KoremodsLoadingPlugin : IFMLLoadingPlugin {
    
    override fun getModContainerClass(): String? = KoremodsModContainer::class.qualifiedName

    override fun getSetupClass(): String? = KoremodsSetup::class.qualifiedName

    override fun getASMTransformerClass(): Array<String> = emptyArray() // TODO
    
    override fun injectData(data: MutableMap<String, Any>?) {}
    
    override fun getAccessTransformerClass(): String? = null
}
