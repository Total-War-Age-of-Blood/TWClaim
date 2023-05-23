package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class JoinTribe {
    public static boolean joinTribe(Player player, String[] args, PlayerData playerData){
        if (Util.checkTribe(args[1].toLowerCase())){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
        TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[1].toLowerCase()));
        // Check that tribe invited player
        if (!tribe.getInvites().contains(player.getUniqueId())){player.sendMessage(ChatColor.RED + "You do not have permission to join this tribe"); return false;}
        // Update the tribe and player data
        List<UUID> tribeInvites = tribe.getInvites();
        tribeInvites.remove(player.getUniqueId());
        tribe.getMembers().put(player.getUniqueId(), "Member");
        List<String> playerTribeInvites = playerData.getInvites();
        playerTribeInvites.remove(tribe.getName());
        playerData.getTribes().put(tribe.getTribeID(), tribe.getName());
        TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
        PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);

        // Message player and tribe owner
        if (Bukkit.getPlayer(tribe.getLeader()) != null){Bukkit.getPlayer(tribe.getLeader()).sendMessage(player.getDisplayName() + "joined " + tribe.getName());}
        player.sendMessage("Joined " + tribe.getName());
        return true;
    }
}
