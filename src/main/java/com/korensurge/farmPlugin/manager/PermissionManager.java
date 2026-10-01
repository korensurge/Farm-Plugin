package com.korensurge.farmPlugin.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PermissionManager {

    // Permission Node Constants
    public static final String ADMIN_PERM = "farmplugin.admin";
    public static final String BOUNTIFUL_YIELD_PERM = "farmplugin.bountifulyield";
    public static final String PREVENT_TRAMPLE_PERM = "farmplugin.preventtrample";


    // Checks if a player has permission for a specific feature
    public boolean hasFeaturePermission(Player player, String permissionNode) {
        return player.hasPermission(permissionNode);
    }


    // Checks administrative permissions on a CommandSender
    public boolean hasAdminPermission(CommandSender sender) {
        return sender.hasPermission(ADMIN_PERM);
    }
}