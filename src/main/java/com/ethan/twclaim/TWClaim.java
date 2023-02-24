package com.ethan.twclaim;

import com.ethan.twclaim.commands.TribeCommand;
import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TWClaim extends JavaPlugin {
    private static TWClaim plugin;
    public static Plugin getPlugin() {return plugin;}

    private static Gson gson = new Gson();
    public static Gson getGson() {return gson;}

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        // Primary config
        if(!getDataFolder().exists()){getDataFolder().mkdir();}
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Event Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerData(), this);

        // Plugin Commands
        getCommand("tribe").setExecutor(new TribeCommand());

        // Check to see if the necessary directories already exist.
        File player_data_folder = new File(getDataFolder(), "PlayerData");
        if (!player_data_folder.exists()) {player_data_folder.mkdir();}
        File tribe_data_folder = new File(getDataFolder(), "TribeData");
        if (!player_data_folder.exists()) {player_data_folder.mkdir();}

        // Load Tribes data from files
        TribeData tribe_hashmap = new TribeData();
        tribe_hashmap.loadTribes();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        TribeData tribe_hashmap = new TribeData();
        tribe_hashmap.saveTribes();
    }
}
