package com.ethan.twclaim.compatibility;
import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.Extender;
import com.ethan.twclaim.events.*;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.*;

import java.util.Set;
import java.util.UUID;

public class Dynmap extends DynmapCommonAPIListener implements Listener {
    static DynmapCommonAPI dynmapCommonAPI;
    @Override
    public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
        Dynmap.dynmapCommonAPI = dynmapCommonAPI;
        System.out.println("DynmapCommonAPI ENABLED HAHAHAHAHA");
    }
    @EventHandler
    public void onBastionCreate(BastionClaimEvent event){
        Bastion bastion = event.getBastion();
        UUID uuid = bastion.getUuid();
        String label = bastion.getName() + " (Out of Fuel!)";
        int[] coordinates = bastion.getCoordinates();
        int radius = bastion.getRadius();
        double x1 = coordinates[0] - radius;
        double x2 = coordinates[0] + radius;
        double z1 = coordinates[2] - radius;
        double z2 = coordinates[2] + radius;
        double[] xPoints = {x1, x2};
        double[] zPoints = {z1, z2};
        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        AreaMarker areaMarker = markerAPI.createMarkerSet(uuid.toString(), label, null, true).createAreaMarker(
                uuid.toString(), label, false, Bukkit.getWorld(bastion.getWorldId()).getName(), xPoints, zPoints, true);
        areaMarker.setFillStyle(0.25, bastion.getColor());
        areaMarker.setLineStyle(1, 0.25, bastion.getColor());
        System.out.println("Creating areamarker between X1: " + x1 + " and X2: " + x2 + " and Z1: " + z1 + " and Z2: " + z2);
        MarkerSet markerSet = markerAPI.getMarkerSet(uuid.toString());
        MarkerIcon icon = skullOrTower(bastion);
        Marker marker = markerSet.createMarker(
                uuid.toString(), label, Bukkit.getWorld(bastion.getWorldId()).getName(), coordinates[0],
                coordinates[1], coordinates[2], icon, true);
    }

    @EventHandler
    public void onBastionDestroy(BastionDestroyEvent event){
        Bastion bastion = event.getBastion();
        if (bastion.getName() == null){return;}
        UUID uuid = bastion.getUuid();
        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        markerAPI.getMarkerSet(uuid.toString()).deleteMarkerSet();
    }

    @EventHandler
    public void onExtenderChangeFather(ExtenderChangeFatherEvent event){
        Extender extender = event.getExtender();
        Bastion oldBastion = event.getOldBastion();
        Bastion newBastion = event.getNewBastion();
        int[] coordinates = extender.getCoordinates();
        int radius = newBastion.getRadius();
        double x1 = coordinates[0] - radius;
        double x2 = coordinates[0] + radius;
        double z1 = coordinates[2] - radius;
        double z2 = coordinates[2] + radius;
        double[] xPoints = {x1, x2};
        double[] zPoints = {z1, z2};
        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        // If extender has an area marker under an old bastion, delete the marker
        if (oldBastion != null){
            if (markerAPI.getMarkerSet(oldBastion.getUuid().toString()).findAreaMarker(extender.getUuid().toString()) != null){
                markerAPI.getMarkerSet(oldBastion.getUuid().toString()).findAreaMarker(extender.getUuid().toString()).deleteMarker();
                markerAPI.getMarkerSet(oldBastion.getUuid().toString()).findMarker(extender.getUuid().toString()).deleteMarker();
            }
        }
        // Check if the extender has an area marker in the bastion's marker set. If not, create a new one. If so, just change the fill color and label.
        AreaMarker areaMarker;
        if (markerAPI.getMarkerSet(newBastion.getUuid().toString()).findAreaMarker(extender.getUuid().toString()) == null){
            areaMarker = markerAPI.getMarkerSet(newBastion.getUuid().toString()).createAreaMarker(
                    extender.getUuid().toString(), newBastion.getName(), false,
                    Bukkit.getWorld(extender.getWorldID()).getName(), xPoints, zPoints, true);
            MarkerSet markerSet = markerAPI.getMarkerSet(newBastion.getUuid().toString());
            MarkerIcon icon = skullOrTower(newBastion);
            Marker marker = markerSet.createMarker(
                    extender.getUuid().toString(), newBastion.getName(), Bukkit.getWorld(newBastion.getWorldId()).getName(), coordinates[0],
                    coordinates[1], coordinates[2], icon, true);
            Block bastionBlock = Bukkit.getWorld(newBastion.getWorldId()).getBlockAt(newBastion.getCoordinates()[0], newBastion.getCoordinates()[1], newBastion.getCoordinates()[2]);
            PersistentDataContainer container = new CustomBlockData(bastionBlock, TWClaim.getPlugin());
            int fuel = container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER);
            if (!(fuel > 0)){marker.setLabel(newBastion.getName() + " (Out of Fuel!)");}
        } else{
            areaMarker = markerAPI.getMarkerSet(newBastion.getUuid().toString()).findAreaMarker(extender.getUuid().toString());
        }
        areaMarker.setFillStyle(0.25, newBastion.getColor());
        areaMarker.setLineStyle(1, 0.25, newBastion.getColor());
    }

    @EventHandler
    public void onExtenderDestroy(ExtenderDestroyEvent event){
        System.out.println("extender destroyed");
        Extender extender = event.getExtender();
        if (extender.getFatherBastion() == null){return;}
        Bastion bastion = Bastion.bastions.get(extender.getFatherBastion());
        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        markerAPI.getMarkerSet(bastion.getUuid().toString()).findAreaMarker(extender.getUuid().toString()).deleteMarker();
        markerAPI.getMarkerSet(bastion.getUuid().toString()).findMarker(extender.getUuid().toString()).deleteMarker();
    }
    @EventHandler
    public void onBastionChangeFuelState(BastionChangeFuelStateEvent event){
        Bastion bastion = event.getBastion();
        boolean hasFuel = event.isHasFuel();
        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        MarkerSet markerSet = markerAPI.getMarkerSet(bastion.getUuid().toString());
        Set<Marker> markers = markerSet.getMarkers();
        Set<AreaMarker> areaMarkers = markerSet.getAreaMarkers();
        for (Marker marker : markers){
            if (hasFuel){
                marker.setMarkerIcon(markerAPI.getMarkerIcon("tower"));
                marker.setLabel(bastion.getName());

            }else{
                marker.setMarkerIcon(markerAPI.getMarkerIcon("skull"));
                marker.setLabel(bastion.getName() + " (Out of Fuel!)");
            }
        }
        for (AreaMarker areaMarker : areaMarkers){
            if (hasFuel){
                areaMarker.setLabel(bastion.getName());
            } else{
                areaMarker.setLabel(bastion.getName() + " (Out of Fuel!)");
            }
        }
    }

    public static MarkerIcon skullOrTower(Bastion bastion){
        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        Block block = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        if(container.get(new NamespacedKey(TWClaim.getPlugin(), "fuel"), PersistentDataType.INTEGER) > 0){
            return markerAPI.getMarkerIcon("tower");
        }
        return markerAPI.getMarkerIcon("skull");
    }
}
