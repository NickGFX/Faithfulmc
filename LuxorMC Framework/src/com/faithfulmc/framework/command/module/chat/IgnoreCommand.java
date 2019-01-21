package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.CommandArgument;
import com.faithfulmc.util.command.CommandWrapper;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class IgnoreCommand extends BaseCommand {
    private final CommandWrapper handler;

    public IgnoreCommand(final BasePlugin plugin) {
        super("ignore", "Ignores a player from messages.");
        this.setUsage("/(command) <list|add|del|clear> [playerName]");
        final ArrayList arguments = new ArrayList(4);
        arguments.add(new IgnoreClearArgument(plugin));
        arguments.add(new IgnoreListArgument(plugin));
        arguments.add(new IgnoreAddArgument(plugin));
        arguments.add(new IgnoreDeleteArgument(plugin));
        Collections.sort(arguments, new CommandWrapper.ArgumentComparator());
        this.handler = new CommandWrapper(arguments);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return this.handler.onCommand(sender, command, label, args);
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return this.handler.onTabComplete(sender, command, label, args);
    }

    private static class IgnoreDeleteArgument extends CommandArgument {
        private final BasePlugin plugin;

        public IgnoreDeleteArgument(final BasePlugin plugin) {
            super("delete", "Un-ignores a player.");
            this.plugin = plugin;
            this.aliases = new String[]{"del", "remove", "unset"};
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName() + " <playerName>";
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            sender.sendMessage(BaseConstants.YELLOW + "You are " + (this.plugin.getUserManager().getUser(((Player) sender).getUniqueId()).getIgnoring().remove(args[1]) ? (ChatColor.RED + "not") : (ChatColor.GREEN + "no longer")) + BaseConstants.YELLOW + " ignoring " + args[1] + '.');
            return true;
        }

        @Override
        public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
            return null;
        }
    }

    private static class IgnoreListArgument extends CommandArgument {
        private final BasePlugin plugin;

        public IgnoreListArgument(final BasePlugin plugin) {
            super("list", "Lists all ignored players.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName();
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            final Set ignoring = this.plugin.getUserManager().getUser(((Player) sender).getUniqueId()).getIgnoring();
            if (ignoring.isEmpty()) {
                sender.sendMessage(BaseConstants.YELLOW + "You are not ignoring anyone.");
                return true;
            }
            sender.sendMessage(BaseConstants.YELLOW + "You are ignoring (" + ignoring.size() + ") members: " + '[' + ChatColor.WHITE + StringUtils.join((Iterable) ignoring, ", ") + BaseConstants.YELLOW + ']');
            return true;
        }

        @Override
        public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
            return Collections.emptyList();
        }
    }

    private static class IgnoreClearArgument extends CommandArgument {
        private final BasePlugin plugin;

        public IgnoreClearArgument(final BasePlugin plugin) {
            super("clear", "Clears all ignored players.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName();
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            ServerParticipator serverParticipator = plugin.getUserManager().getParticipator(sender);
            final Set ignoring = serverParticipator.getIgnoring();
            if (ignoring.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Your ignore list is already empty.");
                return true;
            }
            ignoring.clear();
            serverParticipator.update();
            sender.sendMessage(BaseConstants.YELLOW + "Your ignore list has been cleared.");
            return true;
        }

        @Override
        public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
            return Collections.emptyList();
        }
    }

    private static class IgnoreAddArgument extends CommandArgument {
        private final BasePlugin plugin;

        public IgnoreAddArgument(final BasePlugin plugin) {
            super("add", "Starts ignoring a player.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName() + " <playerName>";
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            final Player player = (Player) sender;
            final UUID uuid = player.getUniqueId();
            final BaseUser baseUser = this.plugin.getUserManager().getUser(uuid);
            final Set ignoring = baseUser.getIgnoring();
            final Player target = BukkitUtils.playerWithNameOrUUID(args[1]);
            if (target == null || !BaseCommand.canSee(sender, target)) {
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[1]));
                return true;
            }
            if (sender.equals(target)) {
                sender.sendMessage(ChatColor.RED + "You may not ignore yourself.");
                return true;
            }
            if (target.hasPermission("base.command.ignore.exempt")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to ignore this player.");
                return true;
            }
            final String targetName = target.getName();
            if (ignoring.add(target.getName())) {
                sender.sendMessage(BaseConstants.GOLD + "You are now ignoring " + targetName + '.');
                baseUser.update();
            } else {
                sender.sendMessage(ChatColor.RED + "You are already ignoring someone named " + targetName + '.');
            }
            return true;
        }

        @Override
        public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
            return (args.length == 2) ? null : Collections.emptyList();
        }
    }
}
