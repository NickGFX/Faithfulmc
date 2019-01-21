package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionSetHomeArgument extends CommandArgument {
    private final HCF plugin;

    public FactionSetHomeArgument(final HCF plugin) {
        super("sethome", "Sets the faction home location.");
        this.plugin = plugin;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        final Player player = (Player) sender;
        final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        final FactionMember factionMember = playerFaction.getMember(player);
        if (factionMember.getRole() == Role.MEMBER) {
            sender.sendMessage(ConfigurationService.RED + "You must be a faction officer to set the home.");
            return true;
        }
        if(ConfigurationService.ORIGINS && player.getLocation().getBlockY() > 100){
            player.sendMessage(ConfigurationService.RED + "You may not set your faction home above Y100");
            return true;
        }
        final Location location = player.getLocation();
        boolean insideTerritory = false;
        for (final Claim claim : playerFaction.getClaims()) {
            if (claim.contains(location)) {
                insideTerritory = true;
                break;
            }
        }
        if (!insideTerritory) {
            player.sendMessage(ConfigurationService.RED + "You may only set your home in your territory.");
            return true;
        }
        playerFaction.setHome(location);
        playerFaction.broadcast(ConfigurationService.TEAMMATE_COLOUR + factionMember.getRole().getAstrix() + sender.getName() + ConfigurationService.YELLOW + " has set the faction home.");
        return true;
    }
}