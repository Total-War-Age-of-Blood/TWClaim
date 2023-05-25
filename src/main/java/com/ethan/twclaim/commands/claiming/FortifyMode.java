package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

public class FortifyMode {
    public static boolean fortifyTribe(Player player, String[] args, PlayerData playerData){
        String tribeName = args[1];
        // Search for tribe in tribe hashmap return error if not found
        if (Util.checkTribe(tribeName.toLowerCase())){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
        if (TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()) == null){
            player.sendMessage(ChatColor.RED + "Tribe does not exist!");
            return false;
        }
        TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        System.out.println(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        System.out.println(TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase())));
        System.out.println(tribe);
        // Check for permission to reinforce blocks
        String permGroup = tribe.getMembers().get(player.getUniqueId());
        System.out.println(tribe.getMembers());
        System.out.println(permGroup);
        String perms = tribe.getPermGroups().get(permGroup);
        System.out.println(perms);
        if (!perms.contains("r")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
        if (playerData.getMode().equalsIgnoreCase("Fortify")){
            playerData.setMode("None");
            player.sendMessage(ChatColor.RED + "Fortify Mode OFF");
        } else{
            playerData.setMode("Fortify");
            playerData.setTarget(tribe.getTribeID());
            player.sendMessage(ChatColor.GREEN + "Fortify Mode ON");
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
        return true;
    }

    public static boolean fortifyPrivate(Player player, PlayerData playerData){
        if (playerData.getMode().equalsIgnoreCase("Fortify")){
            playerData.setMode("None");
            player.sendMessage(ChatColor.RED + "Fortify OFF");
        } else{
            playerData.setMode("Fortify");
            playerData.setTarget(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Fortify ON");
        }
        return true;
    }
}
