package com.faithfulmc.hardcorefactions.timer.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.Timer;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

//TODO
public class TimerCheckArgument extends CommandArgument {
    private final HCF plugin;

    public TimerCheckArgument(HCF plugin) {
        super("check", "Check remaining timer time");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + this.getName() + " <timerName> <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        PlayerTimer temporaryTimer = null;
        for (Timer timer : this.plugin.getTimerManager().getTimers()) {
            if (timer instanceof PlayerTimer && timer.getName().equalsIgnoreCase(args[1])) {
                temporaryTimer = (PlayerTimer) timer;
                break;
            }
        }
        if (temporaryTimer == null) {
            sender.sendMessage(ConfigurationService.RED + "Timer '" + args[1] + "' not found.");
            return true;
        }
        PlayerTimer playerTimer = temporaryTimer;
        new BukkitRunnable() {
            public void run() {
                UUID uuid;
                try {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                    if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore()) {
                        uuid = offlinePlayer.getUniqueId();
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    sender.sendMessage(ConfigurationService.GOLD + "Player '" + ConfigurationService.WHITE + args[2] + ConfigurationService.GOLD + "' not found.");
                    return;
                }
                long remaining = playerTimer.getRemaining(uuid);
                sender.sendMessage(ConfigurationService.YELLOW + args[2] + " has timer " + playerTimer.getName() + " for another " + DurationFormatUtils.formatDurationWords(remaining, true, true));
            }
        }.runTaskAsynchronously(this.plugin);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return (args.length == 2) ? null : Collections.emptyList();
    }
}