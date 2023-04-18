package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ClaimConfirm {
    // TODO prevent claim if in bastion without permission
    public static boolean claimConfirm(Player player, PlayerData playerData){
        // Check that player is in claim mode
        if (!playerData.getMode().equalsIgnoreCase("Claim")){
            player.sendMessage(ChatColor.RED + "Not in claiming mode!");
            return false;
        }
        // Check that both blocks are selected
        HashMap<String, Block> claimSelect = playerData.getClaimSelect();
        if (!(claimSelect.containsKey("First")) || !(claimSelect.containsKey("Second"))){
            player.sendMessage(ChatColor.RED + "Select blocks first!");
            return false;
        }
        // Check that claim size is within limit
        Block firstBlock = claimSelect.get("First");
        Block secondBlock = claimSelect.get("Second");
        // Calculate volume of selected area
        int xDiff = Math.abs(firstBlock.getX() - secondBlock.getX()) + 1;
        int yDiff = Math.abs(firstBlock.getY() - secondBlock.getY()) + 1;
        int zDiff = Math.abs(firstBlock.getZ() - secondBlock.getZ()) + 1;
        int volume = xDiff * yDiff * zDiff;
        if (volume > TWClaim.getPlugin().getConfig().getInt("claim-limit")){
            player.sendMessage(ChatColor.RED + "Too many blocks selected. Max selection size: " + TWClaim.getPlugin().getConfig().getInt("claim-limit"));
            return false;
        }

        // Get a list of blocks inside the area
        int lowerX = Math.min(firstBlock.getX(), secondBlock.getX());
        int lowerY = Math.min(firstBlock.getY(), secondBlock.getY());
        int lowerZ = Math.min(firstBlock.getZ(), secondBlock.getZ());
        int higherX = Math.max(firstBlock.getX(), secondBlock.getX());
        int higherY = Math.max(firstBlock.getY(), secondBlock.getY());
        int higherZ = Math.max(firstBlock.getZ(), secondBlock.getZ());
        List<Block> blockSelect = new ArrayList<>();
        for (int x = lowerX; x <= higherX; x++){
            for (int y = lowerY; y <= higherY; y++){
                for (int z = lowerZ; z <= higherZ; z++){
                    blockSelect.add(player.getWorld().getBlockAt(x, y, z));
                }
            }
        }

        // Look for blocks that are already claimed and remove number from volume
        int claimed = 0;
        for (Block block : blockSelect){
            final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                continue;
            }
            claimed += 1;
        }
        volume -= claimed;

        // Cycle through the player's inventory to get count all reinforcement materials.
        HashMap<String, Integer> reinforcementTypes = Util.getReinforcementTypes();
        LinkedHashMap<Material, Integer> materialCount = new LinkedHashMap<>();
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()){
            if (item == null){continue;}
            if (!reinforcementTypes.containsKey(item.getType().toString().toLowerCase())){continue;}
            if (materialCount.containsKey(item.getType())){
                materialCount.put(item.getType(), item.getAmount() + materialCount.get(item.getType()));
                continue;
            }
            materialCount.put(item.getType(), item.getAmount());
        }

        // Iterate through materialCount to see if there are enough items of one material to claim the whole area.
        HashMap<Material, Integer> selectedMaterials = new HashMap<>();
        int reinforcement = 0;
        String materialType = "";
        boolean enough = false;
        for (Material material : materialCount.keySet()){
            // If there are enough materials, remove them from inventory
            int count = materialCount.get(material);
            if (count >= volume){
                enough = true;
                selectedMaterials.put(material, volume);
                reinforcement = reinforcementTypes.get(material.toString().toLowerCase());
                materialType = material.toString().toLowerCase();
                // Iterate over stacks to remove the items until cost is paid
                for (ItemStack item : inventory.getContents()){
                    if (item == null){continue;}
                    if (selectedMaterials.containsKey(item.getType())){
                        int amount = item.getAmount();
                        if (amount >= volume){
                            item.setAmount(amount - selectedMaterials.get(item.getType()));
                            break;
                        }
                        selectedMaterials.put(item.getType(), selectedMaterials.get(item.getType()) - item.getAmount());
                        item.setAmount(0);
                    }
                }
                break;
            }
        }
        // If there are enough to claim with multiple materials, ask user to confirm
        if (!enough){
            int count = 0;
            for (Material material : materialCount.keySet()){
                int materialAmount = materialCount.get(material);
                // Enough to claim with multiple
                if (count + materialAmount >= volume){
                    selectedMaterials.put(material, volume - count);
                    player.sendMessage("You can claim this area if you use multiple materials. Respond \"Confirm\" if that's okay.");
                    return true;
                    // TODO implement confirmation message for claiming with multiple materials
                }
                selectedMaterials.put(material, materialAmount);
                count += materialAmount;
            }
        }
        // If there are not enough materials to claim the area, send error
        if (!enough){
            player.sendMessage(ChatColor.RED + "Not enough materials!");
            return false;
        }

        // Iterate over the list and reinforce
        for (Block block : blockSelect){
            final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){continue;}
            container.set(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING, playerData.getTarget().toString());
            container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement);
            container.set(new NamespacedKey(TWClaim.getPlugin(), "material"), PersistentDataType.STRING, materialType);
        }
        player.sendMessage(ChatColor.GREEN + "Area claimed");
        return true;
    }
}
