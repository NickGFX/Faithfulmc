package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSetItemsArgument extends CommandArgument {
    private final HCF plugin;

    public KitSetItemsArgument(final HCF plugin) {
        super("setitems", "Sets the items of a kit");
        this.plugin = plugin;
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can set kit items.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Kit kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            sender.sendMessage(ConfigurationService.RED + "Kit '" + args[1] + "' not found.");
            return true;
        }
        final Player player = (Player) sender;
        final PlayerInventory inventory = player.getInventory();
        kit.setItems(inventory.getContents());
        kit.setArmour(inventory.getArmorContents());
        kit.setEffects(player.getActivePotionEffects());
        sender.sendMessage(ConfigurationService.GRAY + "Set the items of kit " + kit.getDisplayName() + " as your current inventory.");
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
