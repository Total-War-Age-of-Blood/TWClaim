package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.*;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
        System.out.println("Subtracting Claimed: " + claimed + "from Volume: " + volume);
        volume -= claimed;

        // Cycle through the player's inventory to count all reinforcement materials.
        HashMap<String, Integer> reinforcementTypes = Util.getReinforcementTypes();
        LinkedHashMap<Material, Integer> materialCount = getReinforcementMaterials(player, reinforcementTypes);

        // Check if there are enough items of one material to claim whole area
        HashMap<Material, Integer> selectedMaterials = new HashMap<>();
        String materialType = enoughForOne(volume, materialCount);

        if (!materialType.equalsIgnoreCase("No")){
            System.out.println("Material String: " + materialType);
            System.out.println("Material: " + Material.getMaterial(materialType));
            selectedMaterials.put(Material.getMaterial(materialType), volume);
            List<VaultValue> vaultValueList = payCost(player, selectedMaterials);
            reinforceBlocks(player, blockSelect, selectedMaterials, vaultValueList);
            return true;
        }

        // If there are enough to claim with multiple materials, ask user to confirm
        selectedMaterials = enoughForMultiple(volume, materialCount);
        if (!selectedMaterials.isEmpty()){
            List<String> argsList = new ArrayList<>(Arrays.asList(args));
            if (!argsList.contains("multiple")){
                TextComponent textComponent = Component.text("You can claim this area if you use multiple materials. ").append(Component.text("Confirm", NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/tribe claim confirm multiple")));
                twClaim.adventure().player(player).sendMessage(textComponent);
                return true;
            }
            List<VaultValue> vaultValueList = payCost(player, selectedMaterials);
            reinforceBlocks(player, blockSelect, selectedMaterials, vaultValueList);
            return true;
        } else{
            player.sendMessage(ChatColor.RED + "Not enough materials!");
            return false;
        }
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
            if (Bastion.inClaimRange(block.getLocation()) != null){
                Bastion bastion = Bastion.inClaimRange(block.getLocation());
                Block bastionBlock = block.getWorld().getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
                PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
                UUID owner = UUID.fromString(container.get(Util.getOwnKey(), PersistentDataType.STRING));
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
        RelevantInventory relevantInventory = getRelevantInventories(player);
        HashMap<Vault, Inventory> vaults = relevantInventory.getVaults();
        for (Vault vault : vaults.keySet()){
            Inventory inventory = vaults.get(vault);
            for (ItemStack item : inventory){
                if (item == null){continue;}
                if (!reinforcementTypes.containsKey(item.getType().toString().toLowerCase())){continue;}
                if (materialCount.containsKey(item.getType())){
                    materialCount.put(item.getType(), item.getAmount() + materialCount.get(item.getType()));
                    continue;
                }
                materialCount.put(item.getType(), item.getAmount());
            }
        }
        return materialCount;
    }

    public static RelevantInventory getRelevantInventories(Player player){
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        Inventory playerInventory = player.getInventory();
        HashMap<Vault, Inventory> inventories = new HashMap<>();
        UUID target = playerData.getTarget();
        for (Vault vault : Vault.vaults.values()){
            UUID owner = vault.getOwner();
            if (!target.equals(owner) && !player.getUniqueId().equals(owner)){continue;}
            Chest chest = (Chest) Bukkit.getWorld(vault.getWorldID()).getBlockAt(vault.getCoordinates()[0], vault.getCoordinates()[1], vault.getCoordinates()[2]).getState();
            Inventory chestInventory = chest.getInventory();
            inventories.put(vault, chestInventory);
        }
        return new RelevantInventory(inventories, playerInventory);
    }

    public static String enoughForOne(int volume, HashMap<Material, Integer> materialCount){
        // Iterate through materialCount to see if there are enough items of one material to claim the whole area.
        String materialType = "No";
        for (Material material : materialCount.keySet()){
            int count = materialCount.get(material);
            if (count >= volume){
                materialType = material.toString();
                break;
            }
        }
        return materialType;
    }

    public static HashMap<Material, Integer> enoughForMultiple(int volume, HashMap<Material, Integer> materialCount){
        HashMap<Material, Integer> selectedMaterials = new HashMap<>();
        int count = 0;
        for (Material material : materialCount.keySet()) {
            int difference = volume - count;
            int materialAmount = materialCount.get(material);
            if (materialAmount >= difference) {
                selectedMaterials.put(material, difference);
                return selectedMaterials;
            }
            selectedMaterials.put(material, materialAmount);
            count += materialAmount;
        }
        if (count >= volume){return selectedMaterials;}
        return new HashMap<>();
    }

    public static List<VaultValue> payCost(Player player, HashMap<Material, Integer> selectedMaterials){
        List<VaultValue> vaultValueList = new ArrayList<>();
        HashMap<Material, Integer> tempMaterials = new HashMap<>();
        for (Material material : selectedMaterials.keySet()){
            tempMaterials.put(material, selectedMaterials.get(material));
        }
        RelevantInventory relevantInventory = getRelevantInventories(player);
        HashMap<Vault, Inventory> vaults = relevantInventory.getVaults();
        for (Vault vault : vaults.keySet()){
            HashMap<Material, Integer> vaultMats = new HashMap<>();
            for (Material material : tempMaterials.keySet()){
                Inventory inventory = vaults.get(vault);
                if (tempMaterials.get(material) == 0){break;}
                int matsPaid = 0;
                for (ItemStack item : inventory){
                    if (item == null){continue;}
                    if (!material.equals(item.getType())){continue;}
                    int requiredAmount = tempMaterials.get(material);
                    if (item.getAmount() >= requiredAmount){
                        item.setAmount(item.getAmount() - requiredAmount);
                        matsPaid += requiredAmount;
                        tempMaterials.put(material, 0);
                        break;
                    }
                    tempMaterials.put(material, requiredAmount - item.getAmount());
                    matsPaid += item.getAmount();
                    item.setAmount(0);
                }
                vaultMats.put(material, matsPaid);
            }
            VaultValue vaultValue = new VaultValue(vault, vaultMats);
            vaultValueList.add(vaultValue);
        }
        return vaultValueList;
    }

    public static void reinforceBlocks(Player player, List<Block> blockSelect, HashMap<Material, Integer> selectedMaterials, List<VaultValue> vaultValueList){
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        Random random = new Random();
        // Iterate over the list and reinforce
        for (Block block : blockSelect){
            final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){continue;}
            // Randomly select a reinforcement type
            ArrayList<Material> keyList = new ArrayList<>(selectedMaterials.keySet());
            int randomNumber = random.nextInt(keyList.size());
            Material material = keyList.get(randomNumber);
            selectedMaterials.put(material, selectedMaterials.get(material) - 1);
            if (selectedMaterials.get(material) == 0){
                selectedMaterials.remove(material);
            }

            // Assign a vault
            Vault assignedVault = null;
            for (VaultValue vaultValue : vaultValueList){
                Vault vault = vaultValue.getVault();
                HashMap<Material, Integer> matsPaid = vaultValue.getMatsPaid();
                if (matsPaid.get(material) > 0){
                    assignedVault = vault;
                    matsPaid.put(material, matsPaid.get(material) - 1);
                    break;
                }
            }
            if (assignedVault == null){System.out.println("Assigned Vault null " + block.getX() + " " + block.getY() + " " + block.getZ());}
            Util.addReinforcement(block, material, playerData, assignedVault);
        }
        player.sendMessage(ChatColor.GREEN + "Area Claimed");
    }
}
