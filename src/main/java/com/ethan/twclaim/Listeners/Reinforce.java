package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.Vault;
import com.ethan.twclaim.util.Util;
import com.ethan.twclaim.data.TribeData;
import com.jeff_media.customblockdata.CustomBlockData;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Reinforce implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        // Cancel if event is firing for left click.
        if (e.getHand() == null){return;}
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){return;}

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

        // Check block is not in range of a foreign bastion that has fuel
        Bastion bastion = Bastion.inClaimRange(block.getLocation());
        if (bastion != null && BastionEvents.hasFuel(bastion)){
            Block bastionBlock = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
            PersistentDataContainer bastionContainer = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
            UUID bastionOwner = UUID.fromString(bastionContainer.get(Util.getOwnKey(), PersistentDataType.STRING));
            if (Util.isTribe(bastionOwner)){
                TribeData tribeData = TribeData.tribe_hashmap.get(bastionOwner);
                if (!tribeData.getMembers().containsKey(player.getUniqueId())){
                    player.sendMessage(ChatColor.RED + "Cannot reinforce blocks in foreign bastion zone");
                    return;
                }
            } else {
                if (!bastionOwner.equals(player.getUniqueId())){
                    player.sendMessage(ChatColor.RED + "Cannot reinforce blocks in foreign bastion zone");
                    return;
                }
            }
        }

        if (e.getItem() == null){
            if (container.has(Util.getKey(), PersistentDataType.INTEGER) || container.has(Util.getBreakCount(), PersistentDataType.INTEGER)){return;}

            // Check vaults for valid reinforcement
            ItemStack item;
            HashMap<Vault, ItemStack> vaultPair = Vault.checkVaults(player);
            if (vaultPair == null){
                ItemStack inventoryItem = Util.findReinforcement(player.getInventory());
                if (inventoryItem == null){
                    // If no valid materials in inventory, send error message
                    player.sendMessage(ChatColor.RED + "No valid reinforcement materials.");
                    e.setCancelled(true);
                    return;
                } else {
                    item = inventoryItem;
                    // If there is a match, add reinforcement to the block and delete reinforcement item from inventory
                    Util.addReinforcement(block, item.getType(), playerData);
                }
            } else {
                List<ItemStack> itemList = new ArrayList<>(vaultPair.values());
                List<Vault> vaultList = new ArrayList<>(vaultPair.keySet());
                Vault vault = vaultList.get(0);
                item = itemList.get(0);

                Util.addReinforcement(block, item.getType(), playerData, vault);
            }

            // Remove material from inventory
            item.setAmount(item.getAmount() - 1);
            // Effects
            e.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation(), 20);
            e.getPlayer().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
        } else{
            ItemStack item = player.getInventory().getItemInMainHand();
            String itemName = item.getType().toString().toLowerCase();

            // If the block has already been reinforced, prevent the player from reinforcing it again.
            if (container.has(Util.getKey(), PersistentDataType.INTEGER) || container.has(Util.getBreakCount(), PersistentDataType.INTEGER)){
                player.sendMessage(ChatColor.RED + "Block is already reinforced");
                return;
            }
            // Validate material
            HashMap<String, Integer> reinforcements = Util.getReinforcementTypes();
            if (reinforcements.containsKey(itemName)){
                Util.addReinforcement(block, item.getType(), playerData);
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(EquipmentSlot.HAND, item);
                e.setCancelled(true);
                e.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation(), 20);
                e.getPlayer().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
            } else{
                player.sendMessage(ChatColor.RED + "Not a reinforcement material");
            }
        }
    }

    // If player is reinforcing and is holding reinforcement material, cancel block placement.
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player player = e.getPlayer();
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        if (!playerData.getMode().equalsIgnoreCase("Reinforce")){return;}
        ItemStack item = e.getItemInHand();
        HashMap<String, Integer> reinforcements = Util.getReinforcementTypes();
        if (reinforcements.containsKey(item.getType().toString().toLowerCase())){
            e.setCancelled(true);
        }
    }
}
