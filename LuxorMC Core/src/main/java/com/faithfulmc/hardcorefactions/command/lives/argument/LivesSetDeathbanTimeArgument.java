package com.faithfulmc.hardcorefactions.command.lives.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class LivesSetDeathbanTimeArgument extends CommandArgument {

    public LivesSetDeathbanTimeArgument() {
        super("setdeathbantime", "Sets the base deathban time");
        this.permission = ("hcf.command.lives.argument." + getName());
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <time>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            long duration = JavaUtils.parse(args[1]);
            if (duration == -1L) {
                sender.sendMessage(ConfigurationService.RED + "Invalid duration, use the correct format: 10m 1s");
            }
            else {
                ConfigurationService.DEFAULT_DEATHBAN_DURATION = duration;
                Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Base death-ban time set to " + org.apache.commons.lang.time.DurationFormatUtils.formatDurationWords(duration, true, true) + " (not including multipliers, etc).");
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}