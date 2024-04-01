package com.ethan.twclaim.data;

import org.bukkit.Material;

import java.util.HashMap;

public class VaultValue {

    /* The Vault Value class needs to record which vaults have paid which materials. This allows us to link the claim
    * protection to the vault the material came from when using the claim command.*/

    Vault vault;
    HashMap<Material, Integer> matsPaid;

    public VaultValue(Vault vault, HashMap<Material, Integer> matsPaid){
        this.vault = vault;
        this.matsPaid = matsPaid;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public HashMap<Material, Integer> getMatsPaid() {
        return matsPaid;
    }

    public void setMatsPaid(HashMap<Material, Integer> matsPaid) {
        this.matsPaid = matsPaid;
    }
}
