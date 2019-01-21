package com.faithfulmc.hardcorefactions.timer.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.timer.GlobalTimer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class KeySaleTimer extends GlobalTimer implements Runnable{
    public KeySaleTimer() {
        super("KEY-SALE", TimeUnit.HOURS.toMillis(3));
    }

    private boolean last = false;

    public void run() {
        long remainingMillis = getRemaining();
        remainingMillis -= remainingMillis % 1000;
        if(remainingMillis > 0 && !last && remainingMillis % (1000 * 60 * 15) == 0) {
            last = true;
            Bukkit.broadcastMessage(ConfigurationService.YELLOW + "The " + getDisplayName() + ChatColor.YELLOW + " will end in " + ConfigurationService.RED + DurationFormatUtils.formatDurationWords(remainingMillis, true, true));
        } else {
            last = false;
        }
    }



    @Override
    public String getScoreboardPrefix() {
        return ChatColor.GREEN + ChatColor.BOLD.toString();
    }
}
