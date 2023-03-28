package com.ethan.twclaim.events;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.util.Util;
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
        if (!playerData.getMode().equalsIgnoreCase("Fortify")){return;}
        HashMap<String, Integer> reinforcements = Util.getReinforcementTypes();

        // Cycle through inventory for first ItemStack that matches a valid reinforcement
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()){
            if (item == null){continue;}
            if (!reinforcements.containsKey(item.getType().toString().toLowerCase())){continue;}
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
            container.set(ownKey, PersistentDataType.STRING, playerData.getTarget().toString());

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
