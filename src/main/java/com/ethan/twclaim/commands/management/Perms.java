package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.data.AutoSave;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;


public class Perms {
    public static void createPerms(Player player, String[] args){
        if (args.length != 5){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms create [tribe] [group] [perms]");
            return;
        }
        // Check that tribe is correct
        // Command format ex: /tribe perms create nerd knight sbfkl
        String tribeName = args[2];
        String groupName = args[3];
        String permsName = args[4];
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms create [tribe] [group] [perms]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (!perms.contains("p")){
            player.sendMessage( ChatColor.RED + "Insufficient Permissions");
            return;
        }
        // Check that player is not trying to change leader perms
        if (groupName.equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot overwrite leader perms");
        }
        // Edit perms
        HashMap<String, String> permsGroups = tribeData.getPermGroups();
        List<Character> PERMS_SYMBOLS = new ArrayList<>(Arrays.asList('k', 'i', 'r', 'b', 's', 'p'));
        for (char character : permsName.toCharArray()){
            if (!PERMS_SYMBOLS.contains(character)){
                player.sendMessage(ChatColor.RED + "Incorrect perms code perms letters: " + PERMS_SYMBOLS);
                return;
            }
        }
        permsGroups.put(groupName, permsName);
        tribeData.setPermGroups(permsGroups);
        TribeData.tribe_hashmap.put(tribeData.getTribeID(), tribeData);
        player.sendMessage("Created " + groupName + " in " + tribeName + " with perms " + permsName);
        AutoSave.setChange(true);
    }

    public static void deletePerms(Player player, String[] args){
        // Check that player has ability to delete perms
        String tribeName = args[2];
        String groupName = args[3];
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase())) || args.length != 4){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms delete [tribe] [group]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (!perms.contains("p")){
            player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            return;
        }

        // Check that player is not trying to delete leader perms
        if (groupName.equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot delete leader perms");
            return;
        }
        // Check that player is not trying to delete member. Important because players will be shifted to member perms
        // if their perms group is deleted.
        if (groupName.equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot delete member perms");
            return;
        }

        // Shift affected players to default perms
        HashMap<UUID, String> members = tribeData.getMembers();
        for (UUID member : members.keySet()){
            String permsGroup = members.get(member);
            if (!permsGroup.equalsIgnoreCase(groupName)){continue;}
            members.put(member, "Member");
        }

        // Remove perms group
        HashMap<String, String> permsGroups = tribeData.getPermGroups();
        if (!permsGroups.containsKey(groupName)){
            player.sendMessage(ChatColor.RED + "Perms group does not exist");
        }
        permsGroups.remove(args[3]);
        TribeData.tribe_hashmap.put(tribeData.getTribeID(), tribeData);
        player.sendMessage("Deleted " + groupName + " from " + tribeName);
        AutoSave.setChange(true);
    }

    public static void editPerms(Player player, String[] args){
        String tribeName = args[2];
        String groupName = args[3];
        String permsName = args[4];
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms create [tribe] [group] [perms]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (!perms.contains("p")){
            player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            return;
        }

        // Check that player is not trying to change leader perms
        if (groupName.equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot edit leader perms");
        }

        // Check that group name is good
        if (!tribeData.getPermGroups().containsKey(groupName)){
            player.sendMessage(ChatColor.RED + "Invalid Group Name");
        }

        // Edit perms
        HashMap<String, String> permsGroups = tribeData.getPermGroups();
        List<Character> PERMS_SYMBOLS = new ArrayList<>(Arrays.asList('k', 'i', 'r', 'b', 's', 'p'));
        for (char character : permsName.toCharArray()){
            if (!PERMS_SYMBOLS.contains(character)){
                player.sendMessage(ChatColor.RED + "Incorrect perms code. Perms letters: " + PERMS_SYMBOLS);
                return;
            }
        }
        permsGroups.put(groupName, permsName);
        tribeData.setPermGroups(permsGroups);
        TribeData.tribe_hashmap.put(tribeData.getTribeID(), tribeData);
        player.sendMessage("Changed " + groupName + " perms to " + permsName);
        AutoSave.setChange(true);
    }

    public static void promoteDemote(Player player, String[] args){
        if (args.length != 5){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms promote [tribe] [player] [group]");
            return;
        }
        String tribeName = args[2];
        String playerName = args[3];
        String groupName = args[4];
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms promote [tribe] [player] [group]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (!perms.contains("p")){
            player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            return;
        }

        // Check that player is not trying to promote or demote from leader
        if (groupName.equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot promote/demote to/from leader perms");
        }

        // Change the player's group in TribeData
        HashMap<String, UUID> memberIds = tribeData.getMemberIds();
        if (memberIds.get(playerName.toLowerCase()) == null){
            player.sendMessage(ChatColor.RED + "Player not recognized");
            return;
        }
        HashMap<UUID, String> members = tribeData.getMembers();
        UUID affectedId = memberIds.get(playerName.toLowerCase());
        members.put(affectedId, groupName);
        tribeData.setMembers(members);
        TribeData.tribe_hashmap.put(tribeData.getTribeID(), tribeData);

        player.sendMessage("Promoted " + playerName + " to " + groupName);
        AutoSave.setChange(true);
    }
}
