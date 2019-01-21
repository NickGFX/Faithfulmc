package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;

public class BroadcastCommand extends BaseCommand {
    private final BasePlugin plugin;

    public BroadcastCommand(final BasePlugin plugin) {
        super("broadcast", "Broadcasts a message to the server.");
        this.setAliases(new String[]{"bc"});
        this.setUsage("/(command) [-p *perm*] <text..>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
                    return;
                }
                final String arg;
                byte position;
                String requiredNode;
                if (args.length > 2 && (arg = args[0]).startsWith("-p")) {
                    position = 1;
                    requiredNode = arg.substring(2, arg.length());
                } else {
                    position = 0;
                    requiredNode = null;
                }
                String message = StringUtils.join((Object[]) args, ' ', (int) position, args.length);
                if (message.length() < 3) {
                    sender.sendMessage(ChatColor.RED + "Broadcasts must be at least 3 characters.");
                    return;
                }
                message = ChatColor.translateAlternateColorCodes('&', String.format(Locale.ENGLISH, plugin.getServerHandler().getBroadcastFormat(), message));
                if (requiredNode != null) {
                    Bukkit.broadcast(message, requiredNode);
                } else {
                    Bukkit.broadcastMessage(message);
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
