package com.faithfulmc.framework.command.module.teleport;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldCommand extends BaseCommand {
    public WorldCommand() {
        super("world", "Change current world.");
        this.setAliases(new String[]{"changeworld", "switchworld"});
        this.setUsage("/(command) <worldName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + this.getUsage());
            return true;
        }
        final World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World '" + args[0] + "' not found.");
            return true;
        }
        final Player player = (Player) sender;
        if (player.getWorld().equals(world)) {
            sender.sendMessage(ChatColor.RED + "You are already in that world.");
            return true;
        }
        final Location origin = player.getLocation();
        final Location location = new Location(world, origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch());
        player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
        sender.sendMessage(BaseConstants.GRAY + "Switched world to '" + world.getName() + BaseConstants.YELLOW + " [" + WordUtils.capitalizeFully(world.getEnvironment().name().replace('_', ' ')) + ']' + BaseConstants.GRAY + "'.");
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        final List worlds = Bukkit.getWorlds();
        final ArrayList results = new ArrayList(worlds.size());
        for (final World world : Bukkit.getWorlds()) {
            results.add(world.getName());
        }
        return BukkitUtils.getCompletions(args, results);
    }
}
