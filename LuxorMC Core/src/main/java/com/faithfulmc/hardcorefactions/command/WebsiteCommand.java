package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WebsiteCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        sender.sendMessage(ConfigurationService.GOLD + ChatColor.BOLD.toString() + "Website: " + ConfigurationService.YELLOW + ConfigurationService.SITE);
        return true;
    }
}
