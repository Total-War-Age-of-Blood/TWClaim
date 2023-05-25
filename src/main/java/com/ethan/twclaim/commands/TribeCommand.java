package com.ethan.twclaim.commands;

import com.ethan.twclaim.TWClaim;
import com.ethan.twclaim.commands.claiming.*;
import com.ethan.twclaim.commands.management.*;
import com.ethan.twclaim.data.PlayerData;
import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;

public class TribeCommand implements CommandExecutor {
    private final TWClaim twClaim;

    public TribeCommand(TWClaim twClaim) {
        this.twClaim = twClaim;
    }

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
            return CreateTribe.createTribe(player, args, tribeFolder, gson, playerData);
        }

        // TODO make tribe list paginated if it becomes too long
        if (args.length == 1 && args[0].equalsIgnoreCase("list")){
            TextComponent textComponent = Component.text("Your Tribes \n", NamedTextColor.GOLD, TextDecoration.UNDERLINED);
            for (String tribe : playerData.getTribes().values()){
                textComponent = textComponent.append(Component.text(tribe + "\n", NamedTextColor.WHITE).decoration(TextDecoration.UNDERLINED, false));
            }
            twClaim.adventure().player(player).sendMessage(textComponent);
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")){
            return AddMember.addMember(player, args, gson);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")){
            return JoinTribe.joinTribe(player, args, playerData);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("kick")) {
            return KickMember.kickMember(player, args, gson);
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

        if (args.length == 1 & args[0].equalsIgnoreCase("reinforce")){
           return ReinforceMode.reinforcePrivate(player, playerData);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reinforce")){
            return ReinforceMode.reinforceTribe(player, args, playerData);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("fortify")){
            return FortifyMode.fortifyPrivate(player, playerData);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("fortify")){
            return FortifyMode.fortifyTribe(player, args, playerData);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("claim")){
            return ClaimMode.claimPrivate(player, playerData);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("claim") && args[1].equalsIgnoreCase("confirm")){
            return ClaimConfirm.claimConfirm(player, playerData, twClaim, args);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("claim")){
            return ClaimMode.claimTribe(player, args, playerData);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("claim") && args[1].equalsIgnoreCase("confirm") && args[2].equalsIgnoreCase("multiple")){
            return ClaimConfirm.claimConfirm(player, playerData, twClaim, args);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            return TribeInfo.tribeInfo(player, args);
        }

        if (args.length < 4 && args[0].equalsIgnoreCase("map")){
            if (args.length == 3){
                try {
                    return TribeMap.tribeMap(player, twClaim, Integer.parseInt(args[1]), args[2]);
                } catch (NumberFormatException e){player.sendMessage(ChatColor.RED + "Usage: /tribe map [y-level] [direction]");}
            }
            return TribeMap.tribeMap(player, twClaim);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("leave")){
            LeaveTribe.leaveTribe(player, args);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("disband")){
            DisbandTribe.disbandTribe(player, args);
            return true;
        }

        // TODO create perms group
        if (args[0].equalsIgnoreCase("perms")){
            if (args[1].equalsIgnoreCase("create")){
                Perms.createPerms(player, args);
            }
            if (args[1].equalsIgnoreCase("delete")){
                Perms.deletePerms(player, args);
            }
            if (args[1].equalsIgnoreCase("edit")){
                Perms.editPerms(player, args);
            }
            if (args[1].equalsIgnoreCase("promote") || args[1].equalsIgnoreCase("demote")){
                Perms.promoteDemote(player, args);
            }
        }
        // TODO change perms group perms

        // TODO trust command to assign a certain member rank to anyone from a different tribe or to players by name

        return false;
    }
}
