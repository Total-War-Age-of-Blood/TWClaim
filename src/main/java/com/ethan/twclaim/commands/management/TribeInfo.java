package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TribeInfo {
    public static boolean tribeInfo(Player player, String[] args){
        // Check if tribe exists
        if (!TribeData.tribeConversionHashmap.containsKey(args[0])){
            player.sendMessage(ChatColor.RED + "This tribe does not exist!");
            return false;
        }
        // Check if player is member of tribe
        TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[1].toLowerCase()));
        List<String> message = new ArrayList<>();
        if (!Util.isInTribe(player.getUniqueId(), tribe.getTribeID())){
            message.add("Owner: " + Bukkit.getOfflinePlayer(player.getUniqueId()));
            HashMap<UUID, String> members = tribe.getMembers();
            members.remove(tribe.getLeader());
            message.add("Members: " + members.values());
            return true;
        }
        message.add("Owner: " + Bukkit.getOfflinePlayer(player.getUniqueId()));
        HashMap<UUID, String> members = tribe.getMembers();
        members.remove(tribe.getLeader());
        StringBuilder membersString = new StringBuilder("Members: ");
        for (UUID member : members.keySet()){
            OfflinePlayer memberPlayer = Bukkit.getPlayer(member);
            membersString.append(members.get(member)).append(memberPlayer.getName()).append(", ");
        }
        message.add(membersString.toString());
        return true;
    }
}
