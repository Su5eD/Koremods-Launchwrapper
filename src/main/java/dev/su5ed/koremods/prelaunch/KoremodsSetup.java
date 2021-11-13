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

package dev.su5ed.koremods.prelaunch;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class KoremodsSetup implements IFMLCallHook {
    private static final List<String> KOTLIN_DEP_PACKAGES = Arrays.asList(
            "org.jetbrains.",
            "kotlin.",
            "org.intellij.lang.",
            "kotlinx.coroutines.",
            "javaslang.",
            "gnu.trove.",
            "codes.som.anthony.koffee.",
            "io.github.config4k.",
            "dev.su5ed.koremods."
    );
    
    public static boolean runtimeDeobfuscationEnabled;
    public static DependencyClassLoader dependencyClassLoader;
    
    static final Logger LOGGER = LogManager.getLogger("Koremods.Setup");
    private Path gameDir;
    private LaunchClassLoader launchCL;
    
    private Path depsPath;
    private URL modJarUrl;
    private JarFile modJar;
    private Attributes modJarAttributes;
    
    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.debug("Injecting data into setup class");
        
        this.gameDir = ((File) data.get("mcLocation")).toPath(); 
        this.launchCL = (LaunchClassLoader) data.get("classLoader"); 
        runtimeDeobfuscationEnabled = (Boolean) data.getOrDefault("runtimeDeobfuscationEnabled", false);
    }

    @Override
    public Void call() throws Exception {
        LOGGER.info("Setting up Koremods");
        
        Path configDir = this.gameDir.resolve("config");
        Path modsDir = this.gameDir.resolve("mods");
        Path koremodsDir = modsDir.resolve(ForgeVersion.mcVersion).resolve("koremods");
        File cacheDir = koremodsDir.resolve("cache").toFile();
        File depsDir = koremodsDir.resolve("dependencies").toFile();
        this.depsPath = depsDir.toPath();
        this.modJarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
        File modFile = new File(this.modJarUrl.toURI());
        this.modJar = new JarFile(modFile);
        
        cacheDir.mkdirs();
        
        this.modJarAttributes = this.modJar.getManifest().getMainAttributes();
        
        URL kotlinDep = extractDependency("Kotlin");
        dependencyClassLoader = new DependencyClassLoader(new URL[]{ this.modJarUrl, kotlinDep }, this.launchCL, KOTLIN_DEP_PACKAGES);
        
        Class<?> launchClass = dependencyClassLoader.loadClass("dev.su5ed.koremods.launch.KoremodsLaunch");
        Method launchMethod = launchClass.getDeclaredMethod("launch", KoremodsSetup.class, File.class, ClassLoader.class, Path.class, Path.class, URL[].class);
        Object instance = launchClass.newInstance();
        
        LOGGER.info("Launching Koremods instance");
        launchMethod.invoke(instance, this, cacheDir, dependencyClassLoader, configDir, modsDir, this.launchCL.getURLs());
        
        return null;
    }

    public URL getModJarUrl() {
        return this.modJarUrl;
    }

    public URL extractDependency(String name) {
        String depName = this.modJarAttributes.getValue("Additional-Dependencies-" + name);
        if (depName == null) throw new IllegalArgumentException("Required dependency " + name + " not found");
        
        ZipEntry entry = this.modJar.getEntry(depName);
        try {
            Path destPath = this.depsPath.resolve(entry.getName());
            if (Files.notExists(destPath)) {
                Files.createDirectories(this.depsPath);
                
                InputStream source = this.modJar.getInputStream(entry);
                OutputStream dest = Files.newOutputStream(destPath);
                IOUtils.copy(source, dest);
            }
            return destPath.toUri().toURL();
        } catch (IOException e) {
            throw new RuntimeException("Can't extract required dependency " + name, e);
        }
    }
}
