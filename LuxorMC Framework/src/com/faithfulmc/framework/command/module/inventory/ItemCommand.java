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

public class ItemCommand extends BaseCommand {
    public ItemCommand() {
        super("item", "Spawns an item.");
        this.setAliases(new String[]{"i", "get"});
        this.setUsage("/(command) <itemName> [quantity]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
            return true;
        }
        final Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + this.getUsage());
            return true;
        }
        if (BasePlugin.getPlugin().getItemDb().getItem(args[0]) == null) {
            sender.sendMessage(BaseConstants.GOLD + "Item named or with ID '" + ChatColor.RESET + args[0] + BaseConstants.GOLD + "' not found.");
            return true;
        }
        if (args.length == 1) {
            if (!p.getInventory().addItem(new ItemStack[]{BasePlugin.getPlugin().getItemDb().getItem(args[0], BasePlugin.getPlugin().getItemDb().getItem(args[0]).getMaxStackSize())}).isEmpty()) {
                p.sendMessage(ChatColor.RED + "Your inventory is full.");
                return true;
            }
            for (final Player on : Bukkit.getOnlinePlayers()) {
                if (on.hasPermission("base.command.give")) {
                    if (on != p) {
                        on.sendMessage(BaseConstants.GRAY + "[" + BaseConstants.GOLD + p.getName() + BaseConstants.YELLOW + " has given himself " + BaseConstants.GRAY + BasePlugin.getPlugin().getItemDb().getItem(args[0]).getMaxStackSize() + ", " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[0])) + "]");
                    } else {
                        on.sendMessage(BaseConstants.GOLD + "You gave yourself " + BasePlugin.getPlugin().getItemDb().getItem(args[0]).getMaxStackSize() + ", " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[0])));
                    }
                }
            }
        }
        if (args.length == 2) {
            if (!p.getInventory().addItem(new ItemStack[]{BasePlugin.getPlugin().getItemDb().getItem(args[0], Integer.parseInt(args[1]))}).isEmpty()) {
                p.sendMessage(ChatColor.RED + "Your inventory is full.");
                return true;
            }
            for (final Player on : Bukkit.getOnlinePlayers()) {
                if (on.hasPermission("base.command.give")) {
                    if (on != p) {
                        on.sendMessage(BaseConstants.GRAY + "[" + BaseConstants.GOLD + p.getName() + BaseConstants.YELLOW + " has given himself" + BaseConstants.GRAY + " " + args[1] + ", " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[0])) + " ]");
                    } else {
                        on.sendMessage(BaseConstants.GOLD + "You gave yourself " + args[1] + ", " + BasePlugin.getPlugin().getItemDb().getName(BasePlugin.getPlugin().getItemDb().getItem(args[0])));
                    }
                }
            }
        }
        return true;
    }
}
