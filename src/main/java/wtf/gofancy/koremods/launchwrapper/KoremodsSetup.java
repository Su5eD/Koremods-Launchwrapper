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

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.gofancy.koremods.prelaunch.KoremodsPrelaunch;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class KoremodsSetup implements IFMLCallHook {
    public static boolean runtimeDeobfuscationEnabled;
    private static final String LAUNCH_PLUGIN_CLASS = "wtf.gofancy.koremods.launchwrapper.KoremodsPlugin";
    
    static final Logger LOGGER = LogManager.getLogger("Koremods.Setup");
    private Path gameDir;
    
    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.debug("Injecting data into setup class");
        
        this.gameDir = ((File) data.get("mcLocation")).toPath();
        runtimeDeobfuscationEnabled = (Boolean) data.getOrDefault("runtimeDeobfuscationEnabled", false);
    }

    @Override
    public Void call() throws Exception {
        LOGGER.info("Setting up Koremods");
        
        KoremodsPrelaunch prelaunch = new KoremodsPrelaunch(this.gameDir, ForgeVersion.mcVersion);
        prelaunch.launch(LAUNCH_PLUGIN_CLASS);
        
        return null;
    }
}
