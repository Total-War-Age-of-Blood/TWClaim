package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.google.common.reflect.TypeToken;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Bastion implements Listener {
    // TODO implement bastion power system and power taps
    public static HashMap<UUID, Bastion> bastions = new HashMap<>();
    // Tracks which bastion the player last interacted with. This allows gui's to interact with bastion objects.
    public static HashMap<UUID, Block> playerLastBastion = new HashMap<>();

    public static void saveBastions() {
        File bastionsFile = new File(TWClaim.getPlugin().getDataFolder(), "bastions.json");
        try {
            if (!bastionsFile.exists()){bastionsFile.createNewFile();}
            FileWriter bastionsWriter = new FileWriter(bastionsFile, false);
            TWClaim.getGson().toJson(bastions, bastionsWriter);
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
            bastions = TWClaim.getGson().fromJson(bastionsReader, new TypeToken<HashMap<UUID, Bastion>>(){}.getType());
            if (bastions == null){
                bastions = new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // information that makes up a bastion
    UUID uuid;
    int[] coordinates;
    int radius;

    public Bastion(UUID uuid, int[] coordinates, int radius){
        this.uuid = uuid;
        this.coordinates = coordinates;
        this.radius = radius;
    }
    // A Bastion is a block with a PDC tag that, when placed, prevents players from teleporting (with items), claiming, placing blocks, and flying
    // Bastions will have a custom crafting recipe
    // Bastions will have durability/power requirement.

    public static void createBastion(Block block, int radius){
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        UUID uuid = UUID.randomUUID();
        container.set(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING, uuid.toString());
        container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER, 0);
        int[] coordinates = new int[3];
        coordinates[0] = block.getX();
        coordinates[1] = block.getY();
        coordinates[2] = block.getZ();
        Bastion bastion = new Bastion(uuid, coordinates, radius);
        bastions.put(uuid, bastion);
    }

    public static Bastion inBastionRange(Player player){
        if (bastions == null || bastions.isEmpty()){return null;}
        for (Bastion bastion : bastions.values()){
            Block block = player.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
            if (block.getLocation().distance(player.getLocation()) <= bastion.radius){return bastion;}
        }
        return null;
    }

    public static void bastionAddFuel(Inventory gui, Player player){
        ArrayList<HashMap<String, Integer>> fuels = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("bastion-fuel");
        int totalAddedFuel = 0;
        int slot = 0;
        for (ItemStack item : gui.getContents()){
            if (slot == 18 || slot == 26 || item == null){
                slot++;
                continue;
            }
            for (HashMap<String, Integer> hash : fuels){
                for (String material : hash.keySet()){
                    // If item is fuel type, add fuel value to bastion PDC and remove ItemStack from inventory
                    if (!material.equalsIgnoreCase(item.getType().toString())){continue;}
                    int fuelAmount = hash.get(material) * item.getAmount();
                    Block bastion = playerLastBastion.get(player.getUniqueId());
                    PersistentDataContainer container = new CustomBlockData(bastion, TWClaim.getPlugin());
                    if (container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER) == null){return;}
                    int bastionFuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
                    bastionFuel = bastionFuel + fuelAmount;
                    totalAddedFuel += fuelAmount;
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER, bastionFuel);
                    // Remove item from inventory
                    item.setAmount(0);
                }
            }
            slot++;
        }
        player.sendMessage(ChatColor.GREEN + "Added " + totalAddedFuel + " Fuel");
    }

    public static void bastionFuelClose(Inventory gui, Player player){
        int slot = 0;
        boolean full = false;
        for (ItemStack item : gui.getContents()){
            if (slot == 18 || slot == 26 || item == null){
                slot++;
                continue;
            }
            if (full){
                player.getWorld().dropItem(player.getLocation(), item);
            } else{
                // Put item in the first available inventory slot, then remove from GUI
                try{
                    player.getInventory().setItem(player.getInventory().firstEmpty(), item);
                } catch (ArrayIndexOutOfBoundsException e){
                    full = true;
                    player.getWorld().dropItem(player.getLocation(), item);
                }
            }
            item.setAmount(0);
            slot++;
        }
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
