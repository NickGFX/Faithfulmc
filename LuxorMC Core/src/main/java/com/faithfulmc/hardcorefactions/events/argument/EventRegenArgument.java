package com.faithfulmc.hardcorefactions.events.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.CitadelFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;


public class EventRegenArgument extends CommandArgument {
    private final HCF plugin;


    public EventRegenArgument(HCF plugin) {

        super("regen", "Regens a citadel event");

        this.plugin = plugin;

        this.aliases = new String[]{"regenerate"};

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <citadelName>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof CitadelFaction)) {

            sender.sendMessage(ConfigurationService.RED + "There is not a citadel faction named '" + args[1] + "'.");

            return true;

        }
        ((CitadelFaction)faction).fillChests(true);
        ((CitadelFaction)faction).setLastChestReset(System.currentTimeMillis());
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        return Collections.emptyList();

    }

}