package com.korensurge.farmPlugin.commands;

import com.korensurge.farmPlugin.FarmPlugin;
import com.korensurge.farmPlugin.manager.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionsCommand implements CommandExecutor, TabCompleter {

    private final FarmPlugin plugin;

    public PermissionsCommand(FarmPlugin plugin) {
        this.plugin = plugin;
    }


    // Command Resolver
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PermissionManager permManager = plugin.getPermissionManager();

        // Check permissions
        if (!permManager.hasAdminPermission(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        // Check syntax /farmplugin <feature> <player> <true|false>
        if (args.length < 3) {
            sender.sendMessage("Usage: /farmplugin <feature> <player> <true|false>");
            return true;
        }

        // Check passed feature
        String featureArg = args[0].toLowerCase();
        String targetPermissions = switch (featureArg) {
            case "bountifulyield", "by" -> PermissionManager.BOUNTIFUL_YIELD_PERM;
            case "preventtrample", "pt" -> PermissionManager.PREVENT_TRAMPLE_PERM;
            default -> null;
        };

        if (targetPermissions == null) {
            sender.sendMessage("Unknown feature '" + args[0] + "' Valid options: bountifulyield / by, preventtrample / pt");
            return true;
        }

        // Check passed player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player '" + args[1] + "' is not online.");
            return true;
        }

        // Check passed state
        String stateArg = args[2].toLowerCase();
        if (!stateArg.equals("true") && !stateArg.equals("false")) {
            sender.sendMessage("Invalid state '" + args[2] + "'. Please specify 'true' or 'false'.");
            return true;
        }

        // Update state
        boolean enable = Boolean.parseBoolean(stateArg);
        String internalCommand = String.format("lp user %s permission set %s %b", target.getName(), targetPermissions, enable);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), internalCommand);

        // Outcome Message
        String newStatus = enable ? "ACTIVATED" : "DEACTIVATED";
        sender.sendMessage("Feature" + featureArg + " is now " + newStatus + " for " + target.getName() + ".");
        return true;
    }


    // Tab Completion
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!plugin.getPermissionManager().hasAdminPermission(sender)) {
            return completions;
        }

        if (args.length == 1) {
            // Suggest features
            List<String> features = List.of("bountifulyield", "preventtrample");
            for (String f : features) {
                if (f.startsWith(args[0].toLowerCase())) {
                    completions.add(f);
                }
                ;
            }
        } else if (args.length == 2) {
            // Suggest names
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            // Suggest true/false
            if ("true".startsWith(args[2].toLowerCase())) {
                completions.add("true");
            }
            if ("false".startsWith(args[2].toLowerCase())) {
                completions.add("false");
            }
        }

        return completions;
    }

}