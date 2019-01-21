package com.faithfulmc.hardcorefactions.events.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.faction.CitadelFaction;
import com.faithfulmc.hardcorefactions.events.faction.ConquestFaction;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EventCreateArgument extends CommandArgument {
    private final HCF plugin;


    public EventCreateArgument(HCF plugin) {

        super("create", "Creates a new event", new String[]{"make", "define"});

        this.plugin = plugin;

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <eventName> <Conquest|KOTH|Citadel>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (faction != null) {

            sender.sendMessage(ConfigurationService.RED + "There is already a faction named " + args[1] + '.');

            return true;

        }

        String upperCase = args[2].toUpperCase();

        switch (upperCase) {
            case "CONQUEST":
                faction = new ConquestFaction(args[1]);
                break;
            case "KOTH":
                faction = new KothFaction(args[1]);
                break;
            case "CITADEL":
                faction = new CitadelFaction(args[1]);
                break;
            default:
                sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
                return true;
        }

        this.plugin.getFactionManager().createFaction(faction, sender);

        sender.sendMessage(ConfigurationService.YELLOW + "Created event faction " + ConfigurationService.WHITE + faction.getDisplayName(sender) + ConfigurationService.YELLOW + " with type " + WordUtils.capitalizeFully(args[2]) + '.');

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 3) {

            return Collections.emptyList();

        }

        EventType[] eventTypes = EventType.values();

        List<String> results = new ArrayList<>(eventTypes.length);

        for (EventType eventType : eventTypes) {

            results.add(eventType.name());

        }

        return results;

    }

}