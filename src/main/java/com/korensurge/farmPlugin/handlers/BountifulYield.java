package com.korensurge.farmPlugin.handlers;

import com.korensurge.farmPlugin.FarmPlugin;
import com.korensurge.farmPlugin.manager.PermissionManager;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.Set;


public class BountifulYield implements Listener {

    private final FarmPlugin plugin;
    // List of Block-Break Harvestable Crops
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
    // List of Right-Click Harvestable Crops
    private final Set<Material> HARVESTABLE_CROPS = Set.of(
            Material.SWEET_BERRY_BUSH,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT
    );

    public BountifulYield(FarmPlugin plugin) {

        this.plugin = plugin;
    }

    // Prevent water from destroying crops
    @EventHandler(ignoreCancelled = true)
    public void onWaterBreakCrop(BlockFromToEvent event) {
        Block targetBlock = event.getToBlock();
        Material targetMaterial = targetBlock.getType();

        if (BREAKABLE_CROPS.contains(targetMaterial) || HARVESTABLE_CROPS.contains(targetMaterial)) {
            event.setCancelled(true);
        }
    }

    // Prevent piston from breaking crops by pushing
    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // Iterate through affected blocks
        for (Block block : event.getBlocks()) {
            Material targetMaterial = block.getType();
            // Cancel if crop
            if (BREAKABLE_CROPS.contains(targetMaterial) || HARVESTABLE_CROPS.contains(targetMaterial)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    // Prevent piston from breaking crops by pulling
    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // Iterate through affected blocks
        for (Block block : event.getBlocks()) {
            Material targetMaterial = block.getType();

            // Check if the block being pulled itself is a crop
            if (BREAKABLE_CROPS.contains(targetMaterial) || HARVESTABLE_CROPS.contains(targetMaterial)) {
                event.setCancelled(true);
                return;
            }

            // Check if the block ABOVE the pulled block has a crop attached to it
            Block blockAbove = block.getRelative(BlockFace.UP);
            Material aboveMaterial = blockAbove.getType();

            if (BREAKABLE_CROPS.contains(aboveMaterial) || HARVESTABLE_CROPS.contains(aboveMaterial)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Prevent entity explosions from destroying crops
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        protectCropsFromExplosionList(event.blockList());
    }

    // Prevent block explosions from destroying crops
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        protectCropsFromExplosionList(event.blockList());
    }


    // Iterates through list of blocks set to be affected by explosions and cancels any crops from being destroyed
    private void protectCropsFromExplosionList(java.util.List<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Material targetMaterial = block.getType();

            // Protect both the crop itself and the Farmland soil beneath it
            if (BREAKABLE_CROPS.contains(targetMaterial) || HARVESTABLE_CROPS.contains(targetMaterial) || targetMaterial == Material.FARMLAND) {
                iterator.remove(); // Removes the block from destruction list
            }
        }
    }


    // Handler for crops that are harvested by breaking the crop
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();

        // Check if player has farmland.bountifulyield perm, vanilla behavior if not
        if (!player.hasPermission(PermissionManager.BOUNTIFUL_YIELD_PERM)) {
            return;
        }

        // If broken block is not a crop, vanilla behavior
        if (!BREAKABLE_CROPS.contains(blockType)) {
            return;
        }

        // If crop is a towering crop, special behavior to account for above crops
        if (blockType == Material.BAMBOO || blockType == Material.SUGAR_CANE || blockType == Material.KELP_PLANT || blockType == Material.KELP) {
            Block current = block;

            // special clause is needed here as top of kelp crop is always Material.KELP
            if (blockType == Material.KELP_PLANT || blockType == Material.KELP) {
                while (current.getType() == Material.KELP || current.getType() == Material.KELP_PLANT) {
                    handleHarvest(player, current, blockType, "break");
                    current.setType(Material.AIR);
                    current = current.getRelative(BlockFace.UP);
                }
            } else { // Handles bamboo and sugar cane
                while (current.getType() == blockType) {
                    handleHarvest(player, current, blockType, "break");
                    current.setType(Material.AIR);
                    current = current.getRelative(BlockFace.UP);
                }
            }
        } else { // Handles non-towering crops
            boolean complete = handleHarvest(player, block, blockType, "break");
            if (complete) {
                event.setCancelled(true);
                block.setType(Material.AIR);
            }
        }

    }


    // Handler for crops that are harvested by right-clicking the crop
    @EventHandler(ignoreCancelled = true)
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        Block block = event.getHarvestedBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();

        // Check if player has farmland.bountifulyield perm, vanilla behavior if not
        if (!player.hasPermission(PermissionManager.BOUNTIFUL_YIELD_PERM)) {
            return;
        }

        // If harvested block is not a crop, vanilla behavior
        if (!HARVESTABLE_CROPS.contains(blockType)) {
            return;
        }
        handleHarvest(player, block, blockType, "harvest");
        event.setCancelled(true);
    }


    // Class to handle the harvest
    private boolean handleHarvest(Player player, Block block, Material blockType, String eventType) {


        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if breakable block has age data and check if fully grown
        // No need for this check for right-click harvestable crops as the PlayerHarvestBlockEvent can only be triggered if they are of an appropriate age
        if (Objects.equals(eventType, "break")) {
            if (block.getBlockData() instanceof Ageable ageable) {

                int currentAge = ageable.getAge();
                int maxAge = ageable.getMaximumAge();
                boolean fullyGrown = (currentAge == maxAge);

                // Return if not fully grown, normal behavior will be followed
                // Not applicable to towering crops as age does not determine harvestability
                if (!fullyGrown && (blockType != Material.BAMBOO) && (blockType != Material.SUGAR_CANE) && (blockType != Material.KELP) && (blockType != Material.KELP_PLANT)) {
                    return false;
                }

            }
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
            case Material.TORCHFLOWER -> Material.TORCHFLOWER_SEEDS;
            case Material.TORCHFLOWER_CROP -> Material.TORCHFLOWER_SEEDS;
            case Material.PITCHER_PLANT -> Material.PITCHER_POD;
            default -> Material.AIR;
        };

        // Determine number of seeds dropped by vanilla
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

        // Replicates vanilla fortune chances
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

        if (totalMult <= 0) {
            player.sendMessage("Non-hoe Item Used");
            return true;
        }

        // Damage Hoe
        // Paper automatically takes into account Unbreaking
        if (tool.getType() != Material.AIR && (toolMult > 0)) {
            tool.damage(1, player);
        }


        // Item spawn behavior for break type harvests
        if (Objects.equals(eventType, "break")) {
            Location location = block.getLocation();
            World world = block.getWorld();

            // If Sea Pickles, account for varying pickle value of block
            if (crop == Material.SEA_PICKLE) {
                if (block.getBlockData() instanceof SeaPickle pickles) {
                    totalMult *= pickles.getPickles();
                    int drop = totalMult;
                    world.dropItemNaturally(location, new ItemStack(crop, drop));
                    return true;
                }

            } else if (crop == Material.MELON) {
                int drop = totalMult * ThreadLocalRandom.current().nextInt(3, 8);
                world.dropItemNaturally(location, new ItemStack(crop, drop));

            } else {
                // Drop items
                int drop = totalMult;
                world.dropItemNaturally(location, new ItemStack(crop, drop));
                if (seed != Material.AIR) {
                    world.dropItemNaturally(location, new ItemStack(seed, seedDrop));
                }
                return true;
            }
        }


        // Item spawn behavior for PlayerHarvest type harvests
        if (Objects.equals(eventType, "harvest")) {

            if (crop == Material.GLOW_BERRIES) {
                int drop = totalMult;
                giveItemOrDrop(player, crop, drop);

                // Set berry metadata to false
                if (block.getBlockData() instanceof CaveVines vines) {
                    vines.setBerries(false);
                    block.setBlockData(vines);
                }
                if (block.getBlockData() instanceof CaveVinesPlant vines) {
                    vines.setBerries(false);
                    block.setBlockData(vines);
                }
            }

            if (crop == Material.SWEET_BERRIES) {
                if (block.getBlockData() instanceof Ageable ageable) {

                    int currentAge = ageable.getAge();

                    // implement random chance of 1-2 berry base at age 2, 2-3 berry base at age 4
                    int randomNumber = ThreadLocalRandom.current().nextInt(1, 101);
                    if (currentAge == 2) {
                        int drop = totalMult * ThreadLocalRandom.current().nextInt(1, 3);
                        giveItemOrDrop(player, crop, drop);
                    }
                    if (currentAge == 3) {
                        int drop = totalMult * ThreadLocalRandom.current().nextInt(2, 4);
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

        // Save any items not able to fit into the player's inventory
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack(item, drop));

        if (!overflow.isEmpty()) {
            for (ItemStack leftover : overflow.values()) {
                // Drop the leftover items at the player's current location
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }


}