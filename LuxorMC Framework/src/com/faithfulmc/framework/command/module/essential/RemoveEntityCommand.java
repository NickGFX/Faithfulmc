package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.primitives.Ints;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoveEntityCommand extends BaseCommand {
    public RemoveEntityCommand() {
        super("removeentity", "Removes all of a specific entity.");
        this.setUsage("/(command) <worldName> <entityType> [removeCustomNamed] [radius]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final World world = Bukkit.getWorld(args[0]);
        final Optional optionalType = Enums.getIfPresent((Class) EntityType.class, args[1].toUpperCase());
        if (!optionalType.isPresent()) {
            sender.sendMessage(ChatColor.RED + "Not an entity named '" + args[1] + "'.");
            return true;
        }
        final EntityType entityType = (EntityType) optionalType.get();
        if (entityType == EntityType.PLAYER) {
            sender.sendMessage(ChatColor.RED + "You cannot remove " + entityType.name() + " entities!");
            return true;
        }
        final boolean removeCustomNamed = args.length > 2 && Boolean.parseBoolean(args[2]);
        Integer radius;
        if (args.length > 3) {
            radius = Ints.tryParse(args[3]);
            if (radius == null) {
                sender.sendMessage(ChatColor.RED + "'" + args[3] + "' is not a number.");
                return true;
            }
            if (radius <= 0) {
                sender.sendMessage(ChatColor.RED + "Radius must be positive.");
                return true;
            }
        } else {
            radius = 0;
        }
        final Location location = (sender instanceof Player) ? ((Player) sender).getLocation() : null;
        int removed = 0;
        for (final Chunk chunk : world.getLoadedChunks()) {
            for (final Entity entity : chunk.getEntities()) {
                Label_0506:
                {
                    if (entity.getType() == entityType && (radius == 0 || (location != null && location.distanceSquared(entity.getLocation()) <= radius))) {
                        if (!removeCustomNamed) {
                            if (entity instanceof Tameable && ((Tameable) entity).isTamed()) {
                                break Label_0506;
                            }
                            if (entity instanceof LivingEntity) {
                                final LivingEntity livingEntity = (LivingEntity) entity;
                                if (livingEntity.getCustomName() != null) {
                                    break Label_0506;
                                }
                            }
                        }
                        entity.remove();
                        ++removed;
                    }
                }
            }
        }
        sender.sendMessage(BaseConstants.YELLOW + "Removed " + removed + " of " + entityType.getName() + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        ArrayList results = null;
        switch (args.length) {
            case 1: {
                final List<World> var11 = Bukkit.getWorlds();
                results = new ArrayList(var11.size());
                for (final World var13 : var11) {
                    results.add(var13.getName());
                }
                return BukkitUtils.getCompletions(args, results);
            }
            case 2: {
                final EntityType[] entityTypes = EntityType.values();
                results = new ArrayList(entityTypes.length);
                final EntityType[] var14 = entityTypes;
                for (int world = entityTypes.length, var15 = 0; var15 < world; ++var15) {
                    final EntityType entityType = var14[var15];
                    if (entityType != EntityType.UNKNOWN && entityType != EntityType.PLAYER) {
                        results.add(entityType.name());
                    }
                }
                return BukkitUtils.getCompletions(args, results);
            }
            default: {
                return Collections.emptyList();
            }
        }
    }
}
