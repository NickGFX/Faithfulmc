package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FactionUninviteArgument extends CommandArgument {
    private static final ImmutableList<String> COMPLETIONS;

    static {
        COMPLETIONS = ImmutableList.of("all");
    }

    private final HCF plugin;

    public FactionUninviteArgument(final HCF plugin) {
        super("uninvite", "Revoke an invitation to a player.", new String[]{"deinvite", "deinv", "uninv", "revoke"});
        this.plugin = plugin;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <all|playerName>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can un-invite from a faction.");
        }
        else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
        }
        else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            }
            else {
                FactionMember factionMember = playerFaction.getMember(player);
                if (factionMember.getRole() == Role.MEMBER) {
                    sender.sendMessage(ConfigurationService.RED + "You must be a faction officer to un-invite players.");
                }
                else {
                    Set<String> invitedPlayerNames = playerFaction.getInvitedPlayerNames();
                    if (args[1].equalsIgnoreCase("all")) {
                        invitedPlayerNames.clear();
                        sender.sendMessage(ConfigurationService.YELLOW + "You have cleared all pending invitations.");
                    }
                    else if (!invitedPlayerNames.remove(args[1])) {
                        sender.sendMessage(ConfigurationService.RED + "There is not a pending invitation for " + args[1] + '.');
                    }
                    else {
                        playerFaction.broadcast(ConfigurationService.YELLOW + factionMember.getRole().getAstrix() + sender.getName() + " has uninvited " + ConfigurationService.ENEMY_COLOUR + args[1] + ConfigurationService.YELLOW + " from the faction.");
                    }
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>( FactionUninviteArgument.COMPLETIONS);
        results.addAll(playerFaction.getInvitedPlayerNames());
        return results;
    }
}