package com.ethan.twclaim.guis;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.Extender;
import com.ethan.twclaim.data.TribeData;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExtenderGUI implements Listener {
    @EventHandler
    public void extenderGUIListener(OpenGUI e){if (e.getGuiName().equals("Extender")) {openExtenderGUI(e.getPlayer());}}

    Inventory gui;

    public void openExtenderGUI(Player player){
        gui = Bukkit.createInventory(null, 36, "Extender");
        // Determine which bastions the player has access to.
        List<Bastion> accessibleBastions = new ArrayList<>();
        for (Bastion bastion : Bastion.bastions.values()){
            int[] coordinates = bastion.getCoordinates();
            Block block = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(coordinates[0], coordinates[1], coordinates[2]);
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            String ownerString = container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING);
            if (ownerString == null){continue;}
            UUID owner = UUID.fromString(ownerString);
            if (!Util.isTribe(owner)){
                if (!owner.equals(player.getUniqueId())){continue;}
                accessibleBastions.add(bastion);
            } else{
                TribeData tribeData = TribeData.tribe_hashmap.get(owner);
                if (!Util.isInTribe(player.getUniqueId(), tribeData.getTribeID())){continue;}
                if (Util.hasPermission(player, tribeData, "b")){
                    accessibleBastions.add(bastion);
                }
            }
        }
        // Get extender
        Block extenderBlock = Extender.playerLastExtender.get(player.getUniqueId());
        PersistentDataContainer container = new CustomBlockData(extenderBlock, TWClaim.getPlugin());
        UUID father = null;
        if (container.has(new NamespacedKey(TWClaim.getPlugin(), "Father"), PersistentDataType.STRING)){
            father = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "Father"), PersistentDataType.STRING));
        }
        // Generate the first page with recursion to change pages.
        generatePage(gui, 1, accessibleBastions, father);

        // Open inv
        player.openInventory(gui);
    }

    // This function will generate the page of potential father bastions, return false.
    public boolean generatePage(Inventory gui, int page, List<Bastion> accessibleBastions, UUID father){
        int PAGE_SIZE = 27;
        int size = accessibleBastions.size();
        if (size <= (page - 1) * PAGE_SIZE){
            return false;
        }
        // Check how many bastions there are
        int bastionsLeft = size - (page - 1) * PAGE_SIZE;
        // Put bastions in inventory using for loop
        for (int i = 0; i < Math.min(bastionsLeft, PAGE_SIZE); i++){
            int starting = (page - 1) * PAGE_SIZE;
            Bastion selected = accessibleBastions.get(starting + i);
            Util.generateGUI(gui, Material.BEACON, selected.getName(), "X: " + selected.getCoordinates()[0] + " Y: " + selected.getCoordinates()[1] + " Z: " + selected.getCoordinates()[2], i);
            // Get the bastion item and put its uuid in pdc for future reference
            ItemStack fatherBastion = gui.getItem(i);
            ItemMeta fatherBastionMeta = fatherBastion.getItemMeta();
            PersistentDataContainer container = fatherBastionMeta.getPersistentDataContainer();
            container.set(new NamespacedKey(TWClaim.getPlugin(), "ID"), PersistentDataType.STRING, selected.getUuid().toString());

            if (father != null){
                if (father.equals(selected.getUuid())){
                    fatherBastionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    fatherBastionMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                }
            }

            fatherBastion.setItemMeta(fatherBastionMeta);
            gui.setItem(i, fatherBastion);
        }

        // Next Page
        ItemStack next = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta nextMeta = (SkullMeta) next.getItemMeta();

        // Giving the player head a custom texture
        PlayerProfile nextProf = Bukkit.getServer().createPlayerProfile(UUID.randomUUID(), null);

        try{
            nextProf.getTextures().setSkin(new URL("http://textures.minecraft.net/texture/18660691d1ca029f120a3ff0eabab93a2306b37a7d61119fcd141ff2f6fcd798"));
            assert nextMeta != null;
            nextMeta.setOwnerProfile(nextProf);
        }catch (MalformedURLException e){e.printStackTrace();}

        assert nextMeta != null;
        nextMeta.setDisplayName(ChatColor.DARK_GREEN + "Next Page");

        next.setItemMeta(nextMeta);
        gui.setItem(33, next);

        // Previous Page
        ItemStack prev = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta prevMeta = (SkullMeta) prev.getItemMeta();

        // Giving the player head a custom texture
        PlayerProfile prevProf = Bukkit.getServer().createPlayerProfile(UUID.randomUUID(), null);

        try{
            prevProf.getTextures().setSkin(new URL("http://textures.minecraft.net/texture/52ba81b47d5ee06b484ea9bdf22934e6abca5e4ced7be3905d6ae6ecd6fcea2a"));
            assert prevMeta != null;
            prevMeta.setOwnerProfile(prevProf);
        }catch (MalformedURLException e){e.printStackTrace();}

        assert prevMeta != null;
        prevMeta.setDisplayName(ChatColor.DARK_GREEN + "Previous Page");

        prev.setItemMeta(prevMeta);
        gui.setItem(31, prev);
        return true;
    }

    @EventHandler
    public void guiClickEvent(InventoryClickEvent e) {
        try {
            if (!Objects.equals(e.getClickedInventory(), gui)) {return;}
        } catch (NullPointerException exception) {return;}

        if (gui == null) {return;}

        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        // Check if slot has beacon or next page button
        ItemStack item = gui.getItem(e.getSlot());
        if (item == null){return;}
        Material type = item.getType();
        if (!type.equals(Material.BEACON)){return;}
        // If slot has bastion, update extender to be assigned to bastion and notify player
        String bastionId = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(TWClaim.getPlugin(), "ID"), PersistentDataType.STRING);
        if (bastionId == null){System.out.println("Bastion Id is not in gui item");}
        UUID bastionUUID = UUID.fromString(bastionId);
        Block extenderBlock = Extender.playerLastExtender.get(p.getUniqueId());
        PersistentDataContainer container = new CustomBlockData(extenderBlock, TWClaim.getPlugin());
        String extenderID = container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING);
        UUID extenderUUID = UUID.fromString(extenderID);
        Extender extender = Extender.extenders.get(extenderUUID);

        // Check if there is a previous father bastion that needs to be removed
        if (extender.getFatherBastion() != null){
            Bastion oldFather = Bastion.bastions.get(extender.getFatherBastion());
            oldFather.getExtenderChildren().remove(extenderUUID);
        }

        Bastion bastion = Bastion.bastions.get(bastionUUID);
        List<UUID> extenderChildren = bastion.getExtenderChildren();
        extender.setFatherBastion(bastionUUID);
        extenderChildren.add(extenderUUID);

        // Update PDC of Extender for GUI purposes
        container.set(new NamespacedKey(TWClaim.getPlugin(), "Father"), PersistentDataType.STRING, bastionUUID.toString());

        // Update GUI
        openExtenderGUI(p);

        // Notify Player
        p.sendMessage(ChatColor.GOLD + "Faster Bastion Added: " + bastion.getName() + " at X: " + bastion.getCoordinates()[0] + " Y: " + bastion.getCoordinates()[1] + " Z: " + bastion.getCoordinates()[2]);
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


