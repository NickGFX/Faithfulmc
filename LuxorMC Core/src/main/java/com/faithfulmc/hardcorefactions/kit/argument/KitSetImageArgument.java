package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSetImageArgument extends CommandArgument {
    private final HCF plugin;

    public KitSetImageArgument(final HCF plugin) {
        super("setimage", "Sets the image of kit in GUI to held item");
        this.plugin = plugin;
        this.aliases = new String[]{"setitem", "setpic", "setpicture"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <kitName>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This argument is only executable by players.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Player player = (Player) sender;
        final ItemStack stack = player.getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
            player.sendMessage(ConfigurationService.RED + "You are not holding anything.");
            return true;
        }
        final Kit kit = this.plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            sender.sendMessage(ConfigurationService.RED + "There is not a kit named " + args[1] + '.');
            return true;
        }
        kit.setImage(stack.clone());
        sender.sendMessage(ConfigurationService.GRAY + "Set image of kit " + ConfigurationService.YELLOW + kit.getDisplayName() + ConfigurationService.GRAY + " to " + ConfigurationService.YELLOW + BasePlugin.getPlugin().getItemDb().getName(stack) + ConfigurationService.GRAY + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }
        final List<Kit> kits = this.plugin.getKitManager().getKits();
        final List<String> results = new ArrayList<>(kits.size());
        for (Kit kit : kits) {
            results.add(kit.getName());
        }
        return results;
    }
}
