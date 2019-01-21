package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.faction.FactionExecutor;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.collect.ArrayListMultimap;
import net.minecraft.util.com.google.common.collect.ImmutableMultimap;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionHelpArgument extends CommandArgument {
    private static final int HELP_PER_PAGE = 10;
    private final FactionExecutor executor;
    private ImmutableMultimap<Integer, String> pages;

    public FactionHelpArgument(FactionExecutor executor) {
        super("help", "View help on how to use factions.");
        this.executor = executor;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            this.showPage(sender, label, 1);
            return true;
        }
        Integer page = Ints.tryParse((String) args[1]);
        if (page == null) {
            sender.sendMessage( ConfigurationService.RED + "'" + args[1] + "' is not a valid number.");
            return true;
        }
        this.showPage(sender, label, page);
        return true;
    }

    private void showPage(CommandSender sender, String label, int pageNumber) {
        if (this.pages == null) {
            boolean isPlayer = sender instanceof Player;
            int val = 1;
            int count = 0;
            ArrayListMultimap<Integer, String> pages = ArrayListMultimap.create();
            for (CommandArgument argument : this.executor.getArguments()) {
                String permission;
                if (argument.equals(this) || (permission = argument.getPermission()) != null && !sender.hasPermission(permission) || argument.isPlayerOnly() && !isPlayer) {
                    continue;
                }
                pages.get(val).add((Object) ConfigurationService.YELLOW + "/" + label + ' ' + argument.getName() + ConfigurationService.LINE_COLOR + " - " + ConfigurationService.GRAY + argument.getDescription());
                if (++count % HELP_PER_PAGE != 0) {
                    continue;
                }
                ++val;
            }
            this.pages = ImmutableMultimap.copyOf(pages);
        }
        int totalPageCount = this.pages.size() / HELP_PER_PAGE + 1;
        if (pageNumber < 1) {
            sender.sendMessage( ConfigurationService.RED + "You cannot view a page less than 1.");
            return;
        }
        if (pageNumber > totalPageCount) {
            sender.sendMessage( ConfigurationService.RED + "There are only " + totalPageCount + " pages.");
            return;
        }
        sender.sendMessage( ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage( ConfigurationService.GOLD + " Faction Help " + ConfigurationService.YELLOW + "(Page " + pageNumber + '/' + totalPageCount + ')');
        for (String message : this.pages.get(pageNumber)) {
            sender.sendMessage("  " + message);
        }
        sender.sendMessage(ConfigurationService.GOLD + " To view other pages, use " + ConfigurationService.YELLOW + '/' + label + ' ' + this.getName() + " <page#>" + ConfigurationService.GOLD + '.');
        sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }
}