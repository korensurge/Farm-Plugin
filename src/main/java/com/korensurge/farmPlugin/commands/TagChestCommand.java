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
import org.bukkit.persistence.PersistentDataContainer;
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

        if (args.length < 1) {
            sender.sendMessage("Usage: /infinitechest <true|false>");
            return true;
        }

        // Block within 5 blocks, must be a chest
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || !(targetBlock.getState() instanceof Chest chest)) {
            player.sendMessage("You must look directly at a chest!");
            return true;
        }


        // Check passed state
        String stateArg = args[0].toLowerCase();
        if (!stateArg.equals("true") && !stateArg.equals("false")) {
            sender.sendMessage("Invalid state '" + args[0] + "'. Please specify 'true' or 'false'.");
            return true;
        }

        boolean enable = Boolean.parseBoolean(stateArg);

        // Tag the chest with the Persistent Data key if it does not exist already
        NamespacedKey key = new NamespacedKey(plugin, "infinite_chest");
        PersistentDataContainer chestKeys = chest.getPersistentDataContainer();
        if (enable) {
            if (chestKeys.has(key)) { // Chest already has the key , do nothing
                sender.sendMessage("Chest is already an infinite chest.");
                return true;
            } else { // Chest does not have the key , add key
                chestKeys.set(key, PersistentDataType.BYTE, (byte) 1);
                chest.update();
                player.sendMessage("Chest marked as an infinite chest!");
            }
        } else {
            if (chestKeys.has(key)) { // Chest already has the key , remove key
                chestKeys.remove(key);
                chest.update();
                player.sendMessage("Chest is no longer an infinite chest!");
            } else { // Chest does not have the key, do nothing
                sender.sendMessage("Chest is not an infinite chest!");
            }
        }
        return true;
    }
}