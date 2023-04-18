package com.ethan.twclaim.util;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

public class Util {
    public static boolean checkTribe(String name){
        boolean tribeFound = false;
        for (TribeData tribeData : TribeData.tribe_hashmap.values()){
            if (!tribeData.getName().equalsIgnoreCase(name)){
                continue;
            }
            tribeFound = true;
            break;
        }
        return !tribeFound;
    }

    public static void removeReinforcement(PersistentDataContainer container, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey){
        container.remove(materialKey);
        container.remove(key);
        container.remove(ownKey);
    }


    public static boolean isTribe(UUID uuid){
        return TribeData.tribe_hashmap.containsKey(uuid);
    }

    public static boolean isInTribe(UUID playerId, UUID tribeId){
        PlayerData playerData = PlayerData.player_data_hashmap.get(playerId);
        if (playerData.getTribes().containsKey(tribeId)){return true;}
        return false;
    }

    public static HashMap<String, Integer> getReinforcementTypes(){
        ArrayList<HashMap<String, Integer>> reinforcementsFromConfig = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");
        // This hashmap contains the material types and their reinforcement values
        HashMap<String, Integer> reinforcements = new HashMap<>();
        for (HashMap<String, Integer> hashMap : reinforcementsFromConfig){
            reinforcements.putAll(hashMap);
        }
        List<String> keyList = new ArrayList<>(reinforcements.keySet());
        List<Integer> valueList = new ArrayList<>(reinforcements.values());
        reinforcements.clear();
        // Make keys lowercase, so they can be matched with lowercase material types
        int n = 0;
        for (String key : keyList){
            key.toLowerCase();
            reinforcements.put(key, valueList.get(n));
            n++;
        }
        return reinforcements;
    }

    public static void generateGUI(Inventory gui, Material material, String display, String lore, int place){
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(display);
        itemMeta.setLore(Collections.singletonList(lore));
        item.setItemMeta(itemMeta);
        gui.setItem(place, item);
    }
}
