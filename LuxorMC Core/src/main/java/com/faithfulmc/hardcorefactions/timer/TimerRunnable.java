package com.faithfulmc.hardcorefactions.timer;


import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.event.TimerExpireEvent;
import org.bukkit.Bukkit;

import java.util.UUID;


public class TimerRunnable {
    private final UUID represented;
    private final Timer timer;
    private long expiryMillis;
    private long pauseMillis;
    private boolean cancelled = false;


    public TimerRunnable(Timer timer, long duration) {
        this.represented = null;
        this.timer = timer;
        setRemaining(duration);

        HCF hcf = HCF.getInstance();
        hcf.getTimerManager().getTimerRunnableList().add(this);

    }


    public TimerRunnable(UUID playerUUID, Timer timer, long duration) {

        this.represented = playerUUID;

        this.timer = timer;

        setRemaining(duration);

        HCF hcf = HCF.getInstance();
        hcf.getTimerManager().getTimerRunnableList().add(this);

    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Timer getTimer() {

        return this.timer;

    }


    public long getRemaining() {

        return getRemaining(false);

    }

    public long getRemaining(long now) {

        return getRemaining(false, now);

    }


    public void setRemaining(long remaining) {

        setExpiryMillis(remaining);

    }


    public long getRemaining(boolean ignorePaused) {

        if ((!ignorePaused) && (this.pauseMillis != 0L)) {

            return this.pauseMillis;

        }

        return this.expiryMillis - System.currentTimeMillis();

    }

    public long getRemaining(boolean ignorePaused, long now) {

        if ((!ignorePaused) && (this.pauseMillis != 0L)) {

            return this.pauseMillis;

        }
        return this.expiryMillis - now;

    }


    public long getExpiryMillis() {

        return this.expiryMillis;

    }


    private void setExpiryMillis(long remainingMillis) {

        long expiryMillis = System.currentTimeMillis() + remainingMillis;

        if (expiryMillis == this.expiryMillis) {

            return;

        }

        this.expiryMillis = expiryMillis;

    }

    public boolean check(long now) {
        if (cancelled) {
            return true;
        }
        if (getRemaining(false, now) <= 0) {
            TimerExpireEvent expireEvent = new TimerExpireEvent(TimerRunnable.this.represented, TimerRunnable.this.timer);
            Bukkit.getPluginManager().callEvent(expireEvent);
            return true;
        }
        return false;
    }


    public long getPauseMillis() {

        return this.pauseMillis;

    }


    public void setPauseMillis(long pauseMillis) {

        this.pauseMillis = pauseMillis;

    }


    public boolean isPaused() {

        return this.pauseMillis != 0L;

    }


    public void setPaused(boolean paused) {

        if (paused == isPaused()) {

            return;

        }

        if (paused) {

            this.pauseMillis = getRemaining(true);

            cancel();

        } else {

            setExpiryMillis(this.pauseMillis);

            this.pauseMillis = 0L;

        }

    }


    public void cancel() {

        cancelled = true;

    }

}