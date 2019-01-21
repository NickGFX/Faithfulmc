package com.faithfulmc.hardcorefactions.hcfclass.old;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.hardcorefactions.hcfclass.event.PvpClassUnequipEvent;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AssassinClass extends HCFClass implements Listener {
    public HashMap<String, Integer> firstAssassinEffects;
    public HashMap<Integer, PotionEffect> modes;
    private PlayerTimer pt;

    public AssassinClass(HCF plugin) {
        super(plugin, "Reaper", TimeUnit.SECONDS.toMillis(10L), ConfigurationService.GRAY);
        this.firstAssassinEffects = new HashMap<>();
        this.modes = new HashMap<>();
        this.passiveEffects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
    }

    @EventHandler
    public void onUnEquip(PvpClassUnequipEvent e) {
        Player p = e.getPlayer();
        for (Player on : Bukkit.getOnlinePlayers()) {
            if ((!on.canSee(p)) && (!on.hasPermission("base.command.vanish"))) {
                on.showPlayer(p);
            }
        }
        this.firstAssassinEffects.remove(p);
    }

    @EventHandler
    public void onDamageSelf(EntityDamageEvent e) {
        if ((e.getEntity() instanceof Player)) {
            Player p = (Player) e.getEntity();
            if ((this.plugin.getHcfClassManager().getEquippedClass(p) == null) || (!this.plugin.getHcfClassManager().getEquippedClass(p).equals(this))) {
                return;
            }
            if ((this.firstAssassinEffects.containsKey(p.getName())) && (((Integer) this.firstAssassinEffects.get(p.getName())).intValue() == 1)) {
                for (Entity entity : p.getNearbyEntities(20.0D, 20.0D, 20.0D)) {
                    if ((entity instanceof Player)) {
                        Player players = (Player) entity;
                        players.sendMessage(ConfigurationService.YELLOW + "A reaper has taken damage in stealth mode near you: " + ConfigurationService.GRAY + ChatColor.ITALIC + "(20 x 20)");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHitOtherPlayers(EntityDamageByEntityEvent e) {
        if (((e.getDamager() instanceof Player)) && ((e.getEntity() instanceof Player))) {
            Player p = (Player) e.getDamager();
            Player ent = (Player) e.getEntity();
            if ((this.firstAssassinEffects.containsKey(p.getName())) && (((Integer) this.firstAssassinEffects.get(p.getName())).intValue() == 1)) {
                afterFiveSeconds(p, true);
            }
        }
    }

    @EventHandler
    public void onClickItem(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_AIR)) {
            HCFClass equipped = this.plugin.getHcfClassManager().getEquippedClass(p);
            if ((equipped == null) || (!equipped.equals(this))) {
                return;
            }
            if (p.getItemInHand().getType() == Material.QUARTZ) {
                if (Cooldowns.isOnCooldown("Assassin_item_cooldown", p)) {
                    p.sendMessage(ConfigurationService.RED + "You still have a " + ChatColor.GREEN + ChatColor.BOLD + "Reaper" + ConfigurationService.RED + " cooldown for another " + HCF.getRemaining(Cooldowns.getCooldownForPlayerLong("Assassin_item_cooldown", p), true) + ConfigurationService.RED + '.');
                    return;
                }
                if (p.getItemInHand().getAmount() == 1) {
                    p.getInventory().remove(p.getItemInHand());
                }
                p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                p.sendMessage(ConfigurationService.YELLOW + "You are now in " + ConfigurationService.GRAY + "Stealth" + ConfigurationService.YELLOW + " Mode");
                for (Player on : Bukkit.getOnlinePlayers()) {
                    on.playEffect(p.getLocation().add(0.5D, 2.0D, 0.5D), Effect.ENDER_SIGNAL, 5);
                    on.playEffect(p.getLocation().add(0.5D, 1.5D, 0.5D), Effect.ENDER_SIGNAL, 5);
                    on.playEffect(p.getLocation().add(0.5D, 1.0D, 0.5D), Effect.ENDER_SIGNAL, 5);
                    on.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    if (!on.hasPermission("base.command.vanish")) {
                        on.hidePlayer(p);
                    }
                }
                Cooldowns.addCooldown("Assassin_item_cooldown", p, 60);
                ((CraftPlayer) p).removePotionEffect(PotionEffectType.SPEED);
                this.firstAssassinEffects.put(p.getName(), Integer.valueOf(1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 4), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 0), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0), true);
                new BukkitRunnable() {
                    public void run() {
                        if ((AssassinClass.this.isApplicableFor(p)) && (AssassinClass.this.firstAssassinEffects.containsKey(p.getName())) && (((Integer) AssassinClass.this.firstAssassinEffects.get(p.getName())).intValue() == 1)) {
                            AssassinClass.this.afterFiveSeconds(p, false);
                        }
                    }
                }

                        .runTaskLater(this.plugin, 100L);
            }
        }
    }

    public void afterFiveSeconds(final Player p, boolean force) {
        if ((this.firstAssassinEffects.containsKey(p.getName())) && (isApplicableFor(p))) {
            for (Player on : Bukkit.getOnlinePlayers()) {
                if ((!on.canSee(p)) && (!on.hasPermission("base.command.vanish"))) {
                    on.showPlayer(p);
                }
                on.playEffect(p.getLocation().add(0.0D, 2.0D, 0.0D), Effect.ENDER_SIGNAL, 3);
                on.playEffect(p.getLocation().add(0.0D, 1.5D, 0.0D), Effect.ENDER_SIGNAL, 3);
                on.playEffect(p.getLocation().add(0.0D, 1.0D, 0.0D), Effect.ENDER_SIGNAL, 3);
                on.playEffect(p.getLocation().add(0.0D, 2.0D, 0.0D), Effect.BLAZE_SHOOT, 5);
                on.playEffect(p.getLocation().add(0.0D, 1.5D, 0.0D), Effect.BLAZE_SHOOT, 5);
                on.playEffect(p.getLocation().add(0.0D, 1.0D, 0.0D), Effect.BLAZE_SHOOT, 5);
            }
            BukkitTask task1 = new BukkitRunnable() {
                public void run() {
                    if ((AssassinClass.this.firstAssassinEffects.containsKey(p.getName())) && (((Integer) AssassinClass.this.firstAssassinEffects.get(p.getName())).intValue() == 2)) {
                        AssassinClass.this.firstAssassinEffects.remove(p.getName());
                        p.sendMessage(ConfigurationService.YELLOW + "You are now in " + ChatColor.GREEN + "Normal" + ConfigurationService.YELLOW + " Mode");
                        if (AssassinClass.this.isApplicableFor(p)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0), true);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1), true);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
                        }
                    }
                }
            }.runTaskLater(this.plugin, 100L);
            if (force) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120, 1), true);
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                this.firstAssassinEffects.remove(p.getName());
                this.firstAssassinEffects.put(p.getName(), 2);
                p.sendMessage(ConfigurationService.YELLOW + "You have been forced into " + ConfigurationService.RED + "Power" + ConfigurationService.YELLOW + " Mode" + ConfigurationService.GRAY.toString() + ChatColor.ITALIC + " (5 Seconds)");
                return;
            }
            this.firstAssassinEffects.remove(p.getName());
            this.firstAssassinEffects.put(p.getName(), 2);
            p.sendMessage(ConfigurationService.YELLOW + "You are now in " + ConfigurationService.RED + "Power" + ConfigurationService.YELLOW + " Mode" + ConfigurationService.GRAY.toString() + ChatColor.ITALIC + " (5 Seconds)");
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1), true);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0), true);
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0), true);
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120, 1), true);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack helmet = playerInventory.getHelmet();
        if ((helmet == null) || (helmet.getType() != Material.CHAINMAIL_HELMET)) {
            return false;
        }
        ItemStack chestplate = playerInventory.getChestplate();
        if ((chestplate == null) || (chestplate.getType() != Material.CHAINMAIL_CHESTPLATE)) {
            return false;
        }
        ItemStack leggings = playerInventory.getLeggings();
        if ((leggings == null) || (leggings.getType() != Material.CHAINMAIL_LEGGINGS)) {
            return false;
        }
        ItemStack boots = playerInventory.getBoots();
        return (boots != null) && (boots.getType() == Material.CHAINMAIL_BOOTS);
    }
}
