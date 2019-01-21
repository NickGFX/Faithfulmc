package com.faithfulmc.hardcorefactions.hcfclass;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.old.AssassinClass;
import com.faithfulmc.hardcorefactions.util.Cooldowns;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class HCFClass implements Listener{
    public static final long DEFAULT_MAX_DURATION;

    static {
        DEFAULT_MAX_DURATION = TimeUnit.MINUTES.toMillis(8L);
    }

    protected final HCF plugin;
    protected final Set<PotionEffect> passiveEffects = new HashSet<>();;
    protected final Map<Material, PotionEffect> clickableEffects = new HashMap<>();
    protected final int buffDelay;
    protected final String name;
    protected final ChatColor chatColor;
    protected long warmupDelay;

    public HCFClass(HCF plugin, int buffDelay, String name, long warmupDelay, ChatColor chatColor) {
        this.plugin = plugin;
        this.name = name;
        this.buffDelay = buffDelay;
        this.chatColor = chatColor;
        this.warmupDelay = warmupDelay;
    }

    public HCFClass(HCF plugin, int buffDelay, String name, ChatColor chatColor) {
        this(plugin, buffDelay, name, TimeUnit.SECONDS.toMillis(5), chatColor);
    }

    public HCFClass(HCF plugin, String name, long warmupDelay, ChatColor chatColor) {
        this(plugin, 0, name, warmupDelay, chatColor);
    }

    public HCFClass(HCF plugin, String name, ChatColor chatColor) {
        this(plugin, 0, name, TimeUnit.SECONDS.toMillis(5), chatColor);
    }

    public boolean hasBuffs(){
        return !clickableEffects.isEmpty();
    }

    public String getDisplayName() {
        return chatColor + ChatColor.BOLD.toString() + name;
    }

    public String getName() {
        return this.name;
    }

    public long getWarmupDelay() {
        return this.warmupDelay;
    }

    public boolean onEquip(final Player player) {
        for (final PotionEffect effect : this.passiveEffects) {
            player.addPotionEffect(effect, true);
        }
        player.sendMessage(ConfigurationService.YELLOW + "Class " + getDisplayName() + ConfigurationService.YELLOW + " has been equipped.");
        return true;
    }

    public void onUnequip(final Player player) {
        for (final PotionEffect effect : this.passiveEffects) {
            for (final PotionEffect active : player.getActivePotionEffects()) {
                if (active.getDuration() > HCFClass.DEFAULT_MAX_DURATION && active.getType().equals(effect.getType()) && active.getAmplifier() == effect.getAmplifier()) {
                    player.removePotionEffect(effect.getType());
                    break;
                }
            }
        }
        if (this instanceof AssassinClass) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
        player.sendMessage(ConfigurationService.YELLOW + "Class " + getDisplayName() + ConfigurationService.YELLOW + " has been un-equipped.");
    }

    public String getCooldown(){
        return "class_" + name.toLowerCase();
    }

    @EventHandler
    public void onPlayerUseBuff(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(clickableEffects.isEmpty()){
            return;
        }
        if (this.plugin.getHcfClassManager().getEquippedClass(player) != null && this.plugin.getHcfClassManager().getEquippedClass(player).equals(this)) {
            ItemStack hand = player.getItemInHand();
            if(hand != null) {
                Material type = hand.getType();
                PotionEffect buff = clickableEffects.get(type);
                if(buff != null) {
                    e.setCancelled(true);
                    if (hasCooldown(player)) {
                        player.sendMessage(ConfigurationService.RED + "You are still on a cooldown for another " + ChatColor.BOLD + Cooldowns.getCooldownForPlayerInt(getCooldown(), player) + ConfigurationService.RED.toString() + " seconds");
                    }
                    else {
                        Cooldowns.addCooldown(getCooldown(), player, buffDelay);
                        player.sendMessage(getDisplayName() + ConfigurationService.ARROW_COLOR + " " + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.YELLOW + ChatColor.BOLD.toString() + WordUtils.capitalize(buff.getType().getName().toLowerCase()) + " " + (buff.getAmplifier() + 1) + ChatColor.YELLOW + " has been activated.");
                        if (hand.getAmount() == 1) {
                            player.getInventory().remove(hand);
                        } else {
                            hand.setAmount(hand.getAmount() - 1);
                            player.setItemInHand(hand);
                        }
                        player.addPotionEffect(buff, true);
                        /*
                        new BukkitRunnable() {
                            public void run() {
                                if(player.isOnline() && isApplicableFor(player)){
                                    if(player.hasPotionEffect(buff.getType())){
                                        player.removePotionEffect(buff.getType());
                                        for(PotionEffect passive: passiveEffects){
                                            if(passive.getType().getId() == buff.getType().getId()){
                                                player.addPotionEffect(passive, true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }.runTaskLater(this.plugin, buff.getDuration());
                        */
                    }
                }
            }
        }
    }

    public boolean hasCooldown(Player player){
        return Cooldowns.isOnCooldown(getCooldown(), player, System.currentTimeMillis());
    }

    public boolean hasCooldown(Player player, long now){
        return Cooldowns.isOnCooldown(getCooldown(), player, now);
    }

    public long getCooldown(Player player){
        return Cooldowns.getCooldownForPlayerLong(getCooldown(), player, System.currentTimeMillis());
    }

    public long getCooldown(Player player, long now){
        return Cooldowns.getCooldownForPlayerLong(getCooldown(), player, now);
    }

    public static long getDefaultMaxDuration() {
        return DEFAULT_MAX_DURATION;
    }

    public Set<PotionEffect> getPassiveEffects() {
        return passiveEffects;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public abstract boolean isApplicableFor(final Player p0);
}