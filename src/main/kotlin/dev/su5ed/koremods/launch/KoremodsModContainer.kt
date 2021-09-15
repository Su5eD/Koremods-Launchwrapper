package dev.su5ed.koremods.launch

import com.google.common.eventbus.EventBus
import net.minecraftforge.fml.common.DummyModContainer
import net.minecraftforge.fml.common.LoadController
import net.minecraftforge.fml.common.ModMetadata

class KoremodsModContainer : DummyModContainer(ModMetadata()) {
    init {
        val metadata = metadata
        metadata.modId = "koremods"
        metadata.name = "Koremods"
        metadata.description = "A coremodding framework running KTS/JSR-223"
        metadata.version = "1.0-SNAPSHOT" // TODO
        metadata.authorList = listOf("Su5eD")
    }

    override fun registerBus(bus: EventBus, controller: LoadController): Boolean = true
}
