package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;

public class EntitiesCommand extends BaseCommand {
    public EntitiesCommand() {
        super("entities", "Checks the entity count in environments.");
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<World> worlds = Bukkit.getWorlds();
        for (final World world : worlds) {
            sender.sendMessage(BaseConstants.GRAY + world.getEnvironment().name());
            final EntityType[] var7;
            final EntityType[] values = var7 = EntityType.values();
            for (int var8 = values.length, var9 = 0; var9 < var8; ++var9) {
                final EntityType entityType = var7[var9];
                if (entityType != EntityType.UNKNOWN) {
                    final Class entityClass = entityType.getEntityClass();
                    if (entityClass != null) {
                        final int amount = world.getEntitiesByClass(entityClass).size();
                        if (amount >= 20) {
                            sender.sendMessage(BaseConstants.YELLOW + " " + entityType.getName() + " with " + amount);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
