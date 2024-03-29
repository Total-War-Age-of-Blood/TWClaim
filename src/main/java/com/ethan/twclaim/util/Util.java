package com.ethan.twclaim.util;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.data.*;
import com.ethan.twclaim.events.BastionClaimEvent;
import com.ethan.twclaim.events.BastionDestroyEvent;
import com.ethan.twclaim.events.ExtenderDestroyEvent;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Util {
    public static boolean checkTribe(String name){
        boolean tribeFound = false;
        for (TribeData tribeData : TribeData.tribe_hashmap.values()){
            if (!tribeData.getName().equalsIgnoreCase(name)){
                continue;
            }
            tribeFound = true;
            break;
        }
        return !tribeFound;
    }

    public static void removeKeys(PersistentDataContainer container){
        // Remove PDCs from block
        container.remove(materialKey);
        container.remove(key);
        container.remove(ownKey);
        container.remove(breakCount);
        if (!isBastion(container) && !isExtender(container)){return;}
        // Remove bastion or extender from PDC
        if (isBastion(container)){
            UUID bastionID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
            Bastion bastion = Bastion.bastions.get(bastionID);
            if (bastion != null){
                Bukkit.getPluginManager().callEvent(new BastionDestroyEvent(bastion));
                Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
                List<UUID> children = bastion.getExtenderChildren();
                for (UUID child : children){
                    Extender extender = Extender.extenders.get(child);
                    extender.setFatherBastion(null);
                }
            }
        }
        if (isExtender(container)){
            UUID extenderID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING));
            Extender extender = Extender.extenders.get(extenderID);
            if (extender != null){Bukkit.getPluginManager().callEvent(new ExtenderDestroyEvent(extender));}
            Extender.extenders.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING)));
            UUID bastionID = extender.getFatherBastion();
            if (bastionID != null){
                Bastion bastion = Bastion.bastions.get(bastionID);
                ArrayList<UUID> children = bastion.getExtenderChildren();
                children.remove(extenderID);
                bastion.setExtenderChildren(children);
            }
        }
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "bastion"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "fuel-consumption"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-teleport"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "anti-flight"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "surveillance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-storage"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "exp-amount"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "range-distance"));
        container.remove(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"));
    }
    public static void removeReinforcement(PersistentDataContainer container, BlockBreakEvent e){
        Block block = e.getBlock();
        if (!isBastion(container) && !isExtender(container)){
            // TODO Checking if block is a vault always happens when removeKeys fires,
            //  so maybe it should be incorporated into removeKeys
            if (isVault(block) != null){
                Vault vault = isVault(block);
                Vault.destroyVault(vault);
            }
            removeKeys(container);
            return;
        }
        // Make a proper bastion or extender drop (the lore disappears when the player mines one normally.)
        ItemStack drop = bastionOrExtender(container);
        e.setDropItems(false);
        block.getWorld().dropItem(e.getBlock().getLocation(), drop);
        if (isVault(block) != null){
            Vault vault = isVault(block);
            Vault.destroyVault(vault);
        }
        removeKeys(container);
    }

    public static void removeReinforcement(PersistentDataContainer container, BlockExplodeEvent e){
        Block block = e.getBlock();
        if (!isBastion(container) && !isExtender(container)){
            if (isVault(block) != null){
                Vault vault = isVault(block);
                Vault.destroyVault(vault);
            }
            removeKeys(container);
            return;
        }
        if (isBastion(container)){
            UUID bastionID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
            Bastion bastion = Bastion.bastions.get(bastionID);
            Block bastionBlock = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
            e.blockList().remove(bastionBlock);
            bastionBlock.setType(Material.AIR);
            e.blockList().add(bastionBlock);
        } else{
            UUID extenderID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING));
            Extender extender = Extender.extenders.get(extenderID);
            Block extenderBlock = Bukkit.getWorld(extender.getWorldID()).getBlockAt(extender.getCoordinates()[0], extender.getCoordinates()[1], extender.getCoordinates()[2]);
            e.blockList().remove(extenderBlock);
            extenderBlock.setType(Material.AIR);
            e.blockList().add(extenderBlock);
        }

        // Make a proper bastion drop (the lore disappears when the player mines one normally.)
        ItemStack drop = bastionOrExtender(container);
        block.getWorld().dropItem(e.getBlock().getLocation(), drop);

        // Remove bastion from PDC
        Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        if (isVault(block) != null){
            Vault vault = isVault(block);
            Vault.destroyVault(vault);
        }
        removeKeys(container);
    }

    public static void removeReinforcement(Block block, PersistentDataContainer container, EntityExplodeEvent e){
        if (!isBastion(container) && !isExtender(container)){
            if (isVault(block) != null){
                Vault vault = isVault(block);
                Vault.destroyVault(vault);
            }
            removeKeys(container);
            return;
        }
        if (isBastion(container)){
            UUID bastionID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
            Bastion bastion = Bastion.bastions.get(bastionID);
            Block bastionBlock = Bukkit.getWorld(bastion.getWorldId()).getBlockAt(bastion.getCoordinates()[0], bastion.getCoordinates()[1], bastion.getCoordinates()[2]);
            e.blockList().remove(bastionBlock);
            bastionBlock.setType(Material.AIR);
            e.blockList().add(bastionBlock);
        } else{
            UUID extenderID = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING));
            Extender extender = Extender.extenders.get(extenderID);
            Block extenderBlock = Bukkit.getWorld(extender.getWorldID()).getBlockAt(extender.getCoordinates()[0], extender.getCoordinates()[1], extender.getCoordinates()[2]);
            e.blockList().remove(extenderBlock);
            extenderBlock.setType(Material.AIR);
            e.blockList().add(extenderBlock);
        }

        // Remove bastion from PDC
        Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        if (isVault(block) != null){
            Vault vault = isVault(block);
            Vault.destroyVault(vault);
        }
        removeKeys(container);

        // Make a proper bastion drop (the lore disappears when the player mines one normally.)
        ItemStack bastionItem = bastionItem();
        e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), bastionItem);
    }

    public static void removeReinforcement(PersistentDataContainer container, BlockBurnEvent e){
        Block block = e.getBlock();
        if (!isBastion(container) && isExtender(container)){
            if (isVault(block) != null){
                Vault vault = isVault(block);
                Vault.destroyVault(vault);
            }
            removeKeys(container);
            return;
        }
        // Remove bastion from PDC
        if (isBastion(container)){
            Bastion.bastions.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)));
        } else{
            Extender.extenders.remove(UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING)));
        }
        if (isVault(block) != null){
            Vault vault = isVault(block);
            Vault.destroyVault(vault);
        }
        removeKeys(container);
    }

    public static boolean isTribe(UUID uuid){
        return TribeData.tribe_hashmap.containsKey(uuid);
    }

    public static boolean isInTribe(UUID playerId, UUID tribeId){
        PlayerData playerData = PlayerData.player_data_hashmap.get(playerId);
        if (playerData.getTribes().containsKey(tribeId)){return true;}
        return false;
    }

    public static HashMap<String, Integer> getReinforcementTypes(){
        ArrayList<HashMap<String, Integer>> reinforcementsFromConfig = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("reinforcements");
        // This hashmap contains the material types and their reinforcement values
        HashMap<String, Integer> reinforcements = new HashMap<>();
        for (HashMap<String, Integer> hashMap : reinforcementsFromConfig){
            reinforcements.putAll(hashMap);
        }
        List<String> keyList = new ArrayList<>(reinforcements.keySet());
        List<Integer> valueList = new ArrayList<>(reinforcements.values());
        reinforcements.clear();
        // Make keys lowercase, so they can be matched with lowercase material types
        int n = 0;
        for (String key : keyList){
            key.toLowerCase();
            reinforcements.put(key, valueList.get(n));
            n++;
        }
        return reinforcements;
    }

    public static void generateGUI(Inventory gui, Material material, String display, String lore, int place){
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(display);
        itemMeta.setLore(Collections.singletonList(lore));
        item.setItemMeta(itemMeta);
        gui.setItem(place, item);
    }

    public static void generateGUI(Inventory gui, Material material, int place, int level, String upgrade, PersistentDataContainer container){
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        String display = "";
        List<String> lore = new ArrayList<>();

        ArrayList<HashMap<String, Integer>> cost;
        String activeUpgrades = container.get(new NamespacedKey(TWClaim.getPlugin(), "active-upgrades"), PersistentDataType.STRING);
        switch (upgrade){
            case "anti-teleport":
                if (activeUpgrades.contains("T")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.GREEN + "Anti-Teleport" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.GREEN + "Anti-Teleport" + ChatColor.RED + " (Inactive)";
                }
                lore.add("Prevent non-member epearl/cfruit");
                if (level != 0){break;}
                display = ChatColor.GREEN + "Anti-Teleport " + ChatColor.RED + "(Not Owned)";
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("anti-teleport");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "anti-flight":
                if (activeUpgrades.contains("F")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.BLUE + "Anti-Flight" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.BLUE + "Anti-Flight" + ChatColor.RED + " (Inactive)";
                }

                lore.add("Prevent non-member elytra use");
                if (level != 0){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("anti-flight");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "surveillance":
                if (activeUpgrades.contains("S")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.WHITE + "" + ChatColor.BOLD + "Surveillance" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.BOLD + "Surveillance" + ChatColor.RED + " (Inactive)";
                }
                lore.add("Non-members glow");
                if (level != 0){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("surveillance");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
            case "exp-storage":
                if (activeUpgrades.contains("E")){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    display = ChatColor.GREEN + "Exp Storage" + ChatColor.GREEN + " (Active)";
                } else {
                    display = ChatColor.GREEN + "Exp Storage" + ChatColor.RED + " (Inactive)";
                }
                lore.add("Store experience");
                if (level != 0){break;}
                lore.add(ChatColor.UNDERLINE + "Cost");
                // Get cost from config
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get("exp-storage");
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        lore.add(hash.get(key) + " " + key);
                    }
                }
                break;
        }
        if (!upgrade.equalsIgnoreCase("range")){
            if (level == 0){
                lore.add(ChatColor.RED + "Not owned");
            } else{
                lore.add(ChatColor.GREEN + "Level: " + level);
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel consumption when active")){continue;}
                        lore.add("Fuel Cost when active: " + hash.get(key));
                    }
                }
            }
            if (upgrade.equalsIgnoreCase("surveillance")){
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel per person spied on")){continue;}
                        lore.add("Fuel Cost per person spotted: " + hash.get(key));
                    }
                }
            }
            if (upgrade.equalsIgnoreCase("exp-storage")){
                cost = (ArrayList<HashMap<String, Integer>>) TWClaim.getPlugin().getConfig().get(upgrade);
                for (HashMap<String, Integer> hash : cost){
                    for (String key : hash.keySet()){
                        if (!key.equalsIgnoreCase("fuel per 100 points")){continue;}
                        lore.add("Fuel Cost per 100 exp: " + hash.get(key));
                    }
                }
            }
        }
        itemMeta.setDisplayName(display);
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
        gui.setItem(place, item);
    }

    public static ItemStack bastionItem(){
        ItemStack bastion = new ItemStack(Material.BEACON);
        ItemMeta bastionMeta = bastion.getItemMeta();
        bastionMeta.setDisplayName(ChatColor.GOLD + "Bastion");
        bastionMeta.setLore(Arrays.asList(ChatColor.GOLD + "Protects your land in a 30 block radius"));
        bastionMeta.getPersistentDataContainer().set(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING, "yes");
        bastion.setItemMeta(bastionMeta);
        return bastion;
    }

    public static ItemStack bastionRangeExtenderItem(int amount){
        ItemStack bastionRangeExtender = new ItemStack(Material.BEACON);
        ItemMeta meta = bastionRangeExtender.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Range Extender");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Extends range of bastion by 30 blocks from where it is placed. Farther from bastion = higher fuel consumption."));
        meta.getPersistentDataContainer().set(new NamespacedKey(TWClaim.getPlugin(), "extender"), PersistentDataType.STRING, "yes");
        bastionRangeExtender.setItemMeta(meta);
        bastionRangeExtender.setAmount(amount);
        return bastionRangeExtender;
    }

    public static boolean hasPermission(Player player, TribeData tribeData, CharSequence perm){
        String group = tribeData.getMembers().get(player.getUniqueId());
        String perms = tribeData.getPermGroups().get(group);
        return perms.contains(perm);
    }
    // Returns true if the block below the special case is reinforced
    public static boolean checkSpecialReinforcement(Tag<Material> tag, Block block, Player player){
        // Get the block's persistent data container
        Block blockBelow = block;
        // Will keep going down until it finds a non-crop block.
        int i = 1;
        while (tag.isTagged(blockBelow.getType())){
            blockBelow = block.getWorld().getBlockAt(block.getX(), block.getY() - i, block.getZ());
            i++;
        }
        PersistentDataContainer container = new CustomBlockData(blockBelow, TWClaim.getPlugin());
        // If block below is not reinforced, return false
        if (!container.has(breakCount, PersistentDataType.INTEGER) && !container.has(getKey(), PersistentDataType.STRING)){return false;}
        // If block below is reinforced, check if player can break it
        // If block below is reinforced and unbreakable, return true
        UUID playerId = player.getUniqueId();
        // If the block is not owned, return false
        if (!container.has(ownKey, PersistentDataType.STRING)){return false;}
        UUID blockOwner = UUID.fromString(container.get(ownKey, PersistentDataType.STRING));
        // Check if player is private owner or tribe member
        if (playerId.equals(blockOwner)){return false;}
        if (Util.isTribe(blockOwner)){
            if (!Util.isInTribe(playerId, blockOwner)){return true;}
            TribeData tribeData = TribeData.tribe_hashmap.get(blockOwner);
            return !Util.hasPermission(player, tribeData, "r");
        }
        return true;
    }
    private static final NamespacedKey materialKey = new NamespacedKey(TWClaim.getPlugin(), "material");
    private static final NamespacedKey ownKey = new NamespacedKey(TWClaim.getPlugin(), "owner");
    private static final NamespacedKey key = new NamespacedKey(TWClaim.getPlugin(), "reinforcement");
    private static final NamespacedKey breakCount = new NamespacedKey(TWClaim.getPlugin(), "break_count");
    private static final NamespacedKey fromVault = new NamespacedKey(TWClaim.getPlugin(), "fromVault");

    public static NamespacedKey getMaterialKey() {
        return materialKey;
    }

    public static NamespacedKey getOwnKey() {
        return ownKey;
    }

    public static NamespacedKey getKey() {
        return key;
    }

    public static NamespacedKey getBreakCount(){
        return breakCount;
    }

    public static NamespacedKey getFromVault(){return fromVault;}

    // This will add the PDC keys to blocks that gain protection from any form of claiming
    public static void addReinforcement(Block block, Material material, PlayerData playerData, boolean fromVault){
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        NamespacedKey materialKey = getMaterialKey();
        container.set(materialKey, PersistentDataType.STRING, material.toString().toLowerCase());
        // Keeps track of how much damage has been done to the block
        NamespacedKey breakCount = getBreakCount();
        container.set(breakCount, PersistentDataType.INTEGER, 0);
        // This key keeps track of the owning tribe
        NamespacedKey ownKey = getOwnKey();
        container.set(ownKey, PersistentDataType.STRING, playerData.getTarget().toString());
        // If block is bastion, trigger bastionClaimEvent
        if (isBastion(block)){
            UUID uuid = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
            Bastion bastion = Bastion.bastions.get(uuid);
            if (bastion.getName() == null){Bastion.nameBastion(bastion, UUID.fromString(container.get(ownKey, PersistentDataType.STRING)));}
            Bukkit.getPluginManager().callEvent(new BastionClaimEvent(bastion));}
        // If material was taken from vault, mark the block
        if (fromVault){container.set(getFromVault(), PersistentDataType.BOOLEAN, true);} else {container.set(getFromVault(), PersistentDataType.BOOLEAN, false);}
    }

    public static void addReinforcement(Block block, ItemStack item, PlayerData playerData, ItemStack heldItem, boolean fromVault){
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        NamespacedKey materialKey = getMaterialKey();
        container.set(materialKey, PersistentDataType.STRING, item.getType().toString().toLowerCase());
        // Keeps track of how much damage has been done to the block
        NamespacedKey breakCount = getBreakCount();
        container.set(breakCount, PersistentDataType.INTEGER, 0);
        // This key keeps track of the owning tribe
        NamespacedKey ownKey = getOwnKey();
        container.set(ownKey, PersistentDataType.STRING, playerData.getTarget().toString());
        // If block is bastion, trigger bastionClaimEvent
        if (heldItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING)){
            UUID uuid = UUID.fromString(container.get(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING));
            Bastion bastion = Bastion.bastions.get(uuid);
            if (bastion.getName() == null){Bastion.nameBastion(bastion, UUID.fromString(container.get(ownKey, PersistentDataType.STRING)));}
            Bukkit.getPluginManager().callEvent(new BastionClaimEvent(bastion));}
        // If material was taken from vault, mark the block
        if (fromVault){container.set(getFromVault(), PersistentDataType.BOOLEAN, true);} else {container.set(getFromVault(), PersistentDataType.BOOLEAN, false);}
    }


    public static boolean isBastion(Block block){
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        return container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING);
    }
    public static boolean isBastion(PersistentDataContainer container){
        return container.has(new NamespacedKey(TWClaim.getPlugin(), "bastion"), PersistentDataType.STRING);
    }

    public static boolean isExtender(Block block){
        PersistentDataContainer container = new CustomBlockData(block, TWClaim.getPlugin());
        return container.has(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING);
    }
    public static boolean isExtender(PersistentDataContainer container){
        return container.has(new NamespacedKey(TWClaim.getPlugin(), "ExtenderUUID"), PersistentDataType.STRING);
    }

    public static void convertToBreakCount(PersistentDataContainer container){
        container.remove(Util.getKey());
        container.set(Util.getBreakCount(), PersistentDataType.INTEGER, 0);
    }

    public static int randomHexColor(){
        Random random = new Random();
        return random.nextInt(0xffffff + 1);
    }

    public static ItemStack bastionOrExtender(PersistentDataContainer container){
        ItemStack drop;
        if (isBastion(container)){
            drop = bastionItem();
        } else{
            drop = bastionRangeExtenderItem(1);
        }
        return drop;
    }

    public static Vault isVault(Block block){
        int[] coordinates = new int[]{block.getX(), block.getY(), block.getZ()};
        for (Vault vault : Vault.vaults.values()){
            if (Arrays.equals(vault.getCoordinates(), coordinates) || Arrays.equals(vault.getSignCoordinates(), coordinates)){
                return vault;
            }
        }
        return null;
    }

    public static ItemStack findReinforcement(Inventory inventory){
        HashMap<String, Integer> reinforcements = Util.getReinforcementTypes();
        System.out.println(inventory.getContents());
        for (ItemStack item : inventory.getContents()){
            if (item == null){continue;}
            if (!reinforcements.containsKey(item.getType().toString().toLowerCase())){continue;}
            return item;
        }
        return null;
    }
}
