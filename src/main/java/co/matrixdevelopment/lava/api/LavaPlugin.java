package co.matrixdevelopment.lava.api;

import java.util.ArrayList;

public class LavaPlugin {
    private ArrayList<String> dependencies = new ArrayList<>();

    public void onEnable() {
    }

    public void onDisable() {
    }

    /**
     * Be careful with this method, it runs on the main server thread! (for now)
     */
    public void onUpdate() {

    }

    protected void dependsOn(String pluginName) {
        dependencies.add(pluginName);
    }

    public ArrayList<String> getDependencies() {
        return dependencies;
    }
}