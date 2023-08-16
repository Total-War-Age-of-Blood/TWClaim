package com.ethan.twclaim.Listeners;

import com.ethan.twclaim.TWClaim;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class Pistons implements Listener {
// Prevent players from using pistons to pop reinforced doors or move reinforced blocks.
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event){
        Block piston = event.getBlock();
        List<Block> affectedBlocks = event.getBlocks();
        NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
        for (Block block : affectedBlocks){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (container.has(ownKey, PersistentDataType.STRING)){
                PersistentDataContainer pistonContainer = new CustomBlockData(piston, TWClaim.getPlugin());
                if (!container.has(ownKey, PersistentDataType.STRING)){continue;}
                UUID blockOwner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
                if(!pistonContainer.has(ownKey, PersistentDataType.STRING)){
                    event.setCancelled(true);
                    return;
                }
                UUID pistonOwner = UUID.fromString(pistonContainer.get(ownKey, PersistentDataType.STRING));
                if (blockOwner.equals(pistonOwner)){continue;}
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event){
        Block piston = event.getBlock();
        List<Block> affectedBlocks = event.getBlocks();
        NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
        for (Block block : affectedBlocks){
            PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
            if (container.has(ownKey, PersistentDataType.STRING)){
                PersistentDataContainer pistonContainer = new CustomBlockData(piston, TWClaim.getPlugin());
                if (!container.has(ownKey, PersistentDataType.STRING)){continue;}
                UUID blockOwner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
                if(!pistonContainer.has(ownKey, PersistentDataType.STRING)){
                    event.setCancelled(true);
                    return;
                }
                UUID pistonOwner = UUID.fromString(pistonContainer.get(ownKey, PersistentDataType.STRING));
                if (blockOwner.equals(pistonOwner)){continue;}
                event.setCancelled(true);
                return;
            }
        }
    }
}
