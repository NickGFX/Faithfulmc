package com.faithfulmc.hardcorefactions.util;

import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Cooldowns {
    private static Map<String, Map> cooldown = new ConcurrentHashMap<>();

    public static void createCooldown(String cooldownName, long defaultTime) {
        cooldown.put(cooldownName, CacheBuilder.newBuilder().expireAfterWrite(defaultTime, TimeUnit.SECONDS).build().asMap());
    }

    public static Map getCooldownMap(String cooldownName) {
        return cooldown.get(cooldownName);
    }

    public static void addCooldown(String cooldownName, Player player, int seconds) {
        long next = System.currentTimeMillis() + seconds * 1000L;
        getCooldownMap(cooldownName).put(player.getUniqueId(), next);
    }

    public static boolean isOnCooldown(String cooldownName, Player player, long now) {
        return cooldown.containsKey(cooldownName) && getCooldownMap(cooldownName).containsKey(player.getUniqueId()) && now <= (Long) getCooldownMap(cooldownName).get(player.getUniqueId());
    }

    public static boolean isOnCooldown(String cooldownName, Player player) {
        return cooldown.containsKey(cooldownName) && getCooldownMap(cooldownName).containsKey(player.getUniqueId()) && System.currentTimeMillis() <= (Long) getCooldownMap(cooldownName).get(player.getUniqueId());
    }

    public static int getCooldownForPlayerInt(String cooldownName, Player player, long now) {
        return (int) (((Long) getCooldownMap(cooldownName).get(player.getUniqueId())) - now) / 1000;
    }

    public static int getCooldownForPlayerInt(String cooldownName, Player player) {
        return (int) (((Long) getCooldownMap(cooldownName).get(player.getUniqueId())) - System.currentTimeMillis()) / 1000;
    }

    public static long getCooldownForPlayerLong(String cooldownName, Player player) {
        return (int) (((Long) getCooldownMap(cooldownName).get(player.getUniqueId())) - System.currentTimeMillis());
    }

    public static long getCooldownForPlayerLong(String cooldownName, Player player, long now) {
        return ((long) getCooldownMap(cooldownName).get(player.getUniqueId())) - now;
    }

    public static void removeCooldown(String cooldownName, Player player) {
        getCooldownMap(cooldownName).remove(player.getUniqueId());
    }
}
