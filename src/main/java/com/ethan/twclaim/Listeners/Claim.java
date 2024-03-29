package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.data.PlayerData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;

public class Claim implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        // Check that action is left or right click on block
        if (!(e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))){return;}
        // Check that player is in claim mode
        Player player = e.getPlayer();
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        if (!playerData.getMode().equals("Claim")){return;}
        HashMap<String, Integer[]> claimSelect = playerData.getClaimSelect();
        Block block = e.getClickedBlock();
        Integer[] blockCoordinates = {block.getX(), block.getY(), block.getZ()};
        // Set first block if left click and second if right click
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
            claimSelect.put("First", blockCoordinates);
            player.sendMessage("First block selected: (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")" + "\n"
                    + "Type /tribe claim confirm to confirm");
        } else if (e.getHand().equals(EquipmentSlot.OFF_HAND)) {
            claimSelect.put("Second", blockCoordinates);
            player.sendMessage("Second block selected: (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")" + "\n"
                    + "Type /tribe claim confirm to confirm");
        }
        PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
    }
}
