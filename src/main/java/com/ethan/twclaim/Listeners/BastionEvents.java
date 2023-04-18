package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class BastionEvents implements Listener {
    @EventHandler
    public void onBastionPlace(BlockPlaceEvent e){
        ItemStack item = e.getItemInHand();
        if (item.getItemMeta() == null){return;}
        ItemMeta itemMeta = item.getItemMeta();
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){return;}
        Bastion.createBastion(e.getBlockPlaced(), 30);
        e.getPlayer().sendMessage("Bastion Placed.");
    }
    @EventHandler
    public void bastionInteract(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if (block == null){return;}
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // Determine block is bastion
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){return;}
        // Determine bastion has owner
        Player player = e.getPlayer();
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
            player.sendMessage(ChatColor.RED + "Reinforce Bastion to activate");
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
        Bastion.playerLastBastion.put(player.getUniqueId(), e.getClickedBlock());
        Bukkit.getPluginManager().callEvent(new OpenGUI(player, "Bastion"));
        e.setCancelled(true);
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e){
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) || !e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)){return;}
        // Check if player is in range of bastion
        Player player = e.getPlayer();
        if (Bastion.inBastionRange(player) == null){return;}
        // Check if player is member of bastion
        Bastion bastion = Bastion.inBastionRange(player);
        Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        UUID owner = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
        if (Util.isTribe(owner)){
            if (Util.isInTribe(player.getUniqueId(), owner)){return;}
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Strangers cannot teleport inside bastion radius");
        } else if (!owner.equals(player.getUniqueId())){
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Strangers cannot teleport inside bastion radius");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player player = e.getPlayer();
        if (Bastion.inBastionRange(player) == null){return;}
        // Check if player is member of bastion
        Bastion bastion = Bastion.inBastionRange(player);
        Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"),PersistentDataType.STRING)){return;}
        UUID owner = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
        if (Util.isTribe(owner)){
            if (Util.isInTribe(player.getUniqueId(), owner)){return;}
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Strangers cannot place blocks inside bastion radius");
        } else if (!owner.equals(player.getUniqueId())){
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Strangers cannot place blocks inside bastion radius");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        Player player = e.getPlayer();
        if (Bastion.inBastionRange(player) == null || !player.isGliding()){return;}
        Bastion bastion = Bastion.inBastionRange(player);
        Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        UUID owner = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
        if (Util.isTribe(owner)){
            if (Util.isInTribe(player.getUniqueId(), owner)){return;}
            player.setGliding(false);
            player.sendMessage(ChatColor.RED + "Strangers cannot glide inside bastion radius");
        } else if (!owner.equals(player.getUniqueId())){
            player.setGliding(false);
            player.sendMessage(ChatColor.RED + "Strangers cannot glide inside bastion radius");
        }
    }
}
