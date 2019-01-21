package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.kit.event.KitRenameEvent;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitRenameArgument extends CommandArgument {
    private final HCF plugin;

    public KitRenameArgument(final HCF plugin) {
        super("rename", "Renames a kit");
        this.plugin = plugin;
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> <newKitName>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        Kit kit = this.plugin.getKitManager().getKit(args[2]);
        if (kit != null) {
            sender.sendMessage(ConfigurationService.RED + "There is already a kit named " + kit.getName() + '.');
            return true;
        }
        kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            sender.sendMessage(ConfigurationService.RED + "There is not a kit named " + args[1] + '.');
            return true;
        }
        final KitRenameEvent event = new KitRenameEvent(kit, kit.getName(), args[2]);
        Bukkit.getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) {
            return true;
        }
        if (event.getOldName().equals(event.getNewName())) {
            sender.sendMessage(ConfigurationService.RED + "This kit is already called " + event.getNewName() + '.');
            return true;
        }
        kit.setName(event.getNewName());
        sender.sendMessage(ConfigurationService.GRAY + "Renamed kit " + event.getOldName() + " to " + event.getNewName() + '.');
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
