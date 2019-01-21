package com.faithfulmc.hardcorefactions.events.tracker;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;


public class CitadelTracker implements EventTracker {

    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.MINUTES.toMillis(1);

    public static long DEFAULT_CAP_MILLIS = TimeUnit.MINUTES.toMillis(30L);
    private final HCF plugin;


    public CitadelTracker(HCF plugin) {

        this.plugin = plugin;

    }

    public static String PREFIX = EventType.CITADEL.getPrefix();


    public EventType getEventType() {

        return EventType.KOTH;

    }


    public void tick(EventTimer eventTimer, EventFaction eventFaction) {

        CaptureZone captureZone = ((KothFaction) eventFaction).getCaptureZone();

        if (captureZone == null) {
            return;
        }

        if (captureZone.getCappingPlayer() != null && (captureZone.getCuboid() == null || !captureZone.getCuboid().contains(captureZone.getCappingPlayer()) || captureZone.getCappingPlayer().isDead() || !captureZone.getCappingPlayer().isValid())) {
            captureZone.setCappingPlayer(null);
        }

        long remainingMillis = captureZone.getRemainingCaptureMillis();

        if (remainingMillis <= 0L) {

            this.plugin.getTimerManager().eventTimer.finishEvent(captureZone.getCappingPlayer());

            eventTimer.clearCooldown();

            return;

        }

        if (remainingMillis == captureZone.getDefaultCaptureMillis()) {

            return;

        }

        int remainingSeconds = (int) (remainingMillis / 1000L);

        if ((remainingSeconds > 0) && (remainingSeconds % 60 == 0)) {
            Bukkit.broadcastMessage(PREFIX  + "Someone is controlling " + ChatColor.LIGHT_PURPLE + captureZone.getDisplayName() + ConfigurationService.YELLOW + ". " + (ConfigurationService.LUXOR ? ChatColor.WHITE : ConfigurationService.RED) + '(' + DateTimeFormats.KOTH_FORMAT.format(remainingMillis) + ')');
        }

    }


    public void onContest(EventFaction eventFaction, EventTimer eventTimer) {
        Bukkit.broadcastMessage(PREFIX + ChatColor.LIGHT_PURPLE + eventFaction.getName() + ConfigurationService.YELLOW + " can now be contested. " + (ConfigurationService.LUXOR ? ChatColor.WHITE : ConfigurationService.RED) + '(' + DateTimeFormats.KOTH_FORMAT.format(eventTimer.getRemaining()) + ')');
    }


    public boolean onControlTake(Player player, CaptureZone captureZone) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight() || player.isFlying() || player.isDead() || (!ConfigurationService.KIT_MAP && plugin.getTimerManager().pvpProtectionTimer.getRemaining(player) > 0)) {
            return false;
        }

        if (this.plugin.getFactionManager().getPlayerFaction(player.getUniqueId()) == null) {
            player.sendMessage(PREFIX + "You must be in a faction to capture for Citadel.");
            return false;
        }

        player.sendMessage(PREFIX + "You are now in control of " + ChatColor.LIGHT_PURPLE + captureZone.getDisplayName() + ConfigurationService.YELLOW + '.');

        return true;

    }


    public boolean onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        player.sendMessage(PREFIX + "You are no longer in control of " + ChatColor.LIGHT_PURPLE + captureZone.getDisplayName() + ConfigurationService.YELLOW + '.');
        long remainingMillis = captureZone.getRemainingCaptureMillis();
        if ((remainingMillis > 0L) && (captureZone.getDefaultCaptureMillis() - remainingMillis > MINIMUM_CONTROL_TIME_ANNOUNCE)) {
            Bukkit.broadcastMessage(PREFIX + ChatColor.LIGHT_PURPLE + player.getName() + ConfigurationService.YELLOW + " has lost control of " + ChatColor.LIGHT_PURPLE + captureZone.getDisplayName() + ConfigurationService.YELLOW + '.' + (ConfigurationService.LUXOR ? ChatColor.WHITE : ConfigurationService.RED) + " (" + DateTimeFormats.KOTH_FORMAT.format(captureZone.getRemainingCaptureMillis()) + ')');
        }
        return true;

    }


    public void stopTiming() {
    }

}