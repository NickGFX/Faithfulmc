package com.faithfulmc.hardcorefactions.events.tracker;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.faction.ConquestFaction;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.event.FactionRemoveEvent;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConquestTracker implements EventTracker, Listener {
    public static final long DEFAULT_CAP_MILLIS;
    public static final String PREFIX = EventType.CONQUEST.getPrefix();
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE;
    private static final Comparator<Map.Entry<PlayerFaction, Integer>> POINTS_COMPARATOR;

    static {
        MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(20L);
        DEFAULT_CAP_MILLIS = TimeUnit.SECONDS.toMillis(30L);
        POINTS_COMPARATOR = ((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
    }
    
    private final Map<PlayerFaction, Integer> factionPointsMap;
    private final HCF plugin;

    public ConquestTracker(final HCF ins) {
        this.factionPointsMap = Collections.synchronizedMap(new LinkedHashMap<PlayerFaction, Integer>());
        this.plugin = ins;
        Bukkit.getPluginManager().registerEvents((Listener) this, (Plugin) this.plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(final FactionRemoveEvent event) {
        final Faction faction = event.getFaction();
        if (faction instanceof PlayerFaction) {
            synchronized (this.factionPointsMap) {
                this.factionPointsMap.remove(faction);
            }
        }
    }

    public Map<PlayerFaction, Integer> getFactionPointsMap() {
        return (Map<PlayerFaction, Integer>) ImmutableMap.copyOf( this.factionPointsMap);
    }

    public int getPoints(final PlayerFaction faction) {
        synchronized (this.factionPointsMap) {
            return MoreObjects.firstNonNull(this.factionPointsMap.get(faction), 0);
        }
    }

    public int setPoints(final PlayerFaction faction, final int amount) {
        if (amount < 0) {
            return amount;
        }
        synchronized (this.factionPointsMap) {
            this.factionPointsMap.put(faction, amount);
            final List<Map.Entry<PlayerFaction, Integer>> entries = Ordering.from(ConquestTracker.POINTS_COMPARATOR).sortedCopy(this.factionPointsMap.entrySet());
            this.factionPointsMap.clear();
            for (final Map.Entry<PlayerFaction, Integer> entry : entries) {
                this.factionPointsMap.put(entry.getKey(), entry.getValue());
            }
        }
        return amount;
    }

    public int takePoints(final PlayerFaction faction, final int amount) {
        return this.setPoints(faction, this.getPoints(faction) - amount);
    }

    public int addPoints(final PlayerFaction faction, final int amount) {
        return this.setPoints(faction, this.getPoints(faction) + amount);
    }

    @Override
    public EventType getEventType() {
        return EventType.CONQUEST;
    }

    @Override
    public void tick(final EventTimer eventTimer, final EventFaction eventFaction) {
        final ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
        final List<CaptureZone> captureZones = conquestFaction.getCaptureZones();
        for (final CaptureZone captureZone : captureZones) {
            final Player cappingPlayer = captureZone.getCappingPlayer();
            if (cappingPlayer == null) {
                continue;
            }
            if (!captureZone.getCuboid().contains(cappingPlayer) || cappingPlayer.isDead() || !cappingPlayer.isValid()) {
                captureZone.setCappingPlayer(null);
                continue;
            }
            final long remainingMillis = captureZone.getRemainingCaptureMillis();
            if (remainingMillis <= 0L) {
                final UUID uuid = cappingPlayer.getUniqueId();
                final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(uuid);
                if (playerFaction != null) {
                    int points = captureZone == conquestFaction.getMain() ? 2 : 1;
                    final int newPoints = this.addPoints(playerFaction, points);
                    if (newPoints >= ConfigurationService.CONQUEST_REQUIRED_WIN_POINTS) {
                        synchronized (this.factionPointsMap) {
                            this.factionPointsMap.clear();
                        }
                        this.plugin.getTimerManager().eventTimer.finishEvent(cappingPlayer);
                        return;
                    }
                    captureZone.setRemainingCaptureMillis(captureZone.getDefaultCaptureMillis());
                    Bukkit.broadcastMessage(PREFIX + ChatColor.LIGHT_PURPLE + playerFaction.getName() + ConfigurationService.YELLOW + " gained " + points + " " + (points == 1 ? "point" : "points") + " for capturing " + captureZone.getDisplayName() + ConfigurationService.YELLOW + ". " + ChatColor.AQUA + '(' + newPoints + '/' + ConfigurationService.CONQUEST_REQUIRED_WIN_POINTS + ')');
                }
                return;
            }
            final int remainingSeconds = (int) Math.round((remainingMillis + 250) / 1000.0);
            if (remainingSeconds % 5 != 0) {
                continue;
            }
            cappingPlayer.sendMessage(PREFIX + ConfigurationService.YELLOW + "Attempting to control " + ChatColor.RESET + captureZone.getDisplayName() + ConfigurationService.YELLOW + ". " + ConfigurationService.GOLD + '(' + remainingSeconds + "s)");
        }
    }

    @Override
    public void onContest(final EventFaction eventFaction, final EventTimer eventTimer) {
        Bukkit.broadcastMessage(PREFIX + ConfigurationService.YELLOW + eventFaction.getName() + " can now be contested.");
    }

    @Override
    public boolean onControlTake(final Player player, final CaptureZone captureZone) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight() || player.isFlying() || player.isDead() || (!ConfigurationService.KIT_MAP && plugin.getTimerManager().pvpProtectionTimer.getRemaining(player) > 0)) {
            return false;
        }
        if (this.plugin.getFactionManager().getPlayerFaction(player.getUniqueId()) == null) {
            player.sendMessage(PREFIX + "You must be in a faction to capture for Conquest.");
            return false;
        }
        return true;
    }

    @Override
    public boolean onControlLoss(final Player player, final CaptureZone captureZone, final EventFaction eventFaction) {
        final long remainingMillis = captureZone.getRemainingCaptureMillis();
        if (remainingMillis > 0L && captureZone.getDefaultCaptureMillis() - remainingMillis > ConquestTracker.MINIMUM_CONTROL_TIME_ANNOUNCE) {
            Bukkit.broadcastMessage(PREFIX + ChatColor.LIGHT_PURPLE + player.getName() + ConfigurationService.YELLOW + " was knocked off " + captureZone.getDisplayName() + ConfigurationService.YELLOW + '.');
        }
        return true;
    }

    @Override
    public void stopTiming() {
        synchronized (this.factionPointsMap) {
            this.factionPointsMap.clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Faction currentEventFac = this.plugin.getTimerManager().eventTimer.getEventFaction();
        if (currentEventFac instanceof ConquestFaction) {
            final Player player = event.getEntity();
            final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction != null) {
                final int oldPoints = this.getPoints(playerFaction);
                if (oldPoints == 0) {
                    return;
                }
                if (this.getPoints(playerFaction) <= (ConfigurationService.KIT_MAP ? 5 : 20)) {
                    this.setPoints(playerFaction, 0);
                } else {
                    this.takePoints(playerFaction, (ConfigurationService.KIT_MAP ? 5 : 20));
                }
                event.setDeathMessage(PREFIX + ChatColor.LIGHT_PURPLE + playerFaction.getName() + ConfigurationService.YELLOW + " lost " + ChatColor.BOLD + Math.min(ConfigurationService.KIT_MAP ? 5 : 20, oldPoints) + ConfigurationService.YELLOW + " points because " + player.getName() + " died." + ChatColor.AQUA + " (" + this.getPoints(playerFaction) + '/' + ConfigurationService.CONQUEST_REQUIRED_WIN_POINTS + ')');
            }
        }
    }
}