package com.faithfulmc.hardcorefactions.deathban;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.PersistableLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BasicDeathbanManager{
    public static final long MAX_DEATHBAN_TIME = TimeUnit.HOURS.toMillis(8L);

    protected final HCF plugin;

    public BasicDeathbanManager(HCF plugin) {
        this.plugin = plugin;
    }

    public double getDeathBanMultiplier(final Player player) {
        return ConfigurationService.DEFAULT_DEATHBAN_DURATION;
    }

    public Deathban applyDeathBan(final Player player, final String reason) {
        final Location location = player.getLocation();
        final Faction factionAt = plugin.getFactionManager().getFactionAt(location);
        long duration = ConfigurationService.DEFAULT_DEATHBAN_DURATION;
        if(ConfigurationService.ORIGINS){
            if(player.hasPermission("Custom")){
                duration = TimeUnit.MINUTES.toMillis(50);
            } else if (player.hasPermission("Faithful")) {
                duration = TimeUnit.MINUTES.toMillis(60);
            } else if (player.hasPermission("Platinum")) {
                duration = TimeUnit.MINUTES.toMillis(70);
            } else if (player.hasPermission("Sapphire")) {
                duration = TimeUnit.MINUTES.toMillis(80);
            } else if (player.hasPermission("Ruby")) {
                duration = TimeUnit.MINUTES.toMillis(90);
            } else if (player.hasPermission("Emerald")) {
                duration = TimeUnit.MINUTES.toMillis(100);
            } else if (player.hasPermission("Diamond")) {
                duration = TimeUnit.MINUTES.toMillis(105);
            } else if (player.hasPermission("Gold")) {
                duration = TimeUnit.MINUTES.toMillis(110);
            } else if (player.hasPermission("Iron")) {
                duration = TimeUnit.MINUTES.toMillis(115);
            }
        }
        else {
            if(player.hasPermission("Custom")){
                duration = TimeUnit.SECONDS.toMillis(30);
            } else if (player.hasPermission("Faithful")) {
                duration = TimeUnit.MINUTES.toMillis(1);
            } else if (player.hasPermission("Platinum")) {
                duration = TimeUnit.MINUTES.toMillis(5);
            } else if (player.hasPermission("Sapphire")) {
                duration = TimeUnit.MINUTES.toMillis(10);
            } else if (player.hasPermission("Ruby")) {
                duration = TimeUnit.MINUTES.toMillis(30);
            } else if (player.hasPermission("Emerald")) {
                duration = TimeUnit.MINUTES.toMillis(45);
            } else if (player.hasPermission("Diamond")) {
                duration = TimeUnit.MINUTES.toMillis(60);
            } else if (player.hasPermission("Gold")) {
                duration = TimeUnit.MINUTES.toMillis(75);
            } else if (player.hasPermission("Iron")) {
                duration = TimeUnit.MINUTES.toMillis(90);
            }
            if (!factionAt.isDeathban() || location.getWorld().getEnvironment() == World.Environment.NETHER) {
                duration /= 2L;
            }
        }
        duration *= ConfigurationService.DEATHBAN_MULTIPLIER;
        return applyDeathBan(player.getUniqueId(), new Deathban(reason, Math.min(MAX_DEATHBAN_TIME, duration), new PersistableLocation(location)));
    }

    public Deathban applyDeathBan(final UUID uuid, final Deathban deathban) {
        this.plugin.getUserManager().getUser(uuid).setDeathban(deathban);
        return deathban;
    }
}
