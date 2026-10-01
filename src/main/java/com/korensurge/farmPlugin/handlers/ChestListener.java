package com.korensurge.farmPlugin.handlers;

import com.korensurge.farmPlugin.FarmPlugin;
import com.korensurge.farmPlugin.manager.PermissionManager;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestListener implements Listener {

    private final FarmPlugin plugin;

    public ChestListener(FarmPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Verify that the player triggered the click
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Ensure the player clicked inside the chest inventory
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) {
            return;
        }

        // Allow normal behavior if shift clicking and has admin perms
        if (event.isShiftClick() && player.hasPermission(PermissionManager.ADMIN_PERM)) {
            return;
        }

        // Ensure they actually clicked on an item slot
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Verify the chest block has the custom PDC tag
        if (event.getClickedInventory().getLocation() == null) {
            return;
        }

        Block block = event.getClickedInventory().getLocation().getBlock();
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "infinite_chest");
        if (!chest.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            return;
        }

        // Cancel item from being grabbed
        event.setCancelled(true);

        // Clone item
        ItemStack giveItem = clickedItem.clone();
        giveItem.setAmount(1);

        // addItem returns any items that couldn't fit if the player's inventory is full
        var excess = player.getInventory().addItem(giveItem);
        if (!excess.isEmpty()) {
            player.sendMessage("You inventory is full!");
        }
    }
}