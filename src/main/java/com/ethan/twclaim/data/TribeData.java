package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TribeData {
    public static HashMap<String, TribeData> tribe_hashmap = new HashMap<>();
    private final transient File tribes_folder = new File(TWClaim.getPlugin().getDataFolder(), "TribeData");

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
                File save_file = new File(tribes_folder, entry.getValue().getTribeID() + ".json");
                FileWriter file_writer = new FileWriter(save_file, false);
                TWClaim.getGson().toJson(entry.getValue(), file_writer);
                file_writer.flush();
                file_writer.close();
            }catch (IOException exception){exception.printStackTrace();}
        }
    }

    UUID tribeID;
    String name;
    UUID leader;
    // A list of tribe members. The String value is their perms group.
    HashMap<UUID, String> members;
    // The first String is the name of the perms group within the tribe. The second string will be the code for which
    // perms that group has access to. Ex: "--aob-".
    HashMap<String, String> permGroups;

    public TribeData(UUID tribeID, String name, UUID leader, HashMap<UUID, String> members, HashMap<String, String> permGroups) {
        this.tribeID = tribeID;
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.permGroups = permGroups;
    }

    public UUID getTribeID() {
        return tribeID;
    }

    public void setTribeID(UUID tribeID) {
        this.tribeID = tribeID;
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

    public HashMap<UUID, String> getMembers() {
        return members;
    }

    public void setMembers(HashMap<UUID, String> members) {
        this.members = members;
    }

    public HashMap<String, String> getPermGroups() {
        return permGroups;
    }

    public void setPermGroups(HashMap<String, String> permGroups) {
        this.permGroups = permGroups;
    }
}
