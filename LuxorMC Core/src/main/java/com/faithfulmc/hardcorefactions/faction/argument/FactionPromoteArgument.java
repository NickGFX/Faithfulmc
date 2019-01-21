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

import java.util.*;

public class FactionPromoteArgument extends CommandArgument {
    private final HCF plugin;

    public FactionPromoteArgument(final HCF plugin) {
        super("promote", "Promotes a player to a captain.");
        this.plugin = plugin;
        this.aliases = new String[]{"captain", "officer", "mod", "moderator"};
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + this.getName() + " <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can set faction captains.");
        } else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        } else {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(uuid);
            FactionMember factionMember;
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            } else if ((factionMember = playerFaction.getMember(uuid)).getRole() != Role.LEADER && factionMember.getRole() != Role.COLEADER) {
                sender.sendMessage(ConfigurationService.RED + "You must be a faction leader to assign members as a captain.");
            } else {
                FactionMember targetMember = playerFaction.getMember(plugin, args[1]);
                if (targetMember == null) {
                    sender.sendMessage(ConfigurationService.RED + "That player is not in your faction.");
                } else if (targetMember.getRole() == Role.COLEADER){
                    if(factionMember.getRole() == Role.COLEADER){
                        sender.sendMessage(ConfigurationService.RED + "You must be the faction leader to assign co-leaders.");
                    }
                    else{
                        Set<FactionMember> coLeaders = playerFaction.getCoLeaders();
                        if(coLeaders.size() >= FactionCoLeaderArgument.MAX_COLEADERS){
                            sender.sendMessage(ConfigurationService.RED + "Your faction already has " + coLeaders.size() + " co-leaders.");
                        }
                        else{
                            Role role = Role.COLEADER;
                            targetMember.setRole(role);
                            playerFaction.broadcast(Relation.MEMBER.toChatColour() + role.getAstrix() + targetMember.getName() + ConfigurationService.YELLOW + " has been assigned as a faction co-leader.");
                        }
                    }
                } else if (targetMember.getRole() != Role.MEMBER) {
                    sender.sendMessage(ConfigurationService.RED + "You can only assign captains to members, " + targetMember.getName() + " is a " + targetMember.getRole().getName() + '.');
                } else {
                    Role role = Role.CAPTAIN;
                    targetMember.setRole(role);
                    playerFaction.broadcast(Relation.MEMBER.toChatColour() + role.getAstrix() + targetMember.getName() + ConfigurationService.YELLOW + " has been assigned as a faction captain.");
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
        if(playerFaction == null){
            return Collections.emptyList();
        }
        FactionMember factionMember = playerFaction.getMember(player);
        if (factionMember.getRole() != Role.LEADER || factionMember.getRole() != Role.COLEADER) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();
        for (FactionMember member: playerFaction.getMembers().values()) {
            if (member.getRole() == Role.MEMBER) {
                results.add(member.getName());
            }
        }
        return results;
    }
}