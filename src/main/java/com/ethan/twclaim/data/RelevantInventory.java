package com.ethan.twclaim.data;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class RelevantInventory {
    /* This class bundles the inventories of the vaults with the inventories of the player.
    * It was made this way because the player inventory does not have a vault, so it wouldn't work with the
    * Hashmap. */

    HashMap<Vault, Inventory> vaults;
    Inventory playerInventory;

    public RelevantInventory(HashMap<Vault, Inventory> vaults, Inventory playerInventory){
        this.vaults = vaults;
        this.playerInventory = playerInventory;
    }

    public HashMap<Vault, Inventory> getVaults() {
        return vaults;
    }

    public void setVaults(HashMap<Vault, Inventory> vaults) {
        this.vaults = vaults;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    public void setPlayerInventory(Inventory playerInventory) {
        this.playerInventory = playerInventory;
    }
}
