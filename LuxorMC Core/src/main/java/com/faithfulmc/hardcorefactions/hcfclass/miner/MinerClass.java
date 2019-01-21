package com.faithfulmc.hardcorefactions.hcfclass.miner;

import com.luxormc.event.PlayerMineItemsEvent;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.hardcorefactions.hcfclass.event.PvpClassEquipEvent;
import com.faithfulmc.hardcorefactions.listener.FoundDiamondsListener;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class MinerClass extends HCFClass{
    private static final int INVISIBILITY_HEIGHT_LEVEL = 50;
    private static final PotionEffect HEIGHT_INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0);

    public MinerClass(HCF plugin) {
        super(plugin, ConfigurationService.KIT_MAP ? "Builder" : "Miner", ChatColor.AQUA);
        this.passiveEffects.add(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
        passiveEffects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
        passiveEffects.add(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1));
        if(ConfigurationService.KIT_MAP){
            passiveEffects.add(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0));
            passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        }
    }

    private void removeInvisibilitySafely(Player player) {
        for (PotionEffect active : player.getActivePotionEffects()) {
            if ((active.getType().equals(PotionEffectType.INVISIBILITY)) && (active.getDuration() > DEFAULT_MAX_DURATION)) {
                player.sendMessage(getDisplayName() + ConfigurationService.YELLOW + " invisibility disabled.");
                (player).removePotionEffect(active.getType());
                break;
            }
        }
        for (PotionEffect effect : passiveEffects) {
            player.addPotionEffect(effect, true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (((entity instanceof Player)) && (BukkitUtils.getFinalAttacker(event, false) != null)) {
            Player player = (Player) entity;
            if (this.plugin.getHcfClassManager().hasClassEquipped(player, this)) {
                removeInvisibilitySafely(player);
            }
        }
    }

    public void onUnequip(Player player) {
        removeInvisibilitySafely(player);
        FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
        MinerLevel level = factionUser.getMinerLevel();
        for(PotionEffect potionEffect: level.getGive()){
            player.removePotionEffect(potionEffect.getType());
        }
        super.onUnequip(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            conformMinerInvisibility(event.getPlayer(), event.getFrom(), event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMineItems(PlayerMineItemsEvent event){
        if(!ConfigurationService.ORIGINS) {
            Player player = event.getPlayer();
            if (isApplicableFor(player)) {
                event.setCancelled(true);
                PlayerInventory playerInventory = player.getInventory();
                Collection<ItemStack> itemStacks = event.getStackCollection();
                ItemStack[] stacks = itemStacks.toArray(new ItemStack[itemStacks.size()]);
                if (!playerInventory.addItem(stacks).isEmpty()) {
                    player.sendMessage(ConfigurationService.YELLOW + "Your inventory is full");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        conformMinerInvisibility(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClassEquip(PvpClassEquipEvent event) {
        Player player = event.getPlayer();
        if (event.getPvpClass().equals(this)) {
            if (player.getLocation().getY() < INVISIBILITY_HEIGHT_LEVEL) {
                player.addPotionEffect(HEIGHT_INVISIBILITY, true);
                player.sendMessage(getDisplayName() + ConfigurationService.YELLOW + " invisibility enabled.");
            }
            FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
            MinerLevel level = factionUser.getMinerLevel();
            for(PotionEffect potionEffect: level.getGive()){
                player.addPotionEffect(potionEffect, true);
            }
        }
    }

    @EventHandler
    public void onPlayerLevel(MinerLevelEvent event){
        FactionUser user = event.getUser();
        MinerLevel level = event.getLevel();
        Player player = user.getPlayer();
        if(player != null && player.isOnline()){
            String message = ConfigurationService.GRAY + "[" + ChatColor.AQUA + "*" + ConfigurationService.GRAY + "] " + ChatColor.AQUA + player.getName() + ConfigurationService.GRAY + " has leveled up to " + ChatColor.AQUA + ChatColor.BOLD.toString() + level.getNick();
            for(Player other: Bukkit.getOnlinePlayers()){
                if(!other.hasMetadata(FoundDiamondsListener.NO_DIAMOND_ALERTS)){
                    other.sendMessage(message);
                }
            }
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            player.sendMessage(getDisplayName() + ConfigurationService.YELLOW + " You leveled up to " + ChatColor.AQUA + ChatColor.BOLD.toString() + level.getNick() + ConfigurationService.YELLOW + " for mining " + level.getAmount() + " diamonds");
            if(plugin.getHcfClassManager().hasClassEquipped(player, this)){
                for(PotionEffect potionEffect: level.getGive()){
                    player.addPotionEffect(potionEffect, true);
                }
            }
        }
    }


    private void conformMinerInvisibility(Player player, Location from, Location to) {
        int fromY = from.getBlockY();
        int toY = to.getBlockY();
        if ((fromY != toY) && (this.plugin.getHcfClassManager().hasClassEquipped(player, this))) {
            boolean isInvisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
            if (toY > INVISIBILITY_HEIGHT_LEVEL) {
                if ((fromY <= INVISIBILITY_HEIGHT_LEVEL)) {
                    removeInvisibilitySafely(player);
                }
            } else {
                if (!isInvisible) {
                    player.addPotionEffect(HEIGHT_INVISIBILITY, true);
                    player.sendMessage(getDisplayName() + ConfigurationService.YELLOW + " invisibility enabled.");
                }
            }
        }
    }

    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack helmet = playerInventory.getHelmet();
        if ((helmet == null) || (helmet.getType() != Material.IRON_HELMET)) {
            return false;
        }
        ItemStack chestplate = playerInventory.getChestplate();
        if ((chestplate == null) || (chestplate.getType() != Material.IRON_CHESTPLATE)) {
            return false;
        }
        ItemStack leggings = playerInventory.getLeggings();
        if ((leggings == null) || (leggings.getType() != Material.IRON_LEGGINGS)) {
            return false;
        }
        ItemStack boots = playerInventory.getBoots();
        return (boots != null) && (boots.getType() == Material.IRON_BOOTS);
    }
}
