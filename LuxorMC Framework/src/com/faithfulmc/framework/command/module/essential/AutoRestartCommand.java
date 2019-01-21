package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.command.CommandArgument;
import com.faithfulmc.util.command.CommandWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class AutoRestartCommand extends BaseCommand {
    private final CommandWrapper handler;

    public AutoRestartCommand(final BasePlugin plugin) {
        super("autore", "Allows management of server restarts.");
        this.setAliases(new String[]{"autorestart"});
        this.setUsage("/(command) <cancel|time|schedule>");
        final ArrayList<CommandArgument> arguments = new ArrayList<CommandArgument>(3);
        arguments.add(new AutoRestartCancelArgument(plugin));
        arguments.add(new AutoRestartScheduleArgument(plugin));
        arguments.add(new AutoRestartTimeArgument(plugin));
        Collections.sort(arguments, new CommandWrapper.ArgumentComparator());
        this.handler = new CommandWrapper(arguments);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return this.handler.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return this.handler.onTabComplete(sender, command, label, args);
    }

    private static class AutoRestartTimeArgument extends CommandArgument {
        private final BasePlugin plugin;

        public AutoRestartTimeArgument(final BasePlugin plugin) {
            super("time", "Gets the remaining time until next restart.");
            this.plugin = plugin;
            this.aliases = new String[]{"remaining", "time"};
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName();
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            /*
            if (!this.plugin.getAutoRestartHandler().isPendingRestart()) {
                sender.sendMessage(ChatColor.RED + "There is not a restart task pending.");
                return true;
            }
            final String reason = this.plugin.getAutoRestartHandler().getReason();
            sender.sendMessage(BaseConstants.YELLOW + "Automatic restart task occurring in " + DurationFormatUtils.formatDurationWords(this.plugin.getAutoRestartHandler().getRemainingMilliseconds(), true, true) + (Strings.nullToEmpty(reason).isEmpty() ? "" : (" for " + reason)) + '.');
            */
            return true;
        }
    }

    private static class AutoRestartScheduleArgument extends CommandArgument {
        private final BasePlugin plugin;

        public AutoRestartScheduleArgument(final BasePlugin plugin) {
            super("schedule", "Schedule an automatic restart.");
            this.plugin = plugin;
            this.aliases = new String[]{"reschedule"};
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName() + " <time> [reason]";
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            /*
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + args[0].toLowerCase() + " <time> [reason]");
                return true;
            }
            final long millis = JavaUtils.parse(args[1]);
            if (millis == -1L) {
                sender.sendMessage(ChatColor.RED + "Invalid duration, use the correct format: 10m1s");
                return true;
            }
            final String reason = StringUtils.join((Object[])args, ' ', 2, args.length);
            this.plugin.getAutoRestartHandler().scheduleRestart(millis, reason);
            Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Scheduled a restart to occur in " + DurationFormatUtils.formatDurationWords(millis, true, true) + (reason.isEmpty() ? "" : (" for " + reason)) + '.');
            */
            return true;
        }
    }

    private static class AutoRestartCancelArgument extends CommandArgument {
        private final BasePlugin plugin;

        public AutoRestartCancelArgument(final BasePlugin plugin) {
            super("cancel", "Cancels the current automatic restart.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName();
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            /*
            if (!this.plugin.getAutoRestartHandler().isPendingRestart()) {
                sender.sendMessage(ChatColor.RED + "There is not a restart task pending.");
                return true;
            }
            this.plugin.getAutoRestartHandler().cancelRestart();
            sender.sendMessage(BaseConstants.YELLOW + "Automatic restart task cancelled.");
            */
            return true;

        }
    }
}
