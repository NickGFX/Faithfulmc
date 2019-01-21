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

public class FactionCoLeaderArgument extends CommandArgument {
    public static final int MAX_COLEADERS = 2;

    private final HCF plugin;

    public FactionCoLeaderArgument(HCF plugin) {
        super("coleader", "Sets a new co-leader for your faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"setcoleader", "newcoleader"};
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can set faction leaders.");
        }
        else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            } else {
                UUID uuid = player.getUniqueId();
                FactionMember selfMember = playerFaction.getMember(uuid);
                Role selfRole = selfMember.getRole();
                if (selfRole != Role.LEADER) {
                    sender.sendMessage(ConfigurationService.RED + "You must be the current faction leader to set a co-leader.");
                } else {
                    FactionMember targetMember = playerFaction.getMember(plugin, args[1]);
                    if (targetMember == null) {
                        sender.sendMessage(ConfigurationService.RED + "Player '" + args[1] + "' is not in your faction.");
                    } else if (selfMember.equals(targetMember)) {
                        sender.sendMessage(ConfigurationService.RED + "You may not do that");
                    } else if(targetMember.getRole() == Role.COLEADER){
                        sender.sendMessage(ConfigurationService.RED + "That player is already a co-leader.");
                    } else {
                        Set<FactionMember> coLeaders = playerFaction.getCoLeaders();
                        if (coLeaders.size() >= MAX_COLEADERS) {
                            sender.sendMessage(ConfigurationService.RED + "Your faction already has " + coLeaders.size() + " co-leaders.");
                        } else {
                            targetMember.setRole(Role.COLEADER);
                            playerFaction.broadcast(Relation.MEMBER.toChatColour() + targetMember.getRole().getAstrix() + targetMember.getName() + ConfigurationService.YELLOW + " has been assigned as a faction co-leader.");
                        }
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
        ArrayList<String> results = new ArrayList<String>();
        Map<UUID, FactionMember> members = playerFaction.getMembers();
        for (Map.Entry<UUID, FactionMember> entry : members.entrySet()) {
            FactionMember factionMember = entry.getValue();
            if(factionMember.getRole() != Role.LEADER) {
                results.add(entry.getValue().getName());
            }
        }
        return results;
    }
}
