package com.faithfulmc.hardcorefactions.command.lives.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class LivesCheckArgument extends CommandArgument {
    private final HCF plugin;

    public LivesCheckArgument(HCF plugin) {
        super("check", "Check how much lives a player has");
        this.plugin = plugin;
        this.permission = ("hcf.command.lives.argument." + getName());
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [playerName]";
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FactionUser factionUser;
        if(args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                sender.sendMessage(ConfigurationService.YELLOW + "You have " + ConfigurationService.GOLD + factionUser.getLives() + ConfigurationService.YELLOW + ConfigurationService.YELLOW + " " + (factionUser.getLives() == 1 ? "life" : "lives") + "." );
            }
            else{
                sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
            }
        }
        else{
            UUID target = plugin.getUserManager().fetchUUID(args[1]);
            if(target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null){
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            }
            else{
                sender.sendMessage(ConfigurationService.YELLOW + factionUser.getName() + " has " + ConfigurationService.GOLD + factionUser.getLives() + ConfigurationService.YELLOW + " " + (factionUser.getLives() == 1 ? "life" : "lives") + ".");
            }
        }
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? null : Collections.emptyList();
    }

}
