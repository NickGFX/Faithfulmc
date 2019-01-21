package com.faithfulmc.framework.command.module.inventory;

import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkullCommand extends BaseCommand {
    private static final ImmutableList<String> SKULL_NAMES;

    static {
        final ImmutableList.Builder builder = new ImmutableList.Builder();
        for (final SkullType skullType : SkullType.values()) {
            builder.add((Object) skullType.name());
        }
        SKULL_NAMES = builder.build();
    }

    public SkullCommand() {
        super("skull", "Spawns a player head skull item.");
        this.setAliases(new String[]{"head", "playerhead"});
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean isPlayerOnlyCommand() {
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
            return true;
        }
        final Optional<SkullType> skullType = (args.length > 0) ? Enums.getIfPresent(SkullType.class, args[0]) : Optional.absent();
        ItemStack stack;
        if (skullType.isPresent()) {
            stack = new ItemStack(Material.SKULL_ITEM, 1, (short) ((SkullType) skullType.get()).getData());
        } else {
            stack = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.getData());
            final String ownerName = (args.length > 0) ? args[0] : sender.getName();
            final SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.setOwner(ownerName);
            stack.setItemMeta((ItemMeta) meta);
        }
        ((Player) sender).getInventory().addItem(new ItemStack[]{stack});
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        final ArrayList<String> completions = new ArrayList<>(SkullCommand.SKULL_NAMES);
        final Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (senderPlayer == null || senderPlayer.canSee(player)) {
                completions.add(player.getName());
            }
        }
        return BukkitUtils.getCompletions(args, completions);
    }
}
