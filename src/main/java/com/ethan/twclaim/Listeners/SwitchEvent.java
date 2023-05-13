package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class SwitchEvent implements Listener {
    // When a player tries to open a door or chest or activate a redstone component that is reinforced, check if the player
    // has switch perms. Cancel the action if player does not have permission.
    // TODO make separate permissions for different kinds of switchable blocks to allow greater perms customization
  public static final  ArrayList<Material> SWITCHABLE = new ArrayList<>(Arrays.asList(Material.CHEST, Material.CHEST_MINECART,
            Material.ENDER_CHEST, Material.FURNACE, Material.FURNACE_MINECART, Material.BLAST_FURNACE,
            Material.CRAFTING_TABLE, Material.SMITHING_TABLE, Material.CARTOGRAPHY_TABLE, Material.LOOM,
            Material.STONECUTTER, Material.SMOKER, Material.ANVIL, Material.TRAPPED_CHEST, Material.BARREL,
            Material.HOPPER, Material.HOPPER_MINECART, Material.DROPPER, Material.DISPENSER, Material.BAMBOO_BUTTON,
            Material.BIRCH_BUTTON, Material.ACACIA_BUTTON, Material.CRIMSON_BUTTON, Material.JUNGLE_BUTTON,
            Material.DARK_OAK_BUTTON, Material.MANGROVE_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON,
            Material.SPRUCE_BUTTON, Material.STONE_BUTTON, Material.WARPED_BUTTON, Material.CHERRY_BUTTON,
            Material.DARK_OAK_DOOR, Material.ACACIA_DOOR, Material.OAK_DOOR, Material.BAMBOO_DOOR,
            Material.BIRCH_DOOR, Material.CRIMSON_DOOR, Material.IRON_DOOR, Material.MANGROVE_DOOR,
            Material.SPRUCE_DOOR, Material.WARPED_DOOR, Material.CHERRY_DOOR, Material.DARK_OAK_TRAPDOOR,
            Material.ACACIA_TRAPDOOR, Material.BAMBOO_TRAPDOOR, Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR, Material.IRON_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.MANGROVE_TRAPDOOR,
            Material.WARPED_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.LEVER,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE, Material.CHERRY_PRESSURE_PLATE, Material.BEACON, Material.BREWING_STAND,
            Material.LECTERN, Material.SHULKER_BOX, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.OAK_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.MANGROVE_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            Material.WARPED_FENCE_GATE, Material.CHERRY_FENCE_GATE));

    public static final  ArrayList<Material> BUTTON = new ArrayList<>(Arrays.asList(Material.BAMBOO_BUTTON,
            Material.BIRCH_BUTTON, Material.ACACIA_BUTTON, Material.CRIMSON_BUTTON, Material.JUNGLE_BUTTON,
            Material.DARK_OAK_BUTTON, Material.MANGROVE_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON,
            Material.SPRUCE_BUTTON, Material.STONE_BUTTON, Material.WARPED_BUTTON, Material.CHERRY_BUTTON));
    public static final  ArrayList<Material> CONTAINER = new ArrayList<>(Arrays.asList(Material.CHEST, Material.CHEST_MINECART,
            Material.ENDER_CHEST, Material.FURNACE, Material.FURNACE_MINECART, Material.BLAST_FURNACE,
            Material.CRAFTING_TABLE, Material.SMITHING_TABLE, Material.CARTOGRAPHY_TABLE, Material.LOOM,
            Material.STONECUTTER, Material.SMOKER, Material.ANVIL, Material.TRAPPED_CHEST, Material.BARREL,
            Material.HOPPER, Material.HOPPER_MINECART, Material.DROPPER, Material.DISPENSER, Material.BEACON,
            Material.BREWING_STAND, Material.SHULKER_BOX));

    public static final  ArrayList<Material> DOOR = new ArrayList<>(Arrays.asList(Material.DARK_OAK_DOOR, Material.ACACIA_DOOR, Material.OAK_DOOR, Material.BAMBOO_DOOR,
            Material.BIRCH_DOOR, Material.CRIMSON_DOOR, Material.IRON_DOOR, Material.MANGROVE_DOOR,
            Material.SPRUCE_DOOR, Material.WARPED_DOOR, Material.CHERRY_DOOR, Material.DARK_OAK_TRAPDOOR,
            Material.ACACIA_TRAPDOOR, Material.BAMBOO_TRAPDOOR, Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR, Material.IRON_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.MANGROVE_TRAPDOOR,
            Material.WARPED_TRAPDOOR, Material.CHERRY_TRAPDOOR));

    public static final  ArrayList <Material> PRESSURE_PLATE = new ArrayList<>(Arrays.asList(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE, Material.CHERRY_PRESSURE_PLATE));

    public static final ArrayList<Material> FENCE_GATE = new ArrayList<>(Arrays.asList(Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.OAK_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.MANGROVE_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            Material.WARPED_FENCE_GATE, Material.CHERRY_FENCE_GATE));
    @EventHandler
    public void onPlayerSwitch(PlayerInteractEvent e){
        Player player = e.getPlayer();
        // Check that block is a switchable block
        Block block = e.getClickedBlock();
        if (block == null){return;}
        Material type = block.getType();
        if(!SWITCHABLE.contains(block.getType()) || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){return;}

        // Check that block is reinforced
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        String ownerString = container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING);
        UUID owner = null;
        if (ownerString == null){
            // If the block is a door, check if there is a door block above or below that is reinforced.
            if (DOOR.contains(type)){
                if (getDoorHalf(block, type)){
                    Block bottomHalf = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                    PersistentDataContainer bottomHalfContainer = new CustomBlockData(bottomHalf, TWClaim.getPlugin());
                    ownerString = bottomHalfContainer.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING);
                    if (ownerString == null){return;}
                    owner = UUID.fromString(ownerString);

                } else {
                    Block topHalf = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
                    PersistentDataContainer topHalfContainer = new CustomBlockData(topHalf, TWClaim.getPlugin());
                    ownerString = topHalfContainer.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING);
                    if (ownerString == null){return;}
                    owner = UUID.fromString(ownerString);
                }
            }
            if (owner == null){return;}
        }
        owner = UUID.fromString(ownerString);

        // Check if player is owner or has switch perms
        if (owner.equals(player.getUniqueId())){return;}
        if (Util.isTribe(owner)){
            TribeData tribe = TribeData.tribe_hashmap.get(owner);
            if (Util.isInTribe(player.getUniqueId(), tribe.getTribeID())){
                String permGroup = tribe.getMembers().get(player.getUniqueId());
                String perms = tribe.getPermGroups().get(permGroup);
                if (perms.contains("s")){
                    return;
                }
            }
        }
        e.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Insufficient Permissions");

    }

    public static boolean getDoorHalf (Block block, Material type){
        Block above = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
        int count = 0;
        while (above.getType().equals(type)) {
            // If above is not door, then stop loop and use count to determine which half. If odd, door is bottom.
            count++;
            above = above.getWorld().getBlockAt(above.getX(), above.getY() + 1, above.getZ());
        }
        // True is top, false, is bottom
        return count % 2 == 0;
    }

}
