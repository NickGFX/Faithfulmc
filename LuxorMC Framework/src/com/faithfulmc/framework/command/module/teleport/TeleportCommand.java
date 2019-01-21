package com.faithfulmc.framework.command.module.teleport;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.List;

public class TeleportCommand extends BaseCommand {
    static final int MAX_COORD = 30000000;
    static final int MIN_COORD_MINUS_ONE = -30000001;
    static final int MIN_COORD = -30000000;

    public TeleportCommand() {
        super("teleport", "Teleport to a player or position.");
        this.setUsage("/(command) (<playerName> [otherPlayerName]) | (x y z)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1 || args.length > 4) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        Player targetA;
        if (args.length != 1 && args.length != 3) {
            if (sender.hasPermission(getPermission() + ".other")) {
                targetA = BukkitUtils.playerWithNameOrUUID(args[0]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Usage: " + this.getUsage());
                return true;
            }
            targetA = (Player) sender;
        }
        if (targetA != null && BaseCommand.canSee(sender, targetA)) {
            if (args.length < 3) {
                final Player targetALocation = BukkitUtils.playerWithNameOrUUID(args[args.length - 1]);
                if (targetALocation == null || !BaseCommand.canSee(sender, targetALocation)) {
                    sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[args.length - 1] + BaseConstants.GOLD + "' not found.");
                    return true;
                }
                if (targetA.equals(targetALocation)) {
                    sender.sendMessage(ChatColor.RED + "The teleportee and teleported are the same player.");
                    return true;
                }
                if (targetA.teleport((Entity) targetALocation, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                    sender.sendMessage(BaseConstants.YELLOW + "Teleported " + targetA.getName() + " to " + targetALocation.getName() + '.');
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to teleport you to " + targetALocation.getName() + '.');
                }
            } else if (targetA.getWorld() != null) {
                final Location targetALocation2 = targetA.getLocation();
                final double x = this.getCoordinate(sender, targetALocation2.getX(), args[args.length - 3]);
                final double y = this.getCoordinate(sender, targetALocation2.getY(), args[args.length - 2], 0, 0);
                final double z = this.getCoordinate(sender, targetALocation2.getZ(), args[args.length - 1]);
                if (x == -3.0000001E7 || y == -3.0000001E7 || z == -3.0000001E7) {
                    sender.sendMessage("Please provide a valid location.");
                    return true;
                }
                targetALocation2.setX(x);
                targetALocation2.setY(y);
                targetALocation2.setZ(z);
                if (targetA.teleport(targetALocation2, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                    sender.sendMessage(String.format(BaseConstants.YELLOW + "Teleported %s to %.2f, %.2f, %.2f.", targetA.getName(), x, y, z));
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to teleport you.");
                }
            }
            return true;
        }
        sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
        return true;
    }

    private double getCoordinate(final CommandSender sender, final double current, final String input) {
        return this.getCoordinate(sender, current, input, -30000000, 30000000);
    }

    private double getCoordinate(final CommandSender sender, final double current, String input, final int min, final int max) {
        final boolean relative = input.startsWith("~");
        double result = relative ? current : 0.0;
        if (!relative || input.length() > 1) {
            final boolean exact = input.contains(".");
            if (relative) {
                input = input.substring(1);
            }
            final double testResult = VanillaCommand.getDouble(sender, input);
            if (testResult == -3.0000001E7) {
                return -3.0000001E7;
            }
            result += testResult;
            if (!exact && !relative) {
                result += 0.5;
            }
        }
        if (min != 0 || max != 0) {
            if (result < min) {
                result = -3.0000001E7;
            }
            if (result > max) {
                result = -3.0000001E7;
            }
        }
        return result;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length != 1 && args.length != 2) ? Collections.emptyList() : null;
    }
}
