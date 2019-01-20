package co.matrixdevelopment.lava.pluginmanager;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;

import co.matrixdevelopment.lava.api.LavaPlugin;

public class LavaPluginManager {
    URLClassLoader pluginLoader;
    ArrayList<LavaPlugin> plugins = new ArrayList<>();

    public LavaPluginManager(ClassLoader classLoader) {
        // pluginLoader = new URLClassLoader(urls);
        discoverPlugins();
    }

    public void addPlugin(LavaPlugin pl) {
        plugins.add(pl);
    }

    private void discoverPlugins() {
        System.out.println(new File(".").getAbsolutePath());
    }
}