package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReinforceMode {
    public static boolean reinforceTribe(Player player, String[] args, PlayerData playerData){
        String tribeName = args[1];
        // Search for tribe in tribe hashmap return error if not found
        if (Util.checkTribe(tribeName.toLowerCase())){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
        TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribe.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of " + tribe.getName());
            return false;
        }
        // Check for permission to reinforce blocks
        String permGroup = tribe.getMembers().get(player.getUniqueId());
        String perms = tribe.getPermGroups().get(permGroup);
        if (!perms.contains("r")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
        if (playerData.getMode().equalsIgnoreCase("Reinforce")){
            playerData.setMode("None");
            player.sendMessage(ChatColor.RED + "Reinforcement Mode OFF");
        } else{
            playerData.setMode("Reinforce");
            playerData.setTarget(tribe.getTribeID());
            player.sendMessage(ChatColor.GREEN + "Reinforcement Mode ON");
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
        return true;
    }

    public static boolean reinforcePrivate(Player player, PlayerData playerData){
        if (playerData.getMode().equalsIgnoreCase("Reinforce")){
            playerData.setMode("None");
            player.sendMessage(ChatColor.RED + "Reinforcement OFF");
        } else{
            playerData.setMode("Reinforce");
            playerData.setTarget(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Reinforcement ON");
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
        return true;
    }
}
