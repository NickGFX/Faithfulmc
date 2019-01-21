package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionLeaderArgument extends CommandArgument {
    private final HCF plugin;

    public FactionLeaderArgument(HCF plugin) {
        super("leader", "Sets the new leader for your faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"setleader", "newleader"};
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can set faction leaders.");
        } else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        } else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            } else {
                UUID uuid = player.getUniqueId();
                FactionMember selfMember = playerFaction.getMember(uuid);
                Role selfRole = selfMember.getRole();
                if (selfRole != Role.LEADER) {
                    sender.sendMessage(ConfigurationService.RED + "You must be the current faction leader to transfer the faction.");
                } else {
                    FactionMember targetMember = playerFaction.getMember(plugin, args[1]);
                    if (targetMember == null) {
                        sender.sendMessage(ConfigurationService.RED + "Player '" + args[1] + "' is not in your faction.");
                    } else if (targetMember.getUniqueId().equals(uuid)) {
                        sender.sendMessage(ConfigurationService.RED + "You are already the faction leader.");
                    } else {
                        targetMember.setRole(Role.LEADER);
                        selfMember.setRole(Role.CAPTAIN);
                        playerFaction.broadcast(ConfigurationService.TEAMMATE_COLOUR + selfMember.getRole().getAstrix() + selfMember.getName() + ConfigurationService.YELLOW + " has transferred leadership of the faction to " + ConfigurationService.TEAMMATE_COLOUR + targetMember.getRole().getAstrix() + targetMember.getName() + ConfigurationService.YELLOW + '.');
                    }
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<String>();
        for (FactionMember factionMember : playerFaction.getMembers().values()) {
            if (factionMember.getRole() != Role.LEADER) {
                results.add(factionMember.getName());
            }
        }
        return results;
    }
}