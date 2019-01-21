package com.faithfulmc.hardcorefactions.timer;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.timer.type.*;
import com.faithfulmc.util.Config;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TimerManager implements Listener, Runnable {
    public final SpawnTagTimer spawnTagTimer;
    public final TeleportTimer teleportTimer;
    public final EventTimer eventTimer;
    public final ArcherTimer archerTimer;
    private final Set<Timer> timers;
    private final JavaPlugin plugin;
    private final List<TimerRunnable> timerRunnableList = new ArrayList<>();
    public LogoutTimer logoutTimer;
    public EnderPearlTimer enderPearlTimer;
    public GoppleTimer goppleTimer;
    public CrappleTimer crappleTimer;
    public PvpProtectionTimer pvpProtectionTimer;
    public PvpClassWarmupTimer pvpClassWarmupTimer;
    public StuckTimer stuckTimer;
    public SOTWTimer sotw;
    public KeySaleTimer keySale;
    public SaleTimer sale;
    private Config config;

    public TimerManager(HCF plugin) {
        this.timers = new HashSet<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        if (!ConfigurationService.KIT_MAP) {
            this.registerTimer(this.pvpProtectionTimer = new PvpProtectionTimer(plugin));
        }
        this.registerTimer(this.archerTimer = new ArcherTimer(plugin));
        this.registerTimer(this.enderPearlTimer = new EnderPearlTimer(plugin));
        this.registerTimer(this.logoutTimer = new LogoutTimer());
        this.registerTimer(this.sotw = new SOTWTimer());
        this.registerTimer(this.goppleTimer = new GoppleTimer(plugin));
        registerTimer(crappleTimer = new CrappleTimer(plugin));
        this.registerTimer(this.stuckTimer = new StuckTimer());
        this.registerTimer(this.spawnTagTimer = new SpawnTagTimer(plugin));
        this.registerTimer(this.teleportTimer = new TeleportTimer(plugin));
        this.registerTimer(this.eventTimer = new EventTimer(plugin));
        this.registerTimer(this.pvpClassWarmupTimer = new PvpClassWarmupTimer(plugin));
        registerTimer(keySale = new KeySaleTimer());
        registerTimer(sale = new SaleTimer());
        plugin.getServer().getScheduler().runTaskTimer(plugin, keySale, 15, 15);
        plugin.getServer().getScheduler().runTaskTimer(plugin, sale, 15, 15);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this, 4, 4);
    }

    public Collection<Timer> getTimers() {
        return this.timers;
    }

    public void registerTimer(Timer timer) {
        this.timers.add(timer);
        if (timer instanceof Listener) {
            this.plugin.getServer().getPluginManager().registerEvents((Listener) timer, this.plugin);
        }
    }

    public void unregisterTimer(Timer timer) {
        this.timers.remove(timer);
    }

    public void reloadTimerData() {
        this.config = new Config(this.plugin, "timers");
        for (Timer timer : this.timers) {
            timer.load(this.config);
        }
    }

    public void saveTimerData() {
        for (Timer timer : this.timers) {
            timer.save(this.config);
        }
        this.config.save();
    }

    public void run() {
        long now = System.currentTimeMillis();
        Iterator<TimerRunnable> iterator = timerRunnableList.iterator();
        while (iterator.hasNext()) {
            TimerRunnable next = iterator.next();
            if (next.check(now)) {
                iterator.remove();
            }
        }
    }

    public List<TimerRunnable> getTimerRunnableList() {
        return timerRunnableList;
    }
}