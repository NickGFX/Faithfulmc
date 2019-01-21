package com.faithfulmc.hardcorefactions.events.argument;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EventScheduleArgument extends CommandArgument{
    private final HCF plugin;

    public EventScheduleArgument(HCF plugin) {
        super("schedule", "Displays event schedule");
        this.plugin = plugin;
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        EventTimer eventTimer = plugin.getTimerManager().eventTimer;
        boolean nextCancelled = eventTimer.isNextCancelled();
        if(nextCancelled || eventTimer.getNextEventFaction() == null || eventTimer.getNextEvent() == null){
            sender.sendMessage(ChatColor.YELLOW + "There are currently no events scheduled");
        } else{
            sender.sendMessage(EventType.KOTH.getPrefix() + ChatColor.LIGHT_PURPLE + eventTimer.getNextEventFaction().getName() + ChatColor.YELLOW + " in " + ChatColor.AQUA + DurationFormatUtils.formatDurationWords(eventTimer.getNextEvent() - System.currentTimeMillis(), true, true));
        }
        return true;
    }
}
