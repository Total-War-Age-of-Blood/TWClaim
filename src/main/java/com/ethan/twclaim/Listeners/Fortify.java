package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

        // Check block is not in range of a foreign bastion that has fuel
        Block block = e.getBlock();
        Bastion bastion = Bastion.inClaimRange(block.getLocation());
        if (bastion != null && BastionEvents.hasFuel(bastion)){
            Block bastionBlock = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
            PersistentDataContainer bastionContainer = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
            UUID bastionOwner = UUID.fromString(bastionContainer.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
            if (Util.isTribe(bastionOwner)){
                TribeData tribeData = TribeData.tribe_hashmap.get(bastionOwner);
                if (!tribeData.getMembers().containsKey(player.getUniqueId())){
                    player.sendMessage(ChatColor.RED + "Cannot fortify blocks in foreign bastion zone");
                    return;
                }
            } else {
                if (!bastionOwner.equals(player.getUniqueId())){
                    player.sendMessage(ChatColor.RED + "Cannot fortify blocks in foreign bastion zone");
                    return;
                }
            }
        }

        // Cycle through inventory for first ItemStack that matches a valid reinforcement
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()){
            if (item == null){continue;}
            if (!reinforcements.containsKey(item.getType().toString().toLowerCase())){continue;}
            // If there is a match, add reinforcement to the block and delete reinforcement item from inventory
            Util.addReinforcement(block, item, playerData, e.getItemInHand());
            // Remove material from inventory
            item.setAmount(item.getAmount() - 1);
            // Effects
            e.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, e.getBlock().getLocation(), 20);
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
            return;
        }
        // If no valid materials in inventory, send error message
        player.sendMessage(ChatColor.RED + "No valid reinforcement materials.");
        e.setCancelled(true);
    }
}
