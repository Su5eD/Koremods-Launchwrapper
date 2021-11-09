package dev.su5ed.koremods.prelaunch.transform;

import dev.su5ed.koremods.prelaunch.KoremodsSetup;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.*;

public class ClassHierarchyManager implements IClassTransformer {
    private static final Map<String, SuperCache> superClasses = new HashMap<>();

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes != null && !superClasses.containsKey(transformedName)) {
            declareASM(bytes);
        }
        return bytes;
    }
    
    private static String toKey(String name) {
        return KoremodsSetup.runtimeDeobfuscationEnabled
                ? FMLDeobfuscatingRemapper.INSTANCE.map(name.replace('.', '/')).replace('/', '.')
                : name;
    }
    
    private static String unKey(String name) {
        return KoremodsSetup.runtimeDeobfuscationEnabled 
                ? FMLDeobfuscatingRemapper.INSTANCE.unmap(name.replace('.', '/')).replace('/', '.')
                : name;
    }
    
    private static SuperCache declareClass(String name) {
        String mappedName = toKey(name);
    
        SuperCache cls = superClasses.get(mappedName);
        if (cls == null) {
            try {
                cls = declareASM(Launch.classLoader.getClassBytes(unKey(mappedName)));
            } catch (IOException ignored) {}
        }
        if (cls == null) {
            try {
                cls = declareReflection(mappedName);
            } catch (ClassNotFoundException ignored) {}
        }
        
        return cls;
    }
    
    private static SuperCache declareReflection(String name) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(name);
        SuperCache cache = getOrCreateCache(name);
        
        if (clazz.isInterface()) cache.superclass = "java.lang.Object";
        else if (name.equals("java.lang.Object")) return cache;
        else cache.superclass = toKey(clazz.getSuperclass().getName());
        
    
        cache.add(cache.superclass);
        for (Class<?> itf : clazz.getInterfaces()) {
            cache.add(toKey(itf.getName()));
        }
    
        return cache;
    }
    
    private static SuperCache declareASM(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        String name = toKey(reader.getClassName());
        SuperCache cache = getOrCreateCache(name);
    
        cache.superclass = toKey(reader.getSuperName().replace('/', '.'));
        cache.add(cache.superclass);

        for (String itf : reader.getInterfaces()) {
            cache.add(toKey(itf.replace('/', '.')));
        }
    
        return cache;
    }
    
    private static SuperCache getOrCreateCache(String name) {
        return superClasses.computeIfAbsent(name, s -> new SuperCache());
    }
    
    public static boolean classExtends(String name, String superclass) {
        String mappedName = toKey(name);
        String mappedSuperName = toKey(superclass);
        
        if (mappedName.equals(mappedSuperName)) return true;
        
        SuperCache cache = declareClass(mappedName);
        if (cache != null) {
            cache.flatten();
            return cache.parents.contains(mappedSuperName);
        }
        
        return false;
    }
    
    public static String getSuperClass(String name) {
        String mappedName = toKey(name);
        
        SuperCache cache = declareClass(mappedName);
        if (cache != null) {
            cache.flatten();
            return cache.superclass;
        }
        
        return "java.lang.Object";
    }
    
    private static class SuperCache {
        private String superclass = null;
        private final Set<String> parents = new HashSet<>();
        
        private boolean flattened;
    
        void add(String parent) {
            this.parents.add(parent);
        }
    
        void flatten() {
            if (!this.flattened) {
                new ArrayList<>(this.parents).forEach(s -> {
                    SuperCache cache = declareClass(s);
                    if (cache != null) {
                        cache.flatten();
                        this.parents.addAll(cache.parents);
                    }
                });
                this.flattened = true;
            }
        }
    }
}
