package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import com.faithfulmc.util.command.CommandWrapper;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class MessageSpyCommand extends BaseCommand {
    private final CommandWrapper handler;

    public MessageSpyCommand(final BasePlugin plugin) {
        super("messagespy", "Spies on the PM's of a player.");
        this.setAliases(new String[]{"ms", "msgspy", "pmspy", "whisperspy", "privatemessagespy", "tellspy"});
        this.setUsage("/(command) <list|add|del|clear> [playerName]");
        final ArrayList arguments = new ArrayList(4);
        arguments.add(new MessageSpyListArgument(plugin));
        arguments.add(new IgnoreClearArgument(plugin));
        arguments.add(new MessageSpyAddArgument(plugin));
        arguments.add(new MessageSpyDeleteArgument(plugin));
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

    private static class MessageSpyListArgument extends CommandArgument {
        private final BasePlugin plugin;

        public MessageSpyListArgument(final BasePlugin plugin) {
            super("list", "Lists all players you're spying on.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName();
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            final ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            final LinkedHashSet spyingNames = new LinkedHashSet();
            final Set<String> messageSpying = participator.getMessageSpying();
            if (messageSpying.size() == 1 && Iterables.getOnlyElement((Iterable) messageSpying).equals("all")) {
                sender.sendMessage(BaseConstants.GRAY + "You are currently spying on the messages of all players.");
                return true;
            }
            for (final String spyingId : messageSpying) {
                final String name = Bukkit.getOfflinePlayer(UUID.fromString(spyingId)).getName();
                if (name != null) {
                    spyingNames.add(name);
                }
            }
            if (spyingNames.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "You are not spying on the messages of any players.");
                return true;
            }
            sender.sendMessage(BaseConstants.GRAY + "You are currently spying on the messages of (" + spyingNames.size() + " players): " + ChatColor.RED + StringUtils.join((Iterable) spyingNames, BaseConstants.GRAY.toString() + ", " + ChatColor.RED) + BaseConstants.GRAY + '.');
            return true;
        }
    }

    private static class IgnoreClearArgument extends CommandArgument {
        private final BasePlugin plugin;

        public IgnoreClearArgument(final BasePlugin plugin) {
            super("clear", "Clears your current spy list.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName();
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            final ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            participator.getMessageSpying().clear();
            participator.update();
            sender.sendMessage(BaseConstants.YELLOW + "You are no longer spying the messages of anyone.");
            return true;
        }
    }

    private static class MessageSpyAddArgument extends CommandArgument {
        private final BasePlugin plugin;

        public MessageSpyAddArgument(final BasePlugin plugin) {
            super("add", "Adds a player to your message spy list.");
            this.plugin = plugin;
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName() + " <all|playerName>";
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            final ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            final Set messageSpying = participator.getMessageSpying();
            final boolean all;
            if ((all = messageSpying.contains("all")) || JavaUtils.containsIgnoreCase(messageSpying, args[1])) {
                sender.sendMessage(ChatColor.RED + "You are already spying on the messages of " + (all ? "all players" : args[1]) + '.');
                return true;
            }
            if (args[1].equalsIgnoreCase("all")) {
                messageSpying.clear();
                messageSpying.add("all");
                participator.update();
                sender.sendMessage(ChatColor.GREEN + "You are now spying on the messages of all players.");
                return true;
            }
            final OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);
            if (!offlineTarget.hasPlayedBefore() && offlineTarget.getPlayer() == null) {
                sender.sendMessage(BaseConstants.GOLD + "Player '" + ChatColor.WHITE + args[1] + BaseConstants.GOLD + "' not found.");
                return true;
            }
            if (offlineTarget.equals(sender)) {
                sender.sendMessage(ChatColor.RED + "You cannot spy on the messages of yourself.");
                return true;
            }
            sender.sendMessage(BaseConstants.YELLOW + "You are " + (messageSpying.add(offlineTarget.getUniqueId().toString()) ? (ChatColor.GREEN + "now") : (ChatColor.RED + "already")) + BaseConstants.YELLOW + " spying on the messages of " + offlineTarget.getName() + '.');
            participator.update();
            return true;
        }

        @Override
        public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
            return (args.length == 2) ? null : Collections.emptyList();
        }
    }

    private static class MessageSpyDeleteArgument extends CommandArgument {
        private final BasePlugin plugin;

        public MessageSpyDeleteArgument(final BasePlugin plugin) {
            super("delete", "Deletes a player from your message spy list.");
            this.plugin = plugin;
            this.aliases = new String[]{"del", "remove"};
        }

        @Override
        public String getUsage(final String label) {
            return '/' + label + ' ' + this.getName() + " <playerName>";
        }

        @Override
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            final ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            final Set messageSpying = participator.getMessageSpying();
            if (args[1].equalsIgnoreCase("all")) {
                messageSpying.remove("all");
                sender.sendMessage(ChatColor.RED + "You are no longer spying on the messages of all players.");
                return true;
            }
            final OfflinePlayer offlineTarget = BukkitUtils.offlinePlayerWithNameOrUUID(args[1]);
            if (!offlineTarget.hasPlayedBefore() && !offlineTarget.isOnline()) {
                sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[1] + BaseConstants.GOLD + "' not found.");
                return true;
            }
            sender.sendMessage("You are " + (messageSpying.remove(offlineTarget.getUniqueId().toString()) ? (ChatColor.GREEN + "no longer") : (ChatColor.RED + "still not")) + BaseConstants.YELLOW + " spying on the messages of " + offlineTarget.getName() + '.');
            return true;
        }

        @Override
        public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
            return (args.length == 2) ? null : Collections.emptyList();
        }
    }
}
