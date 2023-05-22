package com.ethan.twclaim.util;

import com.ethan.twclaim.Listeners.BastionEvents;
import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

    public static void removeReinforcement(PersistentDataContainer container, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey, BlockBreakEvent e){

        // Remove PDCs from block
        container.remove(materialKey);
        container.remove(key);
        container.remove(ownKey);
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){
            return;
        }
        // Remove bastion from PDC
        Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "bastion"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "surveillance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range-distance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"));


        // Make a proper bastion drop (the lore disappears when the player mines one normally.)
        ItemStack bastion = bastionItem();
        e.setDropItems(false);
        e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), bastion);

    }

    public static void removeReinforcement(PersistentDataContainer container, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey, BlockExplodeEvent e){

        // Remove PDCs from block
        container.remove(materialKey);
        container.remove(key);
        container.remove(ownKey);
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){
            return;
        }
        UUID bastionID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
        Bastion bastion = Bastion.bastions.get(bastionID);
        Block bastionBlock = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        e.blockList().remove(bastionBlock);
        bastionBlock.getDrops().clear();
        e.blockList().add(bastionBlock);


        // Remove bastion from PDC
        Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "bastion"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "surveillance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range-distance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"));


        // Make a proper bastion drop (the lore disappears when the player mines one normally.)
        ItemStack bastionItem = bastionItem();
        e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), bastionItem);
    }

    public static void removeReinforcement(PersistentDataContainer container, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey, EntityExplodeEvent e){

        // Remove PDCs from block
        container.remove(materialKey);
        container.remove(key);
        container.remove(ownKey);
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){
            return;
        }
        UUID bastionID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
        Bastion bastion = Bastion.bastions.get(bastionID);
        Block bastionBlock = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        bastionBlock.getDrops().clear();

        // Remove bastion from PDC
        Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "bastion"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "surveillance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range-distance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"));


        // Make a proper bastion drop (the lore disappears when the player mines one normally.)
        ItemStack bastionItem = bastionItem();
        e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), bastionItem);
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

    public static void generateGUI(Inventory gui, Material material, int place, int level, String upgrade, PersistentDataContainer container){
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        String display = "";
        List<String> lore = new ArrayList<>();

        ArrayList<HashMap<String, Integer>> cost;
        String activeUpgrades = container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING);
        switch (upgrade){
            case "anti-teleport":
                if (activeUpgrades.contains("T")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.GREEN + "Anti-Teleport" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.GREEN + "Anti-Teleport" + ChatColor.RED + " (Inactive)";
                }
                lore.add("Prevent non-member epearl/cfruit");
                if (level != 0){break;}
                display = ChatColor.GREEN + "Anti-Teleport " + ChatColor.RED + "(Not Owned)";
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("anti-teleport");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "anti-flight":
                if (activeUpgrades.contains("F")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.BLUE + "Anti-Flight" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.BLUE + "Anti-Flight" + ChatColor.RED + " (Inactive)";
                }

                lore.add("Prevent non-member elytra use");
                if (level != 0){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("anti-flight");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "surveillance":
                if (activeUpgrades.contains("S")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.WHITE + "" + ChatColor.BOLD + "Surveillance" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.BOLD + "Surveillance" + ChatColor.RED + " (Inactive)";
                }
                lore.add("Non-members glow");
                if (level != 0){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("surveillance");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "range":
                int maxLevel = ((ArrayList<Integer>) TWClaim.getPlugin().getConfig().get("bastion-ranges")).size() - 1;
                display = ChatColor.LIGHT_PURPLE + "Range " + "(" + level + "/" + maxLevel + ")";
                lore.add("Increase range of bastion");
                if (level >= ((ArrayList<?>) TWClaim.getPlugin().getConfig().get("bastion-ranges")).size() - 1){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                int nextLevel = level + 1;
                ArrayList<HashMap<String, Integer>> rangeCost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("range.level-" + nextLevel);
                for (HashMap<String, Integer> hash : rangeCost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "exp-storage":
                if (activeUpgrades.contains("E")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.GREEN + "Exp Storage" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.GREEN + "Exp Storage" + ChatColor.RED + " (Inactive)";
                }
                lore.add("Store experience");
                if (level != 0){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("exp-storage");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
        }
        if (!upgrade.equalsIgnoreCase("range")){
            if (level == 0){
                lore.add(ChatColor.RED + "Not owned");
            } else{
                lore.add(ChatColor.GREEN + "Level: " + level);
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel consumption when active")){continue;}
                        lore.add("Fuel Cost when active: " + hash.get(key));
                    }
                }
            }
            if (upgrade.equalsIgnoreCase("surveillance")){
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel per person spied on")){continue;}
                        lore.add("Fuel Cost per person spotted: " + hash.get(key));
                    }
                }
            }
            if (upgrade.equalsIgnoreCase("exp-storage")){
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel per 100 points")){continue;}
                        lore.add("Fuel Cost per 100 exp: " + hash.get(key));
                    }
                }
            }
        }
        itemMeta.setDisplayName(display);
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
        gui.setItem(place, item);
    }

    public static ItemStack bastionItem(){
        ItemStack bastion = new ItemStack(Material.BEACON);
        ItemMeta bastionMeta = bastion.getItemMeta();
        bastionMeta.setDisplayName(ChatColor.GOLD + "Bastion");
        bastionMeta.setLore(Arrays.asList(ChatColor.GOLD + "Protects your land in a 30 block radius"));
        bastionMeta.getPersistentDataContainer().set(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING, "yes");
        bastion.setItemMeta(bastionMeta);
        return bastion;
    }
}
