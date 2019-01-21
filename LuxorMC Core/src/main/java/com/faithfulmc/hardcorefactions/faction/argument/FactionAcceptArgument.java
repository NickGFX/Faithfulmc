package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionAcceptArgument extends CommandArgument {
    private final HCF plugin;

    public FactionAcceptArgument(HCF plugin) {
        super("accept", "Accept a join request from an existing faction.", new String[]{"join", "a"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " <factionName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
        } else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        } else {
            Player player = (Player) sender;
            if (plugin.getFactionManager().getPlayerFaction(player) != null) {
                sender.sendMessage(ConfigurationService.RED + "You are already in a faction.");
            }
            else {
                Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);
                if (faction == null) {
                    sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                }
                else if (!(faction instanceof PlayerFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "You can only join player factions.");
                }
                else {
                    PlayerFaction targetFaction = (PlayerFaction) faction;
                    if (targetFaction.getMembers().size() >= ConfigurationService.FACTION_PLAYER_LIMIT) {
                        sender.sendMessage(faction.getDisplayName(sender) + ConfigurationService.RED + " is full. Faction limits are at " + ConfigurationService.FACTION_PLAYER_LIMIT + '.');
                    }
                    else if (!targetFaction.isOpen() && !targetFaction.getInvitedPlayerNames().contains(player.getName())) {
                        sender.sendMessage(ConfigurationService.RED + faction.getDisplayName(sender) + ConfigurationService.RED + " has not invited you.");
                    }
                    else if (targetFaction.setMember(player, new FactionMember(targetFaction, player, ChatChannel.PUBLIC, Role.MEMBER))) {
                        targetFaction.broadcast(Relation.MEMBER.toChatColour() + sender.getName() + ConfigurationService.YELLOW + " has joined the faction.");
                    }
                }
            }
        }
        return true;
    }
}