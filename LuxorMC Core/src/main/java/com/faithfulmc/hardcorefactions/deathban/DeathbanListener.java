package com.faithfulmc.hardcorefactions.deathban;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.command.HubCommand;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class DeathbanListener implements Listener {

    private static final long LIFE_USE_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String LIFE_USE_DELAY_WORDS = DurationFormatUtils.formatDurationWords(LIFE_USE_DELAY_MILLIS, true, true);
    private static final String DEATH_BAN_BYPASS_PERMISSION = "hcf.deathban.bypass";
    private final ConcurrentMap lastAttemptedJoinMap;
    private final HCF plugin;

    public DeathbanListener(HCF plugin) {
        this.plugin = plugin;
        this.lastAttemptedJoinMap = CacheBuilder.newBuilder().expireAfterWrite(LIFE_USE_DELAY_MILLIS, TimeUnit.MILLISECONDS).build().asMap();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if(ConfigurationService.KIT_MAP && !HCF.getInstance().getEotwHandler().isEndOfTheWorld()){
            return;
        }
        FactionUser user = this.plugin.getUserManager().getUser(event.getUniqueId());
        Deathban deathban = user.getDeathban();
        if ((deathban == null) || (!deathban.isActive())) {
            return;
        }
        if (this.plugin.getEotwHandler().isEndOfTheWorld()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ConfigurationService.YELLOW + "Deathbanned for the entirety of the map due to EOTW.\nCome back for SOTW.");
        } else {
            UUID uuid = event.getUniqueId();
            FactionUser factionUser = plugin.getUserManager().getUser(uuid);
            int lives = factionUser.getLives();
            String formattedDuration = HCF.getRemaining(deathban.getRemaining(), true, false);
            String reason = deathban.getReason();
            String prefix = ConfigurationService.YELLOW + "You are currently death-banned" + (reason != null ? " for " + reason + ".\n" : ".") + ConfigurationService.GOLD + formattedDuration + " remaining.\n" + ConfigurationService.YELLOW + "You currently have " + ConfigurationService.GOLD + (lives <= 0 ? "no" : Integer.valueOf(lives)) + ConfigurationService.YELLOW + " lives.";
            if (lives > 0) {
                long millis = System.currentTimeMillis();
                Long lastAttemptedJoinMillis = (Long) this.lastAttemptedJoinMap.get(uuid);
                if ((lastAttemptedJoinMillis != null) && (lastAttemptedJoinMillis - System.currentTimeMillis() < LIFE_USE_DELAY_MILLIS)) {

                    plugin.getTimerManager().pvpProtectionTimer.setCooldown(null, uuid, TimeUnit.MINUTES.toMillis(30) + 100, true);
                    plugin.getTimerManager().pvpProtectionTimer.setPaused(null, uuid, true);
                    this.lastAttemptedJoinMap.remove(uuid);
                    user.removeDeathban();
                    factionUser.setLives(lives - 1);
                    event.allow();
                } else {
                    this.lastAttemptedJoinMap.put(uuid, millis + LIFE_USE_DELAY_MILLIS);
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, prefix + ConfigurationService.GOLD + "\n\n" + "You are using a life to connect to the server");
                }
                return;
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ConfigurationService.YELLOW + "Still deathbanned for " + formattedDuration + ": " + ConfigurationService.YELLOW + deathban.getReason() + ConfigurationService.YELLOW + '.' + "\nYou can purchase lives at " + ConfigurationService.GOLD + ConfigurationService.STORE + ConfigurationService.YELLOW + " to bypass this.");
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(ConfigurationService.KIT_MAP && !HCF.getInstance().getEotwHandler().isEndOfTheWorld()){
            return;
        }
        final Player player = event.getEntity();
        if (player.hasPermission(DEATH_BAN_BYPASS_PERMISSION)) {
            return;
        }

        Deathban deathban = plugin.getDeathbanManager().applyDeathBan(player, event.getDeathMessage());
        String durationString = HCF.getRemaining(deathban.getRemaining(), true, false);
        boolean eotw = plugin.getEotwHandler().isEndOfTheWorld();
        String message = eotw ? ConfigurationService.RED + "Deathbanned for the entirety of the map due to EOTW." : ConfigurationService.RED + "Deathbanned for " + durationString + " \n\n" + ConfigurationService.YELLOW + deathban.getReason();
        player.sendMessage(new String[50]);
        player.sendMessage(ChatColor.YELLOW + "You have been deathbanned for " + (eotw ? "the rest of the map" : ChatColor.GRAY + "(" + durationString + ")") + ChatColor.YELLOW + " and sent to the hub");
        HubCommand.sendToHub(player);
        new BukkitRunnable() {
            public void run (){
                if(player.isOnline()) {
                    player.kickPlayer(message);
                }
            }
        }.runTaskLater(this.plugin, 5L);
    }
}