package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnchantCommand extends BaseCommand {

    public EnchantCommand() {
        super("enchant", "Unsafely enchant an item.");
        this.setUsage("/(command) <enchantment> <level> [playerName]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
            return true;
        }
        Player target;
        if (args.length > 2 && sender.hasPermission(command.getPermission() + ".others")) {
            target = BukkitUtils.playerWithNameOrUUID(args[2]);
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            target = (Player) sender;
        }
        if (target == null || !BaseCommand.canSee(sender, target)) {
            sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[2] + BaseConstants.GOLD + "' not found.");
            return true;
        }
        final Enchantment enchantment = Enchantment.getByName(args[0]);
        if (enchantment == null) {
            sender.sendMessage(ChatColor.RED + "No enchantment named '" + args[0] + "' found.");
            return true;
        }
        final ItemStack stack = target.getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is not holding an item.");
            return true;
        }
        final Integer level = Ints.tryParse(args[1]);
        if (level == null) {
            sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number.");
            return true;
        }
        final int maxLevel = enchantment.getMaxLevel();
        if (level > maxLevel && !sender.hasPermission(command.getPermission() + ".abovemaxlevel")) {
            sender.sendMessage(ChatColor.RED + "The maximum enchantment level for " + enchantment.getName() + " is " + maxLevel + '.');
            return true;
        }
        if (!enchantment.canEnchantItem(stack) && !sender.hasPermission(command.getPermission() + ".anyitem")) {
            sender.sendMessage(ChatColor.RED + "Enchantment " + enchantment.getName() + " cannot be applied to that item.");
            return true;
        }
        stack.addUnsafeEnchantment(enchantment, (int) level);
        String itemName;
        try {
            itemName = BasePlugin.getPlugin().getNmsProvider().getName(stack);
        } catch (Error var12) {
            itemName = stack.getType().name();
        }
        Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Applied " + enchantment.getName() + " at level " + level + " onto " + itemName + " of " + target.getName() + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        switch (args.length) {
            case 1: {
                final Enchantment[] enchantments = Enchantment.values();
                final ArrayList results = new ArrayList(enchantments.length);
                final Enchantment[] var7 = enchantments;
                for (int var8 = enchantments.length, var9 = 0; var9 < var8; ++var9) {
                    final Enchantment enchantment = var7[var9];
                    results.add(enchantment.getName());
                }
                return BukkitUtils.getCompletions(args, results);
            }
            case 3: {
                return null;
            }
            default: {
                return Collections.emptyList();
            }
        }
    }
}
