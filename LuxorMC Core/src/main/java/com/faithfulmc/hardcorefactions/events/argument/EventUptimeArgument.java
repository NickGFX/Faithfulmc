package com.faithfulmc.hardcorefactions.events.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class EventUptimeArgument extends CommandArgument {
    private final HCF plugin;


    public EventUptimeArgument(HCF plugin) {

        super("uptime", "Check the uptime of an event");

        this.plugin = plugin;

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName();

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        EventTimer eventTimer = this.plugin.getTimerManager().eventTimer;

        if (eventTimer.getRemaining() <= 0L) {

            sender.sendMessage(ConfigurationService.RED + "There is not a running event.");

            return true;

        }

        EventFaction eventFaction = eventTimer.getEventFaction();

        sender.sendMessage(ConfigurationService.YELLOW + "Up-time of " + eventTimer.getName() + " timer" + (eventFaction == null ? "" : new StringBuilder().append(": ").append(ChatColor.BLUE).append('(').append(eventFaction.getDisplayName(sender)).append(ChatColor.BLUE).append(')').toString()) + ConfigurationService.YELLOW + " is " + ConfigurationService.GRAY + org.apache.commons.lang.time.DurationFormatUtils.formatDurationWords(eventTimer.getUptime(), true, true) + ConfigurationService.YELLOW + ", started at " + ConfigurationService.GOLD + DateTimeFormats.HR_MIN_AMPM_TIMEZONE.format(eventTimer.getStartStamp()) + ConfigurationService.YELLOW + '.');

        return true;

    }

}