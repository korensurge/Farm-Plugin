package com.korensurge.farmPlugin;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/*

Ideas:

Multiply drop amount by Fortune level (check hoe for enchantment)

Add command that allows moderator to turn of bountiful yield per player
/bountifulyield <name> <crop> <true/false>

SEA_PICKLES should give hoe amount by a multiplier equal to number of pickles in the cluster

Ensure AGE metadata is high enough that the crop would give yield upon breaking. Maybe make a
dictionary of [CROP:AGE]

Possibly do not break the block if the age is not high enough and send a message

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
            Material.PITCHER_CROP,
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



    // Handles crops that require breaking a block
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {

        Block eventBlock = event.getBlock();

        //Check if broken block is a breakable crop
        if (!BREAKABLE_CROPS.contains(eventBlock.getType()) || !HARVESTABLE_CROPS.contains(eventBlock.getType())) {
            return;
        }

        Player player = event.getPlayer();

        // Check if block has age data and check if fully grown
        if (eventBlock.getBlockData() instanceof Ageable ageable) {
            event.setCancelled(true);
            player.sendMessage("Event Cancelled");

            int currentAge = ageable.getAge();
            int maxAge = ageable.getMaximumAge();
            boolean fullyGrown = (currentAge == maxAge);

            player.sendMessage("Block Type: " + eventBlock.getType());
            player.sendMessage("Current Age: " + currentAge);
            player.sendMessage("Max Age: " + maxAge);
            player.sendMessage("Fully Grown: " + fullyGrown);

            if (fullyGrown) {
                eventBlock.setType(Material.AIR);
            }

            ItemStack tool = player.getInventory().getItemInMainHand();

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

            // Paper automatically takes into account Unbreaking
            tool.damage(1, player);

            int totalMult = fortMult * toolMult;
        }


        //display hand return
        player.sendMessage("--- Break Event Triggered! ---");

    }

}
