package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TribeData {

    // Unlike PlayerData, the Tribe files will be based on Tribe name, as they can change ownership
    public static HashMap<String, TribeData> tribe_hashmap = new HashMap<>();
    private final transient File tribes_folder = new File(TWClaim.getPlugin().getDataFolder(), "Tribes");

    public TribeData() {

    }

    public void loadTribes(){
        if(!tribes_folder.exists()){return;}
        for (File file : Objects.requireNonNull(tribes_folder.listFiles())){
            try{
                FileReader file_reader = new FileReader(file);
                TribeData tribe_data = TWClaim.getGson().fromJson(file_reader, TribeData.class);
                tribe_hashmap.put(tribe_data.getName(), tribe_data);
            }catch (IOException exception){exception.printStackTrace();}
        }
    }

    public void saveTribes(){
        for (Map.Entry<String, TribeData> entry : tribe_hashmap.entrySet()){
            try {
                File save_file = new File(tribes_folder, entry.getValue().getLeader() + ".json");
                FileWriter file_writer = new FileWriter(save_file, false);
                TWClaim.getGson().toJson(entry.getValue(), file_writer);
                file_writer.flush();
                file_writer.close();
            }catch (IOException exception){exception.printStackTrace();}
        }
    }


    String name;
    UUID leader;
    List<String> allies;
    List<String> enemies;
    List<UUID> members;

    public TribeData(String name, UUID leader, List<String> allies, List<String> enemies, List<UUID> members) {
        this.name = name;
        this.leader = leader;
        this.allies = allies;
        this.enemies = enemies;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public List<String> getAllies() {
        return allies;
    }

    public void setAllies(List<String> allies) {
        this.allies = allies;
    }

    public List<String> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<String> enemies) {
        this.enemies = enemies;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }
}
