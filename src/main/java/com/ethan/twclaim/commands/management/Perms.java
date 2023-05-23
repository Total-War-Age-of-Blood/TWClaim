package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;


public class Perms {
    public static void createPerms(Player player, String[] args){
        // Check that tribe is correct
        // Command format ex: /tribe perms create nerd knight sbfkl

        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(args[2].toLowerCase())) || args.length != 5){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms create [tribe] [group] [perms]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[2].toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (perms.contains("p")){
            player.sendMessage("Insufficient Permissions");
            return;
        }
        // Check that player is not trying to change leader perms
        if (args[3].equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot overwrite leader perms");
        }
        // Edit perms
        HashMap<String, String> permsGroups = tribeData.getPermGroups();
        List<Character> PERMS_SYMBOLS = new ArrayList<>(Arrays.asList('k', 'i', 'r', 'b', 's', 'p'));
        for (char character : args[4].toCharArray()){
            if (!PERMS_SYMBOLS.contains(character)){
                player.sendMessage(ChatColor.RED + "Incorrect perms code perms letters: " + PERMS_SYMBOLS);
                return;
            }
        }
        permsGroups.put(args[3], args[4]);
        tribeData.setPermGroups(permsGroups);
    }

    public static void deletePerms(Player player, PlayerData playerData, String[] args){
        // Check that player has ability to delete perms
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(args[2].toLowerCase())) || args.length != 4){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms delete [tribe] [group]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[2].toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (perms.contains("p")){
            player.sendMessage("Insufficient Permissions");
            return;
        }

        // Check that player is not trying to change leader perms
        if (args[3].equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot delete leader perms");
        }
        // Check that player is not trying to delete member. Important because players will be shifted to member perms
        // if their perms group is deleted.
        if (args[3].equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot delete member perms");
        }

        // Shift affected players to default perms
        HashMap<UUID, String> members = tribeData.getMembers();
        for (UUID member : members.keySet()){
            String permsGroup = members.get(member);
            if (!permsGroup.equalsIgnoreCase(args[3])){continue;}
            members.put(member, "Member");
        }

        // Remove perms group
        HashMap<String, String> permsGroups = tribeData.getPermGroups();
        if (!permsGroups.containsKey(args[3])){
            player.sendMessage(ChatColor.RED + "Perms group does not exist");
        }
        permsGroups.remove(args[3]);



    }

    public static void editPerms(Player player, String[] args){
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(args[2].toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms create [tribe] [group] [perms]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[2].toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (perms.contains("p")){
            player.sendMessage("Insufficient Permissions");
            return;
        }

        // Check that player is not trying to change leader perms
        if (args[3].equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot edit leader perms");
        }

        // Edit perms
        HashMap<String, String> permsGroups = tribeData.getPermGroups();
        List<Character> PERMS_SYMBOLS = new ArrayList<>(Arrays.asList('k', 'i', 'r', 'b', 's', 'p'));
        for (char character : args[4].toCharArray()){
            if (!PERMS_SYMBOLS.contains(character)){
                player.sendMessage(ChatColor.RED + "Incorrect perms code perms letters: " + PERMS_SYMBOLS);
                return;
            }
        }
        permsGroups.put(args[3], args[4]);
        tribeData.setPermGroups(permsGroups);
    }

    public static void promoteDemote(Player player, String[] args){
        // Check that player input a tribe and is a tribe member
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(args[2].toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Correct usage: /tribe perms create [tribe] [group] [member]");
            return;
        }
        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[2].toLowerCase()));
        if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){
            player.sendMessage(ChatColor.RED + "Not a member of this tribe");
            return;
        }
        // Check that the player has the ability to edit perms
        String permGroup = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(permGroup);
        if (perms.contains("p")){
            player.sendMessage("Insufficient Permissions");
            return;
        }

        // Check that player is not trying to promote or demote from leader
        if (args[3].equalsIgnoreCase("leader")){
            player.sendMessage(ChatColor.RED + "Cannot promote to leader perms");
        }
    }
}
