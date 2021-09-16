package dev.su5ed.koremods.launch

import com.google.common.eventbus.EventBus
import net.minecraftforge.fml.common.DummyModContainer
import net.minecraftforge.fml.common.LoadController
import net.minecraftforge.fml.common.MetadataCollection
import net.minecraftforge.fml.common.ModMetadata

class KoremodsModContainer : DummyModContainer(readMetadata()) {
    override fun registerBus(bus: EventBus, controller: LoadController): Boolean = true
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
