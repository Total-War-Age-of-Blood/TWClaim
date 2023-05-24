package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.google.gson.Gson;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CreateTribe {
    public static boolean createTribe(Player player, String[] args, File tribeFolder, Gson gson, PlayerData playerData){
        String tribeName = args[1];
        for (TribeData tribeData : TribeData.tribe_hashmap.values()){
            if (!tribeData.getName().equalsIgnoreCase(tribeName)){continue;}
            player.sendMessage(ChatColor.RED + "A tribe with this name already exists!");
            return false;
        }

        // If everything goes well, the tribe is created
        UUID tribeID = UUID.randomUUID();
        HashMap<UUID, String> members = new HashMap();
        members.put(player.getUniqueId(), "Leader");
        HashMap<String, String> perms = new HashMap<>();
        perms.put("Leader", "kirsbp");
        perms.put("Member", "rs");
        TribeData tribe = new TribeData(tribeID, args[1], player.getUniqueId(), new HashMap<>(), members, perms, new ArrayList<>(), player.getDisplayName());
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
            TribeData.tribeConversionHashmap.put(tribe.getName().toLowerCase(), tribeID);
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);

            System.out.println("Saved data!");
            player.sendMessage("You have created " + args[1]);
        }catch(IOException e){e.printStackTrace();}
        return true;
    }
}
