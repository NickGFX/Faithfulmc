package com.faithfulmc.framework.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class ColouredSignListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignCreate(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        if (player != null && player.hasPermission("base.sign.colour")) {
            final String[] lines = event.getLines();
            for (int i = 0; i < lines.length; ++i) {
                if (!player.hasPermission("base.sign.admin") && (event.getLine(i).contains(ChatColor.translateAlternateColorCodes('&', "Sell")) || event.getLine(i).contains("Buy"))) {
                    player.sendMessage(ChatColor.RED + "You have used a sign that you're not allowed.");
                    event.setCancelled(true);
                }
                event.setLine(i, ChatColor.translateAlternateColorCodes('&', lines[i]));
            }
        }
    }
}
