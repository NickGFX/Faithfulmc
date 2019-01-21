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

public class FactionForcePromoteArgument extends CommandArgument {
    private final HCF plugin;

    public FactionForcePromoteArgument(final HCF plugin) {
        super("forcepromote", "Forces the promotion status of a player.");
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
                if (factionMember.getRole() != Role.MEMBER) {
                    sender.sendMessage(ConfigurationService.RED + factionMember.getName() + " is already a " + factionMember.getRole().getName() + '.');
                    return;
                }
                factionMember.setRole(Role.CAPTAIN);
                playerFaction.broadcast(ConfigurationService.GOLD.toString() + ChatColor.BOLD + sender.getName() + " has been forcefully assigned as a captain.");
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 2) ? null : Collections.emptyList();
    }
}