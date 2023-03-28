package com.ethan.twclaim.commands;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.util.Util;
import com.google.gson.Gson;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;


import java.io.*;
import java.util.*;

public class TribeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){return true;}
        // Variables used by multiple commands
        Player player = (Player) sender;
        PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
        Gson gson = TWClaim.getGson();

        File tribeFolder = new File(Bukkit.getPluginManager().getPlugin("TWClaim").getDataFolder(), "TribeData");
        if (!tribeFolder.exists()){tribeFolder.mkdir();}

        if (args.length == 2 && args[0].equalsIgnoreCase("create")){

            for (TribeData tribeData : TribeData.tribe_hashmap.values()){
                if (!tribeData.getName().equalsIgnoreCase(args[1])){continue;}
                player.sendMessage(ChatColor.RED + "A tribe with this name already exists!");
                return false;
            }

            // If everything goes well, the tribe is created
            UUID tribeID = UUID.randomUUID();
            HashMap<UUID, String> members = new HashMap();
            members.put(player.getUniqueId(), "Leader");
            HashMap<String, String> perms = new HashMap<>();
            perms.put("Leader", "kirs");
            perms.put("Member", "--rs");
            TribeData tribe = new TribeData(tribeID, args[1], player.getUniqueId(), members, perms, new ArrayList<>());
            // TODO see if the file saving can be removed
            //  since the hashmap should be saved to file when the server stops
            File tribe_file = new File(tribeFolder, tribeID + ".json");
            try{
                if (!tribe_file.exists()){tribe_file.createNewFile();}

                // Saving new Tribe to file
                Writer writer = new FileWriter(tribe_file, false);
                gson.toJson(tribe, writer);
                writer.flush();
                writer.close();

                // Changing Founder's Player Data to reflect creating the Tribe
                HashMap<UUID, String> tribes = playerData.getTribes();
                tribes.put(tribeID, tribe.getName());
                playerData.setTribes(tribes);


                TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
                TribeData.tribeConversionHashmap.put(tribe.getName().toLowerCase(), tribeID);
                PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);

                System.out.println("Saved data!");
                player.sendMessage("You have created " + args[1]);
            }catch(IOException e){e.printStackTrace();}
            return true;
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("add")){
            // Search for tribe in tribe hashmap return error if not found
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
            // Check for permission to invite players to this tribe
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("i")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            // Check if player is online
            for (PlayerData invited : PlayerData.player_data_hashmap.values()){
                if (!invited.getDisplay().equalsIgnoreCase(args[2])){continue;}
                // Add tribe name to player file
                List<String> invites = invited.getInvites();
                if (invites.contains(args[0])){
                    player.sendMessage(ChatColor.RED + "Player has already been invited to this group!");
                    return false;
                }
                invites.add(args[0]);
                invited.setInvites(invites);
                // Add player uuid to tribe file
                List<UUID> tribeInvites = tribe.getInvites();
                tribeInvites.add(invited.getUuid());

                // Update hashmaps
                PlayerData.player_data_hashmap.put(invited.getUuid(), invited);
                TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
                TribeData.tribeConversionHashmap.put(tribe.getName().toLowerCase(), tribe.getTribeID());
                player.sendMessage("Invited " + args[2] + " to " + args[0]);
                Player invitedPlayer = Bukkit.getPlayer(invited.getUuid());
                invitedPlayer.sendMessage("You have been invited to " + args[0] + ". Use /tribe join " + args[0] + " to join.");
                return true;
            }
            // If the player is offline, search player files
            for (File invitedFile : new File (TWClaim.getPlugin().getDataFolder(), "PlayerData").listFiles()){
                try {
                    Reader invitedFileReader = new FileReader(invitedFile);
                    PlayerData invitedData = gson.fromJson(invitedFileReader, PlayerData.class);
                    if (!invitedData.getDisplay().equalsIgnoreCase(args[2])){continue;}
                    // Found invited player's file.
                    // Add invites to player and tribe files.
                    List<String> invites = invitedData.getInvites();
                    if (invites.contains(tribe.getName())){
                        player.sendMessage(ChatColor.RED + "Player has already been invited to this group!");
                        return false;
                    }
                    invites.add(tribe.getName());
                    invitedData.setInvites(invites);

                    List<UUID> tribeInvites = tribe.getInvites();
                    tribeInvites.add(invitedData.getUuid());
                    tribe.setInvites(tribeInvites);

                    Writer invitedFileWriter = new FileWriter(invitedFile, false);
                    gson.toJson(invitedData, invitedFileWriter);
                    invitedFileWriter.flush();
                    invitedFileWriter.close();
                    player.sendMessage("Invited " + args[2] + " to " + args[0]);
                    return true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // If not found in player files, return error
            player.sendMessage(ChatColor.RED + "Player does not exist!");
            return false;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")){
            if (!Util.checkTribe(args[1])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[1].toLowerCase()));
            // Check that tribe invited player
            if (!tribe.getInvites().contains(player.getUniqueId())){player.sendMessage(ChatColor.RED + "You do not have permission to join this tribe"); return false;}
            // Update the tribe and player data
            List<UUID> tribeInvites = tribe.getInvites();
            tribeInvites.remove(player.getUniqueId());
            tribe.getMembers().put(player.getUniqueId(), "Member");
            List<String> playerTribeInvites = playerData.getInvites();
            playerTribeInvites.remove(tribe.getName());
            playerData.getTribes().put(tribe.getTribeID(), tribe.getName());
            TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);

            // Message player and tribe owner
            if (Bukkit.getPlayer(tribe.getLeader()) != null){Bukkit.getPlayer(tribe.getLeader()).sendMessage(player.getDisplayName() + "joined " + tribe.getName());}
            player.sendMessage("Joined " + tribe.getName());
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("kick")) {
            // Search for tribe in tribe hashmap
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
            // Check for permission to kick players
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("k")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            UUID kickedUUID = null;
            // Get PlayerData for kicked player
            if (Bukkit.getPlayerExact(args[2]) != null){
                // Player is online
                PlayerData kickedData = PlayerData.player_data_hashmap.get(Bukkit.getPlayerExact(args[2]).getUniqueId());
                kickedUUID = kickedData.getUuid();
                // Check that kicked player is in the tribe
                if (!tribe.getMembers().containsKey(kickedData.getUuid())){player.sendMessage(ChatColor.RED + "Player is not in tribe!"); return false;}
                // Make changes
                HashMap<UUID, String> tribes = kickedData.getTribes();
                tribes.remove(tribe.getTribeID());
                kickedData.setTribes(tribes);
                PlayerData.player_data_hashmap.put(kickedUUID, kickedData);
                Player kickedPlayer = Bukkit.getPlayer(kickedData.getUuid());
                kickedPlayer.sendMessage("You have been kicked from " + tribe.getName());
            } else {
                // Player is offline. Get object from files.
                for (File invitedFile : new File (TWClaim.getPlugin().getDataFolder(), "PlayerData").listFiles()) {
                    try {
                        Reader kickedFileReader = new FileReader(invitedFile);
                        PlayerData potentialPlayer = gson.fromJson(kickedFileReader, PlayerData.class);
                        if (!potentialPlayer.getDisplay().equalsIgnoreCase(args[2])) {
                            continue;
                        }
                        // Found invited player's file. Make changes then write to file.
                        kickedUUID = potentialPlayer.getUuid();
                        HashMap<UUID, String> tribes = potentialPlayer.getTribes();
                        tribes.remove(tribe.getTribeID());
                        potentialPlayer.setTribes(tribes);

                        Writer kickedFileWriter = new FileWriter(invitedFile, false);
                        gson.toJson(potentialPlayer, kickedFileWriter);
                        kickedFileWriter.flush();
                        kickedFileWriter.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Change the tribe hashmap to reflect kicking
            HashMap<UUID, String> members = tribe.getMembers();
            if (kickedUUID == null){player.sendMessage(ChatColor.RED + "Player does not exist!"); return false;}
            members.remove(kickedUUID);
            TribeData.tribe_hashmap.put(tribe.getTribeID(), tribe);
            // Send kicker confirmation message
            player.sendMessage("Kicked " + args[2] + " from " + args[0]);
            return true;
        }

        if (args.length == 1 & args[0].equalsIgnoreCase("inspect")){
            if (!playerData.getMode().equalsIgnoreCase("Inspect")){
                playerData.setMode("Inspect");
                player.sendMessage(ChatColor.GREEN + "Inspect Mode ON");
            } else {
                playerData.setMode("None");
                player.sendMessage(ChatColor.RED + "Inspect Mode OFF");
            }
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
            return true;
        }
        // TODO claim individual blocks for tribe
        // TODO implement private modes for Reinforce and Fortify
        // TODO make inspect/reinforce/fortify/claim modes exclusive
        if (args.length == 1 & args[0].equalsIgnoreCase("reinforce")){
            if (playerData.getMode().equalsIgnoreCase("Reinforce")){
                playerData.setMode("None");
                player.sendMessage(ChatColor.RED + "Reinforcement OFF");
            } else{
                playerData.getMode().equalsIgnoreCase("Reinforce");
                playerData.setMode("Reinforce");
                player.sendMessage(ChatColor.GREEN + "Reinforcement ON");
            }
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("reinforce")){
            // Search for tribe in tribe hashmap return error if not found
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
            // Check for permission to reinforce blocks
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("r")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            if (playerData.getMode().equalsIgnoreCase("Reinforce")){
                playerData.setMode("None");
                player.sendMessage(ChatColor.RED + "Reinforcement Mode OFF");
            } else{
                playerData.setMode("Reinforce");
                playerData.setTarget(tribe.getTribeID());
                player.sendMessage(ChatColor.GREEN + "Reinforcement Mode ON");
            }
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("fortify")){
            if (playerData.getMode().equalsIgnoreCase("Fortify")){
                playerData.setMode("None");
                player.sendMessage(ChatColor.RED + "Fortify OFF");
            } else{
                playerData.setMode("Fortify");
                playerData.setTarget(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Fortify ON");
            }
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("fortify")){
            // Search for tribe in tribe hashmap return error if not found
            if (!Util.checkTribe(args[0])){player.sendMessage(ChatColor.RED + "This tribe does not exist!"); return false;}
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
            // Check for permission to reinforce blocks
            String permGroup = tribe.getMembers().get(player.getUniqueId());
            String perms = tribe.getPermGroups().get(permGroup);
            if (!perms.contains("r")){player.sendMessage(ChatColor.RED + "Insufficient Permissions"); return false;}
            if (playerData.getMode().equalsIgnoreCase("Fortify")){
                playerData.setMode("None");
                player.sendMessage(ChatColor.RED + "Fortify Mode OFF");
            } else{
                playerData.setMode("Fortify");
                playerData.setTarget(tribe.getTribeID());
                player.sendMessage(ChatColor.GREEN + "Fortify Mode ON");
            }
            PlayerData.player_data_hashmap.put(player.getUniqueId(), playerData);
            return true;
        }

        // TODO claim area for tribe
        if (args.length == 2 && args[1].equalsIgnoreCase("claim")){
            // Check if tribe exists
            if (!TribeData.tribeConversionHashmap.containsKey(args[0])){
                player.sendMessage(ChatColor.RED + "This tribe does not exist!");
                return false;
            }
            // Check if player is in tribe
            if (!Util.isInTribe(player.getUniqueId(), TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase())).getTribeID())){
                player.sendMessage(ChatColor.RED + "Not a member of this tribe!");
                return false;
            }
            // Check player perms
            TribeData tribeData = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[0].toLowerCase()));
            if (!tribeData.getPermGroups().get(tribeData.getMembers().get(player.getUniqueId())).contains("r")){
                player.sendMessage(ChatColor.RED + "Insufficient Permissions");
            }

            // Activate claiming mode
            if (playerData.getMode().equalsIgnoreCase("Claim")){
                playerData.setMode("None");
                playerData.setTarget(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Claiming Mode OFF");
            } else {
                playerData.setMode("Claim");
                playerData.setTarget(tribeData.getTribeID());
                player.sendMessage(ChatColor.GREEN + "Claiming Mode ON");
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("claim") && args[1].equalsIgnoreCase("confirm")){
            // Check that player is in claim mode
            if (!playerData.getMode().equalsIgnoreCase("Claim")){
                player.sendMessage(ChatColor.RED + "Not in claiming mode!");
                return false;
            }
            // Check that both blocks are selected
            HashMap<String, Block> claimSelect = playerData.getClaimSelect();
            if (!(claimSelect.containsKey("First")) || !(claimSelect.containsKey("Second"))){
                player.sendMessage(ChatColor.RED + "Select blocks first!");
                return false;
            }
            // Check that claim size is within limit
            Block firstBlock = claimSelect.get("First");
            Block secondBlock = claimSelect.get("Second");
            // Calculate volume of selected area
            int xDiff = Math.abs(firstBlock.getX() - secondBlock.getX()) + 1;
            int yDiff = Math.abs(firstBlock.getY() - secondBlock.getY()) + 1;
            int zDiff = Math.abs(firstBlock.getZ() - secondBlock.getZ()) + 1;
            int volume = xDiff * yDiff * zDiff;
            if (volume > TWClaim.getPlugin().getConfig().getInt("claim-limit")){
                player.sendMessage(ChatColor.RED + "Too many blocks selected. Max selection size: " + TWClaim.getPlugin().getConfig().getInt("claim-limit"));
                return false;
            }
            System.out.println(xDiff);
            System.out.println(yDiff);
            System.out.println(zDiff);
            System.out.println("Volume: " + volume);
            // Get a list of blocks inside the area
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
            System.out.println(blockSelect);
            // Look for blocks that are already claimed and remove number from volume
            int claimed = 0;
            for (Block block : blockSelect){
                PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin(TWClaim.class));
                if (!container.has(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner"), PersistentDataType.STRING)){
                    continue;
                }
                claimed += 1;
            }
            volume -= claimed;
            System.out.println("Claimed: " + claimed);

            // Cycle through the player's inventory to get count all reinforcement materials.
            HashMap<String, Integer> reinforcementTypes = Util.getReinforcementTypes();
            LinkedHashMap<Material, Integer> materialCount = new LinkedHashMap<>();
            Inventory inventory = player.getInventory();
            for (ItemStack item : inventory.getContents()){
                if (item == null){continue;}
                if (!reinforcementTypes.containsKey(item.getType().toString().toLowerCase())){continue;}
                if (materialCount.containsKey(item.getType())){
                    materialCount.put(item.getType(), item.getAmount() + materialCount.get(item.getType()));
                    continue;
                }
                materialCount.put(item.getType(), item.getAmount());
            }

            // Iterate through materialCount to see if there are enough items of one material to claim the whole area.
            HashMap<Material, Integer> selectedMaterials = new HashMap<>();
            int reinforcement = 0;
            String materialType = "";
            boolean enough = false;
            for (Material material : materialCount.keySet()){
                // If there are enough materials, remove them from inventory
                int count = materialCount.get(material);
                if (count >= volume){
                    enough = true;
                    selectedMaterials.put(material, volume);
                    reinforcement = reinforcementTypes.get(material.toString().toLowerCase());
                    materialType = material.toString().toLowerCase();
                    // Iterate over stacks to remove the items until cost is paid
                    for (ItemStack item : inventory.getContents()){
                        if (item == null){continue;}
                        if (selectedMaterials.containsKey(item.getType())){
                            int amount = item.getAmount();
                            if (amount >= volume){
                                item.setAmount(amount - selectedMaterials.get(item.getType()));
                                System.out.println(selectedMaterials);
                                break;
                            }
                            selectedMaterials.put(item.getType(), selectedMaterials.get(item.getType()) - item.getAmount());
                            item.setAmount(0);
                        }
                    }
                    break;
                }
            }
            // If there are enough to claim with multiple materials, ask user to confirm
            if (!enough){
                int count = 0;
                for (Material material : materialCount.keySet()){
                    int materialAmount = materialCount.get(material);
                    // Enough to claim with multiple
                    if (count + materialAmount >= volume){
                        selectedMaterials.put(material, volume - count);
                        player.sendMessage("You can claim this area if you use multiple materials. Respond \"Confirm\" if that's okay.");
                        return true;
                        // TODO implement confirmation message for claiming with multiple materials
                    }
                    selectedMaterials.put(material, materialAmount);
                    count += materialAmount;
                }
            }
            // If there are not enough materials to claim the area, send error
            if (!enough){
                player.sendMessage(ChatColor.RED + "Not enough materials!");
                return false;
            }

            // Iterate over the list and reinforce
            for (Block block : blockSelect){
                PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                if (container.has(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner"), PersistentDataType.STRING)){continue;}
                container.set(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner"), PersistentDataType.STRING, playerData.getTarget().toString());
                container.set(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "reinforcement"), PersistentDataType.INTEGER, reinforcement);
                container.set(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "material"), PersistentDataType.STRING, materialType);
                System.out.println(container);
            }
            player.sendMessage(ChatColor.GREEN + "Area claimed");
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("info")) {
            // Check if tribe exists
            if (!TribeData.tribeConversionHashmap.containsKey(args[0])){
                player.sendMessage(ChatColor.RED + "This tribe does not exist!");
                return false;
            }
            // Check if player is member of tribe
            TribeData tribe = TribeData.tribe_hashmap.get(TribeData.tribeConversionHashmap.get(args[1].toLowerCase()));
            List<String> message = new ArrayList<>();
            if (!Util.isInTribe(player.getUniqueId(), tribe.getTribeID())){
                message.add("Owner: " + Bukkit.getOfflinePlayer(player.getUniqueId()));
                HashMap<UUID, String> members = tribe.getMembers();
                members.remove(tribe.getLeader());
                message.add("Members: " + members.values());
                return true;
            }
            message.add("Owner: " + Bukkit.getOfflinePlayer(player.getUniqueId()));
            HashMap<UUID, String> members = tribe.getMembers();
            members.remove(tribe.getLeader());
            StringBuilder membersString = new StringBuilder("Members: ");
            for (UUID member : members.keySet()){
                OfflinePlayer memberPlayer = Bukkit.getPlayer(member);
                membersString.append(members.get(member)).append(memberPlayer.getName()).append(", ");
            }
            message.add(membersString.toString());
            return true;
        }

        // TODO have multiple pages on claims map for y-level range
        if (args.length == 1 && args[0].equalsIgnoreCase("map")){
            // Getting location info
            Location playerLocation = player.getLocation();
            int px = playerLocation.getBlockX();
            int py = playerLocation.getBlockY();
            int pz = playerLocation.getBlockZ();
            int radius = TWClaim.getPlugin().getConfig().getInt("map-radius");
            List<UUID> owners = new ArrayList<>();
            // For loop to get ownership data from blocks in radius
            for (int z = -radius; z <= radius; z++){
                for (int x = -radius; x <= radius; x++){
                    Block block = player.getWorld().getBlockAt(px + x, py, pz + z);
                    PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
                    // Insert null if no owner. Else insert owner
                    if (!container.has(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner"), PersistentDataType.STRING)){
                        owners.add(null);
                        continue;
                    }
                    owners.add(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(TWClaim.class), "owner"), PersistentDataType.STRING)));
                }
            }
            // Iterate through list to assemble message to player
            int lineLength = radius * 2 + 1;
            StringBuilder assembled = new StringBuilder();
            int count = 1;
            int row = 1;
            for (UUID uuid : owners){
                // Makes a new line for the next row of the map
                if (count % (lineLength + 1) == 0){
                    assembled.append("\n");
                    row++;
                    count = 1;
                }
                // Make center of map gold regardless of what is there to show player is on it.
                if (count == radius + 1 && row == radius + 1) {
                    assembled.append(ChatColor.GOLD + "+" + ChatColor.RESET);
                    count++;
                    continue;
                }
                // White + for no owner. Green + for owner Blue + for member Red + for other
                if (uuid == null){
                    assembled.append("+");
                }  else {
                    // Check if owner is tribe
                    if (Util.isTribe(uuid)){
                        // Check if player is member of tribe
                        if (Util.isInTribe(player.getUniqueId(), uuid)){
                            assembled.append(ChatColor.BLUE + "+" + ChatColor.RESET);
                            count++;
                            continue;
                        }
                    }
                    // Check if player matches uuid
                    if (player.getUniqueId().equals(uuid)){
                        assembled.append(ChatColor.DARK_GREEN + "+" + ChatColor.RESET);
                        count++;
                        continue;
                    }
                    assembled.append(ChatColor.RED + "+" + ChatColor.RESET);
                }
                count++;
            }

            // Join all lines into one message and send to player
            List<String> message = new ArrayList<>();
            message.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "Tribes Map" + ChatColor.RESET);
            message.add(assembled.toString());
            message.add(ChatColor.GOLD + "  /N\\");
            message.add("<W + E>");
            message.add("  \\S/");
            message.add(ChatColor.GOLD + "+ = You");
            message.add(ChatColor.DARK_GREEN + "+ = Owned");
            message.add(ChatColor.BLUE + "+ = Member");
            message.add(ChatColor.RED + "+ = Other Tribe");
            player.sendMessage(String.join("\n", message));
            return true;
        }

        // TODO create perms group

        // TODO change perms group perms

        return false;
    }
}
