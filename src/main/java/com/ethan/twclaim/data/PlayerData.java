package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerData implements Listener {
    public static  HashMap<UUID, PlayerData> player_data_hashmap = new HashMap<>();

    // Load PlayerData into HashMap when player joins
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        File player_folder = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("TWClaim")).getDataFolder(), "PlayerData");
        String[] file_list = player_folder.list();
        boolean found_player = false;
        // Iterate through the "Players" folder to see if player has a PlayerData file
        // If player has existing file, load it to the HashMap
        // If player does not have existing file, create new file and load to HashMap
        assert file_list != null;
        for (String file_name : file_list) {
            if (!file_name.equals(player.getUniqueId() + ".json")) {
                continue;
            }
            System.out.println("Player has file on record.");
            File player_file = new File(player_folder, e.getPlayer().getUniqueId() + ".json");
            Gson gson = new Gson();
            try {
                System.out.println("Putting PlayerData in HashMap.");
                FileReader player_file_reader = new FileReader(player_file);
                PlayerData player_data = gson.fromJson(player_file_reader, PlayerData.class);
                player_data_hashmap.put(player.getUniqueId(), player_data);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            found_player = true;
        }

        if (found_player) {
            return;
        }
        // If the player does not have a file, make them one and load into Hashmap.
        PlayerData player_data = new PlayerData(false, false, "");
        try {
            // Check to see if the necessary directories already exist.
            File player_data_folder = new File(TWClaim.getPlugin().getDataFolder(), "PlayerData");
            if (!player_data_folder.exists()) {
                player_data_folder.mkdir();
            }

            File file = new File(player_data_folder, e.getPlayer().getUniqueId() + ".json");
            if (!file.exists()) {
                file.createNewFile();
            }

            Gson gson = new Gson();
            Writer writer = new FileWriter(file, false);
            gson.toJson(player_data, writer);
            writer.flush();
            writer.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), player_data);
        System.out.println("New Player: Put Data in Hashmap");
    }

    // Remove PlayerData from HashMap when player leaves
    // TODO Finish implementing saving/loading for PlayerData and TribeData
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        player_data_hashmap.remove(player.getUniqueId());
    }

    boolean in_tribe;
    boolean leader;
    String tribe;

    // Empty constructor for when this class needs to be called empty.
    public PlayerData(){

    }

    public PlayerData(boolean in_tribe, boolean leader, String tribe) {
        this.in_tribe = in_tribe;
        this.leader = leader;
        this.tribe = tribe;
    }

    public static HashMap<UUID, PlayerData> getPlayer_data_hashmap() {
        return player_data_hashmap;
    }

    public static void setPlayer_data_hashmap(HashMap<UUID, PlayerData> player_data_hashmap) {
        PlayerData.player_data_hashmap = player_data_hashmap;
    }

    public boolean isIn_tribe() {
        return in_tribe;
    }

    public void setIn_tribe(boolean in_tribe) {
        this.in_tribe = in_tribe;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public String getTribe() {
        return tribe;
    }

    public void setTribe(String tribe) {
        this.tribe = tribe;
    }
}
