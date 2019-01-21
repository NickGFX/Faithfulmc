package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.JavaUtils;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyCommand implements CommandExecutor {
    private final HCF plugin;

    public EconomyCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        UUID target;
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        boolean hasStaffPermission = sender.hasPermission(command.getPermission() + ".staff");
        if (args.length > 0 && hasStaffPermission) {
            target = plugin.getUserManager().fetchUUID(args[0]);
        } else if(senderPlayer != null){
            target = senderPlayer.getUniqueId();
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + " <playerName>");
            return true;
        }
        FactionUser factionUser;
        if (target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null) {
            sender.sendMessage(ConfigurationService.RED + "Player not found");
        }
        else if (args.length < 2 || !hasStaffPermission) {
            sender.sendMessage(ConfigurationService.YELLOW + (senderPlayer != null && factionUser.getUserUUID() == senderPlayer.getUniqueId() ? "Your balance" : "Balance of " + factionUser.getName()) + " is " + ChatColor.GREEN + '$' + factionUser.getBalance() + ConfigurationService.GOLD + '.');
        }
        else if (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + factionUser.getName() + ' ' + args[1] + " <amount>");
            } else {
                Integer amount = Ints.tryParse(args[2]);
                if (amount == null) {
                    sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a valid number.");
                } else {
                    factionUser.setBalance(factionUser.getBalance() + amount);
                    sender.sendMessage(new String[]{ConfigurationService.YELLOW + "Added " + '$' + JavaUtils.format(amount) + " to balance of " + factionUser.getName() + '.', ConfigurationService.YELLOW + "Balance of " + factionUser.getName() + " is now " + '$' + factionUser.getBalance() + '.'});
                }
            }
        }
        else if (args[1].equalsIgnoreCase("take") || args[1].equalsIgnoreCase("negate") || args[1].equalsIgnoreCase("minus") || args[1].equalsIgnoreCase("subtract")) {
            if (args.length < 3) {
                sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + factionUser.getName() + ' ' + args[1] + " <amount>");
            } else {
                Integer amount = Ints.tryParse(args[2]);
                if (amount == null) {
                    sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a valid number.");
                } else {
                    factionUser.setBalance(factionUser.getBalance() - amount);
                    sender.sendMessage(new String[]{ConfigurationService.YELLOW + "Taken " + '$' + JavaUtils.format(amount) + " from balance of " + factionUser.getName() + '.', ConfigurationService.YELLOW + "Balance of " + factionUser.getName() + " is now " + '$' + factionUser.getBalance() + '.'});
                }
            }
        }
        else if (!args[1].equalsIgnoreCase("set")) {
            sender.sendMessage(ConfigurationService.GOLD + (senderPlayer != null && factionUser.getUserUUID() == senderPlayer.getUniqueId() ? "Your balance" : "Balance of " + factionUser.getName()) + " is " + ConfigurationService.WHITE + '$' + factionUser.getBalance() + ConfigurationService.GOLD + '.');
        }
        else if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + factionUser.getName() + ' ' + args[1] + " <amount>");
        }
        else {
            Integer amount = Ints.tryParse(args[2]);
            if (amount == null) {
                sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a valid number.");
            } else {
                factionUser.setBalance(amount);
                sender.sendMessage(ConfigurationService.YELLOW + "Set balance of " + factionUser.getName() + "from " + '$' + JavaUtils.format(factionUser.getBalance()) + " to " + '$' + amount + '.');
            }
        }
        return true;
    }
}