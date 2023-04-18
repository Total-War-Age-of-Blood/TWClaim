package com.ethan.twclaim.guis;

import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.events.OpenGUI;
import com.ethan.twclaim.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class BastionFuelGUI implements Listener {
    private Inventory gui;
    public void openBastionGUI(Player player){
        gui = Bukkit.createInventory(null, 27, "Bastion Fuel");
        Util.generateGUI(gui, Material.BARRIER, ChatColor.RED + "Back", "", 18);
        Util.generateGUI(gui, Material.EMERALD_BLOCK, ChatColor.GREEN + "Confirm", "", 26);
        player.openInventory(gui);
    }
    @EventHandler
    public void bastionGUIListener(OpenGUI e){
        if (e.getGuiName().equals("BastionFuel")){openBastionGUI(e.getPlayer());}
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent e){
        try{ if(!Objects.equals(e.getClickedInventory(), gui)){
            return;
        }}catch (NullPointerException exception){return;}

        if (gui == null){return;}

        Player player = (Player) e.getWhoClicked();
        switch (e.getSlot()){
            case 18:
                Bastion.bastionFuelClose(gui, player);
                e.setCancelled(true);
                Bukkit.getPluginManager().callEvent(new OpenGUI(player, "Bastion"));
                break;
            case 26:
                e.setCancelled(true);
                Bastion.bastionAddFuel(gui, player);
                break;
        }
    }

    @EventHandler
    public void onGUIClose(InventoryCloseEvent e){
        try{
            if (!gui.equals(e.getInventory())){return;}
        } catch (NullPointerException exception){return;}
        Player player = (Player) e.getPlayer();
        Bastion.bastionFuelClose(gui, player);
    }
}
