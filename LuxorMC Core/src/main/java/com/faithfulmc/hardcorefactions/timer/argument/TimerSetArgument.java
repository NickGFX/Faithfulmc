package com.faithfulmc.hardcorefactions.timer.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.Timer;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.base.Function;
import net.minecraft.util.com.google.common.base.Predicate;
import net.minecraft.util.com.google.common.collect.FluentIterable;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

//TODO
public class TimerSetArgument extends CommandArgument {
    private static final Pattern WHITESPACE_TRIMMER;

    static {
        WHITESPACE_TRIMMER = Pattern.compile("\\s");
    }

    private final HCF plugin;

    public TimerSetArgument(final HCF plugin) {
        super("set", "Set remaining timer time");
        this.plugin = plugin;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <timerName> <all|playerName> <remaining>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        long duration = JavaUtils.parse(args[3]);
        if (duration == -1L) {
            sender.sendMessage(ConfigurationService.RED + "Invalid duration, use the correct format: 10m 1s");
            return true;
        }
        PlayerTimer playerTimer = null;
        for (Timer timer : this.plugin.getTimerManager().getTimers()) {
            if (timer instanceof PlayerTimer && TimerSetArgument.WHITESPACE_TRIMMER.matcher(timer.getName()).replaceAll("").equalsIgnoreCase(args[1])) {
                playerTimer = (PlayerTimer) timer;
                break;
            }
        }
        if (playerTimer == null) {
            sender.sendMessage(ConfigurationService.RED + "Timer '" + args[1] + "' not found.");
            return true;
        }
        if (args[2].equalsIgnoreCase("all")) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                playerTimer.setCooldown(player, player.getUniqueId(), duration, true);
            }
            sender.sendMessage(ChatColor.BLUE + "Set timer " + playerTimer.getName() + " for all to " + DurationFormatUtils.formatDurationWords(duration, true, true) + '.');
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            Player targetPlayer = null;
            if (target == null || (sender instanceof Player && (targetPlayer = target.getPlayer()) != null && !((Player) sender).canSee(targetPlayer))) {
                sender.sendMessage(ConfigurationService.GOLD + "Player '" + ConfigurationService.WHITE + args[1] + ConfigurationService.GOLD + "' not found.");
                return true;
            }
            playerTimer.setCooldown(targetPlayer, target.getUniqueId(), duration, true);
            sender.sendMessage(ChatColor.BLUE + "Set timer " + playerTimer.getName() + " duration to " + DurationFormatUtils.formatDurationWords(duration, true, true) + " for " + target.getName() + '.');
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            return FluentIterable.from(this.plugin.getTimerManager().getTimers()).filter(new Predicate<Timer>() {
                public boolean apply(final Timer timer) {
                    return timer instanceof PlayerTimer;
                }
            }).transform( new Function<Timer, String>() {
                public String apply(final Timer timer) {
                    return TimerSetArgument.WHITESPACE_TRIMMER.matcher(timer.getName()).replaceAll("");
                }
            }).toList();
        }
        if (args.length == 3) {
            List<String> list = new ArrayList<String>();
            list.add("ALL");
            Player player = (Player) sender;
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player == null || player.canSee(target)) {
                    list.add(target.getName());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}