package com.faithfulmc.framework.command.module.inventory;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand extends BaseCommand {
    public GiveCommand() {
        super("give", "Gives an item to a player.");
        this.setUsage("/(command) <playerName> <itemName> [quantity]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
            return true;
        }
        final Player p = (Player) sender;
        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + this.getUsage());
            return true;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.RESET + args[0] + BaseConstants.GOLD + "' not found.");
            return true;
        }
        final Player t = Bukkit.getPlayer(args[0]);
        if (BasePlugin.getPlugin().getItemDb().getItem(args[1]) == null) {
            sender.sendMessage(BaseConstants.GOLD + "Item named or with ID '" + ChatColor.RESET + args[1] + BaseConstants.GOLD + "' not found.");
            return true;
        }
        if (args.length == 2) {
            if (!t.getInventory().addItem(new ItemStack[]{BasePlugin.getPlugin().getItemDb().getItem(args[1], BasePlugin.getPlugin().getItemDb().getItem(args[1]).getMaxStackSize())}).isEmpty()) {
                p.sendMessage(ChatColor.RED + "The inventory of the player is full.");
                return true;
            }
            for (Player on : Bukkit.getOnlinePlayers()) {
                if (on.hasPermission("base.command.give")) {
                    if (on != p) {
                        on.sendMessage(BaseConstants.GRAY + "[" + BaseConstants.GOLD + p.getName() + BaseConstants.YELLOW + " has given " + t.getName() + BaseConstants.GRAY + " 64, " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[1])) + "]");
                    } else {
                        on.sendMessage(BaseConstants.GOLD + "You gave '" + ChatColor.WHITE + t.getName() + BaseConstants.GOLD + "' " + " 64, " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[1])));
                    }
                }
            }
        }
        if (args.length == 3) {
            if (!t.getInventory().addItem(new ItemStack[]{BasePlugin.getPlugin().getItemDb().getItem(args[1], Integer.parseInt(args[2]))}).isEmpty()) {
                p.sendMessage(ChatColor.RED + "The inventory of the player is full.");
                return true;
            }
            for (Player on : Bukkit.getOnlinePlayers()) {
                if (on.hasPermission("base.command.give")) {
                    if (on != p) {
                        on.sendMessage(BaseConstants.GRAY + "[" + BaseConstants.GOLD + p.getName() + BaseConstants.YELLOW + " has given " + BaseConstants.GOLD + t.getName() + BaseConstants.GRAY + " " + args[2] + ", " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[1])) + "]");
                    } else {
                        on.sendMessage(BaseConstants.GOLD + "You gave '" + ChatColor.WHITE + t.getName() + BaseConstants.GOLD + "' " + args[2] + ", " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[1])));
                    }
                }
            }
        }
        return true;
    }
}
