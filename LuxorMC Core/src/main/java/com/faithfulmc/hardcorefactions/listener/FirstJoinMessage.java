package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.BukkitUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FirstJoinMessage implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 38));
                player.sendMessage(ConfigurationService.YELLOW + "Welcome to " + ConfigurationService.GOLD + ChatColor.BOLD.toString() + ConfigurationService.MAP_TITLE + (ConfigurationService.KIT_MAP ? "" : ConfigurationService.YELLOW + " Map " + ConfigurationService.MAP_NUMBER));
                player.sendMessage(ConfigurationService.ARROW_COLOR + " * " + ConfigurationService.YELLOW + "Faction Size: " + ConfigurationService.GRAY + ConfigurationService.FACTION_PLAYER_LIMIT + " Man / " + (ConfigurationService.MAX_ALLIES_PER_FACTION == 0 ? "No Allies" : ConfigurationService.MAX_ALLIES_PER_FACTION == 1 ? "1 Ally" : ConfigurationService.MAX_ALLIES_PER_FACTION + " Allies"));
                player.sendMessage(ConfigurationService.ARROW_COLOR + " * " + ConfigurationService.YELLOW + "Map Kit: " + ConfigurationService.GRAY + "Protection " + ConfigurationService.ENCHANTMENT_LIMITS.get(Enchantment.PROTECTION_ENVIRONMENTAL) + " /" + ConfigurationService.GRAY + " Sharpness " + ConfigurationService.ENCHANTMENT_LIMITS.get(Enchantment.DAMAGE_ALL));
                player.sendMessage(ConfigurationService.ARROW_COLOR + " * " + ConfigurationService.YELLOW + "Teamspeak: " + ConfigurationService.GRAY + ConfigurationService.TEAMSPEAK);
                player.sendMessage(ConfigurationService.ARROW_COLOR + " * " + ConfigurationService.YELLOW + "Website: " + ConfigurationService.GRAY + ConfigurationService.SITE);
                player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 38));
            }
        }.runTaskLaterAsynchronously(HCF.getInstance(), 10);
    }
}
