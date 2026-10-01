KorenSurge Farm Plugin

Requires use of LuckPerms to handle assignment of permissions.
Version 5.5.85 was used during testing.

Requires use of PaperMC.
Version 1.21.11-132 was used during testing.

Added Functionality Categories:
* Bountiful Yield
* Trample Prevention
* Infinite Chest

Bountiful Yield:
* On by default for all players
  * Toggleable per player using /farmplugin bountifulyield [player] [true|false]

* Enforces usage of a hoe to harvest all crops. 
  * Bamboo
  * Beetroots
  * Brown Mushrooms
  * Cactus
  * Carrots
  * Glow Berries
  * Cocoa
  * Crimson Fungus
  * Kelp
  * Melons
  * Nether Wart
  * Pitcher Plants
  * Pumpkins
  * Red Mushrooms
  * Sea Pickles 
  * Sugar Cane
  * Sweet Berries
  * Torchflowers
  * Warped Fungus
  * Wheat

* Crop drops are increased with increasing quality of tool
  * Wooden/Stone: 1x
  * Iron/Gold: 2x
  * Diamond/Netherite: 3x

* Crop and Seed drops are affected by the Fortune enchantment

* Crop destruction prevention
  * All player breaking/harvesting without a hoe
  * Pistons
  * Liquid breaking
  * Entity explosions
  * Block explosions

Prevent Trample:
* Allows admin toggleable protections against players damaging crops through block impacts
  * On by default for all players
  * Toggleable per player using /farmplugin preventtrample [player] [true|false]

Infinite Chest:
* Allows creation of an infinite chest source
  * Clicking upon an item in the chest will spawn 1 clone of that item in player inventory
* Users with Admin permissions are able to manage chest contents
  * Adjust chest infinite status using /infinitechest <true|false>
  * All standard interactions for adding items to the chest
  * Remove by Shift+Click or using 1-9
  * Standard clicking within an infinite chest will still perform the infinite functionality



<--- Commands --->

* farmplugin:
  * Description: Enable or Disable FarmPlugin features for a player. Utilizes LuckPerms.
  * Usage: /farmplugin [feature] [player] [true|false]
  * Features: bountifulyield [or by] , preventtrample [or pt]
  * Permission: farmplugin.admin
  * Aliases: [fp, farm]
* infinitechest:
  * description: Marks the targeted chest as an infinite item source.
  * permission: farmplugin.admin
  * usage: /infinitechest [true|false]

<--- Permissions --->
* farmplugin.admin
  * description: Allows using plugin management commands and infinite chest
  * default: false
* farmplugin.bountifulyield
  * description: Allows player to use custom crop harvest yield mechanics.
  * default: true
* farmplugin.preventtrample:
  * description: Prevents farmland from being trampled when stepped on.
  * default: true

<--- Recommended Usage --->
* Assign farmplugin.admin to a LuckPerm group ex. admin, fpAdmin
* Users added to this group will be able to manage infinite chests
and manage feature enablement for bountifulyield and preventtrample