package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class FactionInvitesArgument extends CommandArgument {
    private final HCF plugin;


    public FactionInvitesArgument(HCF plugin) {

        super("invites", "View faction invitations.");

        this.plugin = plugin;

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName();

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "Only players can have faction invites.");

            return true;

        }

        List<String> receivedInvites = new ArrayList();

        for (Faction faction : this.plugin.getFactionManager().getFactions()) {

            if ((faction instanceof PlayerFaction)) {

                PlayerFaction targetPlayerFaction = (PlayerFaction) faction;

                if (targetPlayerFaction.getInvitedPlayerNames().contains(sender.getName())) {

                    receivedInvites.add(targetPlayerFaction.getDisplayName(sender));
                }

            }

        }

        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(((Player) sender).getUniqueId());
                String delimiter = ConfigurationService.WHITE + ", " + ConfigurationService.GRAY;

        if (playerFaction != null) {

            Set<String> sentInvites = playerFaction.getInvitedPlayerNames();

            sender.sendMessage(ConfigurationService.YELLOW + "Sent by " + playerFaction.getDisplayName(sender) + ConfigurationService.YELLOW + " (" + sentInvites.size() + ')' + ConfigurationService.YELLOW + ": " + ConfigurationService.GRAY + (sentInvites.isEmpty() ? "Your faction has not invited anyone." : new StringBuilder().append(StringUtils.join((Iterator) sentInvites, delimiter)).append('.').toString()));

        }

        sender.sendMessage(ConfigurationService.YELLOW + "Requested (" + receivedInvites.size() + ')' + ConfigurationService.YELLOW + ": " + ConfigurationService.GRAY + (receivedInvites.isEmpty() ? "No factions have invited you." : new StringBuilder().append(StringUtils.join((Iterator) receivedInvites, new StringBuilder().append(ConfigurationService.WHITE).append(delimiter).toString())).append('.').toString()));

        return true;

    }

}