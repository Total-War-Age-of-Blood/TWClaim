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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

public class TribeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){return true;}
        // Variables used by multiple commands
        Player player = (Player) sender;
        PlayerData player_data = PlayerData.player_data_hashmap.get(player.getUniqueId());
        Gson gson = TWClaim.getGson();

        File tribe_folder = new File(Bukkit.getPluginManager().getPlugin("TWClaim").getDataFolder(), "Tribes");
        if (!tribe_folder.exists()){tribe_folder.mkdir();}

        if (args.length == 2 && args[0].equalsIgnoreCase("create")){

            // You cannot be the leader of multiple tribes at the tribe level, but you can be a member of as many
            // tribes as you want. People who want to rule multiple tribes can either assimilate them or add them
            // as members of a nation.
            if (player_data.isLeader()){
                player.sendMessage(ChatColor.RED + "You are already the leader of a tribe!");
                return true;
            }

            for (File tribe_file : tribe_folder.listFiles()){
                if (!tribe_file.getName().equals(args[1])){continue;}
                player.sendMessage(ChatColor.RED + "A tribe with this name already exists!");
                return true;
            }

            // If everything goes well, the tribe is created
            // TODO save tribe to file
            TribeData tribe = new TribeData(args[1], player.getUniqueId(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(Arrays.asList(player.getUniqueId())));
            File tribe_file = new File(tribe_folder, args[1] + ".json");
            try{
                if (!tribe_file.exists()){tribe_file.createNewFile();}

                // Saving new Tribe to file
                Writer writer = new FileWriter(tribe_file, false);
                gson.toJson(tribe, writer);
                writer.flush();
                writer.close();

                // Changing Founder's Player Data to reflect creating the Tribe
                player_data.setTribe(args[1]);
                player_data.setLeader(true);
                player_data.setIn_tribe(true);

                TribeData.tribe_hashmap.put(args[1], tribe);
                PlayerData.player_data_hashmap.put(player.getUniqueId(), player_data);

                System.out.println("Saved data!");
                player.sendMessage("You have created " + args[1]);
            }catch(IOException e){e.printStackTrace();}

        }

        return false;
    }
}
