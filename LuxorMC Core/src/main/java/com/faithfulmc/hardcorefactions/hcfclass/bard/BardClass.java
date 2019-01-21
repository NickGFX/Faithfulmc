package com.faithfulmc.hardcorefactions.hcfclass.bard;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.util.chat.Lang;
import net.minecraft.util.gnu.trove.map.TObjectLongMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BardClass extends HCFClass implements Runnable {
    private static final long BUFF_COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(6L);
    private static final int TEAMMATE_NEARBY_RADIUS = 20;
    private static final long HELD_REAPPLY_TICKS = 4;
    private final Map<UUID, BardData> bardDataMap;
    private final Map<Material, BardEffect> bardEffects;
    private final BardRestorer bardRestorer;
    private final TObjectLongMap<UUID> msgCooldowns;

    public BardClass(HCF plugin) {
        super(plugin, "Bard", ChatColor.GOLD);
        this.bardDataMap = new ConcurrentHashMap<>();
        this.bardEffects = new EnumMap<>(Material.class);
        this.msgCooldowns = new TObjectLongHashMap<>();
        this.bardRestorer = new BardRestorer(plugin);
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        this.bardEffects.put(Material.SUGAR, new BardEffect(ConfigurationService.ORIGINS ? 20 : 35, new PotionEffect(PotionEffectType.SPEED, 120, 2), new PotionEffect(PotionEffectType.SPEED, 120, 1)));
        this.bardEffects.put(Material.BLAZE_POWDER, new BardEffect(ConfigurationService.ORIGINS ? 45 : 40, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0)));
        this.bardEffects.put(Material.IRON_INGOT, new BardEffect(ConfigurationService.ORIGINS ? 40 : 30, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, ConfigurationService.ORIGINS ? 1 : 2), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0)));
        this.bardEffects.put(Material.GHAST_TEAR, new BardEffect(ConfigurationService.ORIGINS ? 35 : 25, new PotionEffect(PotionEffectType.REGENERATION, 60, ConfigurationService.ORIGINS ? 3 : 2), new PotionEffect(PotionEffectType.REGENERATION, 120, 0)));
        this.bardEffects.put(Material.FEATHER, new BardEffect(ConfigurationService.ORIGINS ? 25 : 30, new PotionEffect(PotionEffectType.JUMP, 120, 6), new PotionEffect(PotionEffectType.JUMP, 120, 1)));
        this.bardEffects.put(Material.SPIDER_EYE, new BardEffect(ConfigurationService.ORIGINS ? 25 : 50, new PotionEffect(PotionEffectType.WITHER, ConfigurationService.ORIGINS ? 80 : 120, ConfigurationService.ORIGINS ? 2 : 1), null));
        this.bardEffects.put(Material.MAGMA_CREAM, new BardEffect(20, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 900, 0), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0)));
        plugin.getServer().getScheduler().runTaskTimer(plugin, this, HELD_REAPPLY_TICKS, HELD_REAPPLY_TICKS);
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getHcfClassManager().hasClassEquipped(player, this)) {
                ItemStack held = player.getItemInHand();
                BardEffect bardEffect;
                PlayerFaction playerFaction;
                if (held != null) {
                    bardEffect = bardEffects.get(held.getType());
                    if (bardEffect != null && bardEffect.heldable != null && !BardClass.this.plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
                        playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                        if (playerFaction != null) {
                            Collection<Entity> nearbyEntities = player.getNearbyEntities(TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS);
                            for (Entity nearby : nearbyEntities) {
                                if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                    Player target = (Player) nearby;
                                    if (playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                        bardRestorer.setRestoreEffect(target, bardEffect.heldable);
                                    }
                                } else if (bardEffect.heldable.getType() == PotionEffectType.JUMP) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 120, 1));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean onEquip(final Player player) {
        if (!ConfigurationService.KIT_MAP && this.plugin.getTimerManager().pvpProtectionTimer.getRemaining(player) > 0L) {
            player.sendMessage(ConfigurationService.RED + "You cannot equip classes that effect PvP while you are protected from pvp" + ConfigurationService.GRAY + " (" + getName() + ")");
            return false;
        }
        if (!super.onEquip(player)) {
            return false;
        }
        BardData bardData = new BardData();
        this.bardDataMap.put(player.getUniqueId(), bardData);
        bardData.startEnergyTracking();
        return true;
    }

    public void onUnequip(Player player) {
        super.onUnequip(player);
        clearBardData(player.getUniqueId());
    }

    private void clearBardData(UUID uuid) {
        this.bardDataMap.remove(uuid);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearBardData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        clearBardData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        HCFClass equipped = this.plugin.getHcfClassManager().getEquippedClass(player);
        if ((equipped == null) || (!equipped.equals(this))) {
            return;
        }
        UUID uuid = player.getUniqueId();
        long lastMessage = this.msgCooldowns.get(uuid);
        long millis = System.currentTimeMillis();
        if ((lastMessage != this.msgCooldowns.getNoEntryValue()) && (lastMessage - millis > 0L)) {
            return;
        }
        ItemStack newStack = player.getInventory().getItem(event.getNewSlot());
        if (newStack != null) {
            BardEffect bardEffect = this.bardEffects.get(newStack.getType());
            if (bardEffect != null) {
                this.msgCooldowns.put(uuid, millis + 1500L);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_AIR) || ((!event.isCancelled()) && (action == Action.RIGHT_CLICK_BLOCK))) {
            ItemStack stack = event.getItem();
            BardEffect bardEffect = this.bardEffects.get(stack.getType());
            if ((bardEffect == null) || (bardEffect.clickable == null)) {
                return;
            }
            event.setUseItemInHand(Event.Result.DENY);
            Player player = event.getPlayer();
            BardData bardData = this.bardDataMap.get(player.getUniqueId());
            if (bardData != null) {
                if (!canUseBardEffect(player, bardData, bardEffect, true)) {
                    return;
                }
                if(ConfigurationService.ORIGINS) {
                    plugin.getTimerManager().spawnTagTimer.setCooldown(player, player.getUniqueId());
                }
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.setItemInHand(new ItemStack(Material.AIR, 1));
                }
                if ((bardEffect != null) && (!this.plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone())) {
                    PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                    if ((playerFaction != null) && (!bardEffect.clickable.getType().equals(PotionEffectType.WITHER))) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS);
                        for (Entity nearby : nearbyEntities) {
                            if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                Player target = (Player) nearby;
                                if (playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                    this.bardRestorer.setRestoreEffect(target, bardEffect.clickable);
                                }
                            }
                        }
                    } else if ((playerFaction != null) && (bardEffect.clickable.getType().equals(PotionEffectType.WITHER))) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS);
                        for (Entity nearby : nearbyEntities) {
                            if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                Player target = (Player) nearby;
                                if (!playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                    this.bardRestorer.setRestoreEffect(target, bardEffect.clickable);
                                }
                            }
                        }
                    } else if (bardEffect.clickable.getType().equals(PotionEffectType.WITHER)) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS);
                        for (Entity nearby : nearbyEntities) {
                            if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                Player target = (Player) nearby;
                                this.bardRestorer.setRestoreEffect(target, bardEffect.clickable);
                            }
                        }
                    }
                }
                this.bardRestorer.setRestoreEffect(player, bardEffect.clickable);
                double newEnergy = setEnergy(player, bardData.getEnergy() - bardEffect.energyCost);
                bardData.buffCooldown = (System.currentTimeMillis() + BUFF_COOLDOWN_MILLIS);
                player.sendMessage(ConfigurationService.YELLOW + "You have just used " + this.name + " buff " + ChatColor.AQUA + Lang.fromPotionEffectType(bardEffect.clickable.getType()) + ' ' + (bardEffect.clickable.getAmplifier() + 1) + ConfigurationService.YELLOW + " costing you " + ChatColor.BOLD + bardEffect.energyCost + ConfigurationService.YELLOW + " energy. " + "Your energy is now " + ChatColor.GREEN + newEnergy * 10.0D / 10.0D + ConfigurationService.YELLOW + '.');
            }
        }
    }

    private boolean canUseBardEffect(Player player, BardData bardData, BardEffect bardEffect, boolean sendFeedback) {
        String errorFeedback = null;
        double currentEnergy = bardData.getEnergy();
        if (bardEffect.energyCost > currentEnergy) {
            errorFeedback = ConfigurationService.RED + "You need at least " + ChatColor.BOLD + bardEffect.energyCost + ConfigurationService.RED + " energy to use this Bard buff, whilst you only have " + ChatColor.BOLD + currentEnergy + ConfigurationService.RED + '.';
        }
        long remaining = bardData.getRemainingBuffDelay();
        if (remaining > 0L) {
            errorFeedback = ConfigurationService.RED + "You still have a cooldown on this " + ChatColor.GREEN + ChatColor.BOLD + "Bard" + ConfigurationService.RED + " buff for another " + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + '.';
        }
        Faction factionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());
        if (factionAt.isSafezone()) {
            errorFeedback = ConfigurationService.RED + "You may not use Bard buffs in safe-zones.";
        }
        if ((sendFeedback) && (errorFeedback != null)) {
            player.sendMessage(errorFeedback);
        }
        return errorFeedback == null;
    }

    public boolean isApplicableFor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if ((helmet == null) || (helmet.getType() != Material.GOLD_HELMET)) {
            return false;
        }
        ItemStack chestplate = player.getInventory().getChestplate();
        if ((chestplate == null) || (chestplate.getType() != Material.GOLD_CHESTPLATE)) {
            return false;
        }
        ItemStack leggings = player.getInventory().getLeggings();
        if ((leggings == null) || (leggings.getType() != Material.GOLD_LEGGINGS)) {
            return false;
        }
        ItemStack boots = player.getInventory().getBoots();
        return (boots != null) && (boots.getType() == Material.GOLD_BOOTS);
    }

    public long getRemainingBuffDelay(Player player) {
        BardData bardData = this.bardDataMap.get(player.getUniqueId());
        return bardData == null ? 0L : bardData.getRemainingBuffDelay();
    }

    public long getRemainingBuffDelay(Player player, long now) {
        BardData bardData = this.bardDataMap.get(player.getUniqueId());
        return bardData == null ? 0L : bardData.getRemainingBuffDelay(now);
    }

    public double getEnergy(Player player) {
        BardData bardData = this.bardDataMap.get(player.getUniqueId());
        return bardData == null ? 0.0D : bardData.getEnergy();
    }

    public long getEnergyMillis(Player player) {
        BardData bardData = this.bardDataMap.get(player.getUniqueId());
        return bardData == null ? 0L : bardData.getEnergyMillis();
    }

    public long getEnergyMillis(Player player, long now) {
        BardData bardData = this.bardDataMap.get(player.getUniqueId());
        return bardData == null ? 0L : bardData.getEnergyMillis(now);
    }

    public double setEnergy(Player player, double energy) {
        BardData bardData = this.bardDataMap.get(player.getUniqueId());
        if (bardData == null) {
            return 0.0D;
        }
        bardData.setEnergy(energy);
        return bardData.getEnergy();
    }
}
