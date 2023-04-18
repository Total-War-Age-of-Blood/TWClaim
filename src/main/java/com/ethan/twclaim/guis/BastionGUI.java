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

import java.util.Objects;

public class BastionGUI implements Listener {
    private Inventory gui;
    // TODO Upgrades should increase the power consumption
    // TODO alter EventHandlers to make them dependent on upgrades
    public void openBastionGUI(Player player){
        gui = Bukkit.createInventory(null, 27, "Bastion");
        Block bastion = Bastion.playerLastBastion.get(player.getUniqueId());
        PersistentDataContainer container = new CustomBlockData(bastion, TWClaim.getPlugin());
        if (container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER) == null){return;}
        int fuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
        Util.generateGUI(gui, Material.COAL, ChatColor.GOLD + "Deposit Fuel", "Current Fuel: " + fuel, 12);
        Util.generateGUI(gui, Material.GOLD_INGOT, ChatColor.RED + "Upgrades", "Upgrade Bastion", 14);
        player.openInventory(gui);
    }

    @EventHandler
    public void bastionGUIListener(OpenGUI e){
        if (e.getGuiName().equals("Bastion")){openBastionGUI(e.getPlayer());}
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent e){
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
}
