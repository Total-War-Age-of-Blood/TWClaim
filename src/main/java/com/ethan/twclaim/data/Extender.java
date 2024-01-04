package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.google.common.reflect.TypeToken;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Extender {
    public static HashMap<UUID, Extender> extenders = new HashMap<>();
    // Tracks which extender the player last interacted with. This allows gui's to interact with bastion objects.
    public static HashMap<UUID, Block> playerLastExtender= new HashMap<>();

    public static void saveExtenders() {
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "extenders.json");
        try {
            if (!bastionsFile.exists()){bastionsFile.createNewFile();}
            FileWriter extendersWriter = new FileWriter(bastionsFile, false);
            TWClaim.getGson().toJson(extenders, extendersWriter);
            extendersWriter.flush();
            extendersWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadExtenders(){
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "extenders.json");
        try {
            if (!bastionsFile.exists()){bastionsFile.createNewFile();}
            FileReader extendersReader = new FileReader(bastionsFile);
            extenders = TWClaim.getGson().fromJson(extendersReader, new TypeToken<HashMap<UUID, Extender>>(){}.getType());
            if (extenders == null){
                extenders = new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createExtender(Block block){
        UUID uuid = UUID.randomUUID();
        int[] coordinates = new int[3];
        coordinates[0] = block.getX();
        coordinates[1] = block.getY();
        coordinates[2] = block.getZ();
        UUID worldID = block.getWorld().getUID();
        Extender extender = new Extender(uuid, coordinates, System.currentTimeMillis(), worldID);
        extenders.put(uuid, extender);

        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        container.set(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING, uuid.toString());
    }

    UUID uuid;
    UUID fatherBastion = null;
    boolean isActive = false;
    int radius = TWClaim.getPlugin().getConfig().getInt("extender-range");
    int[] coordinates;
    long timestamp;
    UUID worldID;

    public Extender(UUID uuid, int[] coordinates, long timestamp, UUID worldID){
        this.uuid = uuid;
        this.coordinates = coordinates;
        this.timestamp = timestamp;
        this.worldID = worldID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getFatherBastion() {
        return fatherBastion;
    }

    public void setFatherBastion(UUID fatherBastion) {
        this.fatherBastion = fatherBastion;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(int[] coordinates) {
        this.coordinates = coordinates;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getWorldID() {
        return worldID;
    }

    public void setWorldID(UUID worldID) {
        this.worldID = worldID;
    }
}
