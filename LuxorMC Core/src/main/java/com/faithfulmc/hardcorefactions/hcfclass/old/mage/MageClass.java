package com.faithfulmc.hardcorefactions.hcfclass.old.mage;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.chat.Lang;
import net.minecraft.util.gnu.trove.map.TObjectLongMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MageClass extends HCFClass implements Listener {
    public static final int HELD_EFFECT_DURATION_TICKS = 100;
    private static final long BUFF_COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(6L);
    private static final int TEAMMATE_NEARBY_RADIUS = 25;
    private static final long HELD_REAPPLY_TICKS = 20L;
    private static final String MARK = BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 8);
    private final Map<UUID, MageData> mageDataMap;
    private final Map<Material, MageEffect> mageEffects;
    private final MageRestorer mageRestorer;
    private final TObjectLongMap<UUID> msgCooldowns;

    public MageClass(HCF plugin) {
        super(plugin, "Drab", TimeUnit.SECONDS.toMillis(1L), ChatColor.LIGHT_PURPLE);
        this.mageDataMap = new HashMap<>();
        this.mageEffects = new EnumMap<>(Material.class);
        this.msgCooldowns = new TObjectLongHashMap<>();
        this.mageRestorer = new MageRestorer(plugin);
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        this.mageEffects.put(Material.SUGAR, new MageEffect(30, new PotionEffect(PotionEffectType.CONFUSION, 120, 2), new PotionEffect(PotionEffectType.CONFUSION, 40, 1)));
        this.mageEffects.put(Material.SPIDER_EYE, new MageEffect(30, new PotionEffect(PotionEffectType.WITHER, 100, 1), null));
        this.mageEffects.put(Material.MAGMA_CREAM, new MageEffect(30, new PotionEffect(PotionEffectType.BLINDNESS, 120, 0), new PotionEffect(PotionEffectType.BLINDNESS, 60, 0)));
    }

    public boolean onEquip(final Player player) {
        if (this.plugin.getTimerManager().pvpProtectionTimer.getRemaining(player) > 0L) {
            player.sendMessage(ConfigurationService.RED + "You cannot equip classes that effect PvP while you are protected from pvp" + ConfigurationService.GRAY + " (" + getName() + ")");
            return false;
        }
        if (!super.onEquip(player)) {
            return false;
        }
        MageData mageData = new MageData();
        this.mageDataMap.put(player.getUniqueId(), mageData);
        mageData.startEnergyTracking();

        mageData.heldTask = new BukkitRunnable() {
            int lastEnergy;

            public void run() {
                ItemStack held = player.getItemInHand();
                PlayerFaction playerFaction;
                if (held != null) {
                    MageEffect mageEffect = (MageEffect) MageClass.this.mageEffects.get(held.getType());
                    if ((mageEffect != null) && (!MageClass.this.plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone())) {
                        playerFaction = MageClass.this.plugin.getFactionManager().getPlayerFaction(player);
                        if (playerFaction != null) {
                            Collection<Entity> nearbyEntities = player.getNearbyEntities(25.0D, 25.0D, 25.0D);
                            for (Entity nearby : nearbyEntities) {
                                if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                    Player target = (Player) nearby;
                                    if (playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                    }
                                }
                            }
                        }
                    }
                }
                int energy = (int) MageClass.this.getEnergy(player);
                if ((energy != 0) && (energy != this.lastEnergy) && ((energy % 10 == 0) || (this.lastEnergy - energy - 1 > 0) || (energy == 100.0D))) {
                    this.lastEnergy = energy;
                    player.sendMessage(ChatColor.AQUA + MageClass.this.name + " Energy: " + ConfigurationService.YELLOW + energy);
                }
            }
        }

                .runTaskTimer(this.plugin, 0L, 20L);
        return true;
    }

    public void onUnequip(Player player) {
        super.onUnequip(player);
        clearMageData(player.getUniqueId());
    }

    private void clearMageData(UUID uuid) {
        MageData mageData = (MageData) this.mageDataMap.remove(uuid);
        if ((mageData != null) && (mageData.heldTask != null)) {
            mageData.heldTask.cancel();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearMageData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        clearMageData(event.getPlayer().getUniqueId());
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
            MageEffect mageEffect = (MageEffect) this.mageEffects.get(newStack.getType());
            if (mageEffect != null) {
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
            MageEffect mageEffect = (MageEffect) this.mageEffects.get(stack.getType());
            if ((mageEffect == null) || (mageEffect.clickable == null)) {
                return;
            }
            event.setUseItemInHand(Event.Result.DENY);
            Player player = event.getPlayer();
            MageData mageData = (MageData) this.mageDataMap.get(player.getUniqueId());
            if (mageData != null) {
                if (!canUseMageEffect(player, mageData, mageEffect, true)) {
                    return;
                }
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.setItemInHand(new ItemStack(Material.AIR, 1));
                }
                PlayerFaction playerFaction;
                if ((mageEffect != null) && (!this.plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone())) {
                    playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                    if ((playerFaction != null) && ((!mageEffect.clickable.getType().equals(PotionEffectType.WITHER)) || (!mageEffect.clickable.getType().equals(PotionEffectType.BLINDNESS)) || (!mageEffect.clickable.getType().equals(PotionEffectType.CONFUSION)))) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(25.0D, 25.0D, 25.0D);
                        for (Entity nearby : nearbyEntities) {
                            if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                Player target = (Player) nearby;
                                if (!playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                    this.mageRestorer.setRestoreEffect(target, mageEffect.clickable);
                                }
                            }
                        }
                    } else if ((playerFaction != null) && ((mageEffect.clickable.getType().equals(PotionEffectType.WITHER)) || (mageEffect.clickable.getType().equals(PotionEffectType.BLINDNESS)) || (mageEffect.clickable.getType().equals(PotionEffectType.CONFUSION)))) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(25.0D, 25.0D, 25.0D);
                        for (Entity nearby : nearbyEntities) {
                            if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                Player target = (Player) nearby;
                                if (!playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                    this.mageRestorer.setRestoreEffect(target, mageEffect.clickable);
                                }
                            }
                        }
                    } else if ((mageEffect.clickable.getType().equals(PotionEffectType.WITHER)) || (mageEffect.clickable.getType().equals(PotionEffectType.BLINDNESS)) || (mageEffect.clickable.getType().equals(PotionEffectType.CONFUSION))) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(25.0D, 25.0D, 25.0D);
                        for (Entity nearby : nearbyEntities) {
                            if (((nearby instanceof Player)) && (!player.equals(nearby))) {
                                Player target = (Player) nearby;
                                if ((this.plugin.getFactionManager().getPlayerFaction(event.getPlayer()) == null) || (!playerFaction.getMembers().containsKey(target.getUniqueId()))) {
                                    this.mageRestorer.setRestoreEffect(target, mageEffect.clickable);
                                }
                            }
                        }
                    }
                }
                this.mageRestorer.setRestoreEffect(player, mageEffect.clickable);
                double newEnergy = setEnergy(player, mageData.getEnergy() - mageEffect.energyCost);
                mageData.buffCooldown = (System.currentTimeMillis() + BUFF_COOLDOWN_MILLIS);
                player.sendMessage(ConfigurationService.YELLOW + "You have just used " + this.name + " buff " + ChatColor.AQUA + Lang.fromPotionEffectType(mageEffect.clickable.getType()) + ' ' + (mageEffect.clickable.getAmplifier() + 1) + ConfigurationService.YELLOW + " costing you " + ChatColor.BOLD + mageEffect.energyCost + ConfigurationService.YELLOW + " energy. " + "Your energy is now " + ChatColor.GREEN + newEnergy * 10.0D / 10.0D + ConfigurationService.YELLOW + '.');
            }
        }
    }

    private boolean canUseMageEffect(Player player, MageData mageData, MageEffect mageEffect, boolean sendFeedback) {
        String errorFeedback = null;
        double currentEnergy = mageData.getEnergy();
        if (mageEffect.energyCost > currentEnergy) {
            errorFeedback = ConfigurationService.RED + "You need at least " + ChatColor.BOLD + mageEffect.energyCost + ConfigurationService.RED + " energy to use this Mage buff, whilst you only have " + ChatColor.BOLD + currentEnergy + ConfigurationService.RED + '.';
        }
        long remaining = mageData.getRemainingBuffDelay();
        if (remaining > 0L) {
            errorFeedback = ConfigurationService.RED + "You still have a cooldown on this " + ChatColor.GREEN + ChatColor.BOLD + "Mage" + ConfigurationService.RED + " buff for another " + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + '.';
        }
        Faction factionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());
        if (factionAt.isSafezone()) {
            errorFeedback = ConfigurationService.RED + "You may not use Mage buffs in safe-zones.";
        }
        if ((sendFeedback) && (errorFeedback != null)) {
            player.sendMessage(errorFeedback);
        }
        return errorFeedback == null;
    }

    public boolean isApplicableFor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if ((helmet == null) || (helmet.getType() != Material.CHAINMAIL_HELMET)) {
            return false;
        }
        ItemStack chestplate = player.getInventory().getChestplate();
        if ((chestplate == null) || (chestplate.getType() != Material.CHAINMAIL_CHESTPLATE)) {
            return false;
        }
        ItemStack leggings = player.getInventory().getLeggings();
        if ((leggings == null) || (leggings.getType() != Material.CHAINMAIL_LEGGINGS)) {
            return false;
        }
        ItemStack boots = player.getInventory().getBoots();
        return (boots != null) && (boots.getType() == Material.CHAINMAIL_BOOTS);
    }

    public long getRemainingBuffDelay(Player player) {
        synchronized (this.mageDataMap) {
            MageData mageData = (MageData) this.mageDataMap.get(player.getUniqueId());
            return mageData == null ? 0L : mageData.getRemainingBuffDelay();
        }
    }

    public double getEnergy(Player player) {
        synchronized (this.mageDataMap) {
            MageData mageData = (MageData) this.mageDataMap.get(player.getUniqueId());
            return mageData == null ? 0.0D : mageData.getEnergy();
        }
    }

    public long getEnergyMillis(Player player) {
        synchronized (this.mageDataMap) {
            MageData mageData = (MageData) this.mageDataMap.get(player.getUniqueId());
            return mageData == null ? 0L : mageData.getEnergyMillis();
        }
    }

    public double setEnergy(Player player, double energy) {
        MageData mageData = (MageData) this.mageDataMap.get(player.getUniqueId());
        if (mageData == null) {
            return 0.0D;
        }
        mageData.setEnergy(energy);
        return mageData.getEnergy();
    }
}
