package com.faithfulmc.hardcorefactions.faction.argument.staff;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionAddPointsArgument extends CommandArgument {
    private final HCF plugin;

    public FactionAddPointsArgument(final HCF plugin) {
        super("addpoints", "Adds to the Points of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + this.getName();
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <playerName|factionName> <newDtr>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (args.length < 3) {
                    sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
                    return;
                }
                Integer newPoints = Ints.tryParse(args[2]);
                if (newPoints == null) {
                    sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a valid number.");
                    return;
                }
                final Faction faction2 = plugin.getFactionManager().getContainingFaction(args[1]);
                if (faction2 == null) {
                    sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                    return;
                }
                if (!(faction2 instanceof PlayerFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "You can only set Points of player factions.");
                    return;
                }
                final PlayerFaction playerFaction = (PlayerFaction) faction2;
                int previousPoints = playerFaction.getPoints();
                newPoints = previousPoints + newPoints;
                playerFaction.setPoints(newPoints);
                Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set Points of " + faction2.getName() + " from " + previousPoints + " to " + newPoints + '.');
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        if (args[1].isEmpty()) {
            return null;
        }
        final Player player = (Player) sender;
        final List<String> results = new ArrayList<String>(this.plugin.getFactionManager().getFactionNameMap().keySet());
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (player.canSee(target) && !results.contains(target.getName())) {
                results.add(target.getName());
            }
        }
        return results;
    }
}