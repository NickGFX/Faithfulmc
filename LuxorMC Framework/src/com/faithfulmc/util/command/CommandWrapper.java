package com.faithfulmc.util.command;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.util.BukkitUtils;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.*;

public class CommandWrapper implements CommandExecutor, TabCompleter {
    public static void printUsage(final CommandSender sender, final String label, final Collection<CommandArgument> arguments) {
        sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
        sender.sendMessage(BaseConstants.GOLD + ChatColor.BOLD.toString() + WordUtils.capitalizeFully(label) + " Help");
        for (final CommandArgument argument : arguments) {
            final String permission = argument.getPermission();
            if (permission == null || sender.hasPermission(permission)) {
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, argument.getUsage(label));
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BaseConstants.YELLOW + "Click to run " + BaseConstants.GRAY + argument.getUsage(label)));
                BaseComponent[] components = new ComponentBuilder(argument.getUsage(label)).color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).event(clickEvent).event(hoverEvent).append(" - " + argument.getDescription()).event(clickEvent).event(hoverEvent).create();
                if(sender instanceof Player){
                    ((Player)sender).spigot().sendMessage(components);
                }
                else{
                    sender.sendMessage(BaseComponent.toLegacyText(components));
                }
            }
        }
        sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
    }

    public static CommandArgument matchArgument(final String id, final CommandSender sender, final Collection<CommandArgument> arguments) {
        for (final CommandArgument argument : arguments) {
            final String permission = argument.getPermission();
            if ((permission == null || sender.hasPermission(permission)) && (argument.getName().equalsIgnoreCase(id) || Arrays.asList(argument.getAliases()).contains(id))) {
                return argument;
            }
        }
        return null;
    }

    public static List<String> getAccessibleArgumentNames(final CommandSender sender, final Collection<CommandArgument> arguments) {
        final List<String> results = new ArrayList<String>();
        for (final CommandArgument argument : arguments) {
            final String permission = argument.getPermission();
            if (permission == null || sender.hasPermission(permission)) {
                results.add(argument.getName());
            }
        }
        return results;
    }
    private final Collection<CommandArgument> arguments;

    public CommandWrapper(final Collection<CommandArgument> arguments) {
        this.arguments = arguments;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
            sender.sendMessage(BaseConstants.GOLD + ChatColor.BOLD.toString() + WordUtils.capitalizeFully(command.getName()) + " Help");
            for (final CommandArgument argument : this.arguments) {
                final String permission = argument.getPermission();
                if (permission == null || sender.hasPermission(permission)) {
                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, argument.getUsage(label));
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BaseConstants.YELLOW + "Click to run " + BaseConstants.GRAY + argument.getUsage(label)));
                    BaseComponent[] components = new ComponentBuilder(argument.getUsage(command.getName())).color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).event(clickEvent).event(hoverEvent).append(" - " + argument.getDescription()).event(clickEvent).event(hoverEvent).create();
                    if(sender instanceof Player){
                        ((Player)sender).spigot().sendMessage(components);
                    }
                    else{
                        sender.sendMessage(BaseComponent.toLegacyText(components));
                    }
                }
            }
            sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
            return true;
        }
        final CommandArgument argument2 = matchArgument(args[0], sender, arguments);
        final String permission2 = (argument2 == null) ? null : argument2.getPermission();
        if (argument2 == null || (permission2 != null && !sender.hasPermission(permission2))) {
            sender.sendMessage(ChatColor.RED + WordUtils.capitalizeFully(command.getName()) + " sub-command " + args[0] + " not found.");
            return true;
        }
        argument2.onCommand(sender, command, label, args);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        List<String> results;
        if (args.length == 1) {
            results = getAccessibleArgumentNames(sender, this.arguments);
        } else {
            final CommandArgument argument = matchArgument(args[0], sender, this.arguments);
            if (argument == null) {
                return Collections.emptyList();
            }
            results = argument.onTabComplete(sender, command, label, args);
            if (results == null) {
                return null;
            }
        }
        return BukkitUtils.getCompletions(args, results);
    }

    public static class ArgumentComparator implements Comparator<CommandArgument>, Serializable {
        @Override
        public int compare(final CommandArgument primaryArgument, final CommandArgument secondaryArgument) {
            return secondaryArgument.getName().compareTo(primaryArgument.getName());
        }
    }
}
