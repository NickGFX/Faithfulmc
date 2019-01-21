package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FactionClaimsArgument extends CommandArgument {
    private final HCF plugin;

    public FactionClaimsArgument(HCF plugin) {
        super("claims", "View all claims for a faction.");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " [factionName]";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Faction targetFaction;
        PlayerFaction selfFaction = sender instanceof Player ? plugin.getFactionManager().getPlayerFaction((Player) sender) : null;
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
                return true;
            }
            else if (selfFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
                return true;
            }
            else {
                targetFaction = selfFaction;
            }
        } else {
            Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);
            if (faction == null) {
                sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                return true;
            } else if (!(faction instanceof ClaimableFaction)) {
                sender.sendMessage(ConfigurationService.RED + "You can only check the claims of factions that can have claims.");
                return true;
            } else {
                targetFaction = faction;
            }
        }
        Collection<Claim> claims = ((ClaimableFaction) targetFaction).getClaims();
        if (claims.isEmpty()) {
            sender.sendMessage(ConfigurationService.RED + "Faction " + targetFaction.getDisplayName(sender) + ConfigurationService.RED + " has no claimed land.");
        } else if (sender instanceof Player && !sender.isOp() && targetFaction instanceof PlayerFaction && ((PlayerFaction) targetFaction).getHome() == null && (selfFaction == null || !selfFaction.equals(targetFaction))) {
            sender.sendMessage(ConfigurationService.RED + "You cannot view the claims of " + targetFaction.getDisplayName(sender) + ConfigurationService.RED + " because their home is unset.");
        } else {
            sender.sendMessage(ConfigurationService.YELLOW + "Claims of " + targetFaction.getDisplayName(sender));
            for (Claim claim : claims) {
                sender.sendMessage(ConfigurationService.GRAY + " " + claim.getFormattedName());
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        if (args[1].isEmpty()) {
            return null;
        }
        Player player = (Player) sender;
        ArrayList<String> results = new ArrayList<String>(this.plugin.getFactionManager().getFactionNameMap().keySet());
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!player.canSee(target) || results.contains(target.getName())) {
                continue;
            }
            results.add(target.getName());
        }
        return results;
    }
}