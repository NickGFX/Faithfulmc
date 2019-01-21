package com.faithfulmc.hardcorefactions.command.lives.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.primitives.Ints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class LivesSetArgument extends CommandArgument {
    private final HCF plugin;

    public LivesSetArgument(HCF plugin) {
        super("set", "Set how much lives a player has");
        this.plugin = plugin;
        this.permission = ("hcf.command.lives.argument." + getName());
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName> <amount>";
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Integer amount = Ints.tryParse(args[2]);
            if (amount == null) {
                sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a number.");
            }
            else {
                UUID target = plugin.getUserManager().fetchUUID(args[1]);
                FactionUser factionUser;
                if (target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null) {
                    sender.sendMessage(ConfigurationService.RED + "Player not found");
                }
                else {
                    factionUser.setLives(amount);
                    sender.sendMessage(ConfigurationService.YELLOW + factionUser.getName() + " now has " + ConfigurationService.GOLD + amount + ConfigurationService.YELLOW + " lives.");
                }
            }
        }
        return true;
    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? null : Collections.emptyList();
    }
}