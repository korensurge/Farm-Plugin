package com.korensurge.farmPlugin.handlers;

import com.korensurge.farmPlugin.FarmPlugin;
import com.korensurge.farmPlugin.manager.PermissionManager;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class PreventTrample implements Listener {

    private final FarmPlugin plugin;

    public PreventTrample(FarmPlugin plugin) {

        this.plugin = plugin;
    }


    // Detects if the a trample event occurs on farmland and cancels it if they are assigned the farmland.preventtrample perm
    @EventHandler(ignoreCancelled = true)
    public void onFarmTrample(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.FARMLAND) {
                // Check for permission, cancel if true
                if (plugin.getPermissionManager().hasFeaturePermission(event.getPlayer(), PermissionManager.PREVENT_TRAMPLE_PERM)) {
                    event.setCancelled(true);
                }
            }
        }
    }


}
