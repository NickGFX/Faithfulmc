package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.type.PvpProtectionTimer;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class PvpTimerCommand implements CommandExecutor, org.bukkit.command.TabCompleter {

    private static final List<String> COMPLETIONS = ImmutableList.of("enable", "time");
    private final HCF plugin;


    public PvpTimerCommand(HCF plugin) {

        this.plugin = plugin;

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");

            return true;

        }

        Player player = (Player) sender;


        PvpProtectionTimer pvpTimer = this.plugin.getTimerManager().pvpProtectionTimer;

        if (args.length < 1) {

            printUsage(sender, label, pvpTimer);

            return true;

        }

        if ((args[0].equalsIgnoreCase("enable")) || (args[0].equalsIgnoreCase("remove")) || (args[0].equalsIgnoreCase("off"))) {

            if (pvpTimer.getRemaining(player) > 0L) {

                sender.sendMessage(ConfigurationService.RED + "Your " + pvpTimer.getDisplayName() + ConfigurationService.RED + " timer is now off.");

                pvpTimer.clearCooldown(player);

                return true;

            }

            sender.sendMessage(ConfigurationService.RED + "Your " + pvpTimer.getDisplayName() + ConfigurationService.RED + " timer is currently not active.");

            return true;

        }

        if ((!args[0].equalsIgnoreCase("remaining")) && (!args[0].equalsIgnoreCase("time")) && (!args[0].equalsIgnoreCase("left")) && (!args[0].equalsIgnoreCase("check"))) {

            printUsage(sender, label, pvpTimer);

            return true;

        }

        long remaining = pvpTimer.getRemaining(player);

        if (remaining <= 0L) {

            sender.sendMessage(ConfigurationService.RED + "Your " + pvpTimer.getDisplayName() + ConfigurationService.RED + " timer is currently not active.");

            return true;

        }

        sender.sendMessage(ConfigurationService.YELLOW + "Your " + pvpTimer.getDisplayName() + ConfigurationService.YELLOW + " timer is active for another " + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.YELLOW + (pvpTimer.isPaused(player) ? " and is currently paused" : "") + '.');

        return true;

    }


    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        return args.length == 1 ? BukkitUtils.getCompletions(args, COMPLETIONS) : java.util.Collections.emptyList();

    }


    private void printUsage(CommandSender sender, String label, PvpProtectionTimer pvpTimer) {

        sender.sendMessage(ConfigurationService.YELLOW + pvpTimer.getName() + " Help");

        sender.sendMessage(ConfigurationService.GRAY + "/" + label + " enable - Disables your PvPTimer");

        sender.sendMessage(ConfigurationService.GRAY + "/" + label + " check - Shows your PvPTimer\'s status");

        sender.sendMessage(ConfigurationService.GRAY + "/lives - Life and deathban related commands.");

    }

}