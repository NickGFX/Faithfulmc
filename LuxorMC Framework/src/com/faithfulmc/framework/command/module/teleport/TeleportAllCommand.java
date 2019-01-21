package com.faithfulmc.framework.command.module.teleport;

import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeleportAllCommand extends BaseCommand {
    public TeleportAllCommand() {
        super("teleportall", "Teleport all players to yourself.");
        this.setAliases(new String[]{"tpall"});
        this.setUsage("/(command)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }
        final Player player = (Player) sender;
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && player.canSee(target)) {
                target.teleport((Entity) player, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
