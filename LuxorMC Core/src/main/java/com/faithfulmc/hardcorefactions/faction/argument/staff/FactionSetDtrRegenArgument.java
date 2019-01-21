package com.faithfulmc.hardcorefactions.faction.argument.staff;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionManager;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;


public class FactionSetDtrRegenArgument extends CommandArgument {
    private final HCF plugin;


    public FactionSetDtrRegenArgument(HCF plugin) {

        super("setdtrregen", "Sets the DTR cooldown of a faction.", new String[]{"setdtrregeneration"});

        this.plugin = plugin;

        this.permission = ("hcf.command.faction.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <playerName|factionName> <newRegen>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        new BukkitRunnable() {
            public void run() {

                if (args.length < 3) {

                    sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

                    return;

                }

                long newRegen = com.faithfulmc.util.JavaUtils.parse(args[2]);

                if (newRegen == -1L) {

                    sender.sendMessage(ConfigurationService.RED + "Invalid duration, use the correct format: 10m 1s");

                    return;

                }

                if (newRegen > FactionManager.MAX_DTR_REGEN_MILLIS) {

                    sender.sendMessage(ConfigurationService.RED + "Cannot set factions DTR regen above " + FactionManager.MAX_DTR_REGEN_WORDS + ".");
                            return;

                }

                Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);

                if (faction == null) {

                    sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");

                    return;

                }

                if (!(faction instanceof PlayerFaction)) {

                    sender.sendMessage(ConfigurationService.RED + "This miner of faction does not use DTR.");

                    return;

                }

                PlayerFaction playerFaction = (PlayerFaction) faction;

                long previousRegenRemaining = playerFaction.getRemainingRegenerationTime();

                playerFaction.setRemainingRegenerationTime(newRegen);

                Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set DTR regen of " + faction.getName() + " from " + org.apache.commons.lang.time.DurationFormatUtils.formatDurationWords(previousRegenRemaining, true, true) + " to " + org.apache.commons.lang.time.DurationFormatUtils.formatDurationWords(newRegen, true, true) + '.');

            }
        }.runTaskAsynchronously(plugin);

        return true;

    }

}