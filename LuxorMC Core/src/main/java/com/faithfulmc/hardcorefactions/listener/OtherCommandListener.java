package com.faithfulmc.hardcorefactions.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class OtherCommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        //CrazyEnchants GKits
        if ((event.getMessage().startsWith("/gkit")) ||
                (event.getMessage().startsWith("/gkits")) ||
                (event.getMessage().equalsIgnoreCase("/gkitz"))) {
            event.setCancelled(true);
            player.performCommand("kits");

        } else
            //CrazyEnchants Menu
            if ((event.getMessage().startsWith("/customenchant")) ||
                    (event.getMessage().startsWith("/customenchants"))) {
                event.setCancelled(true);
                player.performCommand("ce");

            } else
                //BattlePass Menu
                if ((event.getMessage().startsWith("/bp")) ||
                        (event.getMessage().startsWith("/battle"))) {
                    event.setCancelled(true);
                    player.performCommand("battlepass");
                }
    }
}