package com.faithfulmc.hardcorefactions.kit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class KitGuiArgument extends CommandArgument {
    private final HCF plugin;

    public KitGuiArgument(final HCF plugin) {
        super("gui", "Opens the kit gui");
        this.plugin = plugin;
        this.aliases = new String[]{"menu"};
        this.permission = "base.command.kit.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players may open kit GUI's.");
            return true;
        }
        final List kits = this.plugin.getKitManager().getKits();
        if (kits.isEmpty()) {
            sender.sendMessage(ConfigurationService.RED + "No kits have been defined.");
            return true;
        }
        final Player player = (Player) sender;
        player.openInventory(this.plugin.getKitManager().getGui(player));
        sender.sendMessage(ConfigurationService.YELLOW + "You have opened the kit GUI. Middle click to preview, or click to acquire a kit.");
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
