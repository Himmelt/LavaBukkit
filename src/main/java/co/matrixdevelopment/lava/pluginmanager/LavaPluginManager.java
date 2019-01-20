package co.matrixdevelopment.lava.pluginmanager;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;

import co.matrixdevelopment.lava.api.LavaPlugin;

public class LavaPluginManager {
    private URLClassLoader pluginLoader;
    private ArrayList<LavaPlugin> plugins = new ArrayList<>();

    private File pluginDir = new File("./lavaplugins");

    public LavaPluginManager(ClassLoader classLoader) {
        // pluginLoader = new URLClassLoader(urls);
        discoverPlugins();
    }

    public void addPlugin(LavaPlugin pl) {
        plugins.add(pl);
    }

    private void discoverPlugins() {
        if (!pluginDir.exists())
            pluginDir.mkdirs();
    }
}