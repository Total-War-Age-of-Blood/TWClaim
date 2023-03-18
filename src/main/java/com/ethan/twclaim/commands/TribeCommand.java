package com.ethan.twclaim.commands;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TribeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){return true;}
        // Variables used by multiple commands
        Player player = (Player) sender;
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        Gson gson = TWClaim.getGson();

        File tribeFolder = new File(Bukkit.getPluginManager().getPlugin("TWClaim").getDataFolder(), "TribeData");
        if (!tribeFolder.exists()){tribeFolder.mkdir();}

        if (args.length == 2 && args[0].equalsIgnoreCase("create")){

            for (TribeData tribeData : TribeData.tribe_hashmap.values()){
                if (!tribeData.getName().equalsIgnoreCase(args[1])){continue;}
                player.sendMessage(ChatColor.RED + "A tribe with this name already exists!");
                return false;
            }

            // If everything goes well, the tribe is created
            UUID tribeID = UUID.randomUUID();
            HashMap<UUID, String> members = new HashMap();
            members.put(player.getUniqueId(), "Leader");
            HashMap<String, String> perms = new HashMap<>();
            perms.put("Leader", "kirs");
            TribeData tribe = new TribeData(tribeID, args[1], player.getUniqueId(), members, perms, new ArrayList<>());
            // TODO see if the file saving can be removed
            //  since the hashmap should be saved to file when the server stops
            File tribe_file = new File(tribeFolder, tribeID + ".json");
            try{
                if (!tribe_file.exists()){tribe_file.createNewFile();}

                // Saving new Tribe to file
                Writer writer = new FileWriter(tribe_file, false);
                gson.toJson(tribe, writer);
                writer.flush();
                writer.close();

                // Changing Founder's Player Data to reflect creating the Tribe
                HashMap<UUID, String> tribes = playerData.getTribes();
                tribes.put(tribeID, tribe.getName());
                playerData.setTribes(tribes);


                TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
                TribeData.tribeConversionHashmap.put(tribe.getName(), tribeID);
                PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);

                System.out.println("Saved data!");
                player.sendMessage("You have created " + args[1]);
            }catch(IOException e){e.printStackTrace();}
            return true;
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("add")){
            // Search for tribe in tribe hashmap return error if not found
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0]));
            // Check for permission to invite players to this tribe
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("i")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            // Check if player is online
            for (PlayerData invited : PlayerData.player_data_hashmap.values()){
                if (!invited.getDisplay().equalsIgnoreCase(args[2])){continue;}
                // Add tribe name to player file
                List<String> invites = invited.getInvites();
                if (invites.contains(args[0])){
                    player.sendMessage(ChatColor.RED + "Player has already been invited to this group!");
                    return false;
                }
                invites.add(args[0]);
                invited.setInvites(invites);
                // Add player uuid to tribe file
                List<UUID> tribeInvites = tribe.getInvites();
                tribeInvites.add(invited.getUuid());

                // Update hashmaps
                PlayerData.player_data_hashmap.put(invited.getUuid(), invited);
                TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
                TribeData.tribeConversionHashmap.put(tribe.getName(), tribe.getTribeID());
                player.sendMessage("Invited " + args[2] + "to " + args[0]);
                Player invitedPlayer = Bukkit.getPlayer(invited.getUuid());
                invitedPlayer.sendMessage("You have been invited to " + args[0] + ". Use /tribe join " + args[0] + " to join.");
                return true;
            }
            // If the player is offline, search player files
            for (File invitedFile : new File (TWClaim.getPlugin().getDataFolder(), "PlayerData").listFiles()){
                try {
                    Reader invitedFileReader = new FileReader(invitedFile);
                    PlayerData invitedData = gson.fromJson(invitedFileReader, PlayerData.class);
                    if (!invitedData.getDisplay().equalsIgnoreCase(args[2])){continue;}
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
                    player.sendMessage("Invited " + args[2] + " to " + args[0]);
                    return true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // If not found in player files, return error
            player.sendMessage(ChatColor.RED + "Player does not exist!");
            return false;
        }

        // TODO kick member from tribe
        if (args.length == 3 && args[1].equalsIgnoreCase("kick")) {
            // Search for tribe in tribe hashmap
        }
        // TODO claim inspect

        // TODO claim individual blocks for tribe
        // TODO implement private modes for Reinforce and Fortify
        if (args.length == 2 && args[1].equalsIgnoreCase("claim")){
            // Search for tribe in tribe hashmap return error if not found
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0]));
            // Check for permission to reinforce blocks
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("r")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            if (playerData.isReinforcing()){
                playerData.setReinforcing(false);
                player.sendMessage(ChatColor.RED + "Reinforcement Mode OFF");
            } else{
                playerData.setReinforcing(true);
                playerData.setReinforcing(tribe.getTribeID());
                player.sendMessage(ChatColor.GREEN + "Reinforcement Mode ON");
            }
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("fortify")){
            // Search for tribe in tribe hashmap return error if not found
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0]));
            // Check for permission to reinforce blocks
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("r")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            if (playerData.isFortifying()){
                playerData.setFortifying(false);
                player.sendMessage(ChatColor.RED + "Fortify Mode OFF");
            } else{
                playerData.setFortifying(true);
                playerData.setFortifying(tribe.getTribeID());
                player.sendMessage(ChatColor.GREEN + "Fortify Mode ON");
            }
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
            return true;
        }

        // TODO claim area for tribe

        // TODO tribe info

        // TODO claims map

        // TODO create perms group

        // TODO change perms group perms

        return false;
    }
}
