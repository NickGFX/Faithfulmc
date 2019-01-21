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

public class FactionKickArgument extends CommandArgument {
    private final HCF plugin;

    public FactionKickArgument(HCF plugin) {
        super("kick", "Kick a player from the faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"kickmember", "kickplayer"};
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can kick from a faction.");
        }
        else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            } else if (playerFaction.isRaidable() && !plugin.getEotwHandler().isEndOfTheWorld()) {
                sender.sendMessage(ConfigurationService.RED + "You cannot kick players whilst your faction is raidable.");
            } else {
                FactionMember targetMember = playerFaction.getMember(plugin, args[1]);
                if (targetMember == null) {
                    sender.sendMessage(ConfigurationService.RED + "Your faction does not have a member named '" + args[1] + "'.");
                } else {
                    Role selfRole = playerFaction.getMember(player.getUniqueId()).getRole();
                    if (selfRole == Role.MEMBER) {
                        sender.sendMessage(ConfigurationService.RED + "You must be a faction officer to kick members.");
                    } else {
                        Role targetRole = targetMember.getRole();
                        if (targetRole == Role.LEADER) {
                            sender.sendMessage(ConfigurationService.RED + "You cannot kick the faction leader.");
                        } else if (targetRole == Role.COLEADER && selfRole != Role.LEADER) {
                            sender.sendMessage(ConfigurationService.RED + "You cannot kick a faction leader.");
                        } else if (targetRole == Role.CAPTAIN && selfRole == Role.CAPTAIN) {
                            sender.sendMessage(ConfigurationService.RED + "You must be a faction leader to kick captains.");
                        } else if (playerFaction.setMember(targetMember.getUniqueId(), null, true)) {
                            Player onlineTarget = targetMember.toOnlinePlayer();
                            if (onlineTarget != null) {
                                onlineTarget.sendMessage(ConfigurationService.RED.toString() + "You were kicked from " + playerFaction.getName() + '.');
                            }
                            playerFaction.broadcast(ConfigurationService.ENEMY_COLOUR + targetMember.getName() + ConfigurationService.YELLOW + " has been kicked by " + ConfigurationService.TEAMMATE_COLOUR + playerFaction.getMember(player).getRole().getAstrix() + sender.getName() + ConfigurationService.YELLOW + '.');
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
        if (playerFaction == null) {
            return Collections.emptyList();
        }
        Role memberRole = playerFaction.getMember(player.getUniqueId()).getRole();
        if (memberRole == Role.MEMBER) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<String>();
        for(FactionMember factionMember: playerFaction.getMembers().values()){
            Role targetRole = factionMember.getRole();
            if (targetRole == Role.LEADER || targetRole == Role.COLEADER || targetRole == Role.CAPTAIN && (memberRole != Role.LEADER && memberRole != Role.COLEADER)) {
                continue;
            }
            results.add(factionMember.getName());
        }
        return results;
    }
}