package dev.su5ed.koremods.prelaunch;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;

import java.io.InputStream;

public class KoremodsModContainer extends DummyModContainer {

    public KoremodsModContainer() {
        super(readMetadata());
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    private static ModMetadata readMetadata() { // TODO constant modid
        InputStream ins = KoremodsModContainer.class.getClassLoader().getResourceAsStream("koremods.info");
        if (ins == null) {
            KoremodsSetup.LOGGER.error("Couldn't read mod metadata file");
            return new ModMetadata();
        }
    
        MetadataCollection metadata = MetadataCollection.from(ins, "Koremods");
        return metadata.getMetadataForId("koremods", null);
    }
}
