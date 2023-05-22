package com.ethan.twclaim.guis;

import com.ethan.twclaim.Listeners.BastionEvents;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class BastionGUI implements Listener {
    private Inventory gui;
    public void openBastionGUI(Player player){
        gui = Bukkit.createInventory(null, 27, "Bastion");
        Block bastionBlock = Bastion.playerLastBastion.get(player.getUniqueId());
        PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
        Bastion bastion = Bastion.bastions.get(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        if (container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER) == null){return;}
        int fuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
        int fuelConsumption = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"), PersistentDataType.INTEGER);
        Util.generateGUI(gui, Material.COAL, ChatColor.GOLD + "Deposit Fuel", "Current Fuel: " + fuel + "\n" + "Consumption: " + fuelConsumption, 12);
        Util.generateGUI(gui, Material.GOLD_INGOT, ChatColor.RED + "Upgrades", "Upgrade Bastion", 14);
        String activeUpgrades = container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING);
        if (activeUpgrades.contains("E") && BastionEvents.hasFuel(bastion)){
            int exp = container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER);
            Util.generateGUI(gui, Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Exp. Storage", "Exp: " + exp + "\nLeft Click to deposit\nRight click to withdraw", 15);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void bastionGUIListener(OpenGUI e){
        if (e.getGuiName().equals("Bastion")){openBastionGUI(e.getPlayer());}
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

        switch (e.getSlot()){
            case 12:
                Bukkit.getPluginManager().callEvent(new OpenGUI(player, "BastionFuel"));
                break;
            case 14:
                Bukkit.getPluginManager().callEvent(new OpenGUI(player, "BastionUpgrade"));
                break;
            case 15:
                Block bastionBlock = Bastion.playerLastBastion.get(player.getUniqueId());
                PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
                Bastion bastion = Bastion.bastions.get(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
                if (!container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING).contains("E")){return;}
                if (!BastionEvents.hasFuel(bastion)){return;}
                ItemStack item = gui.getItem(15);
                ItemMeta itemMeta = item.getItemMeta();
                // Deposit/Withdraw 1 level on left/right, all levels on shift + left/right
                // Get player's current experience
                int playerTotalExperience = player.getTotalExperience();
                int expToNext = player.getExpToLevel();
                float percentToNext = player.getExp();
                int level = player.getLevel();
                int newAmount = 0;
                // Get stored experience
                int storedExperience = container.get(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER);
                // Deposit all experience
                if (e.getClick().equals(ClickType.SHIFT_LEFT)){
                    // Add player's exp to storage
                    newAmount = storedExperience + playerTotalExperience;
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER, newAmount);
                    // Set player's xp to 0
                    wipeExperience(player);
                } else if (e.getClick().equals(ClickType.SHIFT_RIGHT)){
                    // Withdraw all experience
                    // Wipe player's experience
                    wipeExperience(player);
                    // Add all experience from storage to player
                    player.giveExp(playerTotalExperience + storedExperience);
                    // Set stored exp to 0
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER, 0);
                } else if (e.getClick().equals(ClickType.RIGHT)){
                    // Check that there is enough experience to withdraw
                    if (expToNext > storedExperience){
                        player.sendMessage(ChatColor.RED + "Not enough experience stored");
                        return;
                    }
                    // Wipe player's experience
                    wipeExperience(player);
                    // Give player their experience + exp to next
                    player.giveExp(playerTotalExperience + expToNext);
                    // Subtract exp from stores
                    newAmount = storedExperience - expToNext;
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER, newAmount);
                } else if (e.getClick().equals(ClickType.LEFT)){
                    // Check that deposit can be made
                    if (level < 1){
                        player.sendMessage(ChatColor.RED + "Not enough experience to deposit");
                        return;
                    }
                    // Wipe player experience
                    wipeExperience(player);
                    // Calculate experience player will lose from donating the level
                    // This is done by calculating how much total experience the player would have one level down.
                    int prevLevel = level - 1;
                    int prevTotalExperience;
                    if (level <= 17){
                        prevTotalExperience = (int) (Math.pow(prevLevel, 2) + (6 * prevLevel));
                    } else if (level <= 32){
                        prevTotalExperience = (int) ((int) (2.5 * Math.pow(prevLevel, 2)) - 40.5  * prevLevel + 360);
                    } else{
                        prevTotalExperience = (int) ((int) (4.5 * Math.pow(prevLevel, 2)) - 162.5  * prevLevel + 2220);
                    }

                    // Give player experience for previous level
                    player.giveExp(prevTotalExperience);
                    // Calculate new storage amount
                    newAmount = storedExperience + playerTotalExperience - prevTotalExperience;
                    container.set(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"), PersistentDataType.INTEGER, newAmount);
                } else {return;}
                itemMeta.setLore(Collections.singletonList("Exp: " + newAmount));
                item.setItemMeta(itemMeta);
                gui.setItem(15, item);
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

    public static void wipeExperience(Player player){
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0.0f);
    }
}
