package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class QuitListener implements Listener {
    private final BasePlugin plugin;

    public QuitListener(BasePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        BaseUser baseUser = plugin.getUserManager().getUser(uuid);
        baseUser.setLastSeen(System.currentTimeMillis());
        baseUser.setOnline(false);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        BaseUser baseUser = plugin.getUserManager().getUser(uuid);
        baseUser.setLastSeen(System.currentTimeMillis());
        baseUser.setOnline(false);
    }
}
