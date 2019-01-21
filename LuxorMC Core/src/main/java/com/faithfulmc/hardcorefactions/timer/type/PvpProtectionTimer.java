package com.faithfulmc.hardcorefactions.timer.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.LandMap;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.event.FactionClaimChangedEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerClaimEnterEvent;
import com.faithfulmc.hardcorefactions.faction.event.cause.ClaimChangeCause;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.faction.type.RoadFaction;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.TimerRunnable;
import com.faithfulmc.hardcorefactions.timer.event.TimerClearEvent;
import com.faithfulmc.hardcorefactions.visualise.VisualType;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.base.Optional;
import net.minecraft.util.com.google.common.cache.CacheBuilder;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class PvpProtectionTimer extends PlayerTimer implements Listener {
    private static final String PVP_COMMAND = "/pvp enable";
    private static final long ITEM_PICKUP_DELAY = TimeUnit.SECONDS.toMillis(45);
    private static final long ITEM_PICKUP_MESSAGE_DELAY = 1750;
    private static final String ITEM_PICKUP_MESSAGE_META_KEY = "pickupMessageDelay";
    private final ConcurrentMap<Object, Object> itemUUIDPickupDelays;
    private final HCF plugin;

    public PvpProtectionTimer(HCF plugin) {
        super("PVP Timer", TimeUnit.MINUTES.toMillis(ConfigurationService.PVPTIMER_MINUTES));
        this.plugin = plugin;
        this.itemUUIDPickupDelays = CacheBuilder.newBuilder().expireAfterWrite(ITEM_PICKUP_DELAY + 5000, TimeUnit.MILLISECONDS).build().asMap();
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.GREEN.toString() + ChatColor.BOLD;
    }

    @Override
    public void onExpire(UUID userUUID) {
        Player player = Bukkit.getPlayer(userUUID);
        if (player == null) {
            return;
        }
        if (this.getRemaining(player) <= 0) {
            plugin.getVisualiseHandler().clearVisualType(player, VisualType.CLAIM_BORDER, true);
            player.sendMessage(ConfigurationService.RED.toString() + "You no longer have " + this.getDisplayName() + ConfigurationService.RED + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStop(TimerClearEvent event) {
        Optional<UUID> optionalUserUUID;
        if (event.getTimer().equals(this) && (optionalUserUUID = event.getUserUUID()).isPresent()) {
            this.onExpire(optionalUserUUID.get());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClaimChange(final FactionClaimChangedEvent event) {
        if (event.getCause() != ClaimChangeCause.CLAIM) {
            return;
        }
        final Collection<Claim> claims = event.getAffectedClaims();
        for (final Claim claim : claims) {
            final Collection<Player> players = claim.getPlayers();
            for (final Player player : players) {
                if (this.getRemaining(player) > 0L) {
                    Location location = LandMap.getNearestSafePosition(player, player.getLocation(), 30, true);
                    if(location != null && player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)){
                        player.sendMessage(ConfigurationService.RED + "Land was claimed where you were standing. As you still have your " + getDisplayName() + ConfigurationService.RED + " you were teleported away.");
                    }
                    else{
                        clearCooldown(player);
                        player.sendMessage(ConfigurationService.RED + "Land was claimed where you were standing. So your " + getDisplayName() + ConfigurationService.RED + " was cleared.");
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (setCooldown(player, player.getUniqueId(), defaultCooldown + 100, true)) {
            player.sendMessage(ConfigurationService.YELLOW + "Once you leave Spawn your 30 minutes of " + this.getDisplayName() + ConfigurationService.YELLOW + " will start.");
        }
        if (this.plugin.getFactionManager().getFactionAt(event.getRespawnLocation()).isSafezone()) {
            setPaused(player, player.getUniqueId(), true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        Location location = player.getLocation();
        Iterator iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            this.itemUUIDPickupDelays.put(world.dropItemNaturally(location, (ItemStack) iterator.next()).getUniqueId(), System.currentTimeMillis() + ITEM_PICKUP_DELAY);
            iterator.remove();
        }
        this.clearCooldown(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        long remaining = this.getRemaining(player);
        if (remaining > 0) {
            event.setCancelled(true);
            player.sendMessage(ConfigurationService.RED + "You cannot empty buckets as your " + this.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        long remaining = this.getRemaining(player);
        if (remaining > 0) {
            event.setCancelled(true);
            player.sendMessage(ConfigurationService.RED + "You cannot ignite blocks as your " + this.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        long remaining = this.getRemaining(player);
        if (remaining > 0) {
            UUID itemUUID = event.getItem().getUniqueId();
            Long delay = (Long) this.itemUUIDPickupDelays.get(itemUUID);
            if (delay == null) {
                return;
            }
            long millis = System.currentTimeMillis();
            if (delay - millis > 0) {
                event.setCancelled(true);
                MetadataValue value = player.getMetadata(ITEM_PICKUP_MESSAGE_META_KEY).iterator().hasNext() ?  player.getMetadata(ITEM_PICKUP_MESSAGE_META_KEY).iterator().next() : null;
                if (value != null && value.asLong() - millis <= 0) {
                    player.setMetadata(ITEM_PICKUP_MESSAGE_META_KEY,  new FixedMetadataValue( this.plugin,  (millis + ITEM_PICKUP_MESSAGE_DELAY)));
                    player.sendMessage( ConfigurationService.RED + "You cannot pick this item up for another " +  ChatColor.BOLD + DurationFormatUtils.formatDurationWords( remaining,  true,  true) +  ConfigurationService.RED + " as your " + this.getDisplayName() +  ConfigurationService.RED + " timer is active [" +  ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");
                }
            } else {
                this.itemUUIDPickupDelays.remove(itemUUID);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TimerRunnable runnable = this.cooldowns.get(player.getUniqueId());
        if (runnable != null && runnable.getRemaining() > 0) {
            runnable.setPaused(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerSpawnLocation(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            if (!this.plugin.getEotwHandler().isEndOfTheWorld()) {
                setCooldown(player, player.getUniqueId(), defaultCooldown + 100, true);
                player.sendMessage(ConfigurationService.GRAY + "Once you leave Spawn your 30 minutes of " + this.getName() + (Object) ConfigurationService.GRAY + " will start.");
                if (this.plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
                    setPaused(player, player.getUniqueId(), true);
                }
            }
        } else if (this.isPaused(player) && this.getRemaining(player) > 0 && !this.plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
            this.setPaused(player, player.getUniqueId(), false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerClaimEnterMonitor(PlayerClaimEnterEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            this.clearCooldown(player);
            return;
        }
        Faction toFaction = event.getToFaction();
        Faction fromFaction = event.getFromFaction();
        if (fromFaction.isSafezone() && !toFaction.isSafezone()) {
            if (this.getRemaining(player) > 0) {
                this.setPaused(player, player.getUniqueId(), false);
                player.sendMessage(ConfigurationService.YELLOW + "Your " + this.getDisplayName() + ConfigurationService.YELLOW + " timer is no longer paused.");
            }
        } else if (!fromFaction.isSafezone() && toFaction.isSafezone() && this.getRemaining(player) > 0) {
            player.sendMessage(ConfigurationService.YELLOW + "Your " + this.getDisplayName() + ConfigurationService.YELLOW + " timer is now paused.");
            this.setPaused(player, player.getUniqueId(), true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerClaimEnter(PlayerClaimEnterEvent event) {
        long remaining;
        Player player = event.getPlayer();
        Faction toFaction = event.getToFaction();
        if (toFaction instanceof ClaimableFaction && (remaining = this.getRemaining(player)) > 0) {
            PlayerFaction playerFaction;
            if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT && toFaction instanceof PlayerFaction && (playerFaction = this.plugin.getFactionManager().getPlayerFaction(player)) != null && playerFaction.equals(toFaction)) {
                player.sendMessage(ConfigurationService.YELLOW + "You have entered your own claim, therefore your " + this.getDisplayName() + (Object) ConfigurationService.YELLOW + " has been removed.");
                this.clearCooldown(player);
                return;
            }
            if (!toFaction.isSafezone() && !(toFaction instanceof RoadFaction)) {
                event.setCancelled(true);
                player.sendMessage(ConfigurationService.RED + "You cannot enter " + toFaction.getDisplayName(player) + ConfigurationService.RED + " whilst your " + this.getDisplayName() + (Object) ConfigurationService.RED + " timer is active [" + (Object) ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + (Object) ConfigurationService.RED + " remaining]. " + "Use '" + ConfigurationService.GOLD + PVP_COMMAND + ConfigurationService.RED + "' to remove this timer.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player attacker = BukkitUtils.getFinalAttacker(event, true);
            if (attacker == null) {
                return;
            }
            Player player = (Player) entity;
            long remaining = this.getRemaining(player);
            if (remaining > 0) {
                event.setCancelled(true);
                attacker.sendMessage(ConfigurationService.RED + player.getName() + " has their " + this.getDisplayName() + ConfigurationService.RED + " timer for another " + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + '.');
                return;
            }
            remaining = this.getRemaining(attacker);
            if (remaining > 0) {
                event.setCancelled(true);
                attacker.sendMessage(ConfigurationService.RED + "You cannot attack players whilst your " + this.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]. Use '" + ConfigurationService.GOLD + PVP_COMMAND + ConfigurationService.RED + "' to allow pvp.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            long remaining = this.getRemaining(player);
            if (remaining > 0) {
                if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.LAVA  || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (potion.getShooter() instanceof Player && BukkitUtils.isDebuff(potion)) {
            for (LivingEntity livingEntity : event.getAffectedEntities()) {
                if (!(livingEntity instanceof Player) || this.getRemaining((Player) livingEntity) <= 0) {
                    continue;
                }
                event.setIntensity(livingEntity, 0.0);
            }
        }
    }

    @Override
    public long getRemaining(UUID playerUUID) {
        return this.plugin.getEotwHandler().isEndOfTheWorld() ? 0 : super.getRemaining(playerUUID);
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite) {
        return !this.plugin.getEotwHandler().isEndOfTheWorld() && super.setCooldown(player, playerUUID, duration, overwrite);
    }
}