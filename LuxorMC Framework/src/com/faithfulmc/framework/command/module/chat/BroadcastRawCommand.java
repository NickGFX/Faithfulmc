package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.command.BaseCommand;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BroadcastRawCommand extends BaseCommand {
    public BroadcastRawCommand() {
        super("broadcastraw", "Broadcasts a raw message to the server.");
        this.setAliases(new String[]{"bcraw", "raw", "rawcast"});
        this.setUsage("/(command) [-p *perm*] <text..>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
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
            return true;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (requiredNode != null) {
            Bukkit.broadcast(message, requiredNode);
        } else {
            Bukkit.broadcastMessage(message);
        }
        return true;
    }
}
