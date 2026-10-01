package com.korensurge.farmPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.Set;

/*

Ideas:

Add command that allows moderator to turn of bountiful yield per player
/bountifulyield <name> <true/false>

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
            Material.TORCHFLOWER,
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
            Material.SWEET_BERRY_BUSH,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT
    );





    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();
        // Add Player permission logic -----------------<>
        player.sendMessage("<--- Break Event Triggered! --->");

        if (!BREAKABLE_CROPS.contains(blockType)) {
            player.sendMessage("<--- Block not recognized --->");
            return;
        }
        player.sendMessage("<--- Triggering Handle Harvest --->");

        if (blockType == Material.SUGAR_CANE || blockType == Material.KELP_PLANT || blockType == Material.KELP ) {
            Block current = block;
            int count = 0;

            if (blockType == Material.KELP_PLANT || blockType == Material.KELP) {
                while (current.getType() == Material.KELP || current.getType() == Material.KELP_PLANT) {
                    count++;
                    handleHarvest(player, current, blockType, "break");
                    current.setType(Material.AIR);
                    current = current.getRelative(BlockFace.UP);
                }
            } else {
                while (current.getType() == blockType) {
                    count++;
                    handleHarvest(player, current, blockType, "break");
                    current.setType(Material.AIR);
                    current = current.getRelative(BlockFace.UP);
                }
            }
            player.sendMessage(count + " harvested");

        } else {
            boolean complete = handleHarvest(player, block, blockType, "break");
            if (complete) {
                event.setCancelled(true);
                block.setType(Material.AIR);
                player.sendMessage("<--- Complete --->");
            } else {
                player.sendMessage("<--- Not Eligible --->");
            }
        }

    }





    @EventHandler(ignoreCancelled = true)
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        Block block = event.getHarvestedBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();
        // Add Player permission logic -----------------<>
        player.sendMessage("<--- Harvest Event Triggered! --->");

        if (!HARVESTABLE_CROPS.contains(blockType)) {
            player.sendMessage("<--- Block not recognized --->");
            return;
        }
        player.sendMessage("<--- Triggering Handle Harvest --->");
        handleHarvest(player, block, blockType, "harvest");
        player.sendMessage("<--- Complete --->");
        event.setCancelled(true);
    }





    private boolean handleHarvest(Player player, Block block, Material blockType, String eventType) {

        player.sendMessage("Handling Harvest");

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if breakable block has age data and check if fully grown
        if (Objects.equals(eventType, "break")) {
            if (block.getBlockData() instanceof Ageable ageable) {

                int currentAge = ageable.getAge();
                int maxAge = ageable.getMaximumAge();
                boolean fullyGrown = (currentAge == maxAge);

                player.sendMessage("Block Type: " + blockType);
                player.sendMessage("Current Age: " + currentAge);
                player.sendMessage("Max Age: " + maxAge);
                player.sendMessage("Fully Grown: " + fullyGrown);

                // Return if not fully grown, normal behavior will be followed
                if (!fullyGrown && (blockType != Material.SUGAR_CANE) && (blockType != Material.KELP) && (blockType != Material.KELP_PLANT) ) { return false; }

            } else { player.sendMessage("No Age Data"); }
        }


        // Define item to be dropped based on the block
        Material crop = switch (blockType) {
            case Material.WHEAT -> Material.WHEAT;
            case Material.BEETROOTS -> Material.BEETROOT;
            case Material.CARROTS -> Material.CARROT;
            case Material.MELON -> Material.MELON_SLICE;
            case Material.PUMPKIN -> Material.PUMPKIN;
            case Material.TORCHFLOWER -> Material.TORCHFLOWER;
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
            case Material.CAVE_VINES -> Material.GLOW_BERRIES;
            case Material.CAVE_VINES_PLANT -> Material.GLOW_BERRIES;
            case Material.SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            default -> Material.AIR;
        };

        // Determine seed to be dropped based on the block
        Material seed = switch (blockType) {
            case Material.WHEAT -> Material.WHEAT_SEEDS;
            case Material.BEETROOTS -> Material.BEETROOT_SEEDS;
            case Material.CARROTS -> Material.AIR;
            case Material.MELON -> Material.AIR;
            case Material.PUMPKIN -> Material.AIR;
            case Material.TORCHFLOWER -> Material.TORCHFLOWER_SEEDS;
            case Material.TORCHFLOWER_CROP -> Material.TORCHFLOWER_SEEDS;
            case Material.PITCHER_PLANT -> Material.PITCHER_POD;
            case Material.BAMBOO -> Material.AIR;
            case Material.COCOA -> Material.AIR;
            case Material.SUGAR_CANE -> Material.AIR;
            case Material.CACTUS -> Material.AIR;
            case Material.BROWN_MUSHROOM -> Material.AIR;
            case Material.RED_MUSHROOM -> Material.AIR;
            case Material.KELP -> Material.AIR;
            case Material.KELP_PLANT -> Material.AIR;
            case Material.NETHER_WART -> Material.AIR;
            case Material.CRIMSON_FUNGUS -> Material.AIR;
            case Material.WARPED_FUNGUS -> Material.AIR;
            case Material.SEA_PICKLE -> Material.AIR;
            default -> Material.AIR;
        };

        int seedMult = switch (seed) {
            case Material.WHEAT_SEEDS -> ThreadLocalRandom.current().nextInt(1, 5);
            case Material.BEETROOT_SEEDS -> ThreadLocalRandom.current().nextInt(1, 5);
            case Material.TORCHFLOWER_SEEDS -> 1;
            case Material.PITCHER_POD -> 1;
            default -> 0;
        };

        // Determine multiplier based on hoe quality
        int toolMult = switch (tool.getType()) {
            case Material.WOODEN_HOE -> 1;
            case Material.STONE_HOE -> 1;
            case Material.IRON_HOE -> 2;
            case Material.GOLDEN_HOE -> 2;
            case Material.DIAMOND_HOE -> 3;
            case Material.NETHERITE_HOE -> 3;
            default -> 0;
        };

        // Take Fortune enchantment into account
        int fortLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);

        int chance = ThreadLocalRandom.current().nextInt(1, 101);
        int fortMult = switch (fortLevel) {
            case 1:
                if (chance <= 66) {
                    yield 1;
                } else {
                    yield 2;
                }
            case 2:
                if (chance <= 50) {
                    yield 1;
                } else if (chance <= 75) {
                    yield 2;
                } else {
                    yield 3;
                }
            case 3:
                if (chance <= 40) {
                    yield 1;
                } else if (chance <= 60) {
                    yield 2;
                } else if (chance <= 80) {
                    yield 3;
                } else {
                    yield 4;
                }
            default:
                yield 1;
        };

        int totalMult = fortMult * toolMult;
        int seedDrop = fortMult * seedMult;

        player.sendMessage("Fort Mult:" + fortMult);
        player.sendMessage("Tool Mult:" + toolMult);
        if (totalMult <= 0) {
            player.sendMessage("Non-hoe Item Used");
            return true;
        }

        // Damage Hoe
        // Paper automatically takes into account Unbreaking
        if (tool.getType() != Material.AIR && (toolMult > 0)) {
            tool.damage(1, player);
        }

        if (Objects.equals(eventType, "break")) {
            player.sendMessage("<--- Dropping Item --->");

            Location location = block.getLocation();
            World world = block.getWorld();

            // If Sea Pickles, account for varying pickle value of block
            if (crop == Material.SEA_PICKLE) {
                if (block.getBlockData() instanceof SeaPickle pickles) {
                    totalMult *= pickles.getPickles();
                    int drop = totalMult;
                    player.sendMessage("Dropping " + drop + " " + crop);
                    world.dropItemNaturally(location, new ItemStack(crop, drop));
                    return true;
                }

            } else if (crop == Material.MELON) {
                int drop = totalMult*ThreadLocalRandom.current().nextInt(3, 8);
                player.sendMessage("Dropping " + drop + " " + crop);
                world.dropItemNaturally(location, new ItemStack(crop, drop));

            } else {
                // Drop items
                int drop = totalMult;
                player.sendMessage("Dropping " + drop + " " + crop);
                world.dropItemNaturally(location, new ItemStack(crop, drop));
                if (seed != Material.AIR) {
                    player.sendMessage("Dropping " + seedDrop + " " + seed);
                    world.dropItemNaturally(location, new ItemStack(seed, seedDrop));
                }
                return true;
            }
        }



        if (Objects.equals(eventType, "harvest")) {
            player.sendMessage("<--- Giving Item --->");

            if (crop == Material.GLOW_BERRIES) {
                int drop = totalMult;
                giveItemOrDrop(player, crop, drop);

                if (block.getBlockData() instanceof CaveVines vines) {vines.setBerries(false); block.setBlockData(vines);}
                if (block.getBlockData() instanceof CaveVinesPlant vines) {vines.setBerries(false); block.setBlockData(vines);}
            }

            if (crop == Material.SWEET_BERRIES){
                if (block.getBlockData() instanceof Ageable ageable) {

                    int currentAge = ageable.getAge();

                    // implement random chance of 1-2 berry base at age 2, 2-3 berry base at age 4
                    int randomNumber = ThreadLocalRandom.current().nextInt(1, 101);
                    if (currentAge == 2) {
                        int drop = totalMult*ThreadLocalRandom.current().nextInt(1, 3);
                        giveItemOrDrop(player, crop, drop);
                    }
                    if (currentAge == 3) {
                        int drop = totalMult*ThreadLocalRandom.current().nextInt(2, 4);
                        giveItemOrDrop(player, crop, drop);
                    }
                    // Reset to age 1
                    ageable.setAge(1);
                    block.setBlockData(ageable);
                }
            }
            return true;
        }
        return false;
    } // End of handleHarvest





    private void giveItemOrDrop(Player player, Material item, int drop) {
        player.sendMessage("Dropping " + drop + " " + item);

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack(item, drop));

        if (!overflow.isEmpty()) {
            for (ItemStack leftover : overflow.values()) {
                // Drop the leftover items at the player's current location
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }


}