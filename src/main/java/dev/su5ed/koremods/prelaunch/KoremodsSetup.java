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

import dev.su5ed.koremods.api.SplashScreen;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private static final List<String> LWJGL_DEP_PACKAGES = Arrays.asList(
            "org.lwjgl.",
            "dev.su5ed.koremods.splash."
    );
    
    public static boolean runtimeDeobfuscationEnabled;
    public static DependencyClassLoader dependencyClassLoader;
    
    static final Logger LOGGER = LogManager.getLogger("Koremods.Setup");
    private File gameDir;
    private LaunchClassLoader launchCL;
    private final List<LoggerContext> contexts = new ArrayList<>(); 
    
    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.debug("Injecting data into setup class");
        
        this.gameDir = (File) data.get("mcLocation"); 
        this.launchCL = (LaunchClassLoader) data.get("classLoader"); 
        runtimeDeobfuscationEnabled = (Boolean) data.getOrDefault("runtimeDeobfuscationEnabled", false);
    }

    @Override
    public Void call() throws Exception {
        LOGGER.info("Setting up Koremods");
        
        Path modsDir = gameDir.toPath().resolve("mods");
        Path koremodsDir = modsDir.resolve(ForgeVersion.mcVersion).resolve("koremods");
        File cacheDir = koremodsDir.resolve("cache").toFile();
        File depsDir = koremodsDir.resolve("dependencies").toFile();
        
        cacheDir.mkdirs();
        
        URL rootDep = getClass().getProtectionDomain().getCodeSource().getLocation();
        Path depsPath = depsDir.toPath();
        File file = new File(rootDep.toURI());
        SplashScreen splash = null;
        
        addLoggerContext(this.launchCL);
        
        JarFile jar = new JarFile(file);
        Attributes attributes = jar.getManifest().getMainAttributes();
        
        URL kotlinDep = extractDependency("Kotlin", jar, attributes, depsPath);
        
        dependencyClassLoader = new DependencyClassLoader(new URL[]{ rootDep, kotlinDep }, this.launchCL, KOTLIN_DEP_PACKAGES);
        addLoggerContext(dependencyClassLoader);
        
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            URL lwjglDep = extractDependency("LWJGL", jar, attributes, depsPath);
            DependencyClassLoader splashClassLoader = new DependencyClassLoader(new URL[]{ rootDep, lwjglDep }, dependencyClassLoader, LWJGL_DEP_PACKAGES);
            
            Class<?> splashClass = splashClassLoader.loadClass("dev.su5ed.koremods.splash.KoremodsSplashKt");
            Method initSplashScreen = splashClass.getDeclaredMethod("initSplashScreen");
            splash = (SplashScreen) initSplashScreen.invoke(null);
            
            addLoggerContext(splashClassLoader);
            this.contexts.forEach(splash::injectSplashLogger);
            splash.awaitInit();
            
            System.clearProperty("org.lwjgl.librarypath");
        }
        
        try {
            Class<?> launchClass = dependencyClassLoader.loadClass("dev.su5ed.koremods.launch.KoremodsLaunch");
            Method launchMethod = launchClass.getDeclaredMethod("launch", File.class, ClassLoader.class, Path.class, URL[].class);
            Object instance = launchClass.newInstance();
            
            LOGGER.info("Launching Koremods instance");
            launchMethod.invoke(instance, cacheDir, dependencyClassLoader, modsDir, this.launchCL.getURLs());
            
            if (splash != null) splash.close();
        } catch (Throwable t) {
            if (splash != null) splash.forceClose();
            throw t;
        }
        
        return null;
    }
    
    private void addLoggerContext(ClassLoader classLoader) {
        this.contexts.add((LoggerContext) LogManager.getContext(classLoader, false));
    }
    
    private URL extractDependency(String name, JarFile jar, Attributes manifestAttributes, Path depsDir) {
        String depName = manifestAttributes.getValue("Additional-Dependencies-" + name);
        if (depName == null) throw new IllegalArgumentException("Required dependency " + name + " not found");
        
        ZipEntry entry = jar.getEntry(depName);
        try {
            Path destPath = depsDir.resolve(entry.getName());
            if (Files.notExists(destPath)) {
                Files.createDirectories(depsDir);
                
                InputStream source = jar.getInputStream(entry);
                OutputStream dest = Files.newOutputStream(destPath);
                IOUtils.copy(source, dest);
            }
            return destPath.toUri().toURL();
        } catch (IOException e) {
            throw new RuntimeException("Can't extract required dependency " + name, e);
        }
    }
}
