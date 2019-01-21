package com.faithfulmc.hardcorefactions.faction.argument.staff;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class FactionForceLeaderArgument extends CommandArgument {
    private final HCF plugin;

    public FactionForceLeaderArgument(final HCF plugin) {
        super("forceleader", "Forces the leader of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + this.getName();
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <playerName>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (args.length < 2) {
                    sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
                    return;
                }
                final PlayerFaction playerFaction = plugin.getFactionManager().getContainingPlayerFaction(args[1]);
                if (playerFaction == null) {
                    sender.sendMessage(ConfigurationService.RED + "Faction containing member with IGN or UUID " + args[1] + " not found.");
                    return;
                }
                final FactionMember factionMember = playerFaction.getMember(plugin, args[1]);
                if (factionMember == null) {
                    sender.sendMessage(ConfigurationService.RED + "Faction containing member with IGN or UUID " + args[1] + " not found.");
                    return;
                }
                if (factionMember.getRole() == Role.LEADER) {
                    sender.sendMessage(ConfigurationService.RED + factionMember.getName() + " is already the leader of " + playerFaction.getDisplayName(sender) + ConfigurationService.RED + '.');
                    return;
                }
                final FactionMember leader = playerFaction.getLeader();
                final String oldLeaderName = (leader == null) ? "none" : leader.getName();
                final String newLeaderName = factionMember.getName();
                if (leader != null) {
                    leader.setRole(Role.CAPTAIN);
                }
                factionMember.setRole(Role.LEADER);
                playerFaction.broadcast(ConfigurationService.YELLOW + sender.getName() + " has forcefully set the leader to " + newLeaderName + '.');
                sender.sendMessage(ConfigurationService.GOLD.toString() + ChatColor.BOLD + "Leader of " + playerFaction.getName() + "was forcefully set from " + oldLeaderName + " to " + newLeaderName + '.');
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 2) ? null : Collections.emptyList();
    }
}