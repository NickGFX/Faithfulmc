package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class RepairCommand extends BaseCommand {
    private static final short FULLY_REPAIRED_DURABILITY = 0;

    public RepairCommand() {
        super("repair", "Allows repairing of damaged tools for a player.");
        this.setUsage("/(command) <playerName> [all]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        Player target;
        if (args.length > 0) {
            target = BukkitUtils.playerWithNameOrUUID(args[0]);
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            target = (Player) sender;
        }
        if (target != null && BaseCommand.canSee(sender, target)) {
            final HashSet<ItemStack> toRepair = new HashSet();
            if (args.length >= 2 && args[1].equalsIgnoreCase("all")) {
                final PlayerInventory targetInventory = target.getInventory();
                toRepair.addAll(Arrays.asList(targetInventory.getContents()));
                toRepair.addAll(Arrays.asList(targetInventory.getArmorContents()));
            } else {
                toRepair.add(target.getItemInHand());
            }
            for (final ItemStack stack : toRepair) {
                if (stack != null && stack.getType() != Material.AIR) {
                    stack.setDurability((short) 0);
                }
            }
            sender.sendMessage(BaseConstants.YELLOW + "Repaired " + ((toRepair.size() > 1) ? "inventory" : "held item") + " of " + target.getName() + '.');
            return true;
        }
        sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
