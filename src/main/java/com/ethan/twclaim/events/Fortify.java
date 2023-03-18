package com.ethan.twclaim.events;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Fortify implements Listener {
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e){
        Player player = e.getPlayer();
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        // Return if player is not in fortify mode
        if (!playerData.isFortifying()){return;}
        ArrayList<HashMap<String, Integer>> reinforcementsFromConfig = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");
        // This hashmap contains the material types and their reinforcement values
        HashMap<String, Integer> reinforcements = new HashMap<>();
        for (HashMap<String, Integer> hashMap : reinforcementsFromConfig){
            reinforcements.putAll(hashMap);
        }
        List<String> keyList = new ArrayList<>(reinforcements.keySet());
        List<Integer> valueList = new ArrayList<>(reinforcements.values());
        reinforcements.clear();
        // Make keys lowercase, so they can be matched with lowercase material types
        int n = 0;
        for (String key : keyList){
            key.toLowerCase();
            reinforcements.put(key, valueList.get(n));
            n++;
        }
        player.sendMessage(keyList.toString());

        // Cycle through inventory for first ItemStack that matches a valid reinforcement
        Inventory inventory = player.getInventory();
        int count = -1;
        for (ItemStack item : inventory.getContents()){
            count++;
            if (item == null){continue;}
            if (!keyList.contains(item.getType().toString().toLowerCase())){continue;}
            player.sendMessage("Match!");
            int reinforcement = reinforcements.get(item.getType().toString().toLowerCase());
            // If there is a match, add reinforcement to the block and delete reinforcement item from inventory
            PersistentDataContainer container = new CustomBlockData(e.getBlock(), TWClaim.getPlugin());
            NamespacedKey materialKey = new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "material");
            container.set(materialKey, PersistentDataType.STRING, item.getType().toString().toLowerCase());
            // This key keeps track of the reinforcement value of the block
            NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "reinforcement");
            container.set(key, PersistentDataType.INTEGER, reinforcement);
            // This key keeps track of the owning tribe
            NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner");
            container.set(ownKey, PersistentDataType.STRING, playerData.getFortifying().toString());

            // Remove material from inventory
            item.setAmount(item.getAmount() - 1);

            e.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, e.getBlock().getLocation(), 20);
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
            return;
        }
        // If no valid materials in inventory, send error message
        player.sendMessage(ChatColor.RED + "No valid reinforcement materials.");
        e.setCancelled(true);
    }
}
