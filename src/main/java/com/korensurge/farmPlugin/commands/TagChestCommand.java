package com.korensurge.farmPlugin.commands;

import com.korensurge.farmPlugin.FarmPlugin;
import com.korensurge.farmPlugin.manager.PermissionManager;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class TagChestCommand implements CommandExecutor {

    private final FarmPlugin plugin;

    public TagChestCommand(FarmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Only allow admins to use
        if (!(player.hasPermission(PermissionManager.ADMIN_PERM))) {
            sender.sendMessage("Must be an admin to tag a chest.");
            return true;
        }

        // Block within 5 blocks, must be a chest
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || !(targetBlock.getState() instanceof Chest chest)) {
            player.sendMessage("You must look directly ast a chest!");
            return true;
        }

        // Tag the chest with the Persistent Data key
        NamespacedKey key = new NamespacedKey(plugin, "infinite_chest");
        chest.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        chest.update();

        player.sendMessage("Chest marked as an infinite chest!");
        return true;
    }
}