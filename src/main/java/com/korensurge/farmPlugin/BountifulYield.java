package com.korensurge.farmPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/*

Ideas:

Add command that allows moderator to turn of bountiful yield per player
/bountifulyield <name> <crop> <true/false>

SEA_PICKLES should give hoe amount by a multiplier equal to number of pickles in the cluster

Ensure AGE metadata is high enough that the crop would give yield upon breaking

If all works, decrease hoe durability by 1

Need to check how blockBreakEvent works for bamboo/sugar_cane/kelp
 */

public class BountifulYield implements Listener{

    private final JavaPlugin plugin;
    public BountifulYield(JavaPlugin plugin) {

        this.plugin = plugin;
    }

    private final Set<Material> BREAKABLE_CROPS = Set.of(
            Material.WHEAT,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.MELON,
            Material.PUMPKIN,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_PLANT,
            Material.BAMBOO,
            Material.COCOA,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.KELP,
            Material.KELP_PLANT,
            Material.NETHER_WART,
            Material.CRIMSON_FUNGUS,
            Material.WARPED_FUNGUS,
            Material.SEA_PICKLE
    );

    private final Set<Material> HARVESTABLE_CROPS = Set.of(
            Material.SWEET_BERRIES,
            Material.GLOW_BERRIES
    );

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();
        player.sendMessage("<--- Break Event Triggered! --->");

        if (!BREAKABLE_CROPS.contains(blockType)) {
            return;
        }

        handleHarvest(event, player, block, blockType, "break");

        event.setCancelled(true);
        block.setType(Material.AIR);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        Block block = event.getHarvestedBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();
        player.sendMessage("<--- Harvest Event Triggered! --->");

        if (!HARVESTABLE_CROPS.contains(blockType)) {
            return;
        }

        handleHarvest(event, player, block, blockType, "harvest");

        event.setCancelled(true);
    }

    private void handleHarvest(Event event, Player player, Block block, Material blockType, String eventType) {

        player.sendMessage("Handling Harvest");

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if block has age data and check if fully grown
        if (eventType == "break") {
            if (block.getBlockData() instanceof Ageable ageable) {

                int currentAge = ageable.getAge();
                int maxAge = ageable.getMaximumAge();
                boolean fullyGrown = (currentAge == maxAge);

                // Paper automatically takes into account Unbreaking
                tool.damage(1, player);

                player.sendMessage("Block Type: " + blockType);
                player.sendMessage("Current Age: " + currentAge);
                player.sendMessage("Max Age: " + maxAge);
                player.sendMessage("Fully Grown: " + fullyGrown);

                if (!fullyGrown) { return; }

            } else { player.sendMessage("No Age Data"); return; }

        }

        Material crop = switch (blockType) {
            case Material.WHEAT -> Material.WHEAT;
            case Material.BEETROOTS -> Material.BEETROOT;
            case Material.CARROTS -> Material.CARROT;
            case Material.MELON -> Material.MELON;
            case Material.PUMPKIN -> Material.PUMPKIN;
            case Material.TORCHFLOWER_CROP -> Material.TORCHFLOWER;
            case Material.PITCHER_PLANT -> Material.PITCHER_PLANT;
            case Material.BAMBOO -> Material.BAMBOO;
            case Material.COCOA -> Material.COCOA_BEANS;
            case Material.SUGAR_CANE -> Material.SUGAR_CANE;
            case Material.CACTUS -> Material.CACTUS;
            case Material.BROWN_MUSHROOM -> Material.BROWN_MUSHROOM;
            case Material.RED_MUSHROOM -> Material.RED_MUSHROOM;
            case Material.KELP -> Material.KELP;
            case Material.KELP_PLANT -> Material.KELP;
            case Material.NETHER_WART -> Material.NETHER_WART;
            case Material.CRIMSON_FUNGUS -> Material.CRIMSON_FUNGUS;
            case Material.WARPED_FUNGUS -> Material.WARPED_FUNGUS;
            case Material.SEA_PICKLE -> Material.SEA_PICKLE;
            default -> Material.AIR;
        };

        Material seed = switch (blockType) {
            case Material.WHEAT -> Material.WHEAT;
            case Material.BEETROOTS -> Material.BEETROOT;
            case Material.CARROTS -> Material.CARROT;
            case Material.MELON -> Material.MELON;
            case Material.PUMPKIN -> Material.PUMPKIN;
            case Material.TORCHFLOWER_CROP -> Material.TORCHFLOWER;
            case Material.PITCHER_PLANT -> Material.PITCHER_PLANT;
            case Material.BAMBOO -> Material.BAMBOO;
            case Material.COCOA -> Material.COCOA_BEANS;
            case Material.SUGAR_CANE -> Material.SUGAR_CANE;
            case Material.CACTUS -> Material.CACTUS;
            case Material.BROWN_MUSHROOM -> Material.BROWN_MUSHROOM;
            case Material.RED_MUSHROOM -> Material.RED_MUSHROOM;
            case Material.KELP -> Material.KELP;
            case Material.KELP_PLANT -> Material.KELP;
            case Material.NETHER_WART -> Material.NETHER_WART;
            case Material.CRIMSON_FUNGUS -> Material.CRIMSON_FUNGUS;
            case Material.WARPED_FUNGUS -> Material.WARPED_FUNGUS;
            case Material.SEA_PICKLE -> Material.SEA_PICKLE;
            default -> Material.AIR;
        };

        int toolMult = switch (tool.getType()) {
            case Material.WOODEN_HOE -> 1;
            case Material.STONE_HOE -> 1;
            case Material.IRON_HOE -> 2;
            case Material.GOLDEN_HOE -> 2;
            case Material.DIAMOND_HOE -> 3;
            case Material.NETHERITE_HOE -> 3;
            default -> 0;
        };

        int fortLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int fortMult = (1/(fortLevel+2)) + ((1+fortLevel)/2);

        int totalMult = fortMult * toolMult;

        if (eventType == "break") {

            Location location = block.getLocation();

            // Sea Pickle if statement

            // Give player totalMult number of items

            // Give player seed(s)

        }

        if (eventType == "harvest") {
            // Glow Berry if statement
            // Sweet berry if statement
                // 1-2 at age 3, 2-3 at age 4
                // Reset to age 1
        }

    }

}
