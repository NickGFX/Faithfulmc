package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FactionTopArgument extends CommandArgument{
    private List<PlayerFaction> topFactions = new ArrayList<>();
    private Long lastSort = null;

    public FactionTopArgument() {
        super("top", "Views the factions with the largest number of points");
        Bukkit.getScheduler().runTask(HCF.getInstance(), this::sort);
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName();

    }

    public void sort(){
        long now = System.currentTimeMillis();
        if(lastSort == null || now - lastSort > TimeUnit.MINUTES.toMillis(5)){
            topFactions = HCF.getInstance().getFactionManager().getFactions().stream().filter(faction -> faction instanceof PlayerFaction).map(faction -> (PlayerFaction) faction).sorted(Comparator.comparingInt(this::sort)).collect(Collectors.toList());
            lastSort = now;
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sort();
        if(sender instanceof Player){
            Player player = (Player) sender;
            sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "   Faction Top");
            sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            Iterator<PlayerFaction> iterator = topFactions.iterator();
            int i = 0;
            while (iterator.hasNext() && i < 10){
                PlayerFaction faction = iterator.next();
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "Click to show faction information of " + ChatColor.GRAY + faction.getName()));
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f show " + faction.getName());
                player.spigot().sendMessage(new ComponentBuilder(" " + (i + 1) + ".").color(ChatColor.WHITE)
                        .append(" " + faction.getName()).color(ChatColor.GRAY)
                        .event(hoverEvent)
                        .event(clickEvent)
                        .append(" " + faction.getPoints()).color(ChatColor.GREEN)
                        .append(" Points").color(ChatColor.WHITE)
                        .create());
                i++;
            }
            sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        }
        return true;
    }

    public int sort(PlayerFaction playerFaction){
        return -playerFaction.getPoints();
    }
}
