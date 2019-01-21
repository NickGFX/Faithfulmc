package com.faithfulmc.hardcorefactions.events.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EventDeleteArgument extends CommandArgument {
    private final HCF plugin;


    public EventDeleteArgument(HCF plugin) {

        super("delete", "Deletes an event");

        this.plugin = plugin;

        this.aliases = new String[]{"remove", "del"};

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <eventName>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof EventFaction)) {

            sender.sendMessage(ConfigurationService.RED + "There is not an event faction named '" + args[1] + "'.");

            return true;

        }

        if (this.plugin.getFactionManager().removeFaction(faction, sender)) {

            sender.sendMessage(ConfigurationService.YELLOW + "Deleted event faction " + ConfigurationService.WHITE + faction.getDisplayName(sender) + ConfigurationService.YELLOW + '.');

        }

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        return this.plugin.getFactionManager().getFactions().stream().filter(f -> f instanceof EventFaction && f.getName().startsWith(args[1])).map(Faction::getName).collect(Collectors.toList());

    }

}