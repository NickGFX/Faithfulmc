package com.faithfulmc.hardcorefactions.events.eotw;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.faction.type.SpawnFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class EotwHandler {
    public static final long EOTW_WARMUP_WAIT_MILLIS;
    public static final int EOTW_WARMUP_WAIT_SECONDS;
    private static final long EOTW_CAPPABLE_WAIT;

    static {
        EOTW_WARMUP_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
        EOTW_WARMUP_WAIT_SECONDS = (int) (EotwHandler.EOTW_WARMUP_WAIT_MILLIS / 1000L);
        EOTW_CAPPABLE_WAIT = TimeUnit.MINUTES.toMillis(5);
    }

    private final HCF plugin;
    private EotwRunnable runnable;

    public EotwHandler(final HCF plugin) {
        this.plugin = plugin;
    }

    public EotwRunnable getRunnable() {
        return this.runnable;
    }

    public boolean isEndOfTheWorld() {
        return this.isEndOfTheWorld(true);
    }

    public void setEndOfTheWorld(final boolean yes) {
        if (yes == this.isEndOfTheWorld(false)) {
            return;
        }
        if (yes) {
            this.runnable = new EotwRunnable();
            this.runnable.runTaskTimer(this.plugin, 1L, 100L);
        } else if (this.runnable != null) {;
            this.runnable.cancel();
            this.runnable = null;
        }
    }

    public boolean isEndOfTheWorld(final boolean ignoreWarmup) {
        return this.runnable != null && (!ignoreWarmup || this.runnable.getElapsedMilliseconds() > 0L);
    }

    public static class EotwRunnable extends BukkitRunnable {
        private boolean hasInformedStarted;
        private boolean hasInformedCapable;
        private long startStamp;
        private KothFaction EOTWFACTION;

        public EotwRunnable() {
            this.startStamp = System.currentTimeMillis() + EotwHandler.EOTW_WARMUP_WAIT_MILLIS;
        }

        public long getTimeUntilStarting() {
            final long difference = System.currentTimeMillis() - this.startStamp;
            return (difference > 0L) ? 0L : Math.abs(difference);
        }

        public long getTimeUntilStarting(long now) {
            final long difference = now - this.startStamp;
            return (difference > 0L) ? 0L : Math.abs(difference);
        }

        public long getTimeUntilCappable() {
            return EotwHandler.EOTW_CAPPABLE_WAIT - this.getElapsedMilliseconds();
        }

        public long getElapsedMilliseconds() {
            return System.currentTimeMillis() - this.startStamp;
        }

        public void run() {
            long elapsedMillis = this.getElapsedMilliseconds();
            int elapsedSeconds = (int) Math.round(elapsedMillis / 1000.0);
            if (!this.hasInformedStarted && elapsedSeconds >= 0) {
                Faction eotwFaction = HCF.getInstance().getFactionManager().getFaction("EOTW");
                if(eotwFaction == null){
                    eotwFaction = new KothFaction("EOTW");
                }
                else if(!(eotwFaction instanceof KothFaction)){
                    HCF.getInstance().getFactionManager().removeFaction(eotwFaction, Bukkit.getConsoleSender());
                    eotwFaction = new KothFaction("EOTW");
                }
                Command.broadcastCommandMessage(Bukkit.getConsoleSender(), ConfigurationService.YELLOW + "Created EOTW faction");
                EOTWFACTION = (KothFaction) eotwFaction;
                for (Faction faction : HCF.getInstance().getFactionManager().getFactions()) {
                    if (faction instanceof PlayerFaction) {
                        PlayerFaction playerFaction = (PlayerFaction) faction;
                        playerFaction.setDeathsUntilRaidable(-9999999);
                    }
                    else if(faction instanceof SpawnFaction){
                        for(Claim claim: new ArrayList<>(((SpawnFaction) faction).getClaims())){
                            ((SpawnFaction) faction).removeClaim(claim, Bukkit.getConsoleSender());
                            if(claim.getWorld().getEnvironment() == World.Environment.NORMAL){
                                try {
                                    EOTWFACTION.addClaim(new Claim(EOTWFACTION, claim), Bukkit.getConsoleSender());
                                    Command.broadcastCommandMessage(Bukkit.getConsoleSender(), ConfigurationService.YELLOW + "EOTW Faction claim has been setup");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                Command.broadcastCommandMessage(Bukkit.getConsoleSender(), ConfigurationService.YELLOW + "All factions have been set raidable");


                for(FactionUser factionUser: HCF.getInstance().getUserManager().getUsers().values()) {
                    factionUser.removeDeathban();
                }
                Command.broadcastCommandMessage(Bukkit.getConsoleSender(), ConfigurationService.YELLOW + "All death-bans have been cleared.");

                this.hasInformedStarted = true;
                Bukkit.broadcastMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "EOTW" + ConfigurationService.YELLOW + " has begun.");
            }
            else if(!this.hasInformedCapable && elapsedMillis >= EOTW_CAPPABLE_WAIT){
                if(EOTWFACTION != null){
                    HCF.getInstance().getTimerManager().eventTimer.tryContesting(EOTWFACTION, Bukkit.getConsoleSender());
                }
                hasInformedCapable = true;
            }
            if (elapsedMillis < 0L && elapsedMillis >= -EotwHandler.EOTW_WARMUP_WAIT_MILLIS) {
                Bukkit.broadcastMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "EOTW" + ConfigurationService.YELLOW + " is starting in " + HCF.getRemaining(Math.abs(elapsedMillis), true, false) + '.');
            }
        }
    }
}