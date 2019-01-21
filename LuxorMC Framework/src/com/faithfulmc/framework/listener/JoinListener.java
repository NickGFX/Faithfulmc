package com.faithfulmc.framework.listener;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.server.FaithfulServer;
import com.faithfulmc.framework.server.ServerSettings;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.framework.user.event.UserLoadEvent;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.messgener.ServerAssignedEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Set;
import java.util.UUID;

public class JoinListener implements Listener {
    private final BasePlugin plugin;

    public JoinListener(BasePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUserLoad(UserLoadEvent event){
        BaseUser baseUser = event.getBaseUser();
        Player player = event.getPlayer();
        if(baseUser.getNickName() != null) player.setDisplayName(baseUser.getNickName());
        long now = System.currentTimeMillis();
        boolean newIP = baseUser.tryLoggingAddress(player.getAddress().getAddress().getHostAddress());
        if(player.hasPermission("base.server.staffjoin") && (now - baseUser.getLastSeen() > (1000 * 60 * 15) || newIP)) {
            String prefix = ChatColor.translateAlternateColorCodes('&', BasePlugin.getChat().getPlayerPrefix(player));
            BaseComponent[] message = new ComponentBuilder(player.getName())
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.fromLegacyText(
                                    prefix + player.getName() + "\n" +
                                            ChatColor.GRAY + "Server: " + ChatColor.WHITE + plugin.getGlobalMessager().getId() + "\n" +
                                            ChatColor.GRAY + "Opped: " + ChatColor.WHITE + player.isOp() + "\n" +
                                            ChatColor.GRAY + "New IP: " + ChatColor.WHITE + newIP + "\n" +
                                            ChatColor.GRAY + "Rank: " + ChatColor.WHITE + BasePlugin.getPermission().getPrimaryGroup(player)
                            )))
                    .color(newIP ? ChatColor.RED : ChatColor.GREEN).bold(true)
                    .append(" has logged in ").bold(false).color(ChatColor.GRAY).append(newIP ? "on a new IP" : "on an existing IP")
                    .create();
            for(Player other: Bukkit.getOnlinePlayers()){
                if(other.hasPermission("base.server.staffjoin.notify")){
                    other.spigot().sendMessage(message);
                }
            }
            plugin.getGlobalMessager().broadcastToOtherServers(player, message, "base.server.staffjoin.notify");
        }
        baseUser.setOnline(true);
        baseUser.setLastSeen(System.currentTimeMillis());
        baseUser.setGroup(BasePlugin.getPermission().getPrimaryGroup(player));
        baseUser.setName(player.getName());
        baseUser.setLastServer(plugin.getGlobalMessager().getId());
        if(baseUser.getFirstJoined() == null){
            baseUser.setFirstJoined(baseUser.getLastSeen());
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()){
                baseUser.updateVanishedState(player, baseUser.isVanished());
            }
        });
    }

    @EventHandler
    public void onServerAssigned(ServerAssignedEvent event){
        for(Player player: Bukkit.getOnlinePlayers()){
            BaseUser baseUser = plugin.getUserManager().getUser(player.getUniqueId());
            baseUser.setLastServer(event.getId());
        }
        if(!ServerSettings.HASNAME || !ServerSettings.NAME.equals(event.getId())){
            ServerSettings.setName(event.getId());
            if(BasePlugin.isMongo()){
                if(plugin.getFaithfulServer() != null) plugin.getFaithfulServer().close();
                plugin.setFaithfulServer(new FaithfulServer(plugin));
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        if(BasePlugin.PRACTICE) return;
        Player player = event.getPlayer();
        Location location = player.getLocation();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().setHidden(plugin.getPlayerHiddenManager().shouldBeHidden(location));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event){
        if(BasePlugin.PRACTICE) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            Player player = event.getPlayer();
            CraftPlayer craftPlayer = (CraftPlayer) player;
            EntityPlayer entityPlayer = craftPlayer.getHandle();
            if(!entityPlayer.isHidden() || Bukkit.spigot().getTPS()[0] > 12.0) { // Just hide everyone if the server is lagging?
                craftPlayer.getHandle().setHidden(plugin.getPlayerHiddenManager().shouldBeHidden(to));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerTeleportEvent event){
        if(BasePlugin.PRACTICE) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            Player player = event.getPlayer();
            CraftPlayer craftPlayer = (CraftPlayer) player;
            EntityPlayer entityPlayer = craftPlayer.getHandle();
            if(!entityPlayer.isHidden()) {
                craftPlayer.getHandle().setHidden(plugin.getPlayerHiddenManager().shouldBeHidden(to));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinLow(PlayerJoinEvent event) {
        plugin.getUserManager().joinTask(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event){
        plugin.getUserManager().quitTask(event.getPlayer());
    }
}
