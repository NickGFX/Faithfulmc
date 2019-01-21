package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitApplyArgument extends CommandArgument {
    private final HCF plugin;

    public KitApplyArgument(final HCF plugin) {
        super("apply", "Applies a kit to player");
        this.plugin = plugin;
        this.aliases = new String[]{"give"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> <playerName>";
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
        final Player target = Bukkit.getPlayer(args[2]);
        if (target == null || (sender instanceof Player && !((Player) sender).canSee(target))) {
            sender.sendMessage(ConfigurationService.GOLD + "Player '" + ConfigurationService.WHITE + args[2] + ConfigurationService.GOLD + "' not found.");
            return true;
        }
        if (kit.applyTo(target, true, false)) {
            sender.sendMessage(ConfigurationService.GRAY + "Applied kit '" + kit.getDisplayName() + "' to '" + target.getName() + "'.");
            return true;
        }
        sender.sendMessage(ConfigurationService.RED + "Failed to apply kit " + kit.getDisplayName() + " to " + target.getName() + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2) {
            return (args.length == 3) ? null : Collections.emptyList();
        }
        final List<Kit> kits = this.plugin.getKitManager().getKits();
        final ArrayList results = new ArrayList(kits.size());
        for (final Kit kit : kits) {
            results.add(kit.getName());
        }
        return results;
    }
}
