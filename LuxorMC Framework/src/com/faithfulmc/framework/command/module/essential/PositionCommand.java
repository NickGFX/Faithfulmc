package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PositionCommand extends BaseCommand {
    public PositionCommand() {
        super("position", "Checks the position of a player.");
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        Player target;
        if (args.length > 0) {
            target = BukkitUtils.playerWithNameOrUUID(args[0]);
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            target = (Player) sender;
        }
        if (target != null && BaseCommand.canSee(sender, target)) {
            final Location pos = target.getLocation();
            sender.sendMessage(BaseConstants.YELLOW + "Player: " + target.getName());
            sender.sendMessage(BaseConstants.YELLOW + "World: " + target.getWorld().getName());
            sender.sendMessage(BaseConstants.YELLOW + String.format("Location: (%.3f, %.3f, %.3f)", pos.getX(), pos.getY(), pos.getZ()));
            sender.sendMessage(BaseConstants.YELLOW + "Depth: " + (int) Math.floor(pos.getY()));
            return true;
        }
        sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
