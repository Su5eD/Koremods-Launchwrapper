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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class KoremodsSetup implements IFMLCallHook {
    public static boolean runtimeDeobfuscationEnabled;
    public static DependencyClassLoader dependencyClassLoader;
    
    static final Logger LOGGER = LogManager.getLogger("KoremodsSetup");
    private File gameDir;
    private LaunchClassLoader launchCL;
    
    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.debug("Injecting data into setup class");
        
        this.gameDir = (File) data.get("mcLocation"); 
        this.launchCL = (LaunchClassLoader) data.get("classLoader"); 
        runtimeDeobfuscationEnabled = (Boolean) data.getOrDefault("runtimeDeobfuscationEnabled", false);
    }

    @Override
    public Void call() throws URISyntaxException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        LOGGER.info("Setting up Koremods");
        
        Path modsDir = gameDir.toPath().resolve("mods");
        Path koremodsDir = modsDir.resolve(ForgeVersion.mcVersion).resolve("koremods");
        File cacheDir = koremodsDir.resolve("cache").toFile();
        File depsDir = koremodsDir.resolve("dependencies").toFile();
        
        cacheDir.mkdirs();
        
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        Path depsPath = depsDir.toPath();
        File file = new File(location.toURI());
        List<URL> urls = new ArrayList<>();
        
        if (file.getName().endsWith(".jar")) {
            urls.add(location);
            
            JarFile jar = new JarFile(file);
            Attributes attributes = jar.getManifest().getMainAttributes();
            URL kotlinDep = extractDependency("Kotlin", jar, attributes, depsPath);
            
            if (kotlinDep != null) urls.add(kotlinDep);
        }
        
        dependencyClassLoader = new DependencyClassLoader(urls.toArray(new URL[0]), this.launchCL);
        Class<?> launchClass = dependencyClassLoader.loadClass("dev.su5ed.koremods.launch.KoremodsLaunch");
        Method launchMethod = launchClass.getDeclaredMethod("launch", File.class, ClassLoader.class, Path.class, URL[].class);
        Object instance = launchClass.newInstance();
        
        LOGGER.info("Launching Koremods instance");
        launchMethod.invoke(instance, cacheDir, dependencyClassLoader, modsDir, launchCL.getURLs());
        
        return null;
    }
    
    private URL extractDependency(String name, JarFile jar, Attributes manifestAttributes, Path depsDir) {
        String depName = manifestAttributes.getValue("Additional-Dependencies-" + name);
        if (depName == null) return null;
        
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
