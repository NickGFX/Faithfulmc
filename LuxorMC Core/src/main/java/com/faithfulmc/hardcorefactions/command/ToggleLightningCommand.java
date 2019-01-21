package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ToggleLightningCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabExecutor {
    private final HCF plugin;


    public ToggleLightningCommand(HCF plugin) {

        this.plugin = plugin;

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof org.bukkit.entity.Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");

            return true;

        }

        FactionUser factionUser = this.plugin.getUserManager().getUser(((org.bukkit.entity.Player) sender).getUniqueId());

        boolean newShowLightning = !factionUser.isShowLightning();
                factionUser.setShowLightning(newShowLightning);

        sender.sendMessage(ConfigurationService.YELLOW + "You will now " + (newShowLightning ? ChatColor.GREEN + "able" : new StringBuilder().append(ConfigurationService.RED).append("unable").toString()) + ConfigurationService.YELLOW + " to see lightning strikes on death.");

        return true;

    }


    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        return java.util.Collections.emptyList();

    }

}