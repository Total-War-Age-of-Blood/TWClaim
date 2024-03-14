package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.google.common.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Vault {
    public static HashMap<UUID, Vault> vaults = new HashMap<>();
    public static void saveVaults() {
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "vaults.json");
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

    public static void loadVaults(){
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "vaults.json");
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

    UUID uuid;
    int[] coordinates;
    int[] signCoordinates;

    public Vault(UUID uuid, int[] coordinates, int[] signCoordinates){
        this.uuid = uuid;
        this.coordinates = coordinates;
        this.signCoordinates = signCoordinates;
        vaults.put(uuid, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(int[] coordinates) {
        this.coordinates = coordinates;
    }

    public int[] getSignCoordinates() {
        return signCoordinates;
    }

    public void setSignCoordinates(int[] signCoordinates) {
        this.signCoordinates = signCoordinates;
    }
}
