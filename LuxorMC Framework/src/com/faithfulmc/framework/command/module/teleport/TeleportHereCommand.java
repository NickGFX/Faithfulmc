package com.faithfulmc.framework.command.module.teleport;

import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TeleportHereCommand extends BaseCommand {
    public TeleportHereCommand() {
        super("teleporthere", "Teleport to a player to your position.");
        this.setAliases(new String[]{"tphere"});
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Player player = (Player) sender;
        player.performCommand("tp " + args[0] + ' ' + sender.getName());
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
