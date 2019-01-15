package net.minecraftforge.fml.relauncher;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.util.LangUtils;
import org.apache.logging.log4j.Level;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class DebuggableLaunch {
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static Map<String, Object> blackboard;
    public static DebuggableLaunchLoader classLoader;

    public static void main(String[] args) {
        new DebuggableLaunch().launch(args);
    }

    private DebuggableLaunch() {
        URLClassLoader ucl = (URLClassLoader) getClass().getClassLoader();
        classLoader = new DebuggableLaunchLoader(ucl.getURLs());
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void launch(String[] args) {
        blackboard = new java.util.HashMap<>();
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        OptionSpec<String> profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        OptionSpec<File> gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg()
                .ofType(File.class);
        OptionSpec<File> assetsDirOption = parser.accepts("assetsDir", "Assets directory").withRequiredArg()
                .ofType(File.class);
        OptionSpec<String> tweakClassOption = parser.accepts("tweakClass", "Tweak class(es) to load").withRequiredArg()
                .defaultsTo("net.minecraft.launchwrapper.VanillaTweaker", new String[0]);
        OptionSpec<String> nonOption = parser.nonOptions();

        OptionSet options = parser.parse(args);
        minecraftHome = (File) options.valueOf(gameDirOption);
        assetsDir = (File) options.valueOf(assetsDirOption);
        String profileName = (String) options.valueOf(profileOption);
        List<String> tweakClassNames = new ArrayList(options.valuesOf(tweakClassOption));

        List<String> argumentList = new ArrayList();

        blackboard.put("TweakClasses", tweakClassNames);

        blackboard.put("ArgumentList", argumentList);

        Set<String> allTweakerNames = new java.util.HashSet();

        List<ITweakerDebuggable> allTweakers = new ArrayList();
        try {
            List<ITweakerDebuggable> tweakers = new ArrayList(tweakClassNames.size() + 1);

            blackboard.put("Tweaks", tweakers);

            ITweakerDebuggable primaryTweaker = null;

            Iterator<ITweakerDebuggable> it;

            do {
                for (Iterator<String> it2 = tweakClassNames.iterator(); it2.hasNext();) {
                    String tweakName = (String) it2.next();

                    if (allTweakerNames.contains(tweakName)) {
                        LogWrapper.log(Level.WARN, "Tweak class name %s has already been visited -- skipping",
                                new Object[] { tweakName });

                        it2.remove();
                    } else {
                        allTweakerNames.add(tweakName);

                        LogWrapper.log(Level.INFO, "Loading tweak class name %s", new Object[] { tweakName });

                        classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf('.')));
                        ITweakerDebuggable tweaker = (ITweakerDebuggable) Class.forName(tweakName, true, classLoader)
                                .newInstance();
                        tweakers.add(tweaker);

                        it2.remove();

                        if (primaryTweaker == null) {
                            LogWrapper.log(Level.INFO, "Using primary tweak class name %s", new Object[] { tweakName });
                            primaryTweaker = tweaker;
                        }
                    }
                }

                for (it = tweakers.iterator(); it.hasNext();) {
                    ITweakerDebuggable tweaker = (ITweakerDebuggable) it.next();
                    LogWrapper.log(Level.INFO, "Calling tweak class %s", new Object[] { tweaker.getClass().getName() });
                    tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, assetsDir, profileName);
                    tweaker.injectIntoClassLoader(classLoader);
                    allTweakers.add(tweaker);

                    it.remove();
                }

            } while (!tweakClassNames.isEmpty());

            for (ITweakerDebuggable tweaker : allTweakers) {
                argumentList.addAll(java.util.Arrays.asList(tweaker.getLaunchArguments()));
            }

            String launchTarget = primaryTweaker.getLaunchTarget();
            Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });

            LogWrapper.info("Launching wrapped minecraft {%s}", new Object[] { launchTarget });
            mainMethod.invoke(null, new Object[] { argumentList.toArray(new String[argumentList.size()]) });
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "Unable to launch", new Object[0]);
            System.exit(1);
        }
    }
}
