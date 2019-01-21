package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class GoppleCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
    private final HCF plugin;

    public GoppleCommand(HCF plugin) {
        this.plugin = plugin;

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;

        }
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
        PlayerTimer timer = this.plugin.getTimerManager().goppleTimer;
        long remaining = timer.getRemaining(player);
        if (remaining <= 0L) {
            sender.sendMessage(ConfigurationService.RED + "Your " + timer.getDisplayName() + ConfigurationService.RED + " timer is no longer active.");
            return true;

        }
        sender.sendMessage(ConfigurationService.YELLOW + "Your " + timer.getDisplayName() + ConfigurationService.YELLOW + " timer is active for another " + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.YELLOW + '.');
        return true;

    }


    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return java.util.Collections.emptyList();

    }

}