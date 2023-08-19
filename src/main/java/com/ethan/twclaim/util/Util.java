package com.ethan.twclaim.util;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.Extender;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
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
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING)){
            return;
        }
        // Remove bastion from PDC
        if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));}
        if (container.has(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING)){Extender.extenders.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING)));}
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "bastion"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"));
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
        bastionBlock.setType(Material.AIR);
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
        e.blockList().remove(bastionBlock);
        bastionBlock.setType(Material.AIR);
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
        e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), bastionItem);
    }

    public static void removeReinforcement(PersistentDataContainer container, BlockBurnEvent e, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey){
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

    public static ItemStack bastionRangeExtenderItem(){
        ItemStack bastionRangeExtender = new ItemStack(Material.BEACON);
        ItemMeta meta = bastionRangeExtender.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Range Extender");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Extends range of bastion by 30 blocks from where it is placed. Farther from bastion = higher fuel consumption."));
        meta.getPersistentDataContainer().set(new NamespacedKey(TWClaim.getPlugin(), "extender"), PersistentDataType.STRING, "yes");
        bastionRangeExtender.setItemMeta(meta);
        bastionRangeExtender.setAmount(4);
        return bastionRangeExtender;
    }

    public static boolean hasPermission(Player player, TribeData tribeData, CharSequence perm){
        String group = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(group);
        return perms.contains(perm);
    }
    // Returns true if the block below the special case is reinforced
    public static boolean checkSpecialReinforcement(Tag<Material> tag, BlockBreakEvent e){;
        NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(), "reinforcement");
        NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
        Block block = e.getBlock();
        // Get the block's persistent data container
        Block blockBelow = block;
        // Will keep going down until it finds a non-crop block.
        while (tag.isTagged(blockBelow.getType())){
            blockBelow = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
        }
        PersistentDataContainer container = new CustomBlockData(blockBelow, TWClaim.getPlugin());
        // If block below is not reinforced, return false
        if (!container.has(key, PersistentDataType.INTEGER) && container.has(ownKey, PersistentDataType.STRING)){return false;}
        // If block below is reinforced, check if player can break it
        // If block below is reinforced and unbreakable, return true
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();
        UUID blockOwner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
        // Check if player is private owner
        if (playerId.equals(blockOwner)){return false;}
        // Check if player is tribe member
        if (Util.isTribe(blockOwner)){
            if (!Util.isInTribe(playerId, blockOwner)){return true;}
            TribeData tribeData = TribeData.tribe_hashmap.get(blockOwner);
            return !Util.hasPermission(player, tribeData, "r");
        }
        return true;
    }
}
