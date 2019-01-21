package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.scoreboard.PlayerBoard;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;


public class ToggleSidebarCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabExecutor {
    private final HCF plugin;


    public ToggleSidebarCommand(HCF plugin) {

        this.plugin = plugin;

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof org.bukkit.entity.Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");

            return true;

        }

        PlayerBoard playerBoard = this.plugin.getScoreboardHandler().getPlayerBoard(((org.bukkit.entity.Player) sender).getUniqueId());

        boolean newVisibile = !playerBoard.isSidebarVisible();
                playerBoard.setSidebarVisible(newVisibile);

        sender.sendMessage(ConfigurationService.YELLOW + "Scoreboard sidebar is " + (newVisibile ? ChatColor.GREEN + "now" : new StringBuilder().append(ConfigurationService.RED).append("no longer").toString()) + ConfigurationService.YELLOW + " visible.");

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();

    }

}