package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.ethan.twclaim.util.Util.bastionItem;

public class BreakReinforcement implements Listener {
    NamespacedKey materialKey = new NamespacedKey(TWClaim.getPlugin(), "material");
    NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(), "reinforcement");
    NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        // TODO if block is door and not reinforced, look for reinforcement on other door block and subtract from there
        Player player = e.getPlayer();
        Block block = e.getBlock();
        // Get the block's persistent data container
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // Check that block is reinforced
        if (!container.has(key, PersistentDataType.INTEGER) || !container.has(ownKey, PersistentDataType.STRING)){
            // If block is door, do an investigation
            if (!SwitchEvent.DOOR.contains(block.getType())){return;}
            if (SwitchEvent.getDoorHalf(block, block.getType())){
                block = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
            }else {
                block = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
            }
            container = new CustomBlockData(block, TWClaim.getPlugin());
            if (!container.has(key, PersistentDataType.INTEGER )|| !container.has(ownKey, PersistentDataType.STRING)){return;}
        }
        // Check if player has permission to break the block. First, check if player is member of the tribe that owns
        // the block. Then, check if the player has "break" in their permission string.
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        UUID owner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
        int reinforcement = container.get(key, PersistentDataType.INTEGER);
        ArrayList<HashMap<String, Integer>> reinforcements = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");
        if ((playerData.getTribes().containsKey(owner))){
            System.out.println("Tribe owner");
            TribeData tribe = TribeData.tribe_hashmap.get(owner);
            String permsGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permsGroup);
            if (perms.contains("r")){
                // Get material and reinforcement points from the config
                for (HashMap<String, Integer> hash : reinforcements){
                    for (String material : hash.keySet()){
                        if (!(material.equalsIgnoreCase(container.get(materialKey, PersistentDataType.STRING)))){
                            continue;
                        }
                        // When we find the material, get its key and divide the block's current reinforcement
                        int configReinforcement = hash.get(material);
                        // If it is above the percentage, drop the material as well as the block
                        if (reinforcement / configReinforcement * 100 >= (int) TWClaim.getPlugin().getConfig().get("recover-min")){
                            ItemStack item = new ItemStack(Material.matchMaterial(material));
                            player.getWorld().dropItem(block.getLocation(), item);
                        }
                        // Remove reinforcement keys from block data
                        Util.removeReinforcement(container, materialKey, key, ownKey,e);
                        return;
                    }
                    // Remove reinforcement keys from block data
                    Util.removeReinforcement(container, materialKey, key, ownKey, e);
                }
                return;
            }
        } else if (player.getUniqueId().equals(owner)){
            // Get material and reinforcement points from the config
            for (HashMap<String, Integer> hash : reinforcements){
                for (String material : hash.keySet()){
                    if (!(material.equalsIgnoreCase(container.get(materialKey, PersistentDataType.STRING)))){
                        continue;
                    }
                    // When we find the material, get its key and divide the block's current reinforcement
                    int configReinforcement = hash.get(material);
                    // If it is above the percentage, drop the material as well as the block
                    if (reinforcement / configReinforcement * 100 >= (int) TWClaim.getPlugin().getConfig().get("recover-min")){
                        ItemStack item = new ItemStack(Material.matchMaterial(material));
                        player.getWorld().dropItem(block.getLocation(), item);
                    }
                    // Remove reinforcement keys from block data
                    Util.removeReinforcement(container, materialKey, key, ownKey, e);
                    return;
                }
            }
            // Remove reinforcement keys from block data
            Util.removeReinforcement(container, materialKey, key, ownKey, e);
            return;
        }
        // If block is reinforced, cancel the event and lower reinforcement by 1.
        if (reinforcement - 1 <= 0){
            Util.removeReinforcement(container, materialKey, key, ownKey, e);
            return;
        }
        e.setCancelled(true);
        container.set(key, PersistentDataType.INTEGER, reinforcement - 1);
        player.sendMessage("This block is reinforced");
        e.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation(), 20);
        e.getPlayer().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e){
        List<Block> blockList = e.blockList();
        for (Block block : blockList){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){continue;}
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                blockList.remove(block);
                block.setType(Material.AIR);
                blockList.add(block);
                ItemStack bastionItem = bastionItem();
                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), bastionItem);
            }
            int reinforcement = container.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            if (explosionDamage > reinforcement){
                Util.removeReinforcement(container, materialKey, key, ownKey, e);
                continue;
            }
            blockList.remove(block);
            container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement - explosionDamage);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e){
        List<Block> blockList = e.blockList();
        for (Block block : blockList){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){continue;}
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                blockList.remove(block);
                block.setType(Material.AIR);
                blockList.add(block);
                ItemStack bastionItem = bastionItem();
                e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), bastionItem);
            }
            int reinforcement = container.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            if (explosionDamage > reinforcement){
                Util.removeReinforcement(container, materialKey, key, ownKey, e);
                continue;
            }
            blockList.remove(block);
            container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement - explosionDamage);
        }
    }
}
