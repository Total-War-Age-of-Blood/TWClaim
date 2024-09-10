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
import net.countercraft.movecraft.util.hitboxes.HitBox;
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
        // TODO This becomes a problem once the craft becomes large enough that some of the blocks end up inside itself when it moves.
        //  Blocks processed first may overwrite the containers of blocks processed after before they are transferred.
        //  Instead, all data should be gathered from the craft before any data is moved from one container to another.
        //  Alternatively, you could make it check if the block being written to has a container already and trigger
        //  a cascade forward where that block's transfer is handled before the original transfer.
        //  Not sure how that would work with sets and iterators though.
        for (MovecraftLocation location : oldHitbox){
            PersistentDataContainer container = new CustomBlockData(event.getCraft().getWorld().getBlockAt(location.getX(), location.getY(), location.getZ()), TWClaim.getPlugin());
            if (container.has(Util.getOwnKey(), PersistentDataType.STRING)){
                PersistentDataContainer container2 = new CustomBlockData(event.getCraft().getWorld().getBlockAt(location.getX() + differences[0], location.getY() + differences[1], location.getZ() + differences[2]), TWClaim.getPlugin());
                container2.set(Util.getMaterialKey(), PersistentDataType.STRING, container.get(Util.getMaterialKey(), PersistentDataType.STRING));
                container2.set(Util.getBreakCount(), PersistentDataType.INTEGER, container.get(Util.getBreakCount(), PersistentDataType.INTEGER));
                container2.set(Util.getOwnKey(), PersistentDataType.STRING, container.get(Util.getOwnKey(), PersistentDataType.STRING));
                container2.set(Util.getVaultID(), PersistentDataType.STRING, container.get(Util.getVaultID(), PersistentDataType.STRING));
                container.remove(Util.getMaterialKey());
                container.remove(Util.getBreakCount());
                container.remove(Util.getOwnKey());
                container.remove(Util.getVaultID());
            }
        }
    }

    // We will find where the blocks go by finding the distance to midpoint in their original location and then calculating how much they would travel if the body rotates 90 degrees
    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        System.out.println("Beginning Rotate");
        Craft craft = event.getCraft();
        Set<TrackedLocation> trackedLocations = craft.getTrackedLocations().get(craftKey);
        if (trackedLocations == null){return;}
        System.out.println("Beginning iteration");
        Iterator<TrackedLocation> iterator = trackedLocations.iterator();
        int count = 1;
        while (count <= trackedLocations.size()){
            System.out.println(count);
            count++;
            TrackedLocation trackedLocation = iterator.next();
            MovecraftLocation location;
            if (event.getRotation().equals(MovecraftRotation.CLOCKWISE)){
                trackedLocation.rotate(MovecraftRotation.CLOCKWISE, craft.getHitBox().getMidPoint());
                location = trackedLocation.getAbsoluteLocation();
                System.out.println("New location X: " + location.getX() + " Y: " + location.getY() + " Z: " +location.getZ());
                trackedLocation.rotate(MovecraftRotation.ANTICLOCKWISE, craft.getHitBox().getMidPoint());
            } else if (event.getRotation().equals(MovecraftRotation.ANTICLOCKWISE)) {
                trackedLocation.rotate(MovecraftRotation.ANTICLOCKWISE, craft.getHitBox().getMidPoint());
                location = trackedLocation.getAbsoluteLocation();
                trackedLocation.rotate(MovecraftRotation.CLOCKWISE, craft.getHitBox().getMidPoint());
            } else {return;}
            MovecraftLocation originalLocation = trackedLocation.getAbsoluteLocation();
            System.out.println("Original location X: " + originalLocation.getX() + " Y:" + originalLocation.getY() + " Z: " + originalLocation.getZ());
            PersistentDataContainer oldContainer = new CustomBlockData(craft.getWorld().getBlockAt(originalLocation.getX(), originalLocation.getY(), originalLocation.getZ()), TWClaim.getPlugin());
            PersistentDataContainer container = new CustomBlockData(craft.getWorld().getBlockAt(location.getX(), location.getY(), location.getZ()), TWClaim.getPlugin());
            if (oldContainer.has(Util.getOwnKey(), PersistentDataType.STRING)){
                container.set(Util.getMaterialKey(), PersistentDataType.STRING, oldContainer.get(Util.getMaterialKey(), PersistentDataType.STRING));
                container.set(Util.getBreakCount(), PersistentDataType.INTEGER, oldContainer.get(Util.getBreakCount(), PersistentDataType.INTEGER));
                container.set(Util.getOwnKey(), PersistentDataType.STRING, oldContainer.get(Util.getOwnKey(), PersistentDataType.STRING));
                container.set(Util.getVaultID(), PersistentDataType.STRING, oldContainer.get(Util.getVaultID(), PersistentDataType.STRING));
                Util.removeKeys(oldContainer);
            } else {
                System.out.println("SOMETHING WENT WRONG WITH MOVECRAFT ROTATION CLAIM CHECKING");
            }
        }
        System.out.println("Finished rotation");
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
    }

    @EventHandler
    public void onReleaseCraft(CraftReleaseEvent event){
        Craft craft = event.getCraft();
        craft.getTrackedLocations().clear();
    }
}
