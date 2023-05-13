package com.ethan.twclaim.commands.claiming;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.jeff_media.customblockdata.CustomBlockData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TribeMap {
    // TODO the tribe map should be oriented the same direction the player is facing by default
    public static boolean tribeMap(Player player, TWClaim twClaim){
        // Getting location info
        int radius = TWClaim.getPlugin().getConfig().getInt("map-radius");
        Location location = player.getLocation();

        // Iterate through list to assemble message to player
       generateMap(player, radius, location, twClaim, "N");
        return true;
    }

    public static boolean tribeMap(Player player, TWClaim twClaim, int ylevel, String direction){
        Location location = player.getLocation();
        int radius = TWClaim.getPlugin().getConfig().getInt("map-radius");
        int py = location.getBlockY();
        if (Math.abs(py - ylevel) > radius){
            player.sendMessage(ChatColor.RED + "Y level out of range");
            return false;
        }
        // If y level within range, set location y level to y-level and generate map.
        location.setY(ylevel);
        generateMap(player, radius, location, twClaim, direction);
        return true;
    }

    public static List<UUID> collectData(Player player, int radius, int px, int py, int pz, String direction){
        List<UUID> owners = new ArrayList<>();
        // For loop to get ownership data from blocks in radius. Loop depends on map direction
        if (direction.equalsIgnoreCase("N")){
            for (int z = -radius; z <= radius; z++){
                for (int x = -radius; x <= radius; x++){
                    Block block = player.getWorld().getBlockAt(px + x, py, pz + z);
                    final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                    // Insert null if no owner. Else insert owner
                    if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                        owners.add(null);
                        continue;
                    }
                    owners.add(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)));
                }
            }
        } else if (direction.equalsIgnoreCase("S")){
            for (int z = radius; z >= -radius; z--){
                for (int x = radius; x >= -radius; x--){
                    Block block = player.getWorld().getBlockAt(px + x, py, pz + z);
                    final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                    // Insert null if no owner. Else insert owner
                    if (!container.has(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner"), PersistentDataType.STRING)){
                        owners.add(null);
                        continue;
                    }
                    owners.add(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)));
                }
            }
        } else if (direction.equalsIgnoreCase("E")){
            for (int x = radius; x >= -radius; x--){
                for (int z = -radius; z <= radius; z++){
                    Block block = player.getWorld().getBlockAt(px + x, py, pz + z);
                    final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                    // Insert null if no owner. Else insert owner
                    if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                        owners.add(null);
                        continue;
                    }
                    owners.add(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)));
                }
            }
        } else if (direction.equalsIgnoreCase("W")){
            for (int x = -radius; x <= radius; x++){
                for (int z = radius; z >= -radius; z--){
                    Block block = player.getWorld().getBlockAt(px + x, py, pz + z);
                    final PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                    // Insert null if no owner. Else insert owner
                    if (!container.has(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)){
                        owners.add(null);
                        continue;
                    }
                    owners.add(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "owner"), PersistentDataType.STRING)));
                }
            }
        } else{
            return null;
        }
        return owners;
    }
    public static void generateMap(Player player, int radius, Location location, TWClaim twClaim, String direction){
        int px = location.getBlockX();
        int py = location.getBlockY();
        int pz = location.getBlockZ();
        List<UUID> owners = collectData(player, radius, px, py, pz, direction);
        if (owners == null){
            player.sendMessage(ChatColor.RED + "Incorrect usage. Valid directions: N, S, E, W.");
            return;
        }
        TextComponent textComponent = Component.text("");
        textComponent = textComponent.append(Component.text("Tribe Map ", NamedTextColor.RED, TextDecoration.UNDERLINED).append(Component.text("Y: " + py + "\n")));

        int lineLength = radius * 2 + 1;
        int count = 1;
        int row = 1;
        for (UUID uuid : owners){
            // Makes a new line for the next row of the map
            if (count % (lineLength + 1) == 0){
                // compass top
                if (row == radius){
                    if (direction.equalsIgnoreCase("N")){
                        textComponent = textComponent.append(Component.text("     /N\\", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " N")));
                    } else if (direction.equalsIgnoreCase("S")){
                        textComponent = textComponent.append(Component.text("     /S\\", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " S")));
                    } else if (direction.equalsIgnoreCase("E")){
                        textComponent = textComponent.append(Component.text("     /E\\", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " E")));
                    } else if (direction.equalsIgnoreCase("W")){
                        textComponent = textComponent.append(Component.text("     /W\\", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " W")));
                    }
                }
                // compass center
                if (row == radius + 1){
                    if (direction.equalsIgnoreCase("N")){
                        textComponent = textComponent.append(Component.text("    <W", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " W")));
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.GOLD));
                        textComponent = textComponent.append(Component.text("E>", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " E")));
                    } else if (direction.equalsIgnoreCase("S")){
                        textComponent = textComponent.append(Component.text("    <E", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " E")));
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.GOLD));
                        textComponent = textComponent.append(Component.text("W>", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " W")));
                    } else if (direction.equalsIgnoreCase("E")){
                        textComponent = textComponent.append(Component.text("    <N", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " N")));
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.GOLD));
                        textComponent = textComponent.append(Component.text("S>", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " S")));
                    } else if (direction.equalsIgnoreCase("W")){
                        textComponent = textComponent.append(Component.text("    <S", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " S")));
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.GOLD));
                        textComponent = textComponent.append(Component.text("N>", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " N")));
                    }
                }
                // compass bottom
                if (row == radius + 2){
                    if (direction.equalsIgnoreCase("N")){
                        textComponent = textComponent.append(Component.text("     \\S/", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " S")));
                    } else if (direction.equalsIgnoreCase("S")){
                        textComponent = textComponent.append(Component.text("     \\N/", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " N")));
                    } else if (direction.equalsIgnoreCase("E")){
                        textComponent = textComponent.append(Component.text("     \\W/", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " W")));
                    } else if (direction.equalsIgnoreCase("W")){
                        textComponent = textComponent.append(Component.text("     \\E/", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + py + " E")));
                    }
                }
                textComponent = (TextComponent) textComponent.appendNewline();
                row++;
                count = 1;
            }
            // Make center of map gold regardless of what is there to show player is on it.
            if (count == radius + 1 && row == radius + 1) {
                textComponent = textComponent.append(Component.text("+", NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(Component.text("You", NamedTextColor.GOLD))));
                count++;
                continue;
            }
            // White + for no owner. Green + for owner Blue + for member Red + for other
            if (uuid == null){
                textComponent = textComponent.append(Component.text("+", NamedTextColor.WHITE));
            }  else {
                // Check if owner is tribe
                if (Util.isTribe(uuid)){
                    // Check if player is member of tribe
                    if (Util.isInTribe(player.getUniqueId(), uuid)){
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.BLUE).hoverEvent(Component.text(TribeData.tribe_hashmap.get(uuid).getName(), NamedTextColor.BLUE)));
                    } else {
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.RED).hoverEvent(Component.text(TribeData.tribe_hashmap.get(uuid).getName(), NamedTextColor.RED)));
                    }
                    count++;
                    continue;
                }
                // Check if player matches uuid
                if (player.getUniqueId().equals(uuid)){
                    textComponent = textComponent.append(Component.text("+", NamedTextColor.DARK_GREEN).hoverEvent(Component.text(player.getDisplayName(), NamedTextColor.DARK_GREEN)));
                    count++;
                    continue;
                } else {
                    if (Bukkit.getOfflinePlayer(uuid).getName() != null){
                        textComponent = textComponent.append(Component.text("+", NamedTextColor.RED).hoverEvent(Component.text(Bukkit.getOfflinePlayer(uuid).getName(), NamedTextColor.RED)));
                        count++;
                        continue;
                    }
                    textComponent = textComponent.append(Component.text("+", NamedTextColor.RED).hoverEvent(Component.text("Unknown", NamedTextColor.RED)));
                }
            }
            count++;
        }

        // Pages
        textComponent = textComponent.append(Component.text("\n"));
        // Get list of y levels that are in the radius
        List<Integer> levels = new ArrayList<>();
        for (int x = 0; x < lineLength; x++){
            levels.add(player.getLocation().getBlockY() - radius + x);
        }
        // For every y level in pages, make a clickable element
        for (int level : levels){
            location.setY(level);
            textComponent = textComponent.append(Component.text("[" + level + "]", NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/tribe map " + level + " " + direction)));
        }

        // Send message to player
        twClaim.adventure().player(player).sendMessage(textComponent);
    }
}
