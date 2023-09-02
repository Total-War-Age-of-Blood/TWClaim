package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class AddMember {
    public static boolean addMember(Player player, String[] args, Gson gson){
// Search for tribe in tribe hashmap return error if not found
        String playerName = args[1];
        String tribeName = args[2];
        if (Util.checkTribe(tribeName.toLowerCase())){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
        TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        // Check for permission to invite players to this tribe
        String permGroup = tribe.getMembers().get(player.getUniqueId());
        String perms = tribe.getPermGroups().get(permGroup);
        if (!perms.contains("i")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
        // Check if player is already in tribe
        if (tribe.getMemberIds().containsKey(playerName.toLowerCase())){player.sendMessage(ChatColor.RED + "Player already in tribe."); return false;}
        // Check if player is online
        for (PlayerData invited : PlayerData.player_data_hashmap.values()){
            if (!invited.getDisplay().equalsIgnoreCase(playerName)){continue;}
            // Add tribe name to player file
            List<String> invites = invited.getInvites();
            if (invites.contains(tribeName)){
                player.sendMessage(ChatColor.RED + "Player has already been invited to this group!");
                return false;
            }
            invites.add(tribeName);
            invited.setInvites(invites);
            // Add player uuid to tribe file
            List<UUID> tribeInvites = tribe.getInvites();
            tribeInvites.add(invited.getUuid());

            // Update hashmaps
            PlayerData.player_data_hashmap.put(invited.getUuid(), invited);
            TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
            TribeData.tribeConversionHashmap.put(tribe.getName().toLowerCase(), tribe.getTribeID());
            player.sendMessage("Invited " + playerName + " to " + tribeName);
            Player invitedPlayer = Bukkit.getPlayer(invited.getUuid());
            invitedPlayer.sendMessage("You have been invited to " + tribeName + ". Use /tribe join " + tribeName + " to join.");
            return true;
        }
        // If the player is offline, search player files
        for (File invitedFile : new File (TWClaim.getPlugin().getDataFolder(), "PlayerData").listFiles()){
            try {
                Reader invitedFileReader = new FileReader(invitedFile);
                PlayerData invitedData = gson.fromJson(invitedFileReader, PlayerData.class);
                if (!invitedData.getDisplay().equalsIgnoreCase(playerName)){continue;}
                // Found invited player's file.
                // Add invites to player and tribe files.
                List<String> invites = invitedData.getInvites();
                if (invites.contains(tribe.getName())){
                    player.sendMessage(ChatColor.RED + "Player has already been invited to this group!");
                    return false;
                }
                invites.add(tribe.getName());
                invitedData.setInvites(invites);

                List<UUID> tribeInvites = tribe.getInvites();
                tribeInvites.add(invitedData.getUuid());
                tribe.setInvites(tribeInvites);

                Writer invitedFileWriter = new FileWriter(invitedFile, false);
                gson.toJson(invitedData, invitedFileWriter);
                invitedFileWriter.flush();
                invitedFileWriter.close();
                player.sendMessage("Invited " + playerName + " to " + tribeName);
                return true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // If not found in player files, return error
        player.sendMessage(ChatColor.RED + "Player does not exist!");
        return false;
    }
}
