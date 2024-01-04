package com.ethan.twclaim;

import com.ethan.twclaim.Listeners.*;
import com.ethan.twclaim.commands.TabComplete;
import com.ethan.twclaim.commands.TribeCommand;
import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.Extender;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.ethan.twclaim.guis.BastionFuelGUI;
import com.ethan.twclaim.guis.BastionGUI;
import com.ethan.twclaim.guis.BastionUpgradeGUI;
import com.ethan.twclaim.guis.ExtenderGUI;
import com.ethan.twclaim.util.Util;
import com.google.gson.Gson;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class TWClaim extends JavaPlugin {
    private BukkitAudiences adventure;

    public @NonNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
    private static TWClaim plugin;
    public static Plugin getPlugin() {return plugin;}

    private static Gson gson = new Gson();
    public static Gson getGson() {return gson;}
    @Override
    public void onLoad(){
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Kyori
        this.adventure = BukkitAudiences.create(this);

        // Primary config
        if(!getDataFolder().exists()){getDataFolder().mkdir();}
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Crafting Recipes
        ItemStack bastion = Util.bastionItem();
        ShapedRecipe bastionRecipe = new ShapedRecipe(new NamespacedKey(this, "bastion"), bastion);
        bastionRecipe.shape("AAA", "AEA", "AAA");
        bastionRecipe.setIngredient('E', Material.END_CRYSTAL);
        bastionRecipe.setIngredient('A', Material.COAL_BLOCK);
        Bukkit.addRecipe(bastionRecipe);

        ItemStack bastionRangeExtender = Util.bastionRangeExtenderItem(4);
        ShapedRecipe extenderRecipe = new ShapedRecipe(new NamespacedKey(this, "extender"), bastionRangeExtender);
        extenderRecipe.shape("AAA", "AEA", "AAA");
        extenderRecipe.setIngredient('A', Material.ENDER_EYE);
        extenderRecipe.setIngredient('E', Material.END_CRYSTAL);
        Bukkit.addRecipe(extenderRecipe);

        // Event Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerData(), this);
        Bukkit.getPluginManager().registerEvents(new Reinforce(), this);
        Bukkit.getPluginManager().registerEvents(new BreakReinforcement(), this);
        Bukkit.getPluginManager().registerEvents(new Fortify(), this);
        Bukkit.getPluginManager().registerEvents(new InspectEvent(), this);
        Bukkit.getPluginManager().registerEvents(new Claim(), this);
        Bukkit.getPluginManager().registerEvents(new BastionEvents(), this);
        Bukkit.getPluginManager().registerEvents(new BastionGUI(), this);
        Bukkit.getPluginManager().registerEvents(new BastionFuelGUI(), this);
        Bukkit.getPluginManager().registerEvents(new BastionUpgradeGUI(), this);
        Bukkit.getPluginManager().registerEvents(new SwitchEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ExtenderEvents(), this);
        Bukkit.getPluginManager().registerEvents(new ExtenderGUI(), this);
        Bukkit.getPluginManager().registerEvents(new Pistons(), this);
        Bukkit.getPluginManager().registerEvents(new BigDoorsOpener(), this);

        final Dynmap eventListener = new Dynmap();
        DynmapCommonAPIListener.register(eventListener);
        Bukkit.getPluginManager().registerEvents(eventListener, this);


        // Plugin Commands
        getCommand("tribe").setExecutor(new TribeCommand(this));
        getCommand("tribe").setTabCompleter(new TabComplete());

        // Check to see if the necessary directories already exist.
        File player_data_folder = new File(getDataFolder(), "PlayerData");
        if (!player_data_folder.exists()) {player_data_folder.mkdir();}
        File tribe_data_folder = new File(getDataFolder(), "TribeData");
        if (!tribe_data_folder.exists()) {tribe_data_folder.mkdir();}

        // Load Bastions & Extenders
        Bastion.loadBastions();
        Extender.loadExtenders();
        // Load Tribes data from files
        TribeData tribe_hashmap = new TribeData();
        tribe_hashmap.loadTribes();

        // Tasks
        // Get input from config
        int period = this.getConfig().getInt("base-consumption-period");
        // Convert to minutes
        period = period * 20 * 60;
        BukkitTask fuelConsumption = new FuelConsumption().runTaskTimer(this, period, period);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Save Bastions and Extenders
        Bastion.saveBastions();
        Extender.saveExtenders();
        // Save Player and Tribe Data
        for (PlayerData playerData : PlayerData.player_data_hashmap.values()){
            playerData.setMode("None");
            playerData.setTarget(playerData.getUuid());
            File playerFolder = new File(TWClaim.getPlugin().getDataFolder(), "PlayerData");
            File playerFile = new File(playerFolder, playerData.getUuid() + ".json");
            try {
                FileWriter playerFileWriter = new FileWriter(playerFile, false);
                TWClaim.getGson().toJson(playerData, playerFileWriter);
                playerFileWriter.flush();
                playerFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TribeData tribe_hashmap = new TribeData();
        tribe_hashmap.saveTribes();

        // Kyori
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }
}
