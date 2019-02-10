package net.minecraftforge.fml.relauncher;

import java.io.File;
import java.util.List;

public interface ITweakerDebuggable {
    void acceptOptions(List<String> args, File gameDir, final File assetsDir, String profile);

    void injectIntoClassLoader(DebuggableLaunchLoader classLoader);

    String getLaunchTarget();

    String[] getLaunchArguments();
}