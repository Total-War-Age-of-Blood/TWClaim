package com.ethan.twclaim.commands;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
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

            for (File tribe_file : tribeFolder.listFiles()){
                if (!tribe_file.getName().equals(args[1])){continue;}
                player.sendMessage(ChatColor.RED + "A tribe with this name already exists!");
                return true;
            }

            // If everything goes well, the tribe is created
            UUID tribeID = UUID.randomUUID();
            HashMap<UUID, String> members = new HashMap();
            members.put(player.getUniqueId(), "Leader");
            TribeData tribe = new TribeData(tribeID, args[1], player.getUniqueId(), members, new HashMap());
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
                tribes.put(tribeID, args[1]);
                playerData.setTribes(tribes);

                TribeData.tribe_hashmap.put(args[1], tribe);
                PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);

                System.out.println("Saved data!");
                player.sendMessage("You have created " + args[1]);
            }catch(IOException e){e.printStackTrace();}

        }

        // TODO add member to tribe.
        //  This is tricky because the player inviting needs to specify which tribe they are inviting to.
        if (args.length == 2 && args[0].equalsIgnoreCase("add")){
            // Try to find the player being added. Search first from online players.
            // Then search files if that didn't work.

            // If found, change the tribe and player data to reflect addition

            // Send message to both players
        }

        // TODO kick member from tribe

        // TODO claim inspect

        // TODO claim individual blocks for tribe

        // TODO claim area for tribe

        // TODO tribe info

        // TODO claims map

        // TODO create perms group

        // TODO change perms group perms

        return false;
    }
}
