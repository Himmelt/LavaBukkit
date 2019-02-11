package co.matrixdevelopment.lava.pluginmanager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import co.matrixdevelopment.lava.api.LavaPlugin;

public class LavaPluginManager {
    private URLClassLoader pluginLoader;
    private ArrayList<LavaPlugin> plugins = new ArrayList<>();

    private File pluginDir = new File("./lavaplugins");

    private ArrayList<URL> urls = new ArrayList<>();

    public LavaPluginManager(ClassLoader classLoader) {
        discoverPlugins();
        pluginLoader = new URLClassLoader(urls.toArray(new URL[0]));
        System.out.println(pluginLoader.getURLs());
        try {
            loadPlugins();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        System.out.println(plugins.toString());
    }

    /**
     * Add a plugin to the loaded plugin list
     * @param pl the plugin
     */
    public void addPlugin(LavaPlugin pl) {
        plugins.add(pl);
    }

    /**
     * Load a plugin from a .jar file
     * @param jarFile the jar file to load
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public LavaPlugin loadPlugin(JarFile jarFile) throws IOException, ClassNotFoundException {
        Enumeration<JarEntry> e = jarFile.entries();
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                continue;
            }
            // -6 because of .class
            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            Class<? extends Object> c = pluginLoader.loadClass(className);
            if (c.getSuperclass().getSimpleName().contains("LavaPlugin")) {
                try {
                    return (LavaPlugin) c.newInstance();
                } catch (InstantiationException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }
        jarFile.close();
        return null;
    }

    /**
     * Loads plugins and their dependencies
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void loadPlugins() throws ClassNotFoundException, IOException {
        for (URL url : pluginLoader.getURLs()) {
            JarFile jarFile = new JarFile(url.getFile());
            LavaPlugin lp = loadPlugin(jarFile);
            if (!plugins.contains(lp)) {
                plugins.add(lp);
            }
            for (String s : lp.getDependencies()) {
                JarFile depFile = new JarFile(new File(pluginDir, s)); //if the file cannot be found, this will break very badly
                loadPlugin(depFile);
                if (plugins.contains(lp)) {
                    plugins.add(lp);
                }
            }
        }
    }

    private void discoverPlugins() {
        if (!pluginDir.exists())
            pluginDir.mkdirs();

        for (File f : pluginDir.listFiles()) {
            try {
                System.out.println("Found jar: " + f);
                urls.add(f.toURI().toURL());
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}