package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
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
    public static HashMap<UUID, PlayerData> player_data_hashmap = new HashMap<>();

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
            try {
                System.out.println("Putting PlayerData in HashMap.");
                FileReader player_file_reader = new FileReader(player_file);
                PlayerData player_data = TWClaim.getGson().fromJson(player_file_reader, PlayerData.class);
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
        PlayerData player_data = new PlayerData(new HashMap<>(), player.getDisplayName(), player.getUniqueId());
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

            Writer writer = new FileWriter(file, false);
            TWClaim.getGson().toJson(player_data, writer);
            writer.flush();
            writer.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), player_data);
        System.out.println("New Player: Put Data in Hashmap");
    }

    // Remove PlayerData from HashMap when player leaves
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player_data_hashmap.remove(player.getUniqueId());
    }

    // A list of tribes the player belongs to, which should reduce the amount of searching through files the plugin
    // has to do.
    // TODO figure out what happens if a player is a member of two tribes with the same name. How can someone tell
    //  which tribe they are sending commands for?
    HashMap<UUID, String> tribes;
    String display;
    UUID uuid;

    // Empty constructor for when this class needs to be called empty.
    public PlayerData() {

    }

    public PlayerData(HashMap<UUID, String> tribes, String display, UUID uuid) {
        this.tribes = tribes;
    }

    public HashMap<UUID, String> getTribes() {
        return tribes;
    }

    public void setTribes(HashMap<UUID, String> tribes) {
        this.tribes = tribes;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
