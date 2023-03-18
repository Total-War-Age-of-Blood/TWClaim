package com.ethan.twclaim.events;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BreakReinforcement implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player player = e.getPlayer();
        Block block = e.getBlock();
        // Get the block's persistent data container
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        NamespacedKey materialKey = new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "material");
        NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "reinforcement");
        NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner");
        // Check that block is reinforced
        if (!container.has(key, PersistentDataType.INTEGER) || !container.has(ownKey, PersistentDataType.STRING)){return;}
        // Check if player has permission to break the block. First, check if player is member of the tribe that owns
        // the block. Then, check if the player has "break" in their permission string.
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        UUID owner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
        int reinforcement = container.get(key, PersistentDataType.INTEGER);
        ArrayList<HashMap<String, Integer>> reinforcements = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");
        if ((playerData.getTribes().containsKey(owner))){
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
                        Util.removeReinforcement(container, materialKey, key, ownKey);
                        return;
                    }
                    // Remove reinforcement keys from block data
                    Util.removeReinforcement(container, materialKey, key, ownKey);
                }
                return;
            }
        } else if (player.getUniqueId().equals(owner)){
            // Get material and reinforcement points from the config
            for (HashMap<String, Integer> hash : reinforcements){
                for (String material : hash.keySet()){
                    if (!(material.equalsIgnoreCase(materialKey.getKey()))){
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
                    Util.removeReinforcement(container, materialKey, key, ownKey);
                    return;
                }
            }
            // Remove reinforcement keys from block data
            Util.removeReinforcement(container, materialKey, key, ownKey);
            return;
        }
        // If block is reinforced, cancel the event and lower reinforcement by 1.
        if (reinforcement - 1 <= 0){
            Util.removeReinforcement(container, materialKey, key, ownKey);
            return;
        }
        e.setCancelled(true);
        container.set(key, PersistentDataType.INTEGER, reinforcement - 1);
        player.sendMessage("This block is reinforced");
        e.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation(), 20);
        e.getPlayer().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
    }
}
