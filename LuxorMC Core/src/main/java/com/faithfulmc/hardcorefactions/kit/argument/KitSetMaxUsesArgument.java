package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSetMaxUsesArgument extends CommandArgument {
    private static final List COMPLETIONS_THIRD;

    static {
        COMPLETIONS_THIRD = (List) ImmutableList.of((Object) "UNLIMITED");
    }

    private final HCF plugin;

    public KitSetMaxUsesArgument(final HCF plugin) {
        super("setmaxuses", "Sets the maximum uses for a kit");
        this.plugin = plugin;
        this.aliases = new String[]{"setmaximumuses"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> <amount|unlimited>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Kit kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            sender.sendMessage(ConfigurationService.RED + "There is not a kit named " + args[1] + '.');
            return true;
        }
        Integer amount;
        if (args[2].equalsIgnoreCase("unlimited")) {
            amount = Integer.MAX_VALUE;
        } else {
            amount = Ints.tryParse(args[2]);
            if (amount == null) {
                sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a number.");
                return true;
            }
        }
        kit.setMaximumUses(amount);
        sender.sendMessage(ConfigurationService.GRAY + "Set maximum uses of kit " + kit.getDisplayName() + " to " + ((amount == Integer.MAX_VALUE) ? "unlimited" : amount) + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2) {
            return (args.length == 3) ? KitSetMaxUsesArgument.COMPLETIONS_THIRD : Collections.emptyList();
        }
        final List<Kit> kits = this.plugin.getKitManager().getKits();
        final ArrayList results = new ArrayList(kits.size());
        for (final Kit kit : kits) {
            results.add(kit.getName());
        }
        return results;
    }
}
