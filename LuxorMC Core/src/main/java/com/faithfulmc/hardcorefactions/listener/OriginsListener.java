package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.TimeUnit;

public class OriginsListener implements Listener {
    private final HCF plugin;

    public OriginsListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof EnderDragon){
            Player killer = ((EnderDragon) entity).getKiller();
            if(killer != null){
                PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(killer);
                String killedBy = playerFaction == null? killer.getName() : playerFaction.getName();
                Bukkit.broadcastMessage(ConfigurationService.GOLD + ChatColor.BOLD.toString() + "Enderdragon " + ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + ConfigurationService.YELLOW.toString() + " The " + ChatColor.RED + "Enderdragon" + ConfigurationService.YELLOW + " has been slain by " + ChatColor.AQUA + killedBy);
                for(Player player: Bukkit.getOnlinePlayers()){
                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1f, 1f);
                }
                if(playerFaction != null) {
                    playerFaction.setPoints(playerFaction.getPoints() + 10);
                }
                event.setDroppedExp((int) Math.round(event.getDroppedExp() * 1.25));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){
            Faction factionAt = plugin.getFactionManager().getFactionAt(event.getTo());
            if(!factionAt.isSafezone()) {
                plugin.getTimerManager().spawnTagTimer.setCooldown(player, player.getUniqueId(), TimeUnit.SECONDS.toMillis(8), false);
            }
        }
    }
}
