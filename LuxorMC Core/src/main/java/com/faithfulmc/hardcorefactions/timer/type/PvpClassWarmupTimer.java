package com.faithfulmc.hardcorefactions.timer.type;


import com.luxormc.event.EquipmentSetEvent;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.TimerRunnable;
import com.google.common.base.Preconditions;
import net.minecraft.util.com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class PvpClassWarmupTimer extends PlayerTimer implements Listener {
    protected final ConcurrentMap<Object, Object> classWarmups;
    private final HCF plugin;


    public PvpClassWarmupTimer(HCF plugin) {

        super("Class Warmup", TimeUnit.SECONDS.toMillis(10L), false);

        this.plugin = plugin;

        this.classWarmups = CacheBuilder.newBuilder().expireAfterWrite(this.defaultCooldown + 5000L, TimeUnit.MILLISECONDS).build().asMap();

        new BukkitRunnable() {

            public void run() {

                for (Player player : Bukkit.getOnlinePlayers())
                {
                    PvpClassWarmupTimer.this.attemptEquip(player);
                }
            }
        } .runTaskLater(plugin, 10L);

    }


    public String getScoreboardPrefix() {

        return ChatColor.AQUA + ChatColor.BOLD.toString();

    }


    public TimerRunnable clearCooldown(UUID playerUUID) {

        TimerRunnable runnable = super.clearCooldown(playerUUID);

        if (runnable != null) {

            this.classWarmups.remove(playerUUID);

            return runnable;

        }

        return null;

    }


    public void onExpire(UUID userUUID) {
                Player player = Bukkit.getPlayer(userUUID);

        if (player == null) {

            return;

        }

        String className = (String) this.classWarmups.remove(userUUID);

        Preconditions.checkNotNull(className, "Attempted to equip a class for %s, but nothing was added", new Object[]{player.getName()});

        this.plugin.getHcfClassManager().setEquippedClass(player, this.plugin.getHcfClassManager().getPvpClass(className));

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerQuitEvent event) {

        this.plugin.getHcfClassManager().setEquippedClass(event.getPlayer(), null);

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {

        attemptEquip(event.getPlayer());

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEquipmentSet(EquipmentSetEvent event) {

        HumanEntity humanEntity = event.getHumanEntity();

        if ((humanEntity instanceof Player)) {

            attemptEquip((Player) humanEntity);

        }

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        attemptEquip(event.getPlayer());
    }


    private void attemptEquip(Player player) {

        HCFClass equipped = this.plugin.getHcfClassManager().getEquippedClass(player);

        if (equipped != null) {

            if (equipped.isApplicableFor(player)) {

                return;

            }

            this.plugin.getHcfClassManager().setEquippedClass(player, null);

        }

        HCFClass warmupClass = null;

        String warmup = (String) this.classWarmups.get(player.getUniqueId());

        if (warmup != null) {

            warmupClass = this.plugin.getHcfClassManager().getPvpClass(warmup);

            if (!warmupClass.isApplicableFor(player)) {

                clearCooldown(player.getUniqueId());

            }

        }

        Collection<HCFClass> pvpClasses = this.plugin.getHcfClassManager().getPvpClasses();

        for (HCFClass pvpClass : pvpClasses) {

            if ((warmupClass != pvpClass) && (pvpClass.isApplicableFor(player))) {

                this.classWarmups.put(player.getUniqueId(), pvpClass.getName());

                setCooldown(player, player.getUniqueId(), pvpClass.getWarmupDelay(), false);

                break;

            }

        }

    }

}