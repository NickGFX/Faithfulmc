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

public class KitListArgument extends CommandArgument {
    private final HCF plugin;

    public KitListArgument(final HCF plugin) {
        super("list", "Lists all current kits");
        this.plugin = plugin;
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<Kit> kits = this.plugin.getKitManager().getKits();
        if (kits.isEmpty()) {
            sender.sendMessage(ConfigurationService.RED + "No kits have been defined.");
            return true;
        }
        final ArrayList kitNames = new ArrayList();
        for (final Kit kit : kits) {
            final String permission = kit.getPermissionNode();
            if (permission == null || sender.hasPermission(permission)) {
                kitNames.add(ChatColor.GREEN + kit.getDisplayName());
            }
        }
        final String kitList2 = StringUtils.join((Iterable) kitNames, ConfigurationService.GRAY + ", ");
        sender.sendMessage(ConfigurationService.GRAY + "*** Kits (" + kitNames.size() + '/' + kits.size() + ") ***");
        sender.sendMessage(ConfigurationService.GRAY + "[" + ConfigurationService.WHITE + kitList2 + ConfigurationService.GRAY + ']');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
