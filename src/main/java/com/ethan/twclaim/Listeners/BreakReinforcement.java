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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.ethan.twclaim.util.Util.bastionItem;

public class BreakReinforcement implements Listener {
    NamespacedKey materialKey = new NamespacedKey(TWClaim.getPlugin(), "material");
    NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(), "reinforcement");
    NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player player = e.getPlayer();
        Block block = e.getBlock();
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        boolean cancelBreak = cancelBreak(block, player, materialKey, key, ownKey);
        if (!cancelBreak){
            Util.removeReinforcement(container, materialKey, key, ownKey, e);
            return;
        }
        e.setCancelled(true);
    }

    // Returns false if BlockBreakEvent should not be canceled.
    public static boolean cancelBreak(Block block, Player player, NamespacedKey materialKey, NamespacedKey key, NamespacedKey ownKey){
        // Get the block's persistent data container
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        // Check that block is reinforced
        // If block is crop, do an investigation
        if (Tag.CROPS.isTagged(block.getType())){
            boolean belowReinforced = Util.checkSpecialReinforcement(Tag.CROPS, block, player);
            if (belowReinforced){return true;}
        }
        // If block is a sapling, do an investigation
        if (Tag.SAPLINGS.isTagged(block.getType())){
            boolean belowReinforced = Util.checkSpecialReinforcement(Tag.SAPLINGS, block, player);
            if (belowReinforced){return true;}
        }
        if (Tag.FLOWERS.isTagged(block.getType())){
            boolean belowReinforced = Util.checkSpecialReinforcement(Tag.FLOWERS, block, player);
            if (belowReinforced){return true;}
        }
        if (!container.has(key, PersistentDataType.INTEGER) || !container.has(ownKey, PersistentDataType.STRING)){
            // If block is door, do an investigation
            if (SwitchEvent.DOOR.contains(block.getType())) {
                if (SwitchEvent.getDoorHalf(block, block.getType())) {
                    block = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                } else {
                    block = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
                }
                container = new CustomBlockData(block, TWClaim.getPlugin());
                if (!container.has(key, PersistentDataType.INTEGER) || !container.has(ownKey, PersistentDataType.STRING)) {
                    return false;
                }
            }
            // Will catch if the block is a bastion without an owner
            return false;
        }
        // Check if player has permission to break the block. First, check if player is member of the tribe that owns
        // the block. Then, check if the player has "break" in their permission string.
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        UUID owner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
        int reinforcement = container.get(key, PersistentDataType.INTEGER);
        ArrayList<HashMap<String, Integer>> reinforcements = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");
        if ((playerData.getTribes().containsKey(owner))){
            TribeData tribe = TribeData.tribe_hashmap.get(owner);
            String permsGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permsGroup);
            if (perms.contains("r")){
                // Get material and reinforcement points from the config
                for (HashMap<String, Integer> hash : reinforcements){
                    for (String material : hash.keySet()){
                        if (!(material.equalsIgnoreCase(container.get(materialKey, PersistentDataType.STRING)))){
                            continue;
                        }
                        // When we find the material, get its key and divide the block's current reinforcement
                        int configReinforcement = hash.get(material);
                        // If it is above the percentage, drop the material as well as the block
                        if (reinforcement / configReinforcement * 100 >= (int) TWClaim.getPlugin().getConfig().get("recover-min")){
                            ItemStack item = new ItemStack(Material.matchMaterial(material));
                            player.getWorld().dropItem(block.getLocation(), item);
                        }
                        return false;
                    }
                }
                return false;
            }
        } else if (player.getUniqueId().equals(owner)){
            // Get material and reinforcement points from the config
            for (HashMap<String, Integer> hash : reinforcements){
                for (String material : hash.keySet()){
                    if (!(material.equalsIgnoreCase(container.get(materialKey, PersistentDataType.STRING)))){
                        continue;
                    }
                    // When we find the material, get its key and divide the block's current reinforcement
                    int configReinforcement = hash.get(material);
                    // If it is above the percentage, drop the material as well as the block
                    if (reinforcement / configReinforcement * 100 >= (int) TWClaim.getPlugin().getConfig().get("recover-min")){
                        ItemStack item = new ItemStack(Material.matchMaterial(material));
                        player.getWorld().dropItem(block.getLocation(), item);
                    }
                    return false;
                }
            }
            return false;
        }
        // If block is reinforced, cancel the event and lower reinforcement by 1.
        if (reinforcement - 1 <= 0){
            return false;
        }
        container.set(key, PersistentDataType.INTEGER, reinforcement - 1);
        player.sendMessage("This block is reinforced");
        player.spawnParticle(Particle.ENCHANTMENT_TABLE, block.getLocation(), 20);
        player.playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2);
        return true;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e){
        List<Block> blockList = e.blockList();
        for (Block block : blockList){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (SwitchEvent.DOOR.contains(block.getType())){
                boolean removeBlock = doorExplosion(container, block);
                if (removeBlock){
                    blockList.remove(block);
                }
                continue;
            }
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){continue;}
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                blockList.remove(block);
                block.setType(Material.AIR);
                blockList.add(block);
                ItemStack bastionItem = bastionItem();
                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), bastionItem);
            }
            int reinforcement = container.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            if (explosionDamage > reinforcement){
                Util.removeReinforcement(container, materialKey, key, ownKey, e);
                continue;
            }
            blockList.remove(block);
            container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement - explosionDamage);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e){
        List<Block> blockList = e.blockList();
        for (Block block : blockList){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (SwitchEvent.DOOR.contains(block.getType())){
                boolean removeBlock = doorExplosion(container, block);
                if (removeBlock){
                    blockList.remove(block);
                }
                continue;
            }
            if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){continue;}
            if (container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING) && !container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                blockList.remove(block);
                block.setType(Material.AIR);
                blockList.add(block);
                ItemStack bastionItem = bastionItem();
                e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), bastionItem);
            }
            int reinforcement = container.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            if (explosionDamage > reinforcement){
                Util.removeReinforcement(container, materialKey, key, ownKey, e);
                continue;
            }
            blockList.remove(block);
            container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement - explosionDamage);
        }
    }

    public boolean doorExplosion(PersistentDataContainer container, Block block){
        // Check if the door is reinforced
        // If door not reinforced, or if it can't withstand explosion, check other half for adequate reinforcement
        if (container.has(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER)){
            int reinforcement = container.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
            int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
            if (explosionDamage <= reinforcement){
                container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement - explosionDamage);
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
        if (!otherContainer.has(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER)){
            return false;}
        int reinforcement = otherContainer.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
        int explosionDamage = TWClaim.getPlugin().getConfig().getInt("explosion-damage");
        if (explosionDamage > reinforcement){
            return false;}
        return true;
    }

    @EventHandler
    public void blockBurnEvent(BlockBurnEvent e){
        Block block = e.getBlock();
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER)){return;}
        int fireDamage = TWClaim.getPlugin().getConfig().getInt("fire-damage");
        int reinforcement = container.get(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER);
        if (!(reinforcement >= fireDamage)){
            Util.removeReinforcement(container, e, materialKey, key, ownKey);
            return;
        }
        reinforcement -= fireDamage;
        container.set(new NamespacedKey(TWClaim.getPlugin(), "reinforcement"), PersistentDataType.INTEGER, reinforcement);
        e.setCancelled(true);
    }
}
