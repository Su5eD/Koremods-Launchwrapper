package dev.su5ed.koremods.prelaunch.transform;

import dev.su5ed.koremods.prelaunch.KoremodsSetup;
import net.minecraft.launchwrapper.IClassTransformer;

public class KoremodsTransformerWrapper implements IClassTransformer {
    private static final String TRANSFORMER_CLASS = "dev.su5ed.koremods.launch.KoremodsTransformer";
    private IClassTransformer actualTransformer;
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (this.actualTransformer == null) {
            if (KoremodsSetup.dependencyClassLoader != null) {
                try {
                    Class<?> cl = KoremodsSetup.dependencyClassLoader.loadClass(TRANSFORMER_CLASS);
                    actualTransformer = (IClassTransformer) cl.newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        if (this.actualTransformer != null) {
            return this.actualTransformer.transform(name, transformedName, bytes);
        }
        
        return bytes;
    }
}
