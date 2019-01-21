package com.faithfulmc.hardcorefactions.hcfclass.archer;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.hardcorefactions.scoreboard.PlayerBoard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ArcherClass extends HCFClass {
    public static final HashMap<UUID, UUID> TAGGED = new HashMap<>();
    private static final int INVISIBILITY_SECONDS = 15;
    private static final int INVISIBILITY_TICKS = INVISIBILITY_SECONDS * 20;
    public static final int ARCHER_SPEED_COOLDOWN_DELAY = 25;
    private static final String ARROW_FORCE_METADATA = "ARROW_FORCE";

    public ArcherClass(HCF plugin) {
        super(plugin, 25, "Archer",  ChatColor.GREEN);
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        this.clickableEffects.put(Material.SUGAR, new PotionEffect(PotionEffectType.SPEED, 160, 3));
        this.clickableEffects.put(Material.FEATHER, new PotionEffect(PotionEffectType.JUMP, 120, 4));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityShootBow(EntityShootBowEvent event) {
        Entity projectile = event.getProjectile();
        if ((projectile instanceof Arrow)) {
            projectile.setMetadata(ARROW_FORCE_METADATA, new FixedMetadataValue(this.plugin, event.getForce()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (TAGGED.containsKey(e.getPlayer().getUniqueId())) {
            TAGGED.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (((entity instanceof Player)) && ((damager instanceof Arrow))) {
            Arrow arrow = (Arrow) damager;
            ProjectileSource source = arrow.getShooter();
            if ((source instanceof Player)) {
                Player damaged = (Player) event.getEntity();
                Player shooter = (Player) source;
                HCFClass equipped = this.plugin.getHcfClassManager().getEquippedClass(shooter);
                if ((equipped == null) || (!equipped.equals(this))) {
                    return;
                }
                if (this.plugin.getTimerManager().archerTimer.getRemaining((Player) entity) == 0L) {
                    if ((this.plugin.getHcfClassManager().getEquippedClass(damaged) != null) && (this.plugin.getHcfClassManager().getEquippedClass(damaged).equals(this))) {
                        return;
                    }
                    this.plugin.getTimerManager().archerTimer.setCooldown((Player) entity, entity.getUniqueId());
                    TAGGED.put(damaged.getUniqueId(), shooter.getUniqueId());
                    if(damaged.isOnline()) {
                        for (PlayerBoard playerBoard : plugin.getScoreboardHandler().getPlayerBoards().values()) {
                            playerBoard.init(damaged);
                        }
                    }
                    for(PotionEffect potionEffect: new ArrayList<>(damaged.getActivePotionEffects())){
                        if(potionEffect.getType().getId() == PotionEffectType.INVISIBILITY.getId()){
                            damaged.sendMessage(ConfigurationService.YELLOW + "Since you had invisibility it has been removed.");
                            int ticks = potionEffect.getDuration();
                            damaged.removePotionEffect(potionEffect.getType());
                            if(ticks > INVISIBILITY_TICKS){
                                PotionEffect copyEffect = new PotionEffect(potionEffect.getType(), potionEffect.getDuration() - INVISIBILITY_TICKS, potionEffect.getAmplifier(), potionEffect.isAmbient());
                                new BukkitRunnable(){
                                    public void run() {
                                        if(damaged.isOnline() && !damaged.isDead() && plugin.getTimerManager().archerTimer.getRemaining(damaged) <= 0) {
                                            damaged.addPotionEffect(copyEffect);
                                        }
                                    }
                                }.runTaskLater(plugin, INVISIBILITY_TICKS);
                            }
                            break;
                        }
                    }
                    shooter.sendMessage(ConfigurationService.YELLOW + "You have hit " + ConfigurationService.GOLD + damaged.getName() + ConfigurationService.YELLOW + " and have archer TAGGED");
                    damaged.sendMessage(ConfigurationService.YELLOW + "You have been archer tagged by " + ConfigurationService.GOLD + shooter.getName());
                }
            }
        }
    }

    @Override
    public boolean onEquip(Player player) {
        if(ConfigurationService.ORIGINS) {
            PlayerFaction faction = plugin.getFactionManager().getPlayerFaction(player);
            int amount;
            if (faction == null || (amount = faction.getOnlineArchers().size()) < 2) {
                return super.onEquip(player);
            }
            player.sendMessage(ConfigurationService.RED + "You may not equip archer when your faction already has " + ChatColor.BOLD + amount + ConfigurationService.RED + " archers.");
            return false;
        }
        return super.onEquip(player);
    }

    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack helmet = playerInventory.getHelmet();
        if ((helmet == null) || (helmet.getType() != Material.LEATHER_HELMET)) {
            return false;
        }
        ItemStack chestplate = playerInventory.getChestplate();
        if ((chestplate == null) || (chestplate.getType() != Material.LEATHER_CHESTPLATE)) {
            return false;
        }
        ItemStack leggings = playerInventory.getLeggings();
        if ((leggings == null) || (leggings.getType() != Material.LEATHER_LEGGINGS)) {
            return false;
        }
        ItemStack boots = playerInventory.getBoots();
        return (boots != null) && (boots.getType() == Material.LEATHER_BOOTS);
    }
}
