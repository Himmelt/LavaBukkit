package co.matrixdevelopment.lava.api;

import java.util.ArrayList;

public class LavaPlugin {
    private ArrayList<LavaPlugin> dependencies = new ArrayList<>();

    public void onEnable() {
    }

    public void onDisable() {
    }

    /**
     * Be careful with this method, it runs on the main server thread! (for now)
     */
    public void onUpdate() {

    }

    public void dependsOn(LavaPlugin lp) {
        dependencies.add(lp);
    }
}