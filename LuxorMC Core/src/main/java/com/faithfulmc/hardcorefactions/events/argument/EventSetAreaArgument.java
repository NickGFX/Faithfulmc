package com.faithfulmc.hardcorefactions.events.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import com.faithfulmc.util.cuboid.Cuboid;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class EventSetAreaArgument extends CommandArgument {
    private static final int MIN_EVENT_CLAIM_AREA = 6;
    private final HCF plugin;


    public EventSetAreaArgument(HCF plugin) {

        super("setarea", "Sets the area of an event");

        this.plugin = plugin;

        this.aliases = new String[]{"setclaim", "setclaimarea", "setland"};

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <kothName>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "Only players can set event claim areas");

            return true;

        }

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        WorldEditPlugin worldEditPlugin = this.plugin.getWorldEdit();

        if (worldEditPlugin == null) {

            sender.sendMessage(ConfigurationService.RED + "WorldEdit must be installed to set event claim areas.");

            return true;

        }

        Player player = (Player) sender;

        Selection selection = worldEditPlugin.getSelection(player);

        if (selection == null) {

            sender.sendMessage(ConfigurationService.RED + "You must make a WorldEdit selection to do this.");

            return true;

        }

        if ((selection.getWidth() < MIN_EVENT_CLAIM_AREA) || (selection.getLength() < MIN_EVENT_CLAIM_AREA)) {

            sender.sendMessage(ConfigurationService.RED + "Event claim areas must be at least " + MIN_EVENT_CLAIM_AREA + 'x' + MIN_EVENT_CLAIM_AREA + '.');

            return true;

        }

        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof EventFaction)) {

            sender.sendMessage(ConfigurationService.RED + "There is not an event faction named '" + args[1] + "'.");

            return true;

        }
                ((EventFaction) faction).setClaim(new Cuboid(selection.getMinimumPoint(), selection.getMaximumPoint()), player);

        sender.sendMessage(ConfigurationService.YELLOW + "Updated the claim for event " + faction.getName() + ConfigurationService.YELLOW + '.');

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        return this.plugin.getFactionManager().getFactions().stream().filter(f -> f instanceof EventFaction && f.getName().startsWith(args[1])).map(Faction::getName).collect(Collectors.toList());


    }

}