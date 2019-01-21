package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSetminplaytimeArgument extends CommandArgument {
    private final HCF plugin;

    public KitSetminplaytimeArgument(final HCF plugin) {
        super("setminplaytime", "Sets the minimum playtime to use a kit");
        this.aliases = new String[]{"setminimumplaytime"};
        this.plugin = plugin;
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> <time>";
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
        final long duration = JavaUtils.parse(args[2]);
        if (duration == -1L) {
            sender.sendMessage(ConfigurationService.RED + "Invalid duration, use the correct format: 10m 1s");
            return true;
        }
        kit.setMinPlaytimeMillis(duration);
        sender.sendMessage(ConfigurationService.YELLOW + "Set minimum playtime to use kit " + kit.getDisplayName() + " at " + kit.getMinPlaytimeWords() + '.');
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
