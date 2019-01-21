package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.primitives.Ints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSetIndexArgument extends CommandArgument {
    private final HCF plugin;

    public KitSetIndexArgument(final HCF plugin) {
        super("setindex", "Sets the position of a kit for the GUI");
        this.plugin = plugin;
        this.aliases = new String[]{"setorder", "setindex", "setpos", "setposition"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> <index[0 = minimum]>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Kit kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            sender.sendMessage(ConfigurationService.RED + "Kit '" + args[1] + "' not found.");
            return true;
        }
        Integer newIndex = Ints.tryParse(args[2]);
        if (newIndex == null) {
            sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a number.");
            return true;
        }
        if (newIndex < 1) {
            sender.sendMessage(ConfigurationService.RED + "The kit index cannot be less than " + 1 + '.');
            return true;
        }
        final List kits = this.plugin.getKitManager().getKits();
        final int totalKitAmount = kits.size() + 1;
        if (newIndex > totalKitAmount) {
            sender.sendMessage(ConfigurationService.RED + "The kit index must be a maximum of " + totalKitAmount + '.');
            return true;
        }
        final int previousIndex = kits.indexOf(kit) + 1;
        if (newIndex == previousIndex) {
            sender.sendMessage(ConfigurationService.RED + "Index of kit " + kit.getDisplayName() + " is already " + newIndex + '.');
            return true;
        }
        kits.remove(kit);
        kits.add(--newIndex, kit);
        sender.sendMessage(ConfigurationService.GRAY + "Set the index of kit " + kit.getDisplayName() + " from " + previousIndex + " to " + newIndex + '.');
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
