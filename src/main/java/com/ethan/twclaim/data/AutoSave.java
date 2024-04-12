package com.ethan.twclaim.data;

import com.ethan.twclaim.TWClaim;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class AutoSave extends BukkitRunnable {
    private static boolean change = false;

    public static void setChange(boolean change) {
        AutoSave.change = change;
    }

    @Override
    public void run() {
        if (!change){
            System.out.println("No Change. Did not AutoSave");
            return;}
        Bastion.saveBastions();
        Extender.saveExtenders();
        Vault.saveVaults();

        HashMap<UUID, PlayerData> tempPlayerDataHashMap = new HashMap<>();
        for (UUID uuid : PlayerData.player_data_hashmap.keySet()){
            tempPlayerDataHashMap.put(uuid, PlayerData.player_data_hashmap.get(uuid));
        }
        for (PlayerData tempPlayerData : tempPlayerDataHashMap.values()){
            tempPlayerData.setMode("None");
            tempPlayerData.setTarget(tempPlayerData.getUuid());
            File playerFolder = new File(TWClaim.getPlugin().getDataFolder(), "PlayerData");
            File playerFile = new File(playerFolder, tempPlayerData.getUuid() + ".json");
            try {
                FileWriter playerFileWriter = new FileWriter(playerFile, false);
                TWClaim.getGson().toJson(tempPlayerData, playerFileWriter);
                playerFileWriter.flush();
                playerFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TribeData tribe_hashmap = new TribeData();
        tribe_hashmap.saveTribes();

        change = false;
        System.out.println("TWClaim AutoSaved!");
    }
}
