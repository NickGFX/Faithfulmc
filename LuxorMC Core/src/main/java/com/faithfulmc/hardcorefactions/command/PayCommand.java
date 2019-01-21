package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PayCommand implements CommandExecutor, TabCompleter {
    public static final int MAX_PAYMENT = 50000;
    public static final int CONSOLE_BALANCE = 5000;

    private final HCF plugin;

    public PayCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + " <playerName> <amount>");
        }
        else {
            Integer amount = Ints.tryParse(args[1]);
            if (amount == null) {
                sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is not a valid number.");
            } else if (amount <= 0) {
                sender.sendMessage(ConfigurationService.RED + "You must send money in positive quantities.");
            } else if (amount >= MAX_PAYMENT) {
                sender.sendMessage(ConfigurationService.RED + "You cannot send more than " + MAX_PAYMENT + ".");
            } else {
                Player senderPlayer = sender instanceof Player ? (Player) sender : null;
                FactionUser playerUser = senderPlayer != null ? plugin.getUserManager().getUser(senderPlayer.getUniqueId()) : null;
                int senderBalance = playerUser != null ? playerUser.getBalance() : CONSOLE_BALANCE;
                if (senderBalance < amount) {
                    sender.sendMessage(ConfigurationService.RED + "You tried to pay " + '$' + amount + ", but you only have " + '$' + senderBalance + " in your bank account.");
                } else {
                    UUID target = plugin.getUserManager().fetchUUID(args[0]);
                    FactionUser targetUser;
                    if (target == null || (targetUser = plugin.getUserManager().getUser(target)).getName() == null) {
                        sender.sendMessage(ConfigurationService.RED + "Player not found");
                    } else if (senderPlayer != null && senderPlayer.getUniqueId() == target) {
                        sender.sendMessage(ConfigurationService.RED + "You cannot send money to yourself.");
                    } else {
                        if (playerUser != null) {
                            playerUser.setBalance(playerUser.getBalance() - amount);
                        }
                        targetUser.setBalance(targetUser.getBalance() + amount);
                        if (targetUser.isOnline()) {
                            targetUser.getPlayer().sendMessage(ConfigurationService.YELLOW + sender.getName() + " has sent you " + ConfigurationService.GOLD + '$' + amount + ConfigurationService.YELLOW + '.');
                        }
                        sender.sendMessage(ConfigurationService.YELLOW + "You have sent " + ChatColor.GREEN + '$' + amount + ConfigurationService.YELLOW + " to " + targetUser.getName() + '.');
                    }
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 ? null : Collections.emptyList();
    }

}