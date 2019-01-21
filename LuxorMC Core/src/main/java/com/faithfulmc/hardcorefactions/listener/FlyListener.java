package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.framework.command.SimpleCommandManager;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.PlayerClaimEnterEvent;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.SpawnFaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class FlyListener implements Listener, Runnable{
    private final HCF plugin;
    private static final String STAFF_PERMISSION = "base.command.fly";
    private final Set<UUID> uuidSet = new HashSet<>();

    public FlyListener(HCF plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this, 10, 10);
    }

    public boolean isStaff(Player player){
        return player.getGameMode() == GameMode.CREATIVE || player.isOp() || player.hasPermission(STAFF_PERMISSION);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        if(player.getAllowFlight() && !isStaff(player)){
            player.sendMessage(ChatColor.YELLOW + "Your flight was automatically disabled");
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.2f);
        }
    }

    @EventHandler
    public void onPlayerClaimEnter(PlayerClaimEnterEvent event){
        Player player = event.getPlayer();
        Faction fromFaction = event.getFromFaction();
        if(fromFaction instanceof SpawnFaction && player.getAllowFlight() && !isStaff(player)){
            player.sendMessage(ChatColor.YELLOW + "Your flight was automatically disabled");
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.2f);
            uuidSet.add(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL){
            Player player = (Player) entity;
            if(uuidSet.remove(player.getUniqueId())){
                player.sendMessage(ChatColor.YELLOW + "You were saved from fall damage by your flight mode");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        String command = event.getMessage();
        if(command.startsWith("/")){
            command = command.substring(1, command.length()).toLowerCase();
            String[] cmd = command.split(" ");
            if(cmd[0].equals("fly") && !isStaff(player)){
                if(player.hasPermission("hcf.command.donatorfly")) {
                    Faction faction = plugin.getFactionManager().getFactionAt(player.getLocation());
                    if (!faction.isSafezone()) {
                        player.sendMessage(ChatColor.RED + "You must be in spawn to enable flight");
                    } else if (player.getAllowFlight()) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        player.setFlySpeed(0.2f);
                        player.sendMessage(ChatColor.YELLOW + "Your flight has been " + ChatColor.RED + "disabled");
                        uuidSet.add(player.getUniqueId());
                    } else {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        player.setFlySpeed(0.4f);
                        player.sendMessage(ChatColor.YELLOW + "Your flight has been " + ChatColor.GREEN + "enabled");
                        uuidSet.remove(player.getUniqueId());
                    }
                }
                else{
                    player.sendMessage(SimpleCommandManager.PERMISSION_MESSAGE);
                }
                event.setCancelled(true);
            }
        }
    }

    public void run(){
        Iterator<UUID> iterator = uuidSet.iterator();
        while (iterator.hasNext()){
            UUID uuid = iterator.next();
            Player player = Bukkit.getPlayer(uuid);
            if(player.isOnGround() || player.getAllowFlight()){
                iterator.remove();
            }
        }
    }
}
