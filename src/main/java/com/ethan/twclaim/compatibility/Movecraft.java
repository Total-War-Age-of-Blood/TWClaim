package com.ethan.twclaim.compatibility;
import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.TrackedLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Movecraft implements Listener {
    // When a craft moves, find all the protected blocks in the craft and move those protections along with the craft.
    @EventHandler
    public void onCraftMove(CraftTranslateEvent event){
        HitBox oldHitbox = event.getOldHitBox();
        HitBox hitbox = event.getNewHitBox();

        MovecraftLocation oldMidPoint = oldHitbox.getMidPoint();
        MovecraftLocation midPoint = hitbox.getMidPoint();
        int[] differences = {midPoint.getX() - oldMidPoint.getX(), midPoint.getY() - oldMidPoint.getY(), midPoint.getZ() - oldMidPoint.getZ()};
        for (MovecraftLocation originalLocation : oldHitbox){
            PersistentDataContainer originalContainer = new CustomBlockData(event.getCraft().getWorld().getBlockAt(originalLocation.getX(), originalLocation.getY(), originalLocation.getZ()), TWClaim.getPlugin());
            if (originalContainer.has(Util.getOwnKey(), PersistentDataType.STRING)){
                PersistentDataContainer futureContainer = new CustomBlockData(event.getCraft().getWorld().getBlockAt(originalLocation.getX() + differences[0], originalLocation.getY() + differences[1], originalLocation.getZ() + differences[2]), TWClaim.getPlugin());
                futureContainer.set(Util.getMaterialNewKey(), PersistentDataType.STRING, originalContainer.get(Util.getMaterialKey(), PersistentDataType.STRING));
                futureContainer.set(Util.getBreakCountNew(), PersistentDataType.INTEGER, originalContainer.get(Util.getBreakCount(), PersistentDataType.INTEGER));
                futureContainer.set(Util.getOwnNewKey(), PersistentDataType.STRING, originalContainer.get(Util.getOwnKey(), PersistentDataType.STRING));
                futureContainer.set(Util.getVaultIDNew(), PersistentDataType.STRING, originalContainer.get(Util.getVaultID(), PersistentDataType.STRING));
                originalContainer.remove(Util.getMaterialKey());
                originalContainer.remove(Util.getOwnKey());
                originalContainer.remove(Util.getBreakCount());
                originalContainer.remove(Util.getVaultID());
            }
        }
        for (MovecraftLocation futureLocation : hitbox){
            PersistentDataContainer futureContainer = new CustomBlockData(event.getCraft().getWorld().getBlockAt(futureLocation.getX(), futureLocation.getY(), futureLocation.getZ()), TWClaim.getPlugin());
            if (futureContainer.has(Util.getOwnNewKey(), PersistentDataType.STRING)){
                futureContainer.set(Util.getMaterialKey(), PersistentDataType.STRING, futureContainer.get(Util.getMaterialNewKey(), PersistentDataType.STRING));
                futureContainer.set(Util.getBreakCount(), PersistentDataType.INTEGER, futureContainer.get(Util.getBreakCountNew(), PersistentDataType.INTEGER));
                futureContainer.set(Util.getOwnKey(), PersistentDataType.STRING, futureContainer.get(Util.getOwnNewKey(), PersistentDataType.STRING));
                futureContainer.set(Util.getVaultID(), PersistentDataType.STRING, futureContainer.get(Util.getVaultIDNew(), PersistentDataType.STRING));
                futureContainer.remove(Util.getMaterialNewKey());
                futureContainer.remove(Util.getBreakCountNew());
                futureContainer.remove(Util.getOwnNewKey());
                futureContainer.remove(Util.getVaultIDNew());
            }
        }
    }
    
    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        System.out.println("Beginning Rotate");
        Craft craft = event.getCraft();
        MovecraftLocation originPoint = event.getOriginPoint();
        Set<TrackedLocation> trackedLocations = craft.getTrackedLocations().get(craftKey);
        // if (trackedLocations == null){return;}
        System.out.println("Beginning iteration");
        // Iterator<TrackedLocation> iterator = trackedLocations.iterator();
        int count = 1;
        for (MovecraftLocation originalLocation : event.getOldHitBox()){
            System.out.println(count);
            count++;
            MovecraftLocation futureLocation = MathUtils.rotateVec(event.getRotation(), originalLocation.subtract(originPoint)).add(originPoint);
            PersistentDataContainer oldContainer = new CustomBlockData(craft.getWorld().getBlockAt(originalLocation.getX(), originalLocation.getY(), originalLocation.getZ()), TWClaim.getPlugin());
            PersistentDataContainer container = new CustomBlockData(craft.getWorld().getBlockAt(futureLocation.getX(), futureLocation.getY(), futureLocation.getZ()), TWClaim.getPlugin());
            if (oldContainer.has(Util.getOwnKey(), PersistentDataType.STRING)){
                System.out.println("Original location: " + originalLocation.toString());
                System.out.println("New location: " + futureLocation.toString());
                System.out.println("Adding NewKeys to block");
                container.set(Util.getMaterialNewKey(), PersistentDataType.STRING, oldContainer.get(Util.getMaterialKey(), PersistentDataType.STRING));
                container.set(Util.getBreakCountNew(), PersistentDataType.INTEGER, oldContainer.get(Util.getBreakCount(), PersistentDataType.INTEGER));
                container.set(Util.getOwnNewKey(), PersistentDataType.STRING, oldContainer.get(Util.getOwnKey(), PersistentDataType.STRING));
                container.set(Util.getVaultIDNew(), PersistentDataType.STRING, oldContainer.get(Util.getVaultID(), PersistentDataType.STRING));
                oldContainer.remove(Util.getMaterialKey());
                oldContainer.remove(Util.getOwnKey());
                oldContainer.remove(Util.getBreakCount());
                oldContainer.remove(Util.getVaultID());
            }
        }
        System.out.println("Finished rotation processing");
        System.out.println("Beginning post-processing");

        HitBox hitbox = event.getNewHitBox();
        for (MovecraftLocation location : hitbox){
            PersistentDataContainer container = new CustomBlockData(craft.getWorld().getBlockAt(location.getX(), location.getY(), location.getZ()), TWClaim.getPlugin());
            if (container.has(Util.getOwnNewKey(), PersistentDataType.STRING)){
                System.out.println("Moving NewKeys to Keys");
                container.set(Util.getOwnKey(), PersistentDataType.STRING, container.get(Util.getOwnNewKey(), PersistentDataType.STRING));
                container.set(Util.getMaterialKey(), PersistentDataType.STRING, container.get(Util.getMaterialNewKey(), PersistentDataType.STRING));
                container.set(Util.getBreakCount(), PersistentDataType.INTEGER, container.get(Util.getBreakCountNew(), PersistentDataType.INTEGER));
                container.set(Util.getVaultID(), PersistentDataType.STRING, container.get(Util.getVaultIDNew(), PersistentDataType.STRING));
            }
            container.remove(Util.getOwnNewKey());
            container.remove(Util.getMaterialNewKey());
            container.remove(Util.getBreakCountNew());
            container.remove(Util.getVaultIDNew());
        }

        System.out.println("Rotation Processing complete");
    }

    // When a craft is piloted, find all protected locations in Hitbox and assign them to be tracked.
    private static final NamespacedKey craftKey = new NamespacedKey(TWClaim.getPlugin(), "Craft");
    @EventHandler
    public void onPilotCraft(CraftPilotEvent event){
        Craft craft = event.getCraft();
        HitBox hitbox = craft.getHitBox();
        Set<TrackedLocation> locations = new HashSet<>();
        int count = 0;
        for (MovecraftLocation location : hitbox){
            count++;
            PersistentDataContainer container = new CustomBlockData(craft.getWorld().getBlockAt(location.getX(), location.getY(), location.getZ()), TWClaim.getPlugin());
            if (container.has(Util.getOwnKey())){
                TrackedLocation trackedLocation = new TrackedLocation(craft, location);
                locations.add(trackedLocation);
                System.out.println(count + " Block at X: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ() + " Added to tracked locations");
            }
        }
        craft.getTrackedLocations().put(craftKey, locations);
        System.out.println("Piloting Done!");
        System.out.println("Size of set: " + locations.size());
    }

    @EventHandler
    public void onReleaseCraft(CraftReleaseEvent event){
        Craft craft = event.getCraft();
        craft.getTrackedLocations().clear();
    }
}
