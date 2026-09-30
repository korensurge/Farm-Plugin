package com.korensurge.farmPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class FarmPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Hello World. Farm Plugin has started.");

        // Activate Handlers
        getServer().getPluginManager().registerEvents(new BountifulYield(this), this);
        // Listen for commands to trigger classes
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye World. Farm Plugin has shut down.");

        // Potentially save database info or config
    }
}
