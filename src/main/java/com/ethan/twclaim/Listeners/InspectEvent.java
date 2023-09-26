package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InspectEvent implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        // Cancel if event is firing for right click.
        if (e.getHand().equals(EquipmentSlot.OFF_HAND)){return;}
        // Make sure player was interacting with a block
        if (e.getClickedBlock() == null){return;}
        Player player = e.getPlayer();
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());

        // Check if player is inspecting
        if (!playerData.getMode().equalsIgnoreCase("Inspect")){return;}
        e.setCancelled(true);

        // Give player information on the block
        List<String> messages = new ArrayList<>();
        Block block = e.getClickedBlock();
        // Coordinates of the block
        messages.add("Coordinates: X(" + block.getX() + ")" + " Y(" + block.getY() + ")" + " Z(" + block.getZ() + ")");
        // Owner of the block
        final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
            messages.add("Owner: None");
        } else{
            UUID owner = UUID.fromString(container.get(Util.getOwnKey(), PersistentDataType.STRING));
            // Check if owner is tribe or player
            if (TribeData.tribe_hashmap.containsKey(owner)){
                messages.add("Owner: " + TribeData.tribe_hashmap.get(owner).getName());
            } else{
                OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(owner);
                messages.add("Owner: " + ownerPlayer.getName());
            }
        }
        // Reinforcement Value
        if (container.get(Util.getKey(), PersistentDataType.INTEGER) == null){
            messages.add("Reinforcement: None");
        } else {
            int reinforcement = container.get(Util.getKey(), PersistentDataType.INTEGER);
            messages.add("Reinforcement: " + reinforcement);
        }

        // Number of Breaks
        if (container.get(Util.getBreakCount(), PersistentDataType.INTEGER) == null){
            messages.add("Breaks: None");
        } else {
            int breaks = container.get(Util.getBreakCount(), PersistentDataType.INTEGER);
            messages.add("Breaks: " + breaks);
        }
        player.sendMessage(String.join("\n", messages));
    }
}
