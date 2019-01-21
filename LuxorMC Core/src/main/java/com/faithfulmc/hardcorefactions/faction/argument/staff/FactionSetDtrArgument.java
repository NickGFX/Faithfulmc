package com.faithfulmc.hardcorefactions.faction.argument.staff;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.primitives.Doubles;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionSetDtrArgument extends CommandArgument {
    private final HCF plugin;

    public FactionSetDtrArgument(final HCF plugin) {
        super("setdtr", "Sets the DTR of a faction.");
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
                Double newDTR = Doubles.tryParse(args[2]);
                if (newDTR == null) {
                    sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a valid number.");
                    return;
                }
                if (args[1].equalsIgnoreCase("all")) {
                    for (final Faction faction : plugin.getFactionManager().getFactions()) {
                        if (faction instanceof PlayerFaction) {
                            ((PlayerFaction) faction).setDeathsUntilRaidable(newDTR);
                        }
                    }
                    Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set DTR of all factions to " + newDTR + '.');
                    return;
                }
                final Faction faction2 = plugin.getFactionManager().getContainingFaction(args[1]);
                if (faction2 == null) {
                    sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                    return;
                }
                if (!(faction2 instanceof PlayerFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "You can only set DTR of player factions.");
                    return;
                }
                final PlayerFaction playerFaction = (PlayerFaction) faction2;
                final double previousDtr = playerFaction.getDeathsUntilRaidable();
                newDTR = playerFaction.setDeathsUntilRaidable(newDTR);
                Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set DTR of " + faction2.getName() + " from " + previousDtr + " to " + newDTR + '.');
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