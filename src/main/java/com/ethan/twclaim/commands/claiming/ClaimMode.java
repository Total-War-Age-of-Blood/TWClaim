package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ClaimMode {
    // TODO store info for other arguments such as not claiming air blocks
    public static boolean claimTribe(Player player, String[] args, PlayerData playerData){
        // Check if tribe exists
        if (!TribeData.tribeConversionHashmap.containsKey(args[0])){
            player.sendMessage(ChatColor.RED + "This tribe does not exist!");
            return false;
        }
        // Check if player is in tribe
        if (!Util.isInTribe(player.getUniqueId(), TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase())).getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe!");
            return false;
        }
        // Check player perms
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
        if (!tribeData.getPermGroups().get(tribeData.getMembers().get(player.getUniqueId())).contains("r")){
            player.sendMessage(ChatColor.RED + "Insufficient Permissions");
        }

        // Activate claiming mode
        if (playerData.getMode().equalsIgnoreCase("Claim")){
            playerData.setMode("None");
            playerData.setTarget(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Claiming Mode OFF");
        } else {
            playerData.setMode("Claim");
            playerData.setTarget(tribeData.getTribeID());
            player.sendMessage(ChatColor.GREEN + "Claiming Mode ON");
        }
        return true;
    }

    public static boolean claimPrivate(Player player, PlayerData playerData){
        // Activate claiming mode
        if (playerData.getMode().equalsIgnoreCase("Claim")){
            playerData.setMode("None");
            playerData.setTarget(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Claiming Mode OFF");
        } else {
            playerData.setMode("Claim");
            playerData.setTarget(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Claiming Mode ON");
        }
        return true;
    }
}
