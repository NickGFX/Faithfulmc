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


public class EventRenameArgument extends CommandArgument {
    private final HCF plugin;


    public EventRenameArgument(HCF plugin) {

        super("rename", "Renames an event");

        this.plugin = plugin;

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <oldName> <newName>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Faction originalFaction = this.plugin.getFactionManager().getFaction(args[2]);

        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (originalFaction != null && originalFaction != faction) {

            sender.sendMessage(ConfigurationService.RED + "There is already a faction named " + args[2] + '.');

            return true;

        }

        if (!(faction instanceof EventFaction)) {

            sender.sendMessage(ConfigurationService.RED + "There is not an event faction named '" + args[1] + "'.");

            return true;

        }

        String oldName = faction.getName();

        faction.setName(args[2], sender);

        sender.sendMessage(ConfigurationService.YELLOW + "Renamed event " + ConfigurationService.WHITE + oldName + ConfigurationService.YELLOW + " to " + ConfigurationService.WHITE + faction.getName() + ConfigurationService.YELLOW + '.');

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        return this.plugin.getFactionManager().getFactions().stream().filter(f -> f instanceof EventFaction && f.getName().startsWith(args[1])).map(Faction::getName).collect(Collectors.toList());

    }

}