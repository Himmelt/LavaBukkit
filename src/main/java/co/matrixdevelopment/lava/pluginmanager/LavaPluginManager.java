package co.matrixdevelopment.lava.pluginmanager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarFile;

import co.matrixdevelopment.lava.api.LavaPlugin;

public class LavaPluginManager {
    private URLClassLoader pluginLoader;
    private ArrayList<LavaPlugin> plugins = new ArrayList<>();

    private File pluginDir = new File("./lavaplugins");

    private ArrayList<URL> urls = new ArrayList<>();

    public LavaPluginManager(ClassLoader classLoader) {
        discoverPlugins();
        pluginLoader = new URLClassLoader((URL[]) urls.toArray());
    }

    public void addPlugin(LavaPlugin pl) {
        plugins.add(pl);
    }

    private void discoverPlugins() {
        if (!pluginDir.exists())
            pluginDir.mkdirs();

        for (File f : pluginDir.listFiles()) {
            try {
                JarFile jf = new JarFile(f);
                urls.add(f.toURL());
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}