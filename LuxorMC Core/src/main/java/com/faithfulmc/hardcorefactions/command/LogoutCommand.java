package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.type.LogoutTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class LogoutCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
    private final HCF plugin;


    public LogoutCommand(HCF plugin) {

        this.plugin = plugin;

    }


    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");

            return true;

        }

        Player player = (Player) sender;

        LogoutTimer logoutTimer = this.plugin.getTimerManager().logoutTimer;

        if (!logoutTimer.setCooldown(player, player.getUniqueId())) {

            sender.sendMessage(ConfigurationService.RED + "Your " + logoutTimer.getDisplayName() + ConfigurationService.RED + " timer is already active.");

            return true;

        }

        sender.sendMessage(ConfigurationService.RED + "Your " + logoutTimer.getDisplayName() + ConfigurationService.RED + " timer has started.");

        return true;

    }


    public java.util.List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        return java.util.Collections.emptyList();

    }

}