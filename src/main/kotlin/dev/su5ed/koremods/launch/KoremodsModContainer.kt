package dev.su5ed.koremods.launch

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import dev.su5ed.koremods.KoremodDiscoverer
import net.minecraftforge.fml.common.*
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

class KoremodsModContainer : DummyModContainer(readMetadata()) {
    override fun registerBus(bus: EventBus, controller: LoadController): Boolean {
        bus.register(this)
        return true
    }
    
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    @Subscribe
    fun preInit(event: FMLPreInitializationEvent) {
        if (KoremodDiscoverer.isInitialized()) {
            val mods = Loader.instance().activeModList.map(ModContainer::getModId)
            
            KoremodDiscoverer.transformers.forEach { (modid) -> 
                if (!mods.contains(modid)) throw RuntimeException("Attempted to use nonexistent modid $modid")
            }
        }
    }
}

fun readMetadata(): ModMetadata {
    val ins = KoremodsModContainer::class.java.classLoader
        .getResourceAsStream("koremods.info")
        ?: run {
            koremodLogger.error("Couldn't read mod metadata file")
            return ModMetadata()
        }

    val metadata = MetadataCollection.from(ins, "Koremods")
    return metadata.getMetadataForId("koremods", null)
}
