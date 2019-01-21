package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class HatCommand extends BaseCommand {
    public HatCommand() {
        super("hat", "Wear something on your head.");
        this.setUsage("/(command)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }
        final Player player = (Player) sender;
        final ItemStack stack = player.getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You are not holding anything.");
            return true;
        }
        if (stack.getType().getMaxDurability() != 0) {
            sender.sendMessage(ChatColor.RED + "The item you are holding is not suitable to wear for a hat.");
            return true;
        }
        final PlayerInventory inventory = player.getInventory();
        ItemStack helmet = inventory.getHelmet();
        if (helmet != null && helmet.getType() != Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You are already wearing something as your hat.");
            return true;
        }
        int amount = stack.getAmount();
        if (amount > 1) {
            --amount;
            stack.setAmount(amount);
        } else {
            player.setItemInHand(new ItemStack(Material.AIR, 1));
        }
        helmet = stack.clone();
        helmet.setAmount(1);
        inventory.setHelmet(helmet);
        return true;
    }
}
