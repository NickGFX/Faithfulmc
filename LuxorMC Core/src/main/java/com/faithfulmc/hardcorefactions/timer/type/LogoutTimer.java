package com.faithfulmc.hardcorefactions.timer.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.command.HubCommand;
import com.faithfulmc.hardcorefactions.logger.CombatLogListener;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.Timer;
import com.faithfulmc.hardcorefactions.timer.TimerRunnable;
import com.faithfulmc.hardcorefactions.timer.event.TimerExpireEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LogoutTimer extends PlayerTimer implements org.bukkit.event.Listener {
    public LogoutTimer() {
        super("Logout", ConfigurationService.ORIGINS ? TimeUnit.SECONDS.toMillis(60) : TimeUnit.SECONDS.toMillis(30L), false);
    }

    public String getScoreboardPrefix() {
        return ChatColor.RED.toString() + ChatColor.BOLD;
    }

    private void checkMovement(Player player, Location from, Location to) {
        if ((from.getBlockX() == to.getBlockX()) && (from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        if (getRemaining(player) > 0L) {
            player.sendMessage(ConfigurationService.RED + "You moved a block, " + getDisplayName() + ConfigurationService.RED + " timer cancelled.");
            clearCooldown(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        checkMovement(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        checkMovement(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        if (getRemaining(event.getPlayer().getUniqueId()) > 0L) {
            clearCooldown(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        if (getRemaining(event.getPlayer().getUniqueId()) > 0L) {
            clearCooldown(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        org.bukkit.entity.Entity entity = event.getEntity();
        if ((entity instanceof Player)) {
            Player player = (Player) entity;
            if (getRemaining(player) > 0L) {
                player.sendMessage(ConfigurationService.RED + "You were damaged, " + getDisplayName() + ConfigurationService.RED + " timer ended.");
                clearCooldown(player);
            }
        }
    }

    @Override
    public TimerRunnable create(UUID playerUUID, long duration) {
        return new LogoutTimerRunnable(playerUUID, this, duration);
    }

    public class LogoutTimerRunnable extends TimerRunnable{
        private final UUID playerUUID;
        private boolean sent = false;

        public LogoutTimerRunnable(UUID playerUUID, Timer timer, long duration) {
            super(playerUUID, timer, duration);
            this.playerUUID = playerUUID;
        }

        @Override
        public boolean check(long now) {
            if (isCancelled()) {
                return true;
            }
            long remaining = getRemaining(false, now);
            if (remaining <= 0) {
                TimerExpireEvent expireEvent = new TimerExpireEvent(playerUUID, LogoutTimer.this);
                Bukkit.getPluginManager().callEvent(expireEvent);
                return true;
            }
            else if(!sent && remaining <= 500){
                sent = true;
                Player player = Bukkit.getPlayer(playerUUID);
                if(player != null) {
                    CombatLogListener.addSafeDisconnect(player.getUniqueId());
                    HubCommand.sendToHub(player);
                    player.sendMessage(ChatColor.GREEN + "You have been safely logged out.");
                }
                else{
                    return true;
                }
            }
            return false;
        }
    }

    public void onExpire(java.util.UUID userUUID) {
        Player player = org.bukkit.Bukkit.getPlayer(userUUID);
        if (player == null) {
            return;
        }
        player.kickPlayer(ChatColor.GREEN + "You have been safely logged out.");
    }

    public void run(Player player) {
        long remainingMillis = getRemaining(player);
        if (remainingMillis > 0L) {
            player.sendMessage(getDisplayName() + ChatColor.BLUE + " timer is disconnecting you in " + ChatColor.BOLD + HCF.getRemaining(remainingMillis, true, false) + ChatColor.BLUE + '.');
        }
    }
}