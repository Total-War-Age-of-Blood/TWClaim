package com.ethan.twclaim.commands;

import com.ethan.twclaim.data.PlayerData;
import com.ethan.twclaim.data.TribeData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TabComplete implements TabCompleter {
    // TODO implement TabComplete for commands
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)){return null;}
        Player player = (Player) commandSender;
        List<String> options;
        if (args.length == 1){
            options = new ArrayList<>(Arrays.asList("leave", "kick", "disband", "create", "add", "fortify", "reinforce", "claim", "inspect", "info", "list", "map"));
            Collections.sort(options);
            return options;
        }

        if (args.length == 2){
            if (args[0].equalsIgnoreCase("create")){return null;}
            // Commands that need take an established tribe as an argument
            List<String> NEEDS_TRIBE = new ArrayList<>(Arrays.asList("join", "leave", "disband", "reinforce", "fortify", "claim", "info"));
            if (NEEDS_TRIBE.contains(args[0].toLowerCase())){
                if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info")){
                    options = new ArrayList<>(TribeData.tribeConversionHashmap.keySet());
                    Collections.sort(options);
                    return options;
                }
                PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
                List<String> tribeNames = new ArrayList<>();
                for (UUID key : playerData.getTribes().keySet()){
                    String tribeName = TribeData.tribe_hashmap.get(key).getName();
                    tribeNames.add(tribeName);
                }
                options = tribeNames;
                Collections.sort(options);
                return options;
            }
        }

        if (args.length == 3){
            if (args[0].equalsIgnoreCase("add")){
                PlayerData playerData = PlayerData.player_data_hashmap.get(player.getUniqueId());
                List<String> tribeNames = new ArrayList<>();
                for (UUID key : playerData.getTribes().keySet()){
                    String tribeName = TribeData.tribe_hashmap.get(key).getName();
                    tribeNames.add(tribeName);
                }
                options = tribeNames;
                Collections.sort(options);
                return options;
            }
        }
        return null;
    }
}
