package dev.su5ed.koremods.prelaunch;

import dev.su5ed.koremods.prelaunch.transform.ClassHierarchyManager;
import dev.su5ed.koremods.prelaunch.transform.KoremodsTransformerWrapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import java.util.Map;

@Name("Koremods Loading Plugin")
@MCVersion("1.12.2")
@SortingIndex(1001)
@TransformerExclusions({ "dev.su5ed.koremods." })
public class KoremodsLoadingPlugin implements IFMLLoadingPlugin {
    
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { KoremodsTransformerWrapper.class.getName(), ClassHierarchyManager.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return KoremodsModContainer.class.getName();
    }

    @Override
    public String getSetupClass() {
        return KoremodsSetup.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
