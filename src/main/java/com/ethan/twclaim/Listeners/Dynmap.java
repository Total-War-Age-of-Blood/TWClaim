package com.ethan.twclaim.Listeners;


import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.events.BastionClaimEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;

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
        String label = bastion.getName();
        int[] coordinates = bastion.getCoordinates();
        int radius = bastion.getRadius();
        double x1 = coordinates[0] - radius;
        double x2 = coordinates[1] + radius;
        double z1 = coordinates[0] - radius;
        double z2 = coordinates[1] + radius;
        double[] xPoints = {x1, x2};
        double[] zPoints = {z1, z2};

        final MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
        AreaMarker areaMarker = markerAPI.createMarkerSet(uuid.toString(), label, null, true).createAreaMarker(
                uuid.toString(), label, false, Bukkit.getWorld(bastion.getWorldId()).getName(), xPoints, zPoints, true);
        MarkerIcon icon = markerAPI.getMarkerIcon("Tower");
        Marker marker = markerAPI.createMarkerSet(uuid.toString(), label, null, true).createMarker(
                uuid.toString(), label, Bukkit.getWorld(bastion.getWorldId()).getName(), coordinates[0],
                coordinates[1], coordinates[2], icon, true);
        areaMarker.setFillStyle(1, 1);
        areaMarker.setLineStyle(1, 1, 1111);
        marker.setDescription("Test");
    }
}
