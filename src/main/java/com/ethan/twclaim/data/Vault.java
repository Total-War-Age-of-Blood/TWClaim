package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.util.Util;
import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import sun.jvm.hotspot.debugger.cdbg.Sym;

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
        AutoSave.setChange(true);
    }

    public static HashMap<Vault, ItemStack> checkVaults(Player player){
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        UUID target = playerData.getTarget();
        // We want to prioritize tribe vaults before personal vaults, so the first loop does not include personal vaults
        for (Vault vault : Vault.vaults.values()){
            UUID owner = vault.getOwner();
            Chest chest;
            if (!target.equals(owner)){continue;}
            try{
                chest = (Chest) Bukkit.getWorld(vault.worldID).getBlockAt(vault.coordinates[0], vault.coordinates[1], vault.coordinates[2]).getState();
            }catch(Exception exception){
                exception.printStackTrace();
                continue;
            }

            Inventory chestInventory = chest.getInventory();
            ItemStack item = Util.findReinforcement(chestInventory);
            if (item == null){continue;}
            HashMap<Vault, ItemStack> vaultItemStackHashMap = new HashMap<>();
            vaultItemStackHashMap.put(vault, item);
            return vaultItemStackHashMap;
        }
        // if there are no valid tribe vaults, go to personal vaults
        for (Vault vault : Vault.vaults.values()){
            UUID owner = vault.getOwner();
            if (!player.getUniqueId().equals(owner)){continue;}
            Chest chest = (Chest) Bukkit.getWorld(vault.worldID).getBlockAt(vault.coordinates[0], vault.coordinates[1], vault.coordinates[2]).getState();
            Inventory chestInventory = chest.getInventory();
            ItemStack item = Util.findReinforcement(chestInventory);
            if (item == null){continue;}
            HashMap<Vault, ItemStack> vaultItemStackHashMap = new HashMap<>();
            vaultItemStackHashMap.put(vault, item);
            return vaultItemStackHashMap;
        }
        return null;
    }

    public static boolean itemBackToVault(Player player, PersistentDataContainer container, ItemStack item){
        if (!container.has(Util.getVaultID(), PersistentDataType.STRING)){return false;}
        String vaultIDString = container.get(Util.getVaultID(), PersistentDataType.STRING);
        if (vaultIDString == null || vaultIDString.equalsIgnoreCase("null")){return false;}
        UUID vaultID = UUID.fromString(vaultIDString);
        Vault vault = Vault.vaults.get(vaultID);

        Chest chest = (Chest) Bukkit.getWorld(vault.getWorldID()).getBlockAt(vault.getCoordinates()[0],
                vault.getCoordinates()[1], vault.getCoordinates()[2]).getState();
        Inventory chestInventory = chest.getInventory();
        HashMap<Integer, ItemStack> itemsBack = chestInventory.addItem(item);
        if (!itemsBack.isEmpty()){
            if (getNearbyVaults(vault, item)){return true;}
            UUID owner = UUID.fromString(container.get(Util.getOwnKey(), PersistentDataType.STRING));
            if (Util.isTribe(owner)){
                player.sendMessage(ChatColor.RED + "VAULTS FULL. MATERIAL LOST. CLEAR VAULT SPACE OR MAKE NEW VAULT FOR TRIBE: " + TribeData.tribe_hashmap.get(owner).getName());
            } else {
                player.sendMessage(ChatColor.RED + "VAULTS FULL. MATERIAL LOST. CLEAR VAULT SPACE OR MAKE NEW PERSONAL VAULT.");
            }
            return false;
        }
        return true;
    }

    public static boolean getNearbyVaults(Vault vault, ItemStack item){
        UUID vaultID = vault.getUuid();
        int[] vaultCoords = vault.getCoordinates();
        UUID vaultWorldID = vault.getWorldID();
        Location vaultLocation = Bukkit.getWorld(vaultWorldID).getBlockAt(vaultCoords[0], vaultCoords[1], vaultCoords[2]).getLocation();
        UUID vaultOwner = vault.getOwner();

        for (Vault otherVault : Vault.vaults.values()){
            UUID otherVaultID = otherVault.uuid;
            if (otherVaultID.equals(vaultID)){continue;}
            UUID otherVaultOwner = otherVault.owner;
            if (!otherVaultOwner.equals(vaultOwner)){continue;}
            UUID otherVaultWorldID = otherVault.worldID;
            if (!vaultWorldID.equals(otherVaultWorldID)){continue;}
            int[] otherVaultCoords = otherVault.coordinates;
            Location otherVaultLocation = Bukkit.getWorld(otherVaultWorldID).getBlockAt(otherVaultCoords[0], otherVaultCoords[1], otherVaultCoords[2]).getLocation();
            double distance = vaultLocation.distance(otherVaultLocation);
            if (distance > TWClaim.getPlugin().getConfig().getDouble("vault-max-trade-range")){continue;}

            Chest chest = (Chest) Bukkit.getWorld(otherVault.getWorldID()).getBlockAt(otherVaultLocation).getState();
            Inventory chestInventory = chest.getInventory();
            HashMap<Integer, ItemStack> itemsBack = chestInventory.addItem(item);
            if (!itemsBack.isEmpty()){
                continue;}
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
        AutoSave.setChange(true);
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
