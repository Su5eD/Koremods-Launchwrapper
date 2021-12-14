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

package wtf.gofancy.koremods.launchwrapper;

import com.google.common.eventbus.EventBus;
import wtf.gofancy.koremods.prelaunch.KoremodsBlackboard;
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

    private static ModMetadata readMetadata() {
        InputStream ins = KoremodsModContainer.class.getResourceAsStream("/mcmod.info");
        if (ins == null) {
            KoremodsSetup.LOGGER.error("Couldn't read mod metadata file");
            return new ModMetadata();
        }
    
        MetadataCollection metadata = MetadataCollection.from(ins, KoremodsBlackboard.NAME);
        return metadata.getMetadataForId(KoremodsBlackboard.MODID, null);
    }
}
