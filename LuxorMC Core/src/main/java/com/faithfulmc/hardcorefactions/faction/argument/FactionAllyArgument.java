package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionRelationCreateEvent;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class FactionAllyArgument extends CommandArgument {
    private static final Relation RELATION = Relation.ALLY;
    private final HCF plugin;

    public FactionAllyArgument(HCF plugin) {
        super("ally", "Make an ally pact with other factions.", new String[]{"alliance"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName>";
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
            else if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
                sender.sendMessage(ConfigurationService.RED + "You must be an officer to make relation wishes.");
            }
            else {
                Faction containingFaction = plugin.getFactionManager().getContainingFaction(args[1]);
                if (!(containingFaction instanceof PlayerFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "Player faction named or containing member with IGN or UUID " + args[1] + " not found.");
                }
                else {

                    PlayerFaction targetFaction = (PlayerFaction) containingFaction;
                    if (playerFaction.equals(targetFaction)) {
                        sender.sendMessage(ConfigurationService.RED + "You cannot send " + RELATION.getDisplayName() + ConfigurationService.RED + " requests to your own faction.");
                    }
                    else {

                        Collection<UUID> allied = playerFaction.getAllied();
                        if (allied.size() >= ConfigurationService.MAX_ALLIES_PER_FACTION) {
                            sender.sendMessage(ConfigurationService.RED + "Your faction already has reached the alliance limit, which is " + ConfigurationService.MAX_ALLIES_PER_FACTION + '.');
                        }

                        else if (targetFaction.getAllied().size() >= ConfigurationService.MAX_ALLIES_PER_FACTION) {
                            sender.sendMessage(targetFaction.getDisplayName(sender) + ConfigurationService.RED + " has reached their maximum alliance limit, which is " + ConfigurationService.MAX_ALLIES_PER_FACTION + '.');
                        }

                        else if (allied.contains(targetFaction.getUniqueID())) {
                            sender.sendMessage(ConfigurationService.RED + "Your faction already is " + RELATION.getDisplayName() + 'd' + ConfigurationService.RED + " with " + targetFaction.getDisplayName(playerFaction) + ConfigurationService.RED + '.');
                        }
                        else if (targetFaction.getRequestedRelations().remove(playerFaction.getUniqueID()) != null) {
                            FactionRelationCreateEvent event = new FactionRelationCreateEvent(playerFaction, targetFaction, RELATION);
                            Bukkit.getPluginManager().callEvent(event);
                            targetFaction.setRelation(playerFaction, RELATION);
                            targetFaction.broadcast(ConfigurationService.YELLOW + "Your faction is now " + RELATION.getDisplayName() + 'd' + ConfigurationService.YELLOW + " with " + playerFaction.getDisplayName(targetFaction) + ConfigurationService.YELLOW + '.');
                            playerFaction.setRelation(targetFaction, RELATION);
                            playerFaction.broadcast(ConfigurationService.YELLOW + "Your faction is now " + RELATION.getDisplayName() + 'd' + ConfigurationService.YELLOW + " with " + targetFaction.getDisplayName(playerFaction) + ConfigurationService.YELLOW + '.');
                        }
                        else if (playerFaction.getRequestedRelations().putIfAbsent(targetFaction.getUniqueID(), RELATION) != null) {
                            sender.sendMessage(ConfigurationService.YELLOW + "Your faction has already requested to " + RELATION.getDisplayName() + ConfigurationService.YELLOW + " with " + targetFaction.getDisplayName(playerFaction) + ConfigurationService.YELLOW + '.');
                        }
                        else {
                            playerFaction.broadcast(targetFaction.getDisplayName(playerFaction) + ConfigurationService.YELLOW + " were informed that you wish to be " + RELATION.getDisplayName() + ConfigurationService.YELLOW + '.');
                            targetFaction.broadcast(playerFaction.getDisplayName(targetFaction) + ConfigurationService.YELLOW + " has sent a request to be " + RELATION.getDisplayName() + ConfigurationService.YELLOW + ". Use " + ConfigurationService.ALLY_COLOUR + "/faction " + getName() + ' ' + playerFaction.getName() + ConfigurationService.YELLOW + " to accept.");
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}