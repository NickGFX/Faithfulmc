package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.listener.MobstackListener;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RenameCommand extends BaseCommand {
    public static final List<String> DISALLOWED = Arrays.asList("Hitler","卍","jews","nigger","n1gger");
    public static final Set<Material> DISALLOWED_ITEMS = Sets.newHashSet(Material.PAPER, Material.DISPENSER, Material.NAME_TAG, Material.MONSTER_EGG, Material.MONSTER_EGGS, Material.INK_SACK);

    public RenameCommand() {
        super("rename", "Rename your held item.");
        this.setUsage("/(command) <newItemName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Player player = (Player) sender;
        final ItemStack stack = player.getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You are not holding anything.");
            return true;
        }
        if(DISALLOWED_ITEMS.contains(stack.getType())){
            sender.sendMessage(ChatColor.RED + "You may not rename this item");
            return true;
        }
        final ItemMeta meta = stack.getItemMeta();
        String oldName = meta.getDisplayName();
        if (oldName != null) {
            oldName = oldName.trim();
        }
        String newName;
        if (!args[0].equalsIgnoreCase("none") && !args[0].equalsIgnoreCase("null")) {
            newName = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 0, args.length));
        } else {
            newName = null;
        }
        if (oldName == null && newName == null) {
            sender.sendMessage(ChatColor.RED + "Your held item already has no name.");
            return true;
        }
        if (oldName != null && oldName.equals(newName)) {
            sender.sendMessage(ChatColor.RED + "Your held item is already named this.");
            return true;
        }
        if(newName != null) {
            String lower = newName.toLowerCase();
            for (String word : DISALLOWED) {
                if (lower.contains(word)) {
                    sender.sendMessage(ChatColor.RED + "You may not use that word, you will now be warned");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warn -s " + sender.getName() + " Offensive Item Renaming");
                    return true;
                }
            }
        }
        if(newName != null && newName.startsWith(MobstackListener.STACKED_PREFIX)){
            sender.sendMessage(BaseConstants.YELLOW + "You may not rename your item to that");
            return true;
        }
        else {
            meta.setDisplayName(newName);
            stack.setItemMeta(meta);
            if (newName == null) {
                sender.sendMessage(BaseConstants.YELLOW + "Removed name of held item from " + oldName + '.');
                return true;
            }
            sender.sendMessage(BaseConstants.YELLOW + "Renamed held item from " + ((oldName == null) ? "no name" : oldName) + " to " + newName + BaseConstants.YELLOW + '.');
        }
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
