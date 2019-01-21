package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionRelationRemoveEvent;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionUnallyArgument extends CommandArgument {
    private final HCF plugin;

    public FactionUnallyArgument(HCF plugin) {
        super("unally", "Remove an ally pact with other factions.");
        this.plugin = plugin;
        this.aliases = new String[]{"unalliance", "neutral"};
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " <all|factionName>";
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
                sender.sendMessage(ConfigurationService.RED + "You must be a faction officer to edit relations.");
            }
            else {
                Relation relation = Relation.ALLY;
                Set<PlayerFaction> targetFactions = new HashSet<PlayerFaction>();
                if (args[1].equalsIgnoreCase("all")) {
                    List<PlayerFaction> allies = playerFaction.getAlliedFactions();
                    if (allies.isEmpty()) {
                        sender.sendMessage(ConfigurationService.RED + "Your faction has no allies.");
                        return true;
                    } else {
                        targetFactions.addAll(allies);
                    }
                } else {
                    Faction searchedFaction = plugin.getFactionManager().getContainingFaction(args[1]);
                    if (!(searchedFaction instanceof PlayerFaction)) {
                        sender.sendMessage(ConfigurationService.RED + "Player faction named or containing member with IGN or UUID " + args[1] + " not found.");
                        return true;
                    }
                    targetFactions.add((PlayerFaction) searchedFaction);
                }
                for (PlayerFaction targetFaction : targetFactions) {
                    if (playerFaction.getRelations().remove(targetFaction.getUniqueID()) == null || targetFaction.getRelations().remove(playerFaction.getUniqueID()) == null) {
                        sender.sendMessage(ConfigurationService.RED + "Your faction is not " + relation.getDisplayName() + ConfigurationService.RED + " with " + targetFaction.getDisplayName(playerFaction) + ConfigurationService.RED + '.');
                    }
                    else {
                        FactionRelationRemoveEvent event = new FactionRelationRemoveEvent(playerFaction, targetFaction, Relation.ALLY);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            sender.sendMessage(ConfigurationService.RED + "Could not drop " + relation.getDisplayName() + " with " + targetFaction.getDisplayName(playerFaction) + ConfigurationService.RED + ".");
                        } else {
                            playerFaction.broadcast(ConfigurationService.YELLOW + "Your faction has broken its " + relation.getDisplayName() + ConfigurationService.YELLOW + " with " + targetFaction.getDisplayName(playerFaction) + ConfigurationService.YELLOW + '.');
                            targetFaction.broadcast(ConfigurationService.YELLOW + playerFaction.getDisplayName(targetFaction) + ConfigurationService.YELLOW + " has dropped their " + relation.getDisplayName() + ConfigurationService.YELLOW + " with your faction.");
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
        List<String> completions = new ArrayList<>();
        String startArg = args[1];
        for (PlayerFaction otherFaction : playerFaction.getAlliedFactions()) {
            if (otherFaction.getName().toLowerCase().startsWith(startArg.toLowerCase())) {
                completions.add(otherFaction.getName());
            }
        }
        return completions;
    }
}