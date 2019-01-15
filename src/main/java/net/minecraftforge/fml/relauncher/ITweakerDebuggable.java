package net.minecraftforge.fml.relauncher;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public interface ITweakerDebuggable {
    void acceptOptions(List<String> args, File gameDir, final File assetsDir, String profile);

    void injectIntoClassLoader(DebuggableLaunchLoader classLoader);

    String getLaunchTarget();

    String[] getLaunchArguments();
}