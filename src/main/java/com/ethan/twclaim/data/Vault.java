package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.util.Util;
import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
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
            vaults = TWClaim.getGson().fromJson(bastionsReader, new TypeToken<HashMap<UUID, Vault>>(){}.getType());
            if (vaults == null){
                vaults = new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void destroyVault(Vault vault) {
        vaults.remove(vault.getUuid());
        System.out.println("Vault Destroyed");
        System.out.println(Vault.vaults.keySet());
    }

    public static ItemStack checkVaults(Player player){
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        UUID target = playerData.getTarget();
        for (Vault vault : Vault.vaults.values()){
            UUID owner = vault.getOwner();
            if (!target.equals(owner) && !player.getUniqueId().equals(owner)){continue;}
            Chest chest = (Chest) Bukkit.getWorld(vault.worldID).getBlockAt(vault.coordinates[0], vault.coordinates[1], vault.coordinates[2]).getState();
            Inventory chestInventory = chest.getInventory();
            ItemStack item = Util.findReinforcement(chestInventory);
            if (item == null){continue;}
            return item;
        }
        return null;
    }

    public static boolean itemBackToVault(Player player, PersistentDataContainer container, UUID owner, ItemStack item){
        if (Boolean.TRUE.equals(container.get(Util.getFromVault(), PersistentDataType.BOOLEAN))){
            for (Vault vault : Vault.vaults.values()){
                UUID vaultOwner = vault.getOwner();
                if (owner.equals(vaultOwner)){
                    Chest chest = (Chest) Bukkit.getWorld(vault.getWorldID()).getBlockAt(vault.getCoordinates()[0],
                            vault.getCoordinates()[1], vault.getCoordinates()[2]).getState();
                    Inventory chestInventory = chest.getInventory();
                    int firstEmpty = chestInventory.firstEmpty();
                    if (firstEmpty == -1){continue;}
                    chestInventory.addItem(item);
                    return true;
                }
            }
            player.sendMessage(ChatColor.RED + "VAULTS FULL. MATERIAL LOST. CLEAR VAULT SPACE OR MAKE NEW VAULT.");
            return true;
        }
        return false;
    }

    UUID uuid;
    UUID worldID;
    UUID owner;
    int[] coordinates;
    int[] signCoordinates;

    public Vault(UUID uuid, int[] coordinates, int[] signCoordinates, UUID owner, UUID worldID){
        this.uuid = uuid;
        this.coordinates = coordinates;
        this.signCoordinates = signCoordinates;
        this.owner = owner;
        this.worldID = worldID;
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

    public int[] getSignCoordinates() {
        return signCoordinates;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getWorldID() {
        return worldID;
    }
}
