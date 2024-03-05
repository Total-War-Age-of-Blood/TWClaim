package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.util.Util;
import com.ethan.twclaim.data.TribeData;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
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

import java.util.HashMap;
import java.util.UUID;

public class Reinforce implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        // Cancel if event is firing for left click.
        if (e.getHand() == null || e.getItem() == null){return;}
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){return;}

        // Make sure player was interacting with a block
        if (e.getClickedBlock() == null){return;}
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String itemName = item.getType().toString().toLowerCase();
        // Check that player is in reinforcement mode
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        if (!playerData.getMode().equalsIgnoreCase("Reinforce")){return;}


        Block block = e.getClickedBlock();
        // Not all blocks have persistent data containers, so we need to use the chunk's PDC.
        // To make this easier, we will use Jeff's Custom Block Data library
        // https://github.com/JEFF-Media-GbR/CustomBlockData
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // If the block has already been reinforced, prevent the player from reinforcing it again.
        if (container.has(Util.getKey(), PersistentDataType.INTEGER) || container.has(Util.getBreakCount(), PersistentDataType.INTEGER)){
            player.sendMessage(ChatColor.RED + "Block is already reinforced");
            return;
        }
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
        System.out.println("ReinforceAmount: " + item.getAmount());
        // Validate material
        HashMap<String, Integer> reinforcements = Util.getReinforcementTypes();
        if (reinforcements.containsKey(itemName)){
            Util.addReinforcement(block, item, playerData);
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItem(EquipmentSlot.HAND, item);
            e.setCancelled(true);
        } else{
            player.sendMessage(ChatColor.RED + "Not a reinforcement material");
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
