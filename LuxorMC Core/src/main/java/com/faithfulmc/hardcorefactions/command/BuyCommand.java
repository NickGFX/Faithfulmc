package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BuyCommand implements CommandExecutor {
    private final HCF hcf;

    public BuyCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        sender.sendMessage(ConfigurationService.GOLD.toString() + ChatColor.BOLD.toString() + "Server Store: " + ConfigurationService.YELLOW + ConfigurationService.STORE);
        return true;
    }
}
