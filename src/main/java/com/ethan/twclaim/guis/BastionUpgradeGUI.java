package com.ethan.twclaim.guis;

import com.ethan.twclaim.events.OpenGUI;
import com.ethan.twclaim.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Objects;

public class BastionUpgradeGUI implements Listener {
    private Inventory gui;
    public void openBastionGUI(Player player){
        gui = Bukkit.createInventory(null, 27, "Bastion Upgrades");
        Util.generateGUI(gui, Material.ELYTRA, ChatColor.BLUE + "Anti-Flight", "Prevent non-members from using elytra", 10);
        Util.generateGUI(gui, Material.ENDER_PEARL, ChatColor.GREEN + "Anti-Teleport", "Prevent non-members from teleporting", 11);
        Util.generateGUI(gui, Material.GLOW_INK_SAC, ChatColor.BOLD + "Surveillance", "Non-members receive glowing effect", 12);
        Util.generateGUI(gui, Material.BOW, ChatColor.LIGHT_PURPLE + "Range", "Increase Range of Bastion", 13);
        Util.generateGUI(gui, Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Experience Storage", "Deposit or withdraw experience", 14);
        Util.generateGUI(gui, Material.BARRIER, ChatColor.RED + "Back", "", 18);

        player.openInventory(gui);
    }

    @EventHandler
    public void bastionGUIListener(OpenGUI e){
        if (e.getGuiName().equals("BastionUpgrade")){openBastionGUI(e.getPlayer());}
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
            case 13:
                // TODO If upgrade is not owned, check if player can pay the cost.
                //  If so, remove cost from inventory and add power to Bastion PDC.
                //  If upgrade is owned, click should toggle on/off, affecting power consumption.
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
}
