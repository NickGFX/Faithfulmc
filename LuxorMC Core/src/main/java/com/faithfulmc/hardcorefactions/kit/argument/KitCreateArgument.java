package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.kit.event.KitCreateEvent;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class KitCreateArgument extends CommandArgument {
    private final HCF plugin;

    public KitCreateArgument(final HCF plugin) {
        super("create", "Creates a kit");
        this.plugin = plugin;
        this.aliases = new String[]{"make", "build"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName> [kitDescription]";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players may create kits.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        if (!JavaUtils.isAlphanumeric(args[1])) {
            sender.sendMessage(ConfigurationService.GRAY + "Kit names may only be alphanumeric.");
            return true;
        }
        Kit kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit != null) {
            sender.sendMessage(ConfigurationService.RED + "There is already a kit named " + args[1] + '.');
            return true;
        }
        final Player player = (Player) sender;
        kit = new Kit(args[1], (args.length >= 3) ? args[2] : null, player.getInventory(), player.getActivePotionEffects());
        final KitCreateEvent event = new KitCreateEvent(kit);
        Bukkit.getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) {
            return true;
        }
        this.plugin.getKitManager().createKit(kit);
        sender.sendMessage(ConfigurationService.GRAY + "Created kit '" + kit.getDisplayName() + "'.");
        return true;
    }
}
