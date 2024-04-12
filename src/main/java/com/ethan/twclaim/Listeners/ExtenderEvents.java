package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.AutoSave;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.Extender;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.events.OpenGUI;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;


public class ExtenderEvents implements Listener {
    @EventHandler
    public void onPlaceExtender(BlockPlaceEvent e){
        ItemStack item = e.getItemInHand();
        if (item.getItemMeta() == null){return;}
        ItemMeta itemMeta = item.getItemMeta();
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "extender"), PersistentDataType.STRING)){return;}
        Extender.createExtender(e.getBlock());
        e.getPlayer().sendMessage("Extender Placed.");
        AutoSave.setChange(true);
    }

    @EventHandler
    public void onExtenderInteract(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if (block == null || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){return;}
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // Determine block is extender
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING)){return;}
        // Determine bastion has owner
        Player player = e.getPlayer();
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
            player.sendMessage(ChatColor.RED + "Reinforce Extender to activate");
            return;
        }
        UUID ownKey = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
        // Check if owner is tribe
        if (Util.isTribe(ownKey)){
            if (!Util.isInTribe(player.getUniqueId(), ownKey)){
                player.sendMessage(ChatColor.RED + "Not in tribe!");
                return;
            }
            TribeData tribeData = TribeData.tribe_hashmap.get(ownKey);
            if (!tribeData.getPermGroups().get(tribeData.getMembers().get(player.getUniqueId())).contains("b")){
                player.sendMessage(ChatColor.RED + "Insufficient Permissions");
                return;
            }
        } else{
            if (!ownKey.equals(player.getUniqueId())){
                player.sendMessage(ChatColor.RED + "Insufficient Permissions");
                return;
            }
        }
        // Open a GUI
        Extender.playerLastExtender.put(player.getUniqueId(), e.getClickedBlock());
        Bukkit.getPluginManager().callEvent(new OpenGUI(player, "Extender"));
        e.setCancelled(true);
    }
}
