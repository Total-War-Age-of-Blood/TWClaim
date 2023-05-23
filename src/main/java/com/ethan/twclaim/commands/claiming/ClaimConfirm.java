package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ClaimConfirm {
    public static boolean claimConfirm(Player player, PlayerData playerData, TWClaim twClaim, String[] args){
        // Check that player is in claim mode
        if (!playerData.getMode().equalsIgnoreCase("Claim")){
            player.sendMessage(ChatColor.RED + "Not in claiming mode!");
            return false;
        }
        // Check that both blocks are selected
        HashMap<String, Integer[]> claimSelect = playerData.getClaimSelect();
        if (!(claimSelect.containsKey("First")) || !(claimSelect.containsKey("Second"))){
            player.sendMessage(ChatColor.RED + "Select blocks first!");
            return false;
        }
        // Check that claim size is within limit
        int volume = getVolume(player);
        if (volume > TWClaim.getPlugin().getConfig().getInt("claim-limit")){
            player.sendMessage(ChatColor.RED + "Too many blocks selected. Max selection size: " + TWClaim.getPlugin().getConfig().getInt("claim-limit"));
            return false;
        }

        // Get a list of blocks inside the area
        List<Block> blockSelect = getBlockList(player);

        // Check if any of the selected blocks are within bastion range
        if (isInEnemyBastion(blockSelect, player)){return false;}

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

        // Cycle through the player's inventory to count all reinforcement materials.
        HashMap<String, Integer> reinforcementTypes = Util.getReinforcementTypes();
        System.out.println(reinforcementTypes);
        LinkedHashMap<Material, Integer> materialCount = getReinforcementMaterials(player, reinforcementTypes);

        // Iterate through materialCount to see if there are enough items of one material to claim the whole area.
        Inventory inventory = player.getInventory();
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
                    // TODO implement claiming with multiple materials
                    List<String> argsList = new ArrayList<>();
                    for (String arg: args){
                        argsList.add(arg);
                    }
                    if (!argsList.contains("multiple")){
                        TextComponent textComponent = Component.text("You can claim this area if you use multiple materials. ").append(Component.text("Confirm", NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/tribe claim confirm multiple")));
                        twClaim.adventure().player(player).sendMessage(textComponent);
                        return true;
                    }
                    // Paying with multiple materials
                    for (ItemStack item : inventory.getContents()){
                        if (item == null){continue;}
                        Material itemMat = item.getType();
                        // Check if item is selected reinforcement material
                        if (!selectedMaterials.containsKey(itemMat)){continue;}
                        // Check if volume would equal or go below 0
                        int amount = item.getAmount();
                        // Cost has not been paid
                        if (!(volume - amount <= 0)){
                            item.setAmount(0);
                            volume -= amount;
                            continue;
                        }
                        // Cost has been paid
                        item.setAmount(amount - volume);

                        // Iterate over the list and reinforce
                        for (Block block : blockSelect){
                            final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){continue;}
                            boolean matSelected = false;
                            while (!matSelected){
                                // Randomly select a reinforcement type
                                ArrayList<Material> keyList = new ArrayList<>(selectedMaterials.keySet());
                                Random random = new Random();
                                int randomNumber = random.nextInt(keyList.size());
                                System.out.println(keyList);
                                System.out.println(randomNumber);
                                materialType = keyList.get(randomNumber).toString().toLowerCase();
                                reinforcement = reinforcementTypes.get(materialType);
                                // If the randomly selected material is available, subtract 1 of that material from selectedMaterials
                                if (selectedMaterials.get(keyList.get(randomNumber)) > 0){
                                    selectedMaterials.put(keyList.get(randomNumber), selectedMaterials.get(keyList.get(randomNumber)) - 1);
                                    matSelected = true;
                                }
                            }
                            container.set(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING, playerData.getTarget().toString());
                            container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement);
                            container.set(new NamespacedKey(TWClaim.getPlugin(), "material"), PersistentDataType.STRING, materialType);
                        }
                        player.sendMessage(ChatColor.GREEN + "Area claimed");
                        return true;
                    }
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

    // TODO refactor some code into this function
    public static void confirmMultiple(Player player){
        return;
    }

    public static int getVolume(Player player){
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        HashMap<String, Integer[]> claimSelect = playerData.getClaimSelect();
        Integer[] firstBlockCoordinates = claimSelect.get("First");
        Block firstBlock = player.getWorld().getBlockAt(firstBlockCoordinates[0], firstBlockCoordinates[1], firstBlockCoordinates[2]);
        Integer[] secondBlockCoordinates = claimSelect.get("Second");
        Block secondBlock = player.getWorld().getBlockAt(secondBlockCoordinates[0], secondBlockCoordinates[1], secondBlockCoordinates[2]);
        // Calculate volume of selected area
        int xDiff = Math.abs(firstBlock.getX() - secondBlock.getX()) + 1;
        int yDiff = Math.abs(firstBlock.getY() - secondBlock.getY()) + 1;
        int zDiff = Math.abs(firstBlock.getZ() - secondBlock.getZ()) + 1;
        return xDiff * yDiff * zDiff;
    }

    public static List<Block> getBlockList(Player player){
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        HashMap<String, Integer[]> claimSelect = playerData.getClaimSelect();
        Integer[] firstBlockCoordinates = claimSelect.get("First");
        Block firstBlock = player.getWorld().getBlockAt(firstBlockCoordinates[0], firstBlockCoordinates[1], firstBlockCoordinates[2]);
        Integer[] secondBlockCoordinates = claimSelect.get("Second");
        Block secondBlock = player.getWorld().getBlockAt(secondBlockCoordinates[0], secondBlockCoordinates[1], secondBlockCoordinates[2]);
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
        return blockSelect;
    }

    public static boolean isInEnemyBastion(List<Block> blockSelect, Player player){
        for (Block block : blockSelect){
            if (Bastion.inBastionRange(block.getLocation()) != null){
                Bastion bastion = Bastion.inBastionRange(block.getLocation());
                Block bastionBlock = block.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
                PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
                UUID owner = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING));
                if (Util.isTribe(owner)){
                    if (!Util.isInTribe(player.getUniqueId(), owner)){
                        player.sendMessage(ChatColor.RED + "Selected area intersects foreign bastion radius");
                        return true;
                    }
                } else{
                    if (!player.getUniqueId().equals(owner)){
                        player.sendMessage(ChatColor.RED + "Selected area intersects foreign bastion radius");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static LinkedHashMap<Material, Integer> getReinforcementMaterials(Player player, HashMap<String, Integer> reinforcementTypes){
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
        return materialCount;
    }

    // TODO refactor some code into this function
    public static void getSelectedMaterials(){

    }
}
