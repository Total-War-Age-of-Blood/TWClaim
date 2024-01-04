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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BastionEvents implements Listener {

    // Informational Messages
    public static HashMap<Player, Bastion> territoryTracker = new HashMap<>();
    @EventHandler
    public void onEnterOrLeaveBastion(PlayerMoveEvent e){
        Player player = e.getPlayer();
        Bastion bastion = Bastion.inClaimRange(player.getLocation());
        if (bastion == null){
            if (territoryTracker.containsKey(player)){
                player.sendMessage("Leaving " + territoryTracker.get(player).getName() + " territory");
            }
            territoryTracker.remove(player);
            return;
        }
        if (bastion.getName() == null){return;}
        if (territoryTracker.get(player) == null){
            player.sendMessage("Entering " + bastion.getName() + " territory");
            territoryTracker.put(player, bastion);
        }
        if (!territoryTracker.get(player).equals(bastion)) {
            player.sendMessage("Entering " + bastion.getName() + " territory");
            territoryTracker.put(player, bastion);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        territoryTracker.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBastionPlace(BlockPlaceEvent e){
        ItemStack item = e.getItemInHand();
        if (item.getItemMeta() == null){return;}
        ItemMeta itemMeta = item.getItemMeta();
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){return;}
        Bastion.createBastion(e.getBlockPlaced(), TWClaim.getPlugin().getConfig().getInt("bastion-range"));
        e.getPlayer().sendMessage("Bastion Placed.");
    }
    @EventHandler
    public void bastionInteract(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if (block == null || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){return;}
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
        // Set the bastion's name if it has not been set already
        Bastion bastion = Bastion.bastions.get(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        if (bastion.getName() == null){
            Bastion.nameBastion(bastion, ownKey);
        }
        e.setCancelled(true);
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e){
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) && !e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)){return;}
        // Check if player is in range of bastion
        Player player = e.getPlayer();
        if (Bastion.inClaimRange(e.getTo()) == null){return;}
        Bastion bastion = Bastion.inClaimRange(e.getTo());
        if (!hasFuel(bastion)){return;}
        Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // Check if bastion has anti-teleport
        if (container.get(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"), PersistentDataType.INTEGER) == null){return;}
        if (container.get(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"), PersistentDataType.INTEGER) == 0){return;}
        // Check if upgrade is active
        String activeUpgrades = container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING);
        if (!activeUpgrades.contains("T")){return;}
        // Check if player is member of bastion
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
        if (Bastion.inClaimRange(e.getBlock().getLocation()) == null){return;}
        // Check if player is member of bastion
        Bastion bastion = Bastion.inClaimRange(player.getLocation());
        if (!hasFuel(bastion)){return;}
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
    public void onPlayerGlide(PlayerMoveEvent e){
        Player player = e.getPlayer();
        if (Bastion.inClaimRange(player.getLocation()) == null || !player.isGliding()){return;}
        Bastion bastion = Bastion.inClaimRange(player.getLocation());
        if (!hasFuel(bastion)){return;}
        Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // Check if bastion has anti-flight
        if (container.get(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"), PersistentDataType.INTEGER) == null){return;}
        if (container.get(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"), PersistentDataType.INTEGER) == 0){return;}
        String activeUpgrades = container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING);
        if (!activeUpgrades.contains("F")){return;}
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

    @EventHandler
    public void onEnterSurveillance(PlayerMoveEvent e){
        Player player = e.getPlayer();
        // Check if player is sneaking
        if (player.isSneaking()){return;}
        if (Bastion.inClaimRange(player.getLocation()) == null){return;}
        // Check if bastion has surveillance
        Bastion bastion = Bastion.inClaimRange(player.getLocation());
        // Check if bastion has fuel
        if (!hasFuel(bastion)){return;}
        Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        int surveillanceLevel = container.get(new NamespacedKey(TWClaim.getPlugin(), "surveillance"), PersistentDataType.INTEGER);
        String activeUpgrades = container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING);
        if (surveillanceLevel == 0 || !activeUpgrades.contains("S")){return;}
        // Check if player is member of tribe-owned bastion
        UUID owner = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
        if (Util.isTribe(owner) && Util.isInTribe(player.getUniqueId(), owner)){return;}
        // Check if player is owner of private bastion
        if (owner.equals(player.getUniqueId())){return;}
        // Give player glowing effect
        if (!bastion.getUnderSurveillance().contains(player.getUniqueId())){
            ArrayList<UUID> underSurveillance = bastion.getUnderSurveillance();
            underSurveillance.add(player.getUniqueId());
            bastion.setUnderSurveillance(underSurveillance);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 8, 0));
    }

    public static boolean hasFuel(Bastion bastion){
        Block block = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        int fuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
        return fuel > 0;
    }
}
