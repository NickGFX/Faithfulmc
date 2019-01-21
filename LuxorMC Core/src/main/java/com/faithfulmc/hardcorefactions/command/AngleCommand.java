package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.util.JavaUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;


public class AngleCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Location location = ((Player) sender).getLocation();
        sender.sendMessage(ConfigurationService.GOLD + JavaUtils.format(location.getYaw()) + " yaw" + ConfigurationService.WHITE + ", " + ConfigurationService.GOLD + JavaUtils.format(location.getPitch()) + " pitch");
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

}