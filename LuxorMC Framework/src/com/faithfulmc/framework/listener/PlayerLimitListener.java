package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.messgener.GlobalMessager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PlayerLimitListener implements Listener , PluginMessageListener{
    private static final String BYPASS_FULL_JOIN = "base.serverfull.bypass";
    private final BasePlugin plugin;
    private static double a = 1.0;

    public PlayerLimitListener(BasePlugin plugin) {
        this.plugin = plugin;
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "Queue", this);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player player = GlobalMessager.getRandomPlayer();
            if(player != null){
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeUTF("playercount_get");
                data.writeUTF(player.getUniqueId().toString());
                player.sendPluginMessage(plugin, "BungeeCord", data.toByteArray());
            }
        }, 20, 40);
    }

    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if (s.equals("Queue")) {
            ByteArrayDataInput output = ByteStreams.newDataInput(bytes);
            String channel = output.readUTF();
            switch (channel) {
                case "playercount_return":
                {
                    output.readInt();
                    try{
                        a = output.readDouble();
                    } catch (Throwable throwable){
                        //Do nothing
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if(event.getResult() == PlayerLoginEvent.Result.KICK_FULL || event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST){
            event.allow();
        }
    }

    public static int getOnlineCount(){
        return (int) Math.floor(a * Bukkit.getOnlinePlayers().size());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event){
        String playerName = event.getName();
        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED){
            if(hasWhitelist(playerName)){
                event.allow();
            }
        }
        else if(Bukkit.hasWhitelist()){
            if(!hasWhitelist(playerName)){
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "Server is currently whitelisted");
            }
        }
        else if(getOnlineCount() >= Bukkit.getMaxPlayers()){
            if(!hasWhitelist(playerName)){
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "Server is currently full");
            }
        }
    }

    public boolean hasWhitelist(String playerName){
        for(OfflinePlayer offlinePlayer: Bukkit.getOperators()){
            if(offlinePlayer.getName().equalsIgnoreCase(playerName)){
                return true;
            }
        }
        for(OfflinePlayer offlinePlayer: Bukkit.getWhitelistedPlayers()){
            if(offlinePlayer.getName().equalsIgnoreCase(playerName)){
                return true;
            }
        }
        return false;
    }
}
