package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SpeedCommand extends BaseCommand {
    private static final float DEFAULT_FLIGHT_SPEED = 2.0f;
    private static final float DEFAULT_WALK_SPEED = 1.0f;
    private static final ImmutableList COMPLETIONS_FIRST;
    private static final ImmutableList COMPLETIONS_SECOND;

    static {
        COMPLETIONS_FIRST = ImmutableList.of((Object) "fly", (Object) "walk");
        COMPLETIONS_SECOND = ImmutableList.of((Object) "reset");
    }

    public SpeedCommand() {
        super("speed", "Sets the fly/walk speed of a player.");
        this.setUsage("/(command) <fly|walk> <speedMultiplier|reset> [playerName]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        Player target;
        if (args.length > 2 && sender.hasPermission(command.getPermission() + ".others")) {
            target = BukkitUtils.playerWithNameOrUUID(args[2]);
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            target = (Player) sender;
        }
        if (target != null && BaseCommand.canSee(sender, target)) {
            Boolean flight;
            if (args[0].equalsIgnoreCase("fly")) {
                flight = true;
            } else {
                if (!args[0].equalsIgnoreCase("walk")) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                    return true;
                }
                flight = false;
            }
            Float multiplier;
            if (args[1].equalsIgnoreCase("reset")) {
                multiplier = (flight ? 2.0f : 1.0f);
            } else {
                multiplier = Floats.tryParse(args[1]);
                if (multiplier == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid speed multiplier: '" + args[1] + "'.");
                    return true;
                }
            }
            if (flight) {
                final float walkSpeed = 0.1f * multiplier;
                try {
                    target.setFlySpeed(walkSpeed);
                    Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Flight speed of " + target.getName() + " has been set to " + multiplier + '.');
                } catch (IllegalArgumentException var11) {
                    if (walkSpeed < 0.1f) {
                        sender.sendMessage(ChatColor.RED + "Speed multiplier too low: " + multiplier);
                    } else if (walkSpeed > 0.1f) {
                        sender.sendMessage(ChatColor.RED + "Speed multiplier too high: " + multiplier);
                    }
                }
            } else {
                final float walkSpeed = 0.2f * multiplier;
                try {
                    target.setWalkSpeed(walkSpeed);
                    Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Walking speed of " + target.getName() + " has been set to " + multiplier + '.');
                } catch (IllegalArgumentException var12) {
                    if (walkSpeed < 0.2f) {
                        sender.sendMessage(ChatColor.RED + "Speed multiplier too low: " + multiplier);
                    } else if (walkSpeed > 0.2f) {
                        sender.sendMessage(ChatColor.RED + "Speed multiplier too high: " + multiplier);
                    }
                }
            }
            return true;
        }
        sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[2] + BaseConstants.GOLD + "' not found.");
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        switch (args.length) {
            case 1: {
                return BukkitUtils.getCompletions(args, (List<String>) SpeedCommand.COMPLETIONS_FIRST);
            }
            case 2: {
                return BukkitUtils.getCompletions(args, (List<String>) SpeedCommand.COMPLETIONS_SECOND);
            }
            case 3: {
                return null;
            }
            default: {
                return Collections.emptyList();
            }
        }
    }
}
