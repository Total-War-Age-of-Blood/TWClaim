package com.ethan.twclaim;

import com.ethan.twclaim.data.Bastion;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FuelConsumption extends BukkitRunnable {
    // TODO rewrite code to remove lists from fuel costs to make them easier to path to.
    @Override
    public void run() {
        // Loop through all the bastions and subtract fuel
        for (Bastion bastion : Bastion.bastions.values()){
            // Get Persistent Data Container
            Block block = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            int fuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
            int fuelConsumption = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"), PersistentDataType.INTEGER);
            // If players crossed through the area with surveillance active, add their cost to fuelConsumption.
            ArrayList<UUID> surveillance = bastion.getUnderSurveillance();
            if (surveillance != null){
                ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("surveillance");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel")){continue;}
                        int surveillanceCost = hash.get(key);
                        fuelConsumption += surveillanceCost * surveillance.size();
                    }
                }
            }
            if (fuel - fuelConsumption < 0){
                container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER, 0);
            } else{
                container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER, fuel - fuelConsumption);
            }
        }
    }
}
