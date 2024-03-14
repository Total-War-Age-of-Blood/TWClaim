package com.ethan.twclaim.compatibility;


import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import nl.pim16aap2.bigDoors.Door;
import nl.pim16aap2.bigDoors.events.DoorEventTogglePrepare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/*
    The door toggle event will be canceled if the door contains any blocks reinforced to a group the player does not
    have permission to.
 */
public class BigDoorsOpener implements Listener {
    @EventHandler
    public static void onDoorToggle(DoorEventTogglePrepare event){
     Door door = event.getDoor();
     door.getPrimeOwner();
     UUID playerUUID = door.getPlayerUUID();

     Location minLoc = door.getMinimum();
     Location maxLoc = door.getMaximum();
     int minX = minLoc.getBlockX();
     int minY = minLoc.getBlockY();
     int minZ = minLoc.getBlockZ();

     int maxX = maxLoc.getBlockX();
     int maxY = maxLoc.getBlockY();
     int maxZ = maxLoc.getBlockZ();

     for (int i = minX; i <= maxX; i++){
         for (int j = minY; j <= maxY; j++){
             for (int k = minZ; k <= maxZ; k++){
                 Block block = minLoc.getWorld().getBlockAt(i, j, k);
                 PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                 if (!container.has(Util.getOwnKey(), PersistentDataType.STRING)){continue;}
                 UUID owner = UUID.fromString(container.get(Util.getOwnKey(), PersistentDataType.STRING));

                 if (Util.isTribe(owner)){
                     TribeData tribeData = TribeData.tribe_hashmap.get(owner);
                     if (!Util.isInTribe(playerUUID, owner)){
                         event.setCancelled(true);
                         Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "You lack permission to open doors claimed by strangers.");
                         return;
                     }
                     String playerGroup = tribeData.getMembers().get(playerUUID);
                     String playerPerms = tribeData.getPermGroups().get(playerGroup);
                     if (!playerPerms.contains("r")){
                         event.setCancelled(true);
                         Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "You lack permission to open doors claimed by " + tribeData.getName() + ".");
                         return;
                     }
                 } else{
                     if (playerUUID.equals(owner)){continue;}
                     event.setCancelled(true);
                     Bukkit.getPlayer(playerUUID).sendMessage(ChatColor.RED + "You lack permission to open doors claimed by strangers.");
                     return;
                 }
             }
         }
     }
    }
}
