package com.ethan.twclaim.util;

import com.ethan.twclaim.data.TribeData;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

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
        return tribeFound;
    }

    public static void removeReinforcement(PersistentDataContainer container, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey){
        container.remove(materialKey);
        container.remove(key);
        container.remove(ownKey);
    }
}
