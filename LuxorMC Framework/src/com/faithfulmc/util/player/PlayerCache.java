package com.faithfulmc.util.player;

import com.faithfulmc.util.InventoryUtils;
import com.google.common.collect.Sets;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public class PlayerCache {
    public UUID playerUUID;
    public Location location;
    public GameMode gameMode;
    public boolean allowFlight;
    public boolean flying;
    public ItemStack[] inventory;
    public ItemStack[] armor;
    public double health;
    public int food;
    public int level;
    public float xp;
    public int fireTicks;
    public Collection<PotionEffect> potions;

    public PlayerCache(final Player player) {
        this.playerUUID = player.getUniqueId();
        this.location = player.getLocation();
        this.gameMode = player.getGameMode();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();
        this.inventory = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.health = ((Damageable) player).getHealth();
        this.food = player.getFoodLevel();
        this.level = player.getLevel();
        this.xp = player.getExp();
        this.fireTicks = player.getFireTicks();
        this.potions = (Collection<PotionEffect>) Sets.newHashSet((Iterable) player.getActivePotionEffects());
    }

    public PlayerCache(final Location location, final GameMode gamemode, final boolean allowFlight, final boolean flying, final ItemStack[] inventory, final ItemStack[] armor, final double health, final int food, final int level, final float xp, final int fireTicks, final Collection<PotionEffect> potions) {
        this.location = location;
        this.gameMode = gamemode;
        this.allowFlight = allowFlight;
        this.flying = flying;
        this.inventory = InventoryUtils.deepClone(inventory);
        this.armor = InventoryUtils.deepClone(armor);
        this.health = health;
        this.food = food;
        this.level = level;
        this.xp = xp;
        this.fireTicks = fireTicks;
        this.potions = potions;
    }

    public void apply(final Player player) {
        PlayerUtil.wipe(player);
        if (this.location != null) {
            player.teleport(this.location);
        }
        player.setGameMode(this.gameMode);
        player.setAllowFlight(this.allowFlight);
        player.setFlying(this.flying);
        player.getInventory().setContents(this.inventory);
        player.getInventory().setArmorContents(this.armor);
        player.setHealth(this.health);
        player.setFoodLevel(this.food);
        player.setLevel(this.level);
        player.setExp(this.xp);
        player.setFireTicks(this.fireTicks);
        player.getActivePotionEffects().clear();
        for (final PotionEffect effect : this.potions) {
            player.addPotionEffect(effect);
        }
    }
}
