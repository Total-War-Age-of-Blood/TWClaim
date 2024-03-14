package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.google.common.reflect.TypeToken;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Vault implements Listener {
    public static HashMap<UUID, Bastion> vaults = new HashMap<>();
    public static void saveBastions() {
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "bastions.json");
        try {
            if (!bastionsFile.exists()){bastionsFile.createNewFile();}
            FileWriter bastionsWriter = new FileWriter(bastionsFile, false);
            TWClaim.getGson().toJson(vaults, bastionsWriter);
            bastionsWriter.flush();
            bastionsWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadBastions(){
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "bastions.json");
        try {
            if (!bastionsFile.exists()){bastionsFile.createNewFile();}
            FileReader bastionsReader = new FileReader(bastionsFile);
            vaults = TWClaim.getGson().fromJson(bastionsReader, new TypeToken<HashMap<UUID, Bastion>>(){}.getType());
            if (vaults == null){
                vaults = new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
