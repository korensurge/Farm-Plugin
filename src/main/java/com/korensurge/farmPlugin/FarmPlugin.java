package com.korensurge.farmPlugin;

import com.korensurge.farmPlugin.commands.PermissionsCommand;
import com.korensurge.farmPlugin.commands.TagChestCommand;
import com.korensurge.farmPlugin.handlers.BountifulYield;
import com.korensurge.farmPlugin.handlers.ChestListener;
import com.korensurge.farmPlugin.handlers.PreventTrample;
import com.korensurge.farmPlugin.manager.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmPlugin extends JavaPlugin {

    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        getLogger().info("Hello World. Farm Plugin has started.");

        // Instantiate PermissionManager
        this.permissionManager = new PermissionManager();

        // Instantiate listener for PermissionCommand
        PermissionsCommand cmd = new PermissionsCommand(this);
        if (this.getCommand("farmplugin") != null) {
            this.getCommand("farmplugin").setExecutor(cmd);
            this.getCommand("farmplugin").setTabCompleter(cmd);
        }

        // Instantiate listener for TagChestCommand
        if (getCommand("infinitechest") != null) {
            getCommand("infinitechest").setExecutor(new TagChestCommand(this));
        }

        // Activate Handlers
        getServer().getPluginManager().registerEvents(new BountifulYield(this), this);
        getServer().getPluginManager().registerEvents(new PreventTrample(this), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye World. Farm Plugin has shut down.");
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}
