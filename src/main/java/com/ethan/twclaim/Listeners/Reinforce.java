package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;

public class Reinforce implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        // Cancel if event is firing for left click.
        if (e.getHand().equals(EquipmentSlot.HAND)){return;}
        // Make sure player was interacting with a block
        if (e.getClickedBlock() == null){return;}
        Player player = e.getPlayer();
        // Check that player is in reinforcement mode
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        if (!playerData.getMode().equalsIgnoreCase("Reinforce")){return;}

        Block block = e.getClickedBlock();
        // Not all blocks have persistent data containers, so we need to use the chunk's PDC.
        // To make this easier, we will use Jeff's Custom Block Data library
        // https://github.com/JEFF-Media-GbR/CustomBlockData
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // If the block has already been reinforced, prevent the player from reinforcing it again.
        if (container.has(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER)){
            player.sendMessage(ChatColor.RED + "Block is already reinforced");
            return;
        }
        // Check that the player is holding a reinforcement material
        // The material and reinforcement value are a hashmap inside an array of other hashmaps.
        ArrayList<HashMap<String, Integer>> reinforcements = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");

        // Iterate through the hash table to see if any of the entries in the config match the item in the player's hand
        for (HashMap<String, Integer> hash : reinforcements){
            for (String material : hash.keySet()){
                if (!(material.equalsIgnoreCase(player.getInventory().getItemInMainHand().getType().toString()))){continue;}
                // If a match is found, reinforce the block and return
                int reinforcement = hash.get(material);
                // This key keeps track of the reinforcement material
                NamespacedKey materialKey = new NamespacedKey(TWClaim.getPlugin(), "material");
                container.set(materialKey, PersistentDataType.STRING, player.getInventory().getItemInMainHand().getType().toString());
                // This key keeps track of the reinforcement value of the block
                NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(), "reinforcement");
                container.set(key, PersistentDataType.INTEGER, reinforcement);
                // This key keeps track of the owning tribe
                NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
                container.set(ownKey, PersistentDataType.STRING, playerData.getTarget().toString());
                // Remove 1 item from player's hand
                ItemStack item = player.getInventory().getItemInMainHand();
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(EquipmentSlot.HAND, item);
                return;
            }
        }
    }
}
