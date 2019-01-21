package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.kit.event.KitRemoveEvent;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitDeleteArgument extends CommandArgument {
    private final HCF plugin;

    public KitDeleteArgument(final HCF plugin) {
        super("delete", "Deletes a kit");
        this.plugin = plugin;
        this.aliases = new String[]{"del", "remove"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Kit kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            sender.sendMessage(ConfigurationService.RED + "There is not a kit named " + args[1] + '.');
            return true;
        }
        final KitRemoveEvent event = new KitRemoveEvent(kit);
        Bukkit.getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) {
            return true;
        }
        this.plugin.getKitManager().removeKit(kit);
        sender.sendMessage(ConfigurationService.GRAY + "Removed kit '" + args[1] + "'.");
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }
        final List<Kit> kits = this.plugin.getKitManager().getKits();
        final ArrayList results = new ArrayList(kits.size());
        for (final Kit kit : kits) {
            results.add(kit.getName());
        }
        return results;
    }
}
