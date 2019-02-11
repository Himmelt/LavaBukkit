package net.minecraftforge.fml.relauncher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.logging.log4j.Level;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LogWrapper;

public class DebuggableLaunchLoader extends URLClassLoader {
    public static final int BUFFER_SIZE = 4096;
    public List<URL> sources;
    private ClassLoader parent = ClassLoader.getSystemClassLoader();

    private List<IClassTransformer> transformers = new java.util.ArrayList<>(2);
    private Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    private Set<String> invalidClasses = new HashSet<>(1000);

    private Set<String> classExceptions = new HashSet<String>();
    private Set<String> transformExceptions = new HashSet<String>();
    private Map<Package, Manifest> packageManifests = new ConcurrentHashMap<>();
    private Map<String, byte[]> resourceCache = new ConcurrentHashMap<>(1000);
    private Set<String> negativeResourceCache = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());

    private IClassNameTransformer renameTransformer;

    private static final Manifest EMPTY = new Manifest();

    private final ThreadLocal<byte[]> loadBuffer = new ThreadLocal<>();

    private static final String[] RESERVED_NAMES = { "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
            "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9" };

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
    private static final boolean DEBUG_FINER = (DEBUG)
            && (Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingFiner", "false")));
    private static final boolean DEBUG_SAVE = (DEBUG)
            && (Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingSave", "false")));
    private static File tempFolder = null;

    public DebuggableLaunchLoader(URL[] sources) {
        super(sources);
        System.out.println("I was called, when I shouldn't have been!");
        this.sources = new ArrayList<>(Arrays.asList(sources));

        addClassLoaderExclusion("java.");
        addClassLoaderExclusion("sun.");
        addClassLoaderExclusion("org.lwjgl.");
        addClassLoaderExclusion("org.apache.logging.");
        addClassLoaderExclusion("net.minecraft.launchwrapper.");

        addTransformerExclusion("javax.");
        addTransformerExclusion("argo.");
        addTransformerExclusion("org.objectweb.asm.");
        addTransformerExclusion("com.google.common.");
        addTransformerExclusion("org.bouncycastle.");
        addTransformerExclusion("net.minecraft.launchwrapper.injector.");

        if (DEBUG_SAVE) {
            int x = 1;
            tempFolder = new File(DebuggableLaunch.minecraftHome, "CLASSLOADER_TEMP");
            while ((tempFolder.exists()) && (x <= 10)) {
                tempFolder = new File(DebuggableLaunch.minecraftHome, "CLASSLOADER_TEMP" + x++);
            }

            if (tempFolder.exists()) {
                LogWrapper.info("DEBUG_SAVE enabled, but 10 temp directories already exist, clean them and try again.",
                        new Object[0]);
                tempFolder = null;
            } else {
                LogWrapper.info("DEBUG_SAVE Enabled, saving all classes to \"%s\"",
                        new Object[] { tempFolder.getAbsolutePath().replace('\\', '/') });
                tempFolder.mkdirs();
            }
        }
    }

    public void addURL(URL url) {
        super.addURL(url);
        this.sources.add(url);
    }

    public void setup(URL[] sources) {

        this.sources = new ArrayList<>(Arrays.asList(sources));
        for (URL url : sources) {
            addURL(url);
        }

        addClassLoaderExclusion("java.");
        addClassLoaderExclusion("sun.");
        addClassLoaderExclusion("org.lwjgl.");
        addClassLoaderExclusion("org.apache.logging.");
        addClassLoaderExclusion("net.minecraft.launchwrapper.");

        addTransformerExclusion("javax.");
        addTransformerExclusion("argo.");
        addTransformerExclusion("org.objectweb.asm.");
        addTransformerExclusion("com.google.common.");
        addTransformerExclusion("org.bouncycastle.");
        addTransformerExclusion("net.minecraft.launchwrapper.injector.");

        if (DEBUG_SAVE) {
            int x = 1;
            tempFolder = new File(DebuggableLaunch.minecraftHome, "CLASSLOADER_TEMP");
            while ((tempFolder.exists()) && (x <= 10)) {
                tempFolder = new File(DebuggableLaunch.minecraftHome, "CLASSLOADER_TEMP" + x++);
            }

            if (tempFolder.exists()) {
                LogWrapper.info("DEBUG_SAVE enabled, but 10 temp directories already exist, clean them and try again.",
                        new Object[0]);
                tempFolder = null;
            } else {
                LogWrapper.info("DEBUG_SAVE Enabled, saving all classes to \"%s\"",
                        new Object[] { tempFolder.getAbsolutePath().replace('\\', '/') });
                tempFolder.mkdirs();
            }
        }
    }

    public void registerTransformer(String transformerClassName) {
        try {
            IClassTransformer transformer = (IClassTransformer) loadClass(transformerClassName).newInstance();
            this.transformers.add(transformer);
            if (((transformer instanceof IClassNameTransformer)) && (this.renameTransformer == null)) {
                this.renameTransformer = ((IClassNameTransformer) transformer);
            }
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "A critical problem occurred registering the ASM transformer class %s",
                    new Object[] { transformerClassName });
        }
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (this.invalidClasses.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        for (String exception : this.classExceptions) {
            if (name.startsWith(exception)) {
                return this.parent.loadClass(name);
            }
        }

        if (this.cachedClasses.containsKey(name)) {
            return (Class<?>) this.cachedClasses.get(name);
        }

        for (String exception : this.transformExceptions) {
            if (name.startsWith(exception)) {
                try {
                    Class<?> clazz = super.findClass(name);
                    this.cachedClasses.put(name, clazz);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    this.invalidClasses.add(name);
                    throw e;
                }
            }
        }
        try {
            String transformedName = transformName(name);
            if (this.cachedClasses.containsKey(transformedName)) {
                return (Class<?>) this.cachedClasses.get(transformedName);
            }

            String untransformedName = untransformName(name);

            int lastDot = untransformedName.lastIndexOf('.');
            String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);
            String fileName = untransformedName.replace('.', '/').concat(".class");
            URLConnection urlConnection = findCodeSourceConnectionFor(fileName);

            java.security.CodeSigner[] signers = null;

            if ((lastDot > -1) && (!untransformedName.startsWith("net.minecraft."))) {
                if ((urlConnection instanceof JarURLConnection)) {
                    JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                    JarFile jarFile = jarURLConnection.getJarFile();

                    if ((jarFile != null) && (jarFile.getManifest() != null)) {
                        Manifest manifest = jarFile.getManifest();
                        java.util.jar.JarEntry entry = jarFile.getJarEntry(fileName);

                        Package pkg = getPackage(packageName);
                        getClassBytes(untransformedName);
                        signers = entry.getCodeSigners();
                        if (pkg == null) {
                            pkg = definePackage(packageName, manifest, jarURLConnection.getJarFileURL());
                            this.packageManifests.put(pkg, manifest);
                        } else if ((pkg.isSealed()) && (!pkg.isSealed(jarURLConnection.getJarFileURL()))) {
                            LogWrapper.severe("The jar file %s is trying to seal already secured path %s",
                                    new Object[] { jarFile.getName(), packageName });
                        } else if (isSealed(packageName, manifest)) {
                            LogWrapper.severe(
                                    "The jar file %s has a security seal for path %s, but that path is defined and not secure",
                                    new Object[] { jarFile.getName(), packageName });
                        }
                    }
                } else {
                    Package pkg = getPackage(packageName);
                    if (pkg == null) {
                        pkg = definePackage(packageName, null, null, null, null, null, null, null);
                        this.packageManifests.put(pkg, EMPTY);
                    } else if (pkg.isSealed()) {
                        LogWrapper.severe("The URL %s is defining elements for sealed path %s",
                                new Object[] { urlConnection.getURL(), packageName });
                    }
                }
            }

            byte[] transformedClass = runTransformers(untransformedName, transformedName,
                    getClassBytes(untransformedName));
            if (DEBUG_SAVE) {
                saveTransformedClass(transformedClass, transformedName);
            }

            java.security.CodeSource codeSource = urlConnection == null ? null
                    : new java.security.CodeSource(urlConnection.getURL(), signers);
            Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            this.cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            this.invalidClasses.add(name);
            if (DEBUG) {
                LogWrapper.log(Level.TRACE, e, "Exception encountered attempting classloading of %s",
                        new Object[] { name });
                org.apache.logging.log4j.LogManager.getLogger("LaunchWrapper").log(Level.ERROR,
                        "Exception encountered attempting classloading of %s", e);
            }
            throw new ClassNotFoundException(name, e);
        }
    }

    private void saveTransformedClass(byte[] data, String transformedName) {
        if (tempFolder == null) {
            return;
        }

        File outFile = new File(tempFolder, transformedName.replace('.', File.separatorChar) + ".class");
        File outDir = outFile.getParentFile();

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        if (outFile.exists()) {
            outFile.delete();
        }
        try {
            LogWrapper.fine("Saving transformed class \"%s\" to \"%s\"",
                    new Object[] { transformedName, outFile.getAbsolutePath().replace('\\', '/') });

            java.io.OutputStream output = new java.io.FileOutputStream(outFile);
            output.write(data);
            output.close();
        } catch (IOException ex) {
            LogWrapper.log(Level.WARN, ex, "Could not save transformed class \"%s\"", new Object[] { transformedName });
        }
    }

    private String untransformName(String name) {
        if (this.renameTransformer != null) {
            return this.renameTransformer.unmapClassName(name);
        }

        return name;
    }

    private String transformName(String name) {
        if (this.renameTransformer != null) {
            return this.renameTransformer.remapClassName(name);
        }

        return name;
    }

    private boolean isSealed(String path, Manifest manifest) {
        Attributes attributes = manifest.getAttributes(path);
        String sealed = null;
        if (attributes != null) {
            sealed = attributes.getValue(java.util.jar.Attributes.Name.SEALED);
        }

        if (sealed == null) {
            attributes = manifest.getMainAttributes();
            if (attributes != null) {
                sealed = attributes.getValue(java.util.jar.Attributes.Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    private URLConnection findCodeSourceConnectionFor(String name) {
        URL resource = findResource(name);
        if (resource != null) {
            try {
                return resource.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    private byte[] runTransformers(String name, String transformedName, byte[] basicClass) {
        if (DEBUG_FINER) {
            LogWrapper.finest("Beginning transform of {%s (%s)} Start Length: %d", new Object[] { name, transformedName,
                    Integer.valueOf(basicClass == null ? 0 : basicClass.length) });
            for (IClassTransformer transformer : this.transformers) {
                String transName = transformer.getClass().getName();
                LogWrapper.finest("Before Transformer {%s (%s)} %s: %d", new Object[] { name, transformedName,
                        transName, Integer.valueOf(basicClass == null ? 0 : basicClass.length) });
                basicClass = transformer.transform(name, transformedName, basicClass);
                LogWrapper.finest("After  Transformer {%s (%s)} %s: %d", new Object[] { name, transformedName,
                        transName, Integer.valueOf(basicClass == null ? 0 : basicClass.length) });
            }
            LogWrapper.finest("Ending transform of {%s (%s)} Start Length: %d", new Object[] { name, transformedName,
                    Integer.valueOf(basicClass == null ? 0 : basicClass.length) });
        } else {
            for (IClassTransformer transformer : this.transformers) {
                basicClass = transformer.transform(name, transformedName, basicClass);
            }
        }
        return basicClass;
    }

    public List<URL> getSources() {
        return this.sources;
    }

    private byte[] readFully(InputStream stream) {
        try {
            byte[] buffer = getOrCreateBuffer();

            int totalLength = 0;
            int read;
            while ((read = stream.read(buffer, totalLength, buffer.length - totalLength)) != -1) {
                totalLength += read;

                if (totalLength >= buffer.length - 1) {
                    byte[] newBuffer = new byte[buffer.length + 4096];
                    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                    buffer = newBuffer;
                }
            }

            byte[] result = new byte[totalLength];
            System.arraycopy(buffer, 0, result, 0, totalLength);
            return result;
        } catch (Throwable t) {
            LogWrapper.log(Level.WARN, t, "Problem loading class", new Object[0]);
        }
        return new byte[0];
    }

    private byte[] getOrCreateBuffer() {
        byte[] buffer = (byte[]) this.loadBuffer.get();
        if (buffer == null) {
            this.loadBuffer.set(new byte['?']);
            buffer = (byte[]) this.loadBuffer.get();
        }
        return buffer;
    }

    public List<IClassTransformer> getTransformers() {
        return java.util.Collections.unmodifiableList(this.transformers);
    }

    public void addClassLoaderExclusion(String toExclude) {
        classExceptions.add(toExclude);
    }

    public void addTransformerExclusion(String toExclude) {
        transformExceptions.add(toExclude);
    }

    public byte[] getClassBytes(String name) throws IOException {
        if (this.negativeResourceCache.contains(name))
            return null;
        if (this.resourceCache.containsKey(name))
            return (byte[]) this.resourceCache.get(name);
        byte[] data;
        if (name.indexOf('.') == -1) {
            for (String reservedName : RESERVED_NAMES) {
                if (name.toUpperCase(java.util.Locale.ENGLISH).startsWith(reservedName)) {
                    data = getClassBytes("_" + name);
                    if (data != null) {
                        this.resourceCache.put(name, data);
                        return data;
                    }
                }
            }
        }

        InputStream classStream = null;
        try {
            String resourcePath = name.replace('.', '/').concat(".class");
            URL classResource = findResource(resourcePath);

            if (classResource == null) {
                if (DEBUG)
                    LogWrapper.finest("Failed to find class resource %s", new Object[] { resourcePath });
                this.negativeResourceCache.add(name);
                return null;
            }
            classStream = classResource.openStream();

            if (DEBUG)
                LogWrapper.finest("Loading class %s from resource %s", new Object[] { name, classResource.toString() });
            data = readFully(classStream);
            this.resourceCache.put(name, data);
            return data;
        } finally {
            closeSilently(classStream);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void clearNegativeEntries(Set<String> entriesToClear) {
        this.negativeResourceCache.removeAll(entriesToClear);
    }
}
