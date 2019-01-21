package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSetDescriptionArgument extends CommandArgument {
    private final HCF plugin;

    public KitSetDescriptionArgument(final HCF plugin) {
        super("setdescription", "Sets the description of a kit");
        this.plugin = plugin;
        this.aliases = new String[]{"setdesc"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> <none|description>";
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
        if (!args[2].equalsIgnoreCase("none") && !args[2].equalsIgnoreCase("null")) {
            final String description = ChatColor.translateAlternateColorCodes('&', StringUtils.join((Object[]) args, ' ', 2, args.length));
            kit.setDescription(description);
            sender.sendMessage(ConfigurationService.YELLOW + "Set description of kit " + kit.getDisplayName() + " to " + description + '.');
            return true;
        }
        kit.setDescription(null);
        sender.sendMessage(ConfigurationService.YELLOW + "Removed description of kit " + kit.getDisplayName() + '.');
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
