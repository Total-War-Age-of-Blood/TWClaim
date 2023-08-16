package com.ethan.twclaim;

import com.ethan.twclaim.data.Bastion;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
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
        // Encased in try block to prevent breaking if error is thrown
        try{
            // Loop through all the bastions and subtract fuel
            for (Bastion bastion : Bastion.bastions.values()){
                // Get Persistent Data Container
                Block block = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
                PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                int fuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
                int fuelConsumption = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"), PersistentDataType.INTEGER);
                // If exp storage is active, burn an amount of fuel per amount of points
                if (container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"), PersistentDataType.INTEGER) != 0){
                    int storedExp = container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER);
                    ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("exp-storage");
                    for (HashMap<String, Integer> hash : cost){
                        for (String key : hash.keySet()){
                            if (!key.equalsIgnoreCase("fuel per 100 points")){continue;}
                            fuelConsumption += storedExp / 100 * hash.get(key);
                        }
                    }
                }
                // Add cost for having extenders
                if (bastion.getExtenderChildren().size() > 0){
                    fuelConsumption += bastion.getExtenderChildren().size() * TWClaim.getPlugin().getConfig().getInt("extender-cost");
                }
                // If players crossed through the area with surveillance active, add their cost to fuelConsumption.
                ArrayList<UUID> surveillance = bastion.getUnderSurveillance();
                if (surveillance != null){
                    ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("surveillance");
                    for (HashMap<String, Integer> hash : cost){
                        for (String key : hash.keySet()){
                            if (!key.equalsIgnoreCase("fuel per person spied on")){continue;}
                            int surveillanceCost = hash.get(key);
                            fuelConsumption += surveillanceCost * surveillance.size();
                        }
                    }
                    // Clear surveillance list
                    surveillance.clear();
                    bastion.setUnderSurveillance(surveillance);
                }
                if (fuel - fuelConsumption <= 0){
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER, 0);
                    if (container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"), PersistentDataType.INTEGER) != 0){
                        int storedExp = container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER);
                        if (!(storedExp > 0)){continue;}
                        ExperienceOrb experienceOrb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
                        experienceOrb.setExperience(storedExp);
                        container.set(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER, 0);
                    }
                } else{
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER, fuel - fuelConsumption);
                }
            }
        }catch (Exception exception){System.out.println(exception);}
    }
}
