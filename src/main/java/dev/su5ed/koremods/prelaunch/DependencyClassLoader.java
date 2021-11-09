package dev.su5ed.koremods.prelaunch;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyClassLoader extends URLClassLoader {
    private static final List<String> LOADED_PACKAGES = Arrays.asList(
            "org.jetbrains.kotlin.",
            "kotlin.",
            "org.intellij.lang.",
            "kotlinx.coroutines.",
            "javaslang.",
            "gnu.trove.",
            "codes.som.anthony.koffee.",
            "io.github.config4k.",
            "dev.su5ed.koremods."
    );
    
    private final Map<String, Class<?>> cachedClasses = new HashMap<>();
    private final boolean strict;

    public DependencyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        
        this.strict = urls.length > 0;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (this.cachedClasses.containsKey(name)) return this.cachedClasses.get(name);
        
        if (this.strict && LOADED_PACKAGES.stream().anyMatch(name::startsWith)) {
            Class<?> cls = findClass(name);
            this.cachedClasses.put(name, cls);
            return cls;
        }
        
        return super.loadClass(name, resolve);
    }
}
