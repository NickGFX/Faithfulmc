package com.faithfulmc.util.command;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.chat.Text;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ArgumentExecutor implements CommandExecutor, TabCompleter {
    protected final List<CommandArgument> arguments;
    protected final String label;

    public ArgumentExecutor(final String label) {
        this.arguments = new ArrayList<CommandArgument>();
        this.label = label;
    }

    public boolean containsArgument(final CommandArgument argument) {
        return this.arguments.contains(argument);
    }

    public void addArgument(final CommandArgument argument) {
        this.arguments.add(argument);
    }

    public void removeArgument(final CommandArgument argument) {
        this.arguments.remove(argument);
    }

    public CommandArgument getArgument(final String id) {
        for (final CommandArgument argument : this.arguments) {
            final String name = argument.getName();
            if (name.equalsIgnoreCase(id) || Arrays.asList(argument.getAliases()).contains(id.toLowerCase())) {
                return argument;
            }
        }
        return null;
    }

    public String getLabel() {
        return this.label;
    }

    public List<CommandArgument> getArguments() {
        return ImmutableList.copyOf(this.arguments);
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
        final CommandArgument argument2 = this.getArgument(args[0]);
        final String permission2 = (argument2 == null) ? null : argument2.getPermission();
        if (argument2 == null || (permission2 != null && !sender.hasPermission(permission2))) {
            sender.sendMessage(ChatColor.RED + WordUtils.capitalizeFully(this.label) + " sub-command " + args[0] + " not found.");
            return true;
        }
        argument2.onCommand(sender, command, label, args);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        List<String> results = new ArrayList<String>();
        if (args.length < 2) {
            for (final CommandArgument argument : this.arguments) {
                final String permission = argument.getPermission();
                if (permission == null || sender.hasPermission(permission)) {
                    results.add(argument.getName());
                }
            }
        } else {
            final CommandArgument argument2 = this.getArgument(args[0]);
            if (argument2 == null) {
                return results;
            }
            final String permission2 = argument2.getPermission();
            if (permission2 == null || sender.hasPermission(permission2)) {
                results = argument2.onTabComplete(sender, command, label, args);
                if (results == null) {
                    return null;
                }
            }
        }
        return BukkitUtils.getCompletions(args, results);
    }
}
