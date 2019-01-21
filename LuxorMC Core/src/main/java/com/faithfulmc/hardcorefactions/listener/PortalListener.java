package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import net.minecraft.util.gnu.trove.map.TObjectLongMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PortalListener implements Listener {
    private static final long PORTAL_MESSAGE_DELAY_THRESHOLD = 2500L;
    private final TObjectLongMap<UUID> messageDelays;
    private final HCF plugin;

    public PortalListener(HCF plugin) {
        this.messageDelays = new TObjectLongHashMap<>();
        this.plugin = plugin;
    }

    @Deprecated
    public Location getEndExit() {
        return ConfigurationService.KIT_MAP ? new Location(Bukkit.getWorld("world"), -0.5, 72, -200, 0, 0) : new Location(Bukkit.getWorld("world"), 0.5, 70, 200.5, 180, 0);
    }

    @Deprecated
    public Location getEndSpawn() {
        return ConfigurationService.KIT_MAP ? new Location(Bukkit.getWorld("world_the_end"), 88.5, 61.5, -40.5, 90, 0) : new Location(Bukkit.getWorld("world_the_end"), 730.5, 66, -88.5, 45, 0);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityPortal(EntityPortalEvent event) {
        if ((event.getEntity() instanceof EnderDragon)) {
            event.setCancelled(true);
        }
        else if(event.getEntity() instanceof EnderPearl){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPortalEvent(EntityPortalEvent event){
        Location from = event.getFrom();
        if(from.getWorld().getEnvironment() == World.Environment.NETHER && plugin.getFactionManager().getFactionAt(from).isSafezone()){
            event.getPortalTravelAgent().setCanCreatePortal(false);
            event.setTo(Bukkit.getWorld("world").getSpawnLocation().clone().add(0.5, 0.5, 0.5));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Block block = event.getTo().getBlock();
        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.getBlockY() != to.getBlockY()) {
            if (block != null && from.getWorld().getEnvironment() == World.Environment.THE_END) {
                Material type = block.getType();
                if (type == Material.STATIONARY_WATER || type == Material.WATER) {
                    new BukkitRunnable() {
                        public void run() {
                            player.setFallDistance(0);
                            player.setNoDamageTicks(0);
                            player.teleport(plugin.getEndExit());
                        }
                    }.runTask(plugin);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        onPlayerMove(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }
        World toWorld = event.getTo().getWorld();
        World fromWorld = event.getFrom().getWorld();
        if ((toWorld != null) && (toWorld.getEnvironment() == World.Environment.THE_END) && (ConfigurationService.KIT_MAP || !plugin.getFactionManager().getFactionAt(event.getFrom()).isSafezone())) {
            event.useTravelAgent(false);
            event.setTo(plugin.getEndSpawn());
            event.setCancelled(false);
        }
        if(fromWorld != null && fromWorld.getEnvironment() == World.Environment.THE_END){
            event.setTo(plugin.getEndExit());
            event.useTravelAgent(false);
            event.setCancelled(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();
        World to = player.getWorld();
        if ((from.getEnvironment() != World.Environment.THE_END) && (to.getEnvironment() == World.Environment.THE_END) && (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))) {
            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPortalEnter(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }
        Location to = event.getTo();
        World toWorld = to.getWorld();
        if (toWorld == null) {
            return;
        }
        if (toWorld.getEnvironment() == World.Environment.THE_END) {
            Player player = event.getPlayer();
            PlayerTimer timer = this.plugin.getTimerManager().spawnTagTimer;
            long remaining;
            if ((remaining = timer.getRemaining(player)) > 0L) {
                message(player, ConfigurationService.RED + "You cannot enter the End whilst your " + timer.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");
                event.setCancelled(true);
                return;
            }
            if (!ConfigurationService.KIT_MAP) {
                timer = this.plugin.getTimerManager().pvpProtectionTimer;
                if ((remaining = timer.getRemaining(player)) > 0L) {
                    message(player, ConfigurationService.RED + "You cannot enter the End whilst your " + timer.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void message(Player player, String message) {
        long last = this.messageDelays.get(player.getUniqueId());
        long millis = System.currentTimeMillis();
        if ((last != this.messageDelays.getNoEntryValue()) && (last + PORTAL_MESSAGE_DELAY_THRESHOLD - millis > 0L)) {
            return;
        }
        this.messageDelays.put(player.getUniqueId(), millis);
        player.sendMessage(message);
    }
}
