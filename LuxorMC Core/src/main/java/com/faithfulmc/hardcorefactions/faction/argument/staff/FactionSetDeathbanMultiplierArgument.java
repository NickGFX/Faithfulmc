package com.faithfulmc.hardcorefactions.faction.argument.staff;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.primitives.Doubles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;


public class FactionSetDeathbanMultiplierArgument extends CommandArgument {
    private static final double MIN_MULTIPLIER = 0.0D;
    private static final double MAX_MULTIPLIER = 5.0D;
    private final HCF plugin;


    public FactionSetDeathbanMultiplierArgument(HCF plugin) {

        super("setdeathbanmultiplier", "Sets the deathban multiplier of a faction.");

        this.plugin = plugin;

        this.permission = ("hcf.command.faction.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <playerName|factionName> <newMultiplier>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        new BukkitRunnable() {
            public void run() {
                if (args.length < 3) {

                    sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

                    return;

                }

                Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);

                if (faction == null) {

                    sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");

                    return;

                }

                Double multiplier = Doubles.tryParse(args[2]);

                if (multiplier == null) {

                    sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a valid number.");

                    return;

                }

                if (multiplier.doubleValue() < 0.0D) {

                    sender.sendMessage(ConfigurationService.RED + "Deathban multipliers may not be less than " + 0.0D + '.');

                    return;

                }

                if (multiplier.doubleValue() > 5.0D) {

                    sender.sendMessage(ConfigurationService.RED + "Deathban multipliers may not be more than " + 5.0D + '.');

                    return;

                }

                double previousMultiplier = faction.getDeathbanMultiplier();

                faction.setDeathbanMultiplier(multiplier.doubleValue());

                Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set deathban multiplier of " + faction.getName() + " from " + previousMultiplier + " to " + multiplier + '.');
            }
        }.runTaskAsynchronously(plugin);


        return true;

    }

}