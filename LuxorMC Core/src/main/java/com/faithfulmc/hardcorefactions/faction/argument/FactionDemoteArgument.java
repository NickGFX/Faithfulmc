package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionDemoteArgument extends CommandArgument {
    private final HCF plugin;

    public FactionDemoteArgument(HCF plugin) {
        super("demote", "Demotes a captain.", new String[]{"uncaptain", "delcaptain", "delofficer"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
        }
        else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            }
            else {
                FactionMember playerMember = playerFaction.getMember(player.getUniqueId());
                if (playerMember.getRole() != Role.LEADER && playerMember.getRole() != Role.COLEADER) {
                    sender.sendMessage(ConfigurationService.RED + "You must be a officer to edit the roster.");
                }
                else {
                    FactionMember targetMember = playerFaction.getMember(plugin, args[1]);
                    if (targetMember == null) {
                        sender.sendMessage(ConfigurationService.RED + "That player is not in your faction.");
                    } else if (playerMember.getRole() == Role.LEADER && targetMember.getRole() == Role.COLEADER) {
                        targetMember.setRole(Role.CAPTAIN);
                        playerFaction.broadcast(Relation.MEMBER.toChatColour() + targetMember.getName() + ConfigurationService.YELLOW + " has been demoted from faction co-leader.");
                    } else if (targetMember.getRole() != Role.CAPTAIN) {
                        sender.sendMessage(ConfigurationService.RED + "You can only demote faction captains.");
                    } else {
                        targetMember.setRole(Role.MEMBER);
                        playerFaction.broadcast(Relation.MEMBER.toChatColour() + targetMember.getName() + ConfigurationService.YELLOW + " has been demoted from a faction captain.");
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
        if (playerFaction == null || (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER || playerFaction.getMember(player.getUniqueId()).getRole() != Role.COLEADER)) {
            return Collections.emptyList();
        }
       List<String> results = new ArrayList<String>();
        for (FactionMember factionMember: playerFaction.getMembers().values()) {
            if(factionMember.getRole() == Role.CAPTAIN) {
                results.add(factionMember.getName());
            }
        }
        return results;
    }
}