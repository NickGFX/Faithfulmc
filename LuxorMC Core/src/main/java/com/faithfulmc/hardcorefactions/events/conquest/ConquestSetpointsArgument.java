package com.faithfulmc.hardcorefactions.events.conquest;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.tracker.ConquestTracker;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ConquestSetpointsArgument extends CommandArgument {
    private final HCF plugin;

    public ConquestSetpointsArgument(final HCF plugin) {
        super("setpoints", "Sets the points of a faction in the Conquest event", "hcf.command.conquest.argument.setpoints");
        this.plugin = plugin;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <factionName> <amount>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Faction faction = this.plugin.getFactionManager().getFaction(args[1]);
        if (!(faction instanceof PlayerFaction)) {
            sender.sendMessage(ConfigurationService.RED + "Faction " + args[1] + " is either not found or is not a player faction.");
            return true;
        }
        final Integer amount = Ints.tryParse(args[2]);
        if (amount == null) {
            sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a number.");
            return true;
        }
        if (amount > ConfigurationService.CONQUEST_REQUIRED_WIN_POINTS) {
            sender.sendMessage(ConfigurationService.RED + "Maximum points for Conquest is " + ConfigurationService.CONQUEST_REQUIRED_WIN_POINTS + '.');
            return true;
        }
        final PlayerFaction playerFaction = (PlayerFaction) faction;
        ((ConquestTracker) EventType.CONQUEST.getEventTracker()).setPoints(playerFaction, amount);
        Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set the points of faction " + playerFaction.getName() + " to " + amount + '.');
        return true;
    }
}