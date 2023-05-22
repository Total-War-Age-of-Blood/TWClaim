package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveTribe {
    public static void leaveTribe(Player player, String[] args){
        // Check that the player gave the name of a tribe
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(args[1].toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Tribe does not exist");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
        // Check that player is in the tribe
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "You are not a member of this tribe");
            return;
        }
        // Check that player is not leader
        if (player.getUniqueId().equals(tribeData.getLeader())){
            player.sendMessage(ChatColor.RED + "The leader cannot leave his tribe. Oo /disband to delete your tribe");
            return;
        }
        // Remove tribe from PlayerData and player from TribeData
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        playerData.getTribes().remove(tribeData.getTribeID());
        tribeData.getMembers().remove(player.getUniqueId());

        player.sendMessage("You have left " + tribeData.getName());
    }
}