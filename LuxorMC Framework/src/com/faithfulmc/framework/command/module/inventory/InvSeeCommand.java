package com.faithfulmc.framework.command.module.inventory;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.StaffPriority;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class InvSeeCommand extends BaseCommand implements Listener {
    private final InventoryType[] types;
    private final Map inventories;

    public InvSeeCommand(final BasePlugin plugin) {
        super("invsee", "View the inventory of a player.");
        this.types = new InventoryType[]{InventoryType.BREWING, InventoryType.CHEST, InventoryType.DISPENSER, InventoryType.ENCHANTING, InventoryType.FURNACE, InventoryType.HOPPER, InventoryType.PLAYER, InventoryType.WORKBENCH};
        this.inventories = new EnumMap(InventoryType.class);
        this.setAliases(new String[]{"inventorysee", "inventory", "inv"});
        this.setUsage("/(command) <playerName>");
        Bukkit.getPluginManager().registerEvents((Listener) this, (Plugin) plugin);
    }

    @Override
    public boolean isPlayerOnlyCommand() {
        return true;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            final Player player = BukkitUtils.playerWithNameOrUUID(args[0]);
            if (player == null) {
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                return true;
            }
            sender.sendMessage(BaseConstants.YELLOW + "This players inventory contains: ");
            for (final ItemStack var15 : player.getInventory().getContents()) {
                if (var15 != null) {
                    sender.sendMessage(BaseConstants.GRAY + var15.getType().toString().replace("_", "").toLowerCase() + ": " + var15.getAmount());
                }
            }
            return true;
        } else {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            final Player player = (Player) sender;
            Object inventory = null;
            final InventoryType[] types = this.types;
            final int length = types.length;
            int i = 0;
            while (i < length) {
                final InventoryType target = types[i];
                if (target.name().equalsIgnoreCase(args[0])) {
                    final Inventory selfPriority;
                    inventory = this.inventories.putIfAbsent(target, selfPriority = Bukkit.createInventory((InventoryHolder) player, target));
                    if (inventory == null) {
                        inventory = selfPriority;
                        break;
                    }
                    break;
                } else {
                    ++i;
                }
            }
            if (inventory == null) {
                final Player var16 = BukkitUtils.playerWithNameOrUUID(args[0]);
                if (sender.equals(var16)) {
                    sender.sendMessage(ChatColor.RED + "You cannot check the inventory of yourself.");
                    return true;
                }
                if (var16 == null || !BaseCommand.canSee(sender, var16)) {
                    sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                    return true;
                }
                final StaffPriority var17 = StaffPriority.of(player);
                if (var17 != StaffPriority.ADMIN && StaffPriority.of(var16).isMoreThan(var17)) {
                    sender.sendMessage(ChatColor.RED + "You do not have access to check the inventory of that player.");
                    return true;
                }
                inventory = var16.getInventory();
            }
            player.openInventory((Inventory) inventory);
            return true;
        }
    }

    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        final InventoryType[] values = InventoryType.values();
        final ArrayList results = new ArrayList(values.length);
        final InventoryType[] senderPlayer = values;
        for (int var8 = values.length, target = 0; target < var8; ++target) {
            final InventoryType type = senderPlayer[target];
            results.add(type.name());
        }
        final Player var9 = (sender instanceof Player) ? (Player) sender : null;
        for (final Player var11 : Bukkit.getOnlinePlayers()) {
            if (var9 == null || var9.canSee(var11)) {
                results.add(var11.getName());
            }
        }
        return BukkitUtils.getCompletions(args, results);
    }
}
