package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.MapSorting;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.chat.*;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FactionListArgument extends CommandArgument {
    private static final int MAX_FACTIONS_PER_PAGE = 10;

    private static net.md_5.bungee.api.ChatColor fromBukkit(final ChatColor chatColor) {
        return net.md_5.bungee.api.ChatColor.getByChar(chatColor.getChar());
    }

    private final HCF plugin;

    public FactionListArgument(final HCF plugin) {
        super("list", "See a list of all factions.");
        this.plugin = plugin;
        this.aliases = new String[]{"l"};
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        Integer page;
        if (args.length < 2) {
            page = 1;
        } else {
            page = Ints.tryParse(args[1]);
            if (page == null) {
                sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is not a valid number.");
                return true;
            }
        }
        new BukkitRunnable() {
            public void run() {
                //TODO - caching
                FactionListArgument.this.showList(page, label, sender);
            }
        }.runTaskAsynchronously(this.plugin);
        return true;
    }

    private void showList(final int pageNumber, final String label, final CommandSender sender) {
        if (pageNumber < 1) {
            sender.sendMessage(ConfigurationService.RED + "You cannot view a page less than 1.");
            return;
        }
        Map<PlayerFaction, Integer> factionOnlineMap = new HashMap<PlayerFaction, Integer>();
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (senderPlayer == null || senderPlayer.canSee(target)) {
                final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(target);
                if (playerFaction != null) {
                    factionOnlineMap.put(playerFaction, factionOnlineMap.getOrDefault(playerFaction, 0) + 1);
                }
            }
        }
        Map<Integer, List<BaseComponent[]>> pages = new HashMap<Integer, List<BaseComponent[]>>();
        List<Map.Entry<PlayerFaction, Integer>> sortedMap = MapSorting.sortedValues(factionOnlineMap, Comparator.reverseOrder());
        for (Map.Entry<PlayerFaction, Integer> entry : sortedMap) {
            int currentPage = pages.size();
            List<BaseComponent[]> results = pages.get(currentPage);
            if (results == null || results.size() >= 10) {
                pages.put(++currentPage, results = new ArrayList<>(10));
            }
            PlayerFaction playerFaction2 = entry.getKey();
            String displayName = playerFaction2.getDisplayName(sender);
            int index = results.size() + ((currentPage > 1) ? ((currentPage - 1) * 10) : 0) + 1;
            ComponentBuilder builder = new ComponentBuilder("  " + index + ". ").color((net.md_5.bungee.api.ChatColor.getByChar(ConfigurationService.WHITE.getChar())));
            builder.append(displayName).color(fromBukkit(ConfigurationService.RED)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, '/' + label + " show " + playerFaction2.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ConfigurationService.YELLOW + "Click to view " + displayName + ConfigurationService.YELLOW + '.').create()));
            builder.append(" [" + ConfigurationService.GREEN + entry.getValue() + ConfigurationService.SCOREBOARD_COLOR + '/' + playerFaction2.getMembers().size() + " Online] ").color(fromBukkit(ConfigurationService.SCOREBOARD_COLOR));
            builder.append(" [").color(fromBukkit(ConfigurationService.SCOREBOARD_COLOR));
            builder.append(JavaUtils.format(playerFaction2.getDeathsUntilRaidable())).color(fromBukkit(playerFaction2.getDtrColour()));
            builder.append('/' + JavaUtils.format(playerFaction2.getMaximumDeathsUntilRaidable()) + " DTR]").color(fromBukkit(ConfigurationService.SCOREBOARD_COLOR));
            results.add(builder.create());
        }
        int maxPages = pages.size();
        if (pageNumber > maxPages) {
            sender.sendMessage(ConfigurationService.RED + "There " + ((maxPages == 1) ? ("is only " + maxPages + " page") : ("are only " + maxPages + " pages")) + ".");
            return;
        }
        sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ConfigurationService.YELLOW + " Faction List " + ConfigurationService.GRAY + "(Page " + pageNumber + '/' + maxPages + ')');
        Collection<BaseComponent[]> components = pages.get(pageNumber);
        for (BaseComponent[] component : components) {
            if (component == null) {
                continue;
            }
            if (senderPlayer != null) {
                senderPlayer.spigot().sendMessage(component);
            } else {
                sender.sendMessage(TextComponent.toPlainText(component));
            }
        }
        sender.sendMessage(ConfigurationService.YELLOW + " To view other pages, use " + ConfigurationService.GOLD + '/' + label + ' ' + this.getName() + " <page#>" + ConfigurationService.GOLD + '.');
        sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }
}