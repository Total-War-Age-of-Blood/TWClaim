package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.AutoSave;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class KickMember {
    public static boolean kickMember(Player player, String[] args, Gson gson){
        String playerName = args[1];
        String tribeName = args[2];
        // Search for tribe in tribe hashmap
        if (Util.checkTribe(tribeName.toLowerCase())){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
        TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        // Check for permission to kick players
        String permGroup = tribe.getMembers().get(player.getUniqueId());
        String perms = tribe.getPermGroups().get(permGroup);
        if (!perms.contains("k")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
        UUID kickedUUID = null;
        // Get PlayerData for kicked player
        if (Bukkit.getPlayerExact(playerName) != null){
            // Player is online
            PlayerData kickedData = PlayerData.player_data_hashmap.get(Bukkit.getPlayerExact(playerName).getUniqueId());
            kickedUUID = kickedData.getUuid();
            // Check that kicked player is in the tribe
            if (!tribe.getMembers().containsKey(kickedData.getUuid())){player.sendMessage(ChatColor.RED + "Player is not in tribe!"); return false;}
            // Make changes
            HashMap<UUID, String> tribes = kickedData.getTribes();
            tribes.remove(tribe.getTribeID());
            kickedData.setTribes(tribes);
            PlayerData.player_data_hashmap.put(kickedUUID, kickedData);
            Player kickedPlayer = Bukkit.getPlayer(kickedData.getUuid());
            kickedPlayer.sendMessage("You have been kicked from " + tribe.getName());
        } else {
            // Player is offline. Get object from files.
            for (File invitedFile : new File (TWClaim.getPlugin().getDataFolder(), "PlayerData").listFiles()) {
                try {
                    Reader kickedFileReader = new FileReader(invitedFile);
                    PlayerData potentialPlayer = gson.fromJson(kickedFileReader, PlayerData.class);
                    if (!potentialPlayer.getDisplay().equalsIgnoreCase(args[2])) {
                        continue;
                    }
                    // Found invited player's file. Make changes then write to file.
                    kickedUUID = potentialPlayer.getUuid();
                    HashMap<UUID, String> tribes = potentialPlayer.getTribes();
                    tribes.remove(tribe.getTribeID());
                    potentialPlayer.setTribes(tribes);

                    Writer kickedFileWriter = new FileWriter(invitedFile, false);
                    gson.toJson(potentialPlayer, kickedFileWriter);
                    kickedFileWriter.flush();
                    kickedFileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Change the tribe hashmap to reflect kicking
        HashMap<UUID, String> members = tribe.getMembers();
        if (kickedUUID == null){player.sendMessage(ChatColor.RED + "Player does not exist!"); return false;}
        members.remove(kickedUUID);
        HashMap<String, UUID> memberIds = tribe.getMemberIds();
        memberIds.remove(playerName.toLowerCase());
        tribe.setMemberIds(memberIds);
        TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
        // Send kicker confirmation message
        player.sendMessage("Kicked " + playerName + " from " + tribeName);
        AutoSave.setChange(true);
        return true;
    }
}
