package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.GlobalTimer;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.JavaUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;


public class TimerCommand implements CommandExecutor {
    private final GlobalTimer timer;
    private final String name;
    private final String displayName;

    public TimerCommand(GlobalTimer timer) {
        this.timer = timer;
        name = timer.getName();
        displayName = timer.getDisplayName();
    }

    private String commandName;

    public boolean onCommand(CommandSender commandSender, Command cmd, String cmdLabel, String[] args) {
        commandName = cmd.getName();
        if (args.length == 0) {
            sendHelp(commandSender);
        }
        else if (args.length == 2) {
            long duration = JavaUtils.parse(StringUtils.join(args, ' ', 1, args.length));
            if (duration == -1L) {
                commandSender.sendMessage(ConfigurationService.RED + "Invalid duration, use the correct format: 10m 1s");
            } else {
                timer.setRemaining(duration, true);
                commandSender.sendMessage(ConfigurationService.YELLOW + name + " set to " + ConfigurationService.RED + HCF.getRemaining(duration, true, true));
            }
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sendHelp(commandSender);
            }
            else if (args[0].equalsIgnoreCase("start")) {
                timer.setRemaining(TimeUnit.HOURS.toMillis(3L), true);
                timer.setPaused(false);
                commandSender.sendMessage(ConfigurationService.YELLOW + name + " started.");
            }
            else if (timer.getRemaining() <= 0L) {
                commandSender.sendMessage(ConfigurationService.RED + name + " Timer hasn't started yet!");
            }
            else if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("stop")) {
                timer.setRemaining(TimeUnit.SECONDS.toMillis(0L), true);
                timer.setPaused(false);
                commandSender.sendMessage(ConfigurationService.YELLOW + name + " stopped.");
            }
            else if (args[0].equalsIgnoreCase("pause")) {
                timer.setPaused(true);
                commandSender.sendMessage(ConfigurationService.YELLOW + name + " paused.");
            }
            else if (args[0].equalsIgnoreCase("unpause")) {
                timer.setPaused(false);
                commandSender.sendMessage(ConfigurationService.YELLOW + name + " un-paused.");
            }
            else {
                commandSender.sendMessage(ConfigurationService.RED + "/" + commandName + " help.");
                return false;
            }
        }
        return true;
    }

    public void sendHelp(CommandSender commandSender){
        commandSender.sendMessage(BukkitUtils.STRAIGHT_LINE_DEFAULT);
        commandSender.sendMessage(ConfigurationService.YELLOW + displayName + ConfigurationService.YELLOW + " Help");
        commandSender.sendMessage(ConfigurationService.GRAY + "/" + commandName + " start - Starts the " + displayName + ConfigurationService.GRAY + ".");
        commandSender.sendMessage(ConfigurationService.GRAY + "/" + commandName + " stop - Stops the " + displayName + ConfigurationService.GRAY + ".");
        commandSender.sendMessage(ConfigurationService.GRAY + "/" + commandName + " pause - Pauses the " + displayName + ConfigurationService.GRAY + ".");
        commandSender.sendMessage(ConfigurationService.GRAY + "/" + commandName + " unpause - Un-pause the " + displayName + ConfigurationService.GRAY + ".");
        commandSender.sendMessage(ConfigurationService.GRAY + "/" + commandName + " set <time> - Set the  " + displayName + ConfigurationService.GRAY + ".");
        commandSender.sendMessage(BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

}