package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.ethan.twclaim.util.Util.*;

public class BreakReinforcement implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player player = e.getPlayer();
        Block block = e.getBlock();
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        boolean cancelBreak = cancelBreak(block, player, TWClaim.getPlugin());
        if (!cancelBreak){
            Util.removeReinforcement(container, e);
            return;
        }
        e.setCancelled(true);
    }

    // Returns false if BlockBreakEvent should not be canceled.
    public static boolean cancelBreak(Block block, Player player, Plugin TWClaim){
        // Get the block's persistent data container
        PersistentDataContainer container = new CustomBlockData(block, TWClaim);
        String material = container.get(getMaterialKey(), PersistentDataType.STRING);
        HashMap<String, Integer> reinforcements = Util.getReinforcementTypes();
        // Check that reinforcement type exists
        if (!reinforcements.containsKey(material)){
            Util.removeKeys(container);
            return false;}
        int configReinforcement = reinforcements.get(material);
        // Because breakCount is new, we need a way to convert old blocks. Here it is:
        if (container.has(getKey(), PersistentDataType.INTEGER) && !container.has(getBreakCount(), PersistentDataType.INTEGER)){
            // Best just to reset the reinforcement
            Util.convertToBreakCount(container);
        }

        // Check block is reinforced and investigate special blocks
        if (Tag.CROPS.isTagged(block.getType())){
            boolean belowReinforced = Util.checkSpecialReinforcement(Tag.CROPS, block, player);
            if (belowReinforced){return true;}
        }
        if (Tag.SAPLINGS.isTagged(block.getType())){
            boolean belowReinforced = Util.checkSpecialReinforcement(Tag.SAPLINGS, block, player);
            if (belowReinforced){return true;}
        }
        if (Tag.FLOWERS.isTagged(block.getType())){
            boolean belowReinforced = Util.checkSpecialReinforcement(Tag.FLOWERS, block, player);
            if (belowReinforced){return true;}
        }
        if (!container.has(getBreakCount(), PersistentDataType.INTEGER) || !container.has(getOwnKey(), PersistentDataType.STRING)){
            if (SwitchEvent.DOOR.contains(block.getType())) {
                if (SwitchEvent.getDoorHalf(block, block.getType())) {
                    block = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                } else {
                    block = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
                }
                container = new CustomBlockData(block, TWClaim);
                if (!container.has(getBreakCount(), PersistentDataType.INTEGER) || !container.has(getOwnKey(), PersistentDataType.STRING)) {
                    return false;
                }
            }
            // Will catch if the block is a bastion without an owner
            return false;
        }
        // Check if player has permission to break the block. First, check if player is member of the tribe that owns
        // the block. Then, check if the player has "break" in their permission string.
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        UUID owner = UUID.fromString(container.get(getOwnKey(), PersistentDataType.STRING));

        int breakCount = container.get(getBreakCount(), PersistentDataType.INTEGER);
        int recoverMin = TWClaim.getConfig().getInt("recover-min");
        if ((playerData.getTribes().containsKey(owner))){
            TribeData tribe = TribeData.tribe_hashmap.get(owner);
            String permsGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permsGroup);
            if (perms.contains("r")){
                // If it is above the percentage, drop the material as well as the block
                if (100 - (breakCount / configReinforcement * 100) >= recoverMin){
                    ItemStack item = new ItemStack(Material.matchMaterial(material));
                    player.getWorld().dropItem(block.getLocation(), item);
                }
                return false;
            }
            return false;
        }
        else if (player.getUniqueId().equals(owner)){
            // Check if block should drop reinforcement material
            System.out.println(recoverMin);
            System.out.println((float) breakCount/ (float) configReinforcement);
            if (100 - (((float) breakCount / (float) configReinforcement) * 100) >= (float) recoverMin){
                ItemStack item = new ItemStack(Material.matchMaterial(material));
                player.getWorld().dropItem(block.getLocation(), item);
            }
            return false;
        }
        // If block is reinforced, cancel the event and ++breakCount
        if (breakCount + 1 > configReinforcement){
            return false;
        }
        container.set(getBreakCount(), PersistentDataType.INTEGER, breakCount + 1);
        player.sendMessage("This block is reinforced");
        player.spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation(), 20);
        player.playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
        return true;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e){
        List<Block> blockList = e.blockList();
        List<Block> removeList = new ArrayList<>();
        for (Block block : blockList){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (SwitchEvent.DOOR.contains(block.getType())){
                boolean removeBlock = doorExplosion(container, block);
                if (removeBlock){
                    removeList.add(block);
                }
                continue;
            }
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){continue;}
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                removeList.add(block);
                block.setType(Material.AIR);
                e.blockList().add(block);
                ItemStack bastionItem = bastionItem();
                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), bastionItem);
            }
            if (container.has(Util.getKey(), PersistentDataType.INTEGER)){convertToBreakCount(container);}
            int breakCount = container.get(Util.getBreakCount(), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            int configReinforcement = getReinforcementTypes().get(container.get(Util.getMaterialKey(), PersistentDataType.STRING));
            if (breakCount + explosionDamage > configReinforcement){
                Util.removeReinforcement(container, e);
                continue;
            }
            removeList.add(block);
            container.set(Util.getBreakCount(), PersistentDataType.INTEGER, breakCount + explosionDamage);
        }
        e.blockList().removeAll(removeList);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e){
        List<Block> blockList = e.blockList();
        List<Block> removeList = new ArrayList<>();
        for (Block block : blockList){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (SwitchEvent.DOOR.contains(block.getType())){
                boolean removeBlock = doorExplosion(container, block);
                if (removeBlock){
                    removeList.add(block);
                }
                continue;
            }
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){continue;}
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                removeList.add(block);
                block.setType(Material.AIR);
                e.blockList().add(block);
                ItemStack bastionItem = bastionItem();
                e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), bastionItem);
            }
            if (container.has(Util.getKey(), PersistentDataType.INTEGER)){convertToBreakCount(container);}
            int breakCount = container.get(Util.getBreakCount(), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            int configReinforcement = getReinforcementTypes().get(container.get(Util.getMaterialKey(), PersistentDataType.STRING));
            if (breakCount + explosionDamage > configReinforcement){
                Util.removeReinforcement(container, e);
                continue;
            }
            removeList.add(block);
            container.set(Util.getBreakCount(), PersistentDataType.INTEGER, breakCount + explosionDamage);
        }
        e.blockList().removeAll(removeList);
    }

    public boolean doorExplosion(PersistentDataContainer container, Block block){
        // Check if the door is reinforced
        // If door not reinforced, or if it can't withstand explosion, check other half for adequate reinforcement
        if (container.has(Util.getKey(), PersistentDataType.INTEGER) || container.has(Util.getBreakCount(), PersistentDataType.INTEGER)){
            if (container.has(Util.getKey(), PersistentDataType.INTEGER)){convertToBreakCount(container);}
            int configReinforcement = Util.getReinforcementTypes().get(container.get(Util.getMaterialKey(), PersistentDataType.STRING));
            int breakCount = container.get(Util.getBreakCount(), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            container.set(Util.getBreakCount(), PersistentDataType.INTEGER, breakCount + explosionDamage);
            if (breakCount + explosionDamage <= configReinforcement){
                return true;
            }
        }
        Block otherHalf;
        if (SwitchEvent.getDoorHalf(block, block.getType())){
            otherHalf = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
        } else{
            otherHalf = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
        }
        PersistentDataContainer otherContainer = new CustomBlockData(otherHalf, TWClaim.getPlugin());
        if (!otherContainer.has(Util.getKey(), PersistentDataType.INTEGER) && !otherContainer.has(Util.getBreakCount(), PersistentDataType.INTEGER)){
            return false;}
        if (otherContainer.has(Util.getKey(), PersistentDataType.INTEGER)){convertToBreakCount(otherContainer);}
        int breakCount = otherContainer.get(Util.getBreakCount(), PersistentDataType.INTEGER);
        int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
        int configReinforcement = Util.getReinforcementTypes().get(otherContainer.get(Util.getMaterialKey(), PersistentDataType.STRING));
        otherContainer.set(Util.getBreakCount(), PersistentDataType.INTEGER, breakCount + explosionDamage);
        return breakCount + explosionDamage <= configReinforcement;
    }

    @EventHandler
    public void blockBurnEvent(BlockBurnEvent e){
        Block block = e.getBlock();
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        if (!container.has(Util.getKey(), PersistentDataType.INTEGER) && !container.has(Util.getBreakCount(), PersistentDataType.INTEGER)){return;}
        if (container.has(Util.getKey(), PersistentDataType.INTEGER)){convertToBreakCount(container);}
        int fireDamage = TWClaim.getPlugin().getConfig().getInt("fire-damage");
        int breakCount = container.get(Util.getBreakCount(), PersistentDataType.INTEGER);
        int configReinforcement = getReinforcementTypes().get(container.get(Util.getMaterialKey(), PersistentDataType.STRING));
        if (breakCount + fireDamage > configReinforcement){
            Util.removeReinforcement(container, e);
            return;
        }
        container.set(Util.getBreakCount(), PersistentDataType.INTEGER, breakCount + fireDamage);
        e.setCancelled(true);
    }
}
