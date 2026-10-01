package com.korensurge.farmPlugin;

import com.korensurge.farmPlugin.commands.PermissionsCommand;
import com.korensurge.farmPlugin.handlers.BountifulYield;
import com.korensurge.farmPlugin.handlers.PreventTrample;
import com.korensurge.farmPlugin.manager.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmPlugin extends JavaPlugin {

    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        getLogger().info("Hello World. Farm Plugin has started.");

        this.permissionManager = new PermissionManager();

        PermissionsCommand cmd = new PermissionsCommand(this);
        if (this.getCommand("farmplugin") != null) {
            this.getCommand("farmplugin").setExecutor(cmd);
            this.getCommand("farmplugin").setTabCompleter(cmd);
        }

        // Activate Handlers
        getServer().getPluginManager().registerEvents(new BountifulYield(this), this);
        getServer().getPluginManager().registerEvents(new PreventTrample(this), this);
        // Listen for commands to trigger classes
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye World. Farm Plugin has shut down.");

        // Potentially save database info or config
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}
