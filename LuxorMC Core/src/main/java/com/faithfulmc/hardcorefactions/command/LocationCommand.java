package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;


public class LocationCommand implements CommandExecutor, TabCompleter {
    private final HCF plugin;


    public LocationCommand(HCF plugin) {

        this.plugin = plugin;

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player target;

        if ((args.length >= 1) && (sender.hasPermission(command.getPermission() + ".others"))) {

            target = Bukkit.getPlayer(args[0]);

        } else {

            if (!(sender instanceof Player)) {

                sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + " [playerName]");

                return true;

            }

            target = (Player) sender;

        }

        if ((target == null) || (((sender instanceof Player)) && (!((Player) sender).canSee(target)))) {

            sender.sendMessage(ConfigurationService.GOLD + "Player '" + ConfigurationService.WHITE + args[0] + ConfigurationService.GOLD + "' not found.");

            return true;

        }

        Location location = target.getLocation();

        Faction factionAt = this.plugin.getFactionManager().getFactionAt(location);

        sender.sendMessage(ConfigurationService.YELLOW + (target == sender ? "You are " : target.getName() + " is ") + "in the territory of " + factionAt.getDisplayName(sender) + ConfigurationService.YELLOW + '(' + (factionAt.isSafezone() ? ChatColor.GREEN + "Non-Deathban" : new StringBuilder().append(ConfigurationService.RED).append("Deathban").toString()) + ConfigurationService.YELLOW + ')');
        sender.sendMessage(ConfigurationService.YELLOW + "Position: " + ChatColor.WHITE + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockY() + ")");
        return true;

    }


    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {

        return null;

    }
}

