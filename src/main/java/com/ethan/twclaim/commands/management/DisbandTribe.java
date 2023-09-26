package com.ethan.twclaim.commands.management;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class DisbandTribe {
    private static final File TRIBES_FOLDER = new File(TWClaim.getPlugin().getDataFolder(), "TribeData");
    private static final File PLAYER_FOLDER = new File(TWClaim.getPlugin().getDataFolder(), "PlayerData");

    public static void disbandTribe(Player player, String[] args){
        String tribeName = args[1];
        // Check that player submitted a tribe
        if (!Util.isTribe(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()))){
            player.sendMessage(ChatColor.RED + "Tribe does not exist");
            return;
        }

        TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(tribeName.toLowerCase()));

        // Check that player is leader of tribe
        if (!player.getUniqueId().equals(tribeData.getLeader())){
            player.sendMessage(ChatColor.RED + "Only the leader can disband the tribe");
        }

        // Remove the tribe from PlayerData in hashmap
        for (PlayerData playerData : PlayerData.player_data_hashmap.values()){
            playerData.getTribes().remove(tribeData.getTribeID());
            playerData.getInvites().remove(tribeName);
        }

        // Remove the tribe from hashmaps
        TribeData.tribeConversionHashmap.remove(tribeData.getName());
        TribeData.tribe_hashmap.remove(tribeData.getTribeID());

        // Remove the tribe from stored PlayerData
        for (File file : Objects.requireNonNull(PLAYER_FOLDER.listFiles())){
            try{
                FileReader file_reader = new FileReader(file);
                PlayerData playerData = TWClaim.getGson().fromJson(file_reader, PlayerData.class);
                playerData.getTribes().remove(tribeData.getTribeID());
                playerData.getInvites().remove(tribeName);
            }catch (IOException exception){exception.printStackTrace();}
        }

        // Remove the tribe in storage
        for (File file : Objects.requireNonNull(TRIBES_FOLDER.listFiles())){
            try{
                FileReader file_reader = new FileReader(file);
                TribeData tribe_data = TWClaim.getGson().fromJson(file_reader, TribeData.class);
                if (tribe_data.getTribeID().equals(tribeData.getTribeID())){
                    file.delete();
                    break;
                }
            }catch (IOException exception){exception.printStackTrace();}
        }
        player.sendMessage("You have disbanded " + tribeData.getName());
    }
}
