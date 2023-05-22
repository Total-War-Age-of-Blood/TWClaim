package com.ethan.twclaim.guis;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.events.OpenGUI;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BastionUpgradeGUI implements Listener {
    private Inventory gui;
    final int antiFlight = 10;
    final int antiTeleport = 11;
    final int surveillance = 12;
    final int range = 13;
    final int expStorage = 14;

    public void openBastionGUI(Player player){
        gui = Bukkit.createInventory(null, 27, "Bastion Upgrades");
        Block bastionBlock = Bastion.playerLastBastion.get(player.getUniqueId());
        PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
        int antiTeleportLevel = container.get(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"), PersistentDataType.INTEGER);
        int antiFlightLevel = container.get(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"), PersistentDataType.INTEGER);
        int surveillanceLevel = container.get(new NamespacedKey(TWClaim.getPlugin(), "surveillance"), PersistentDataType.INTEGER);
        int rangeLevel = container.get(new NamespacedKey(TWClaim.getPlugin(), "range"), PersistentDataType.INTEGER);
        int expStorageLevel = container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"), PersistentDataType.INTEGER);
        container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER);
        Util.generateGUI(gui, Material.ELYTRA, antiFlight, antiFlightLevel, "anti-flight", container);
        Util.generateGUI(gui, Material.ENDER_PEARL, antiTeleport, antiTeleportLevel, "anti-teleport", container);
        Util.generateGUI(gui, Material.GLOW_INK_SAC, surveillance, surveillanceLevel, "surveillance", container);
        Util.generateGUI(gui, Material.BOW, range, rangeLevel, "range", container);
        Util.generateGUI(gui, Material.EXPERIENCE_BOTTLE, expStorage, expStorageLevel, "exp-storage", container);
        Util.generateGUI(gui, Material.BARRIER, ChatColor.RED + "Back", "", 18);

        player.openInventory(gui);
    }

    @EventHandler
    public void bastionGUIListener(OpenGUI e){
        if (e.getGuiName().equals("BastionUpgrade")){openBastionGUI(e.getPlayer());}
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent e){
        if (e.getClickedInventory() == null){return;}
        Player player = (Player) e.getWhoClicked();
        try{
            if(!Objects.equals(e.getClickedInventory(), gui)){
                if (!Objects.equals(player.getOpenInventory().getTopInventory(), gui) || !e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)){return;}
                e.setCancelled(true);
                return;
            }
        }catch (NullPointerException exception){return;}
        e.setCancelled(true);

        Block bastionBlock = Bastion.playerLastBastion.get(player.getUniqueId());
        PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
        ArrayList<HashMap<String, Integer>> cost = new ArrayList<>();
        switch (e.getSlot()){
            case antiFlight:
                clickUpgrade(container, ChatColor.BLUE + "Anti-Flight", "anti-flight", "F", player, antiFlight);
                break;
            case antiTeleport:
                clickUpgrade(container, ChatColor.GREEN + "Anti-Teleport", "anti-teleport", "T", player, antiTeleport);
                break;
            case surveillance:
                clickUpgrade(container, ChatColor.WHITE + "" + ChatColor.BOLD + "Surveillance", "surveillance", "S", player, surveillance);
                break;
            case range:
                clickRange(container, "range", player, range);
                break;
            case expStorage:
                clickUpgrade(container, ChatColor.GREEN + "Exp Storage", "exp-storage", "E", player, expStorage);
                break;
            case 18:
                Bukkit.getPluginManager().callEvent(new OpenGUI(player, "Bastion"));
                break;
        }
    }

    @EventHandler
    public void onPlayerDrag(InventoryDragEvent e){
        try{
            if(!Objects.equals(e.getInventory(), gui)){
                return;
            }
        }catch (NullPointerException ignored){return;}
        e.setCancelled(true);
    }

    public boolean processPurchase(Player player, HashMap<String, Integer> requiredAmounts){
        // Check if cost can be paid
        if (!checkCost(player.getInventory(), requiredAmounts)){
            player.sendMessage(ChatColor.RED + "Can't afford upgrade");
            return false;
        }
        // Iterate through required materials
        for (String key : requiredAmounts.keySet()){
            int requiredAmount = requiredAmounts.get(key);
            // Iterate through inventory to pay cost
            for (ItemStack item : player.getInventory().getContents()){
                if (item == null){continue;}
                String material = item.getType().toString();
                if (!material.equalsIgnoreCase(key)){continue;}
                int invAmount = item.getAmount();
                // Remove materials from inventory
                if (invAmount >= requiredAmount){
                    item.setAmount(invAmount - requiredAmount);
                    break;
                } else{
                    requiredAmount -= invAmount;
                    item.setAmount(0);
                }
            }
        }
        return true;
    }

    public boolean checkCost(Inventory inventory, HashMap<String, Integer> requiredAmounts){
        // Iterate through the required materials
        for (String key : requiredAmounts.keySet()){
            int costAmount = requiredAmounts.get(key);
            // Iterate through inventory to pay cost
            for (ItemStack item : inventory.getContents()){
                if (item == null){continue;}
                String material = item.getType().toString();
                if (!material.equalsIgnoreCase(key)){continue;}
                int invAmount = item.getAmount();
                // Once cost is paid, move to next material
                if (invAmount >= costAmount){
                    costAmount = 0;
                    break;
                } else{
                    costAmount -= invAmount;
                }
            }
            // If a cost is unable to be paid, return false
            if (costAmount != 0){return false;}
        }
        return true;
    }

    private void clickUpgrade(PersistentDataContainer container, String display, String upgrade, String upgradeKey, Player player, int place){
        // If upgrade is bought, toggle the power
        int level = container.get(new NamespacedKey(TWClaim.getPlugin(), upgrade), PersistentDataType.INTEGER);
        ItemStack item =  gui.getItem(place);
        ItemMeta itemMeta = item.getItemMeta();
        if (level > 0){
            NamespacedKey activeUpgradesKey = new NamespacedKey(TWClaim.getPlugin(), "active-upgrades");
            String activeUpgrades = container.get(activeUpgradesKey, PersistentDataType.STRING);
            // If upgrade is active
            if (activeUpgrades.contains(upgradeKey)){
                // Update upgrades string, GUI, and fuel consumption
                activeUpgrades = activeUpgrades.replace(upgradeKey, "");
                container.set(activeUpgradesKey, PersistentDataType.STRING, activeUpgrades);
                NamespacedKey fuelConsumptionKey = new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption");
                // Get fuel consumption from config and subtract from PDC
                ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel consumption when active")){continue;}
                        container.set(fuelConsumptionKey, PersistentDataType.INTEGER, container.get(fuelConsumptionKey, PersistentDataType.INTEGER) - hash.get(key));
                    }
                }
                itemMeta.setDisplayName(display + ChatColor.RED + " (Inactive)");
                itemMeta.removeEnchant(Enchantment.DURABILITY);
                item.setItemMeta(itemMeta);
                gui.setItem(place, item);
                return;
            }
            // If upgrade is inactive
            activeUpgrades += upgradeKey;
            container.set(activeUpgradesKey, PersistentDataType.STRING, activeUpgrades);
            NamespacedKey fuelConsumptionKey = new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption");
            // Get fuel consumption from config and add to PDC
            ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
            for (HashMap<String, Integer> hash : cost){
                for (String key : hash.keySet()){
                    if (!key.equalsIgnoreCase("fuel consumption when active")){continue;}
                    container.set(fuelConsumptionKey, PersistentDataType.INTEGER, container.get(fuelConsumptionKey, PersistentDataType.INTEGER) + hash.get(key));
                }
            }
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.setDisplayName(display + ChatColor.GREEN + " (Active)");
            item.setItemMeta(itemMeta);
            gui.setItem(place, item);
            return;
        }
        // Try to purchase upgrade
        ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
        HashMap<String, Integer> requiredAmounts = new HashMap<>();
        for (HashMap<String, Integer> hash : cost){
            for(String key : hash.keySet()){
                if (key.contains("fuel")){continue;}
                int amount = hash.get(key);
                if (requiredAmounts.containsKey(key)){
                    requiredAmounts.put(key, requiredAmounts.get(key) + amount);
                    continue;
                }
                requiredAmounts.put(key, amount);
            }
        }
        if(!processPurchase(player, requiredAmounts)){return;}
        // Mark upgrade as inactive
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Level 1");
        if (!upgrade.equalsIgnoreCase("range")) {
            cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
            for (HashMap<String, Integer> hash : cost) {
                for (String key : hash.keySet()) {
                    if (!key.equalsIgnoreCase("fuel consumption when active")) {
                        continue;
                    }
                    lore.add("Fuel Cost when active: " + hash.get(key));
                }
            }
            if (upgrade.equalsIgnoreCase("surveillance")) {
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost) {
                    for (String key : hash.keySet()) {
                        if (!key.equalsIgnoreCase("fuel per person spied on")) {
                            continue;
                        }
                        lore.add("Fuel Cost per person spotted: " + hash.get(key));
                    }
                }
            }
            if (upgrade.equalsIgnoreCase("exp-storage")) {
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost) {
                    for (String key : hash.keySet()) {
                        if (!key.equalsIgnoreCase("fuel per 100 points")) {
                            continue;
                        }
                        lore.add("Fuel Cost per 100 exp: " + hash.get(key));
                    }
                }
            }
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        gui.setItem(place, item);
        // Add the upgrade to bastion PDC
        NamespacedKey upgradeLevel = new NamespacedKey(TWClaim.getPlugin(), upgrade);
        container.set(upgradeLevel, PersistentDataType.INTEGER, 1);
    }

    private void clickRange(PersistentDataContainer container, String upgrade, Player player, int place){
        ArrayList<Integer> ranges = (ArrayList<Integer>) TWClaim.getPlugin().getConfig().get("bastion-ranges");
        int maxLevel = ranges.size() - 1;
        int level = container.get(new NamespacedKey(TWClaim.getPlugin(), upgrade), PersistentDataType.INTEGER);

        // If power is at max level, do nothing
        if (level >= maxLevel){return;}
        // Try to purchase upgrade
        int nextLevel = level + 1;
        ArrayList<HashMap<String, Integer>> cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("range.level-" + nextLevel);
        HashMap<String, Integer> requiredAmounts = new HashMap<>();
        int additionalFuelConsumption = 0;
        for (HashMap<String, Integer> hash : cost){
            for(String key : hash.keySet()){
                if (key.contains("fuel")){
                    additionalFuelConsumption += hash.get(key);
                    continue;
                }
                int amount = hash.get(key);
                if (requiredAmounts.containsKey(key)){
                    requiredAmounts.put(key, requiredAmounts.get(key) + amount);
                    continue;
                }
                requiredAmounts.put(key, amount);
            }
        }
        if(!processPurchase(player, requiredAmounts)){return;}
        // Update the gui, PDC, and Bastion object if successful
        UUID bastionId = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
        Bastion bastion = Bastion.bastions.get(bastionId);
        container.set(new NamespacedKey(TWClaim.getPlugin(), upgrade), PersistentDataType.INTEGER, nextLevel);
        container.set(new NamespacedKey(TWClaim.getPlugin(), "range-distance"), PersistentDataType.INTEGER, ranges.get(nextLevel));
        int fuelConsumption = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"), PersistentDataType.INTEGER);
        container.set(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"), PersistentDataType.INTEGER, fuelConsumption + additionalFuelConsumption);
        bastion.setRadius(ranges.get(nextLevel));
        ItemStack item =  gui.getItem(place);
        ItemMeta itemMeta = item.getItemMeta();
        // If there is another level, show that level's costs
        List<String> lore = new ArrayList<>();
        String display = ChatColor.LIGHT_PURPLE + "Range " + "(" + nextLevel + "/" + maxLevel + ")";
        itemMeta.setDisplayName(display);
        lore.add("Increases range of bastion");
        if (!(nextLevel >= maxLevel)){
            lore.add(ChatColor.UNDERLINE + "Cost");
            // Get cost from config
            cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("range.level-" + (nextLevel + 1));
            for (HashMap<String, Integer> hash : cost){
                for (String key : hash.keySet()){
                    lore.add(hash.get(key) + " " + key);
                }
            }
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        gui.setItem(range, item);
    }
}
