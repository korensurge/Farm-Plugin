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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestListener implements Listener {

    private static final long COOLDOWN_MS = 100;
    private final FarmPlugin plugin;
    // Cooldown
    private final Map<UUID, Long> clickCooldowns = new HashMap<>();

    public ChestListener(FarmPlugin plugin) {
        this.plugin = plugin;
    }


    // Prevent adding or removing items from chest by clicking, spawn items in player inventory
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // --- Checks to verify an infinite chest is being interacted with --- //
        // Verify that the player triggered the click
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Verify top inventory is a chest
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getType() != InventoryType.CHEST || topInventory.getLocation() == null) {
            return;
        }

        // Verify the chest block has the custom PDC tag
        Block block = topInventory.getLocation().getBlock();
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }
        NamespacedKey key = new NamespacedKey(plugin, "infinite_chest");
        if (!chest.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            return;
        }


        boolean isAdmin = player.hasPermission(PermissionManager.ADMIN_PERM);
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack cursorItem = event.getCursor();
        boolean isHoldingItemOnCursor = cursorItem != null && cursorItem.getType() != Material.AIR;

        // Allow admins to place items in the chest
        if (isAdmin && isHoldingItemOnCursor) {
            return;
        }

        // Prevent non-admin shift-clicking
        if (event.isShiftClick() && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER) {
            if (!isAdmin) {
                event.setCancelled(true);
                player.sendMessage("§cMust be an admin to deposit items into an infinite chest.");
                return;
            }
            // If admin shift-clicks in their own inventory, allow vanilla transfer
            return;
        }

        // Handle chest slot interactions
        if (clickedInventory != null && clickedInventory.getType() == InventoryType.CHEST) {

            // Allow admin to manage with shift click
            if (isAdmin && event.isShiftClick()) {
                return; // Vanilla behavior removes the source item for maintenance
            }

            ClickType click = event.getClick();

            // Admin only hotbar keys insertion
            if (!isAdmin) {
                // Block hotbar swap key inserts (pressing 1-9 over a chest slot)
                if (click == ClickType.NUMBER_KEY) {
                    event.setCancelled(true);
                    player.sendMessage("§cMust be an admin to deposit items into an infinite chest.");
                    return;
                }
            } else {
                if (click == ClickType.NUMBER_KEY) {
                    return;
                }
            }

            // Cancel default pickup behavior
            event.setCancelled(true);

            // Filter out non-single clicks
            if (click == ClickType.DOUBLE_CLICK || click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
                return;
            }

            // Check if player clicked an actual display item
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            // Rate-limiting check (Debounce)
            long now = System.currentTimeMillis();
            long lastClick = clickCooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (now - lastClick < COOLDOWN_MS) {
                return;
            }
            clickCooldowns.put(player.getUniqueId(), now);

            // Give 1 item
            ItemStack giveItem = clickedItem.clone();
            giveItem.setAmount(1);

            // Handle full inventory
            var excess = player.getInventory().addItem(giveItem);
            if (!excess.isEmpty()) {
                player.sendMessage("§cYour inventory is full!");
            }
        }
    }


    // Handle dragging items into a chest and prevent if not an admin
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Check for player
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        // Check if interacting inventory is of a chest
        if (topInventory.getType() != InventoryType.CHEST || topInventory.getLocation() == null) {
            return;
        }

        // Check if the block is a chest
        Block block = topInventory.getLocation().getBlock();
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        // Check if chest is an infinite chest
        NamespacedKey key = new NamespacedKey(plugin, "infinite_chest");
        if (!chest.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            return;
        }

        // Cancel interaction if event affects chest lost and user is not an admin
        boolean affectsChest = event.getRawSlots().stream().anyMatch(slot -> slot < topInventory.getSize());
        if (affectsChest && !player.hasPermission(PermissionManager.ADMIN_PERM)) {
            event.setCancelled(true);
            player.sendMessage("Must be an admin to deposit items into an infinite chest.");
        }
    }

}