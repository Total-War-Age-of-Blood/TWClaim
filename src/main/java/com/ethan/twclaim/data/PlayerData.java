package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;
import java.util.*;

public class PlayerData implements Listener {
    public static HashMap<UUID, PlayerData> player_data_hashmap = new HashMap<>();

    // Load PlayerData into HashMap when player joins
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        File player_folder = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("TWClaim")).getDataFolder(), "PlayerData");
        String[] fileList = player_folder.list();
        // Iterate through the "Players" folder to see if player has a PlayerData file
        // If player has existing file, load it to the HashMap
        // If player does not have existing file, create new file and load to HashMap
        assert fileList != null;
        for (String file_name : fileList) {
            if (!file_name.equals(player.getUniqueId() + ".json")) {
                continue;
            }
            // System.out.println("Player has file on record.");
            File playerFile = new File(player_folder, e.getPlayer().getUniqueId() + ".json");
            try {
                // System.out.println("Putting PlayerData in HashMap.");
                FileReader playerFileReader = new FileReader(playerFile);
                PlayerData playerData = TWClaim.getGson().fromJson(playerFileReader, PlayerData.class);
                player_data_hashmap.put(player.getUniqueId(), playerData);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return;
        }

        // If the player does not have a file, make them one and load into Hashmap.
        PlayerData playerData = new PlayerData(new HashMap<>(), new ArrayList<>(), player.getDisplayName(),
                player.getUniqueId(), new HashMap<>(), "None");
        try {
            // Check to see if the necessary directories already exist.
            File playerDataFolder = new File(TWClaim.getPlugin().getDataFolder(), "PlayerData");
            if (!playerDataFolder.exists()) {
                playerDataFolder.mkdir();
            }

            File file = new File(playerDataFolder, e.getPlayer().getUniqueId() + ".json");
            if (!file.exists()) {
                file.createNewFile();
            }

            Writer writer = new FileWriter(file, false);
            TWClaim.getGson().toJson(playerData, writer);
            writer.flush();
            writer.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
    }

    // Remove PlayerData from HashMap when player leaves
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        playerData.setMode("None");
        // Save playerData to file
        try {
            File playerFolder = new File(TWClaim.getPlugin().getDataFolder(), "PlayerData");
            File playerFile = new File(playerFolder, player.getUniqueId() + ".json");
            FileWriter playerFileWriter = new FileWriter(playerFile, false);
            TWClaim.getGson().toJson(playerData, playerFileWriter);

            playerFileWriter.flush();
            playerFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player_data_hashmap.remove(player.getUniqueId());
    }

    HashMap<UUID, String> tribes;
    List<String> invites;
    String display;
    UUID uuid;
    HashMap<String, Integer[]> claimSelect = new HashMap<>();
    String mode;
    UUID target;

    // Empty constructor for when this class needs to be called empty.
    public PlayerData() {

    }

    public PlayerData(HashMap<UUID, String> tribes, List<String> invites, String display, UUID uuid,
                      HashMap<String, Integer[]> claimSelect, String mode) {
        this.tribes = tribes;
        this.invites = invites;
        this.display = display;
        this.uuid = uuid;
        this.claimSelect = claimSelect;
        this.mode = mode;
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

    public List<String> getInvites() {
        return invites;
    }

    public void setInvites(List<String> invites) {
        this.invites = invites;
    }

    public HashMap<String, Integer[]> getClaimSelect() {
        return claimSelect;
    }

    public void setClaimSelect(HashMap<String, Integer[]> claimSelect) {
        this.claimSelect = claimSelect;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public UUID getTarget() {
        return target;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }
}
