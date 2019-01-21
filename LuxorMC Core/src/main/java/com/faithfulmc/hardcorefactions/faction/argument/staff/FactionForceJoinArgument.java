package com.faithfulmc.hardcorefactions.faction.argument.staff;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FactionForceJoinArgument extends CommandArgument {
    private final HCF plugin;

    public FactionForceJoinArgument(HCF plugin) {
        super("forcejoin", "Forcefully join a faction.");
        this.plugin = plugin;
        this.permission = ("hcf.command.faction.argument." + getName());
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can join factions.");
        }
        else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction != null) {
                sender.sendMessage(ConfigurationService.RED + "You are already in a faction.");
            } else {
                Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);
                if (faction == null) {
                    sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                } else if (!(faction instanceof PlayerFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "You can only join player factions.");
                } else {
                    playerFaction = (PlayerFaction) faction;
                    if (playerFaction.setMember(player, new FactionMember(playerFaction, player, ChatChannel.PUBLIC, Role.MEMBER), true)) {
                        playerFaction.broadcast(ConfigurationService.GOLD.toString() + ChatColor.BOLD + sender.getName() + " has forcefully joined the faction.");
                    }
                }
            }
        }
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if ((args.length != 2) || (!(sender instanceof Player))) {
            return Collections.emptyList();
        }
        if (args[1].isEmpty()) {
            return null;
        }
        Player player = (Player) sender;
        List<String> results = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if ((player.canSee(target)) && (!results.contains(target.getName()))) {
                results.add(target.getName());
            }
        }
        return results;

    }

}