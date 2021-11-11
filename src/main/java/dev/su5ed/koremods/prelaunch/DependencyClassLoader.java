package dev.su5ed.koremods.prelaunch;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyClassLoader extends URLClassLoader { // TODO Javadoc
    private static final List<String> EXCLUDED_PACKAGES = Collections.singletonList(
            "dev.su5ed.koremods.api."
    );
    
    private final List<String> priorityClasses;
    private final URLClassLoader delegateParent;
    private final DelegateClassLoader delegateClassLoader;
    private final Map<String, Class<?>> cachedClasses = new HashMap<>();
    private final boolean strict;

    public DependencyClassLoader(URL[] urls, URLClassLoader parent, List<String> priorityClasses) {
        super(urls, null);
        
        this.delegateParent = parent;
        this.priorityClasses = priorityClasses;
        this.delegateClassLoader = new DelegateClassLoader(this.delegateParent);
        this.strict = urls.length > 0;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (EXCLUDED_PACKAGES.stream().noneMatch(name::startsWith)) {
            if (this.cachedClasses.containsKey(name)) return this.cachedClasses.get(name);
            
            if (this.strict && this.priorityClasses.stream().anyMatch(name::startsWith)) {
                Class<?> cls = findClass(name);
                this.cachedClasses.put(name, cls);
                return cls;
            }
        }
        
        return this.delegateClassLoader.loadClass(name, resolve);
    }

    @Override
    public URL[] getURLs() {
        URL[] urls = super.getURLs();
        URL[] delegateUrls = this.delegateParent.getURLs();

        URL[] merged = new URL[urls.length + delegateUrls.length];
        int i = 0;
        for (URL url : urls) merged[i++] = url;
        for (URL url : delegateUrls) merged[i++] = url;

        return merged;
    }

    private static class DelegateClassLoader extends ClassLoader {
        
        protected DelegateClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }
    }
}
