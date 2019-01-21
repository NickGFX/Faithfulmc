package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.listener.DeathListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RefundCommand implements org.bukkit.command.CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        String usage = ConfigurationService.RED + "/refund <player>";
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "You must be a player");
        }
        else if (args.length < 1) {
            sender.sendMessage(usage);
        }
        else {
            Player target;
            if ((target = Bukkit.getPlayer(args[0])) == null) {
                sender.sendMessage(ConfigurationService.RED + "Player must be online");
            } else {
                if (DeathListener.inventoryContents.containsKey(target.getUniqueId())) {
                    target.getInventory().setContents(DeathListener.inventoryContents.remove(target.getUniqueId()));
                    target.getInventory().setArmorContents(DeathListener.armorContents.remove(target.getUniqueId()));
                    Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Refunded " + target.getName() + "'s items");
                } else {
                    sender.sendMessage(ConfigurationService.RED + "Player was already refunded items");
                }
            }
        }
        return true;
    }
}