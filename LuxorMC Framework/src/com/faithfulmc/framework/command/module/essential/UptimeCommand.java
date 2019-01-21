package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.TimeZone;

public class UptimeCommand extends BaseCommand {
    private static final FastDateFormat TIME_FORMATTER;

    static {
        TIME_FORMATTER = FastDateFormat.getInstance("dd/MM HH:mm:ss", TimeZone.getTimeZone("GMT+1"), Locale.ENGLISH);
    }

    public UptimeCommand() {
        super("uptime", "Check the uptime of the server.");
        this.setUsage("/(command)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        final String upTime = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - startTime, true, true);
        sender.sendMessage(ChatColor.BLUE + "Server up-time: " + BaseConstants.GOLD + upTime + ChatColor.BLUE + ", started at " + UptimeCommand.TIME_FORMATTER.format(startTime) + ".");
        return true;
    }
}
