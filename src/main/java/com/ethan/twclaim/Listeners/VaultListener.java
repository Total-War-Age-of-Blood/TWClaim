package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.data.Vault;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class VaultListener implements Listener {
    /* Detect when a player edits a sign to mark a vault.
    * 1. Check if the sign is placed on the right block(s).
    * 2. Check if the block is protected by TWClaim
    * 3. Check if the player has switch perms on the chest
    * 4. If everything checks out, create vault */

    @EventHandler
    public void changeSignEvent(SignChangeEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        String line = event.getLine(0);
        if (line == null){return;}
        if (!line.equalsIgnoreCase("[twvault]")){
            for (Vault vault : Vault.vaults.values()){
                int[] signCoordinates = vault.getSignCoordinates();
                if (Arrays.equals(signCoordinates, new int[]{block.getX(), block.getY(), block.getZ()})){
                    Vault.destroyVault(vault);
                    player.sendMessage(ChatColor.RED + "Vault Destroyed");
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
                }
            }
            return;
        }

        if (!(block.getState().getBlockData() instanceof WallSign)){
            System.out.println("Not a wall sign.");
            return;
        }
        org.bukkit.block.data.type.WallSign sign = (WallSign) block.getState().getBlockData();
        Block attachedBlock = block.getRelative(sign.getFacing().getOppositeFace());
        if (!attachedBlock.getType().equals(Material.CHEST) && !attachedBlock.getType().equals(Material.TRAPPED_CHEST)){
            System.out.println(attachedBlock.getType());
            player.sendMessage(ChatColor.RED + "Only chest and trapped chest can be vault.");
            return;
        }

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) attachedBlock.getState();
        InventoryHolder chestHolder = chest.getInventory().getHolder();
        if (chestHolder instanceof DoubleChest){
            DoubleChest doubleChest = (DoubleChest) chestHolder;
            org.bukkit.block.Chest leftChest = (org.bukkit.block.Chest) doubleChest.getLeftSide();
            org.bukkit.block.Chest rightChest = (org.bukkit.block.Chest) doubleChest.getRightSide();

            for (Vault vault : Vault.vaults.values()){
                int[] vaultCoords = vault.getCoordinates();
                int[] leftChestCoords = new int[]{leftChest.getX(), leftChest.getY(), leftChest.getZ()};
                int[] rightChestCoords = new int[]{rightChest.getX(), rightChest.getY(), rightChest.getZ()};
                if (Arrays.equals(leftChestCoords, vaultCoords) || Arrays.equals(rightChestCoords, vaultCoords)){
                    player.sendMessage(ChatColor.RED + "This chest is already a vault");
                    return;
                }
            }
        } else{
            for (Vault vault : Vault.vaults.values()){
                int [] chestCoords = new int[]{chest.getX(), chest.getY(), chest.getZ()};
                int[] vaultCoords = vault.getCoordinates();
                if (Arrays.equals(chestCoords, vaultCoords)){
                    player.sendMessage(ChatColor.RED + "This chest is already a vault");
                    return;
                }
            }
        }

        PersistentDataContainer container = new CustomBlockData(attachedBlock, TWClaim.getPlugin());
        if (!container.has(Util.getOwnKey(), PersistentDataType.STRING)){
            player.sendMessage(ChatColor.RED + "Chest must be owned by player or tribe. (Reinforce, Fortify, or Claim)");
            return;
        }
        UUID tribeID = UUID.fromString(container.get(Util.getOwnKey(), PersistentDataType.STRING));
        TribeData tribeData = TribeData.tribe_hashmap.get(tribeID);
        if (!Util.isTribe(tribeID)){
            if (!tribeID.equals(player.getUniqueId())){
                player.sendMessage(ChatColor.RED + "Someone else owns this chest.");
                return;
            }
        } else{
            if (!Util.isInTribe(player.getUniqueId(), tribeID)){
                player.sendMessage(ChatColor.RED + "This chest is owned by another tribe.");
                return;
            }
            String group = tribeData.getMembers().get(player.getUniqueId());
            String perms = tribeData.getPermGroups().get(group);
            if (!perms.contains("s")){
                player.sendMessage(ChatColor.RED + "You don't have permission to make vaults for this tribe. (Perm: \"s\")");
                return;
            }
        }
        Vault vault = new Vault(UUID.randomUUID(), new int[]{attachedBlock.getX(), attachedBlock.getY(), attachedBlock.getZ()},
                new int[]{block.getX(), block.getY(), block.getZ()}, UUID.fromString(container.get(Util.getOwnKey(), PersistentDataType.STRING)),
                attachedBlock.getWorld().getUID());
        player.sendMessage("Vault Created");
    }
}
