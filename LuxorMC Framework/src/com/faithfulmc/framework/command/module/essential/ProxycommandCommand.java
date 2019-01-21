package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageRecipient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProxycommandCommand extends BaseCommand {
    private static final String PROXY_CHANNEL = "BungeeCord";
    private final BasePlugin plugin;

    public ProxycommandCommand(final BasePlugin plugin) {
        super("proxycommand", "Used to execute a command from the proxy.");
        this.setUsage("/(command) <command args..>");
        this.plugin = plugin;
        Bukkit.getMessenger().registerOutgoingPluginChannel((Plugin) plugin, "BungeeCord");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("DispatchCommand");
        out.writeUTF(sender.getName());
        final String fullCommand;
        out.writeUTF(fullCommand = StringUtils.join((Object[]) args, ' ', 0, args.length));
        PluginMessageRecipient pluginMessageRecipient;
        if (sender instanceof PluginMessageRecipient) {
            pluginMessageRecipient = (PluginMessageRecipient) sender;
        } else {
            pluginMessageRecipient = (PluginMessageRecipient) Iterables.getFirst((Iterable) Bukkit.getOnlinePlayers(), (Object) null);
            if (pluginMessageRecipient == null) {
                sender.sendMessage(ChatColor.RED + "Unable to send plugin message, no players are online.");
                return true;
            }
        }
        pluginMessageRecipient.sendPluginMessage((Plugin) this.plugin, "BungeeCord", out.toByteArray());
        Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Executed proxy command " + fullCommand + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
