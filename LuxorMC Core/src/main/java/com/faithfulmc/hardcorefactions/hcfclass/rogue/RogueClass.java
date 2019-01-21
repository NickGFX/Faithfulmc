package com.faithfulmc.hardcorefactions.hcfclass.rogue;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RogueClass extends HCFClass{
    public static final PotionEffect SLOWNESS = new PotionEffect(PotionEffectType.SLOW, 60, 2);

    public RogueClass(HCF plugin) {
        super(plugin, 90, "Rogue", ChatColor.RED);
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        clickableEffects.put(Material.SUGAR, new PotionEffect(PotionEffectType.SPEED, 20 * 8, 4));
        clickableEffects.put(Material.FEATHER, new PotionEffect(PotionEffectType.JUMP, 20 * 6, 3));
    }

    private final String BACKSTAB_META = "BACKSTAB";
    private final long BACKSTACK_COOLDOWN = 1750;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity instanceof Player && damager instanceof Player) {
            Player attacker = (Player) damager;
            if (plugin.getHcfClassManager().getEquippedClass(attacker) == this) {
                ItemStack stack = attacker.getItemInHand();
                if (stack != null && stack.getType() == Material.GOLD_SWORD && stack.getEnchantments().isEmpty()) {
                    Player player = (Player) entity;
                    long now = System.currentTimeMillis();
                    long stab = attacker.hasMetadata(BACKSTAB_META) ? BukkitUtils.getMetaData(attacker, BACKSTAB_META, plugin).asLong() : 0;
                    long cooldown = now - stab;
                    if(cooldown > BACKSTACK_COOLDOWN) {
                        player.sendMessage(ConfigurationService.GOLD + attacker.getName() + ConfigurationService.YELLOW + " has backstabbed you.");
                        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
                        attacker.sendMessage(ConfigurationService.YELLOW + "You have backstabbed " + ConfigurationService.GOLD + player.getName() + ConfigurationService.YELLOW + '.');
                        attacker.setItemInHand(new ItemStack(Material.AIR, 1));
                        attacker.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
                        attacker.updateInventory();
                        attacker.addPotionEffect(SLOWNESS, true);
                        event.setDamage(8);
                        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
                        attacker.setMetadata(BACKSTAB_META, new FixedMetadataValue(plugin, now));
                    }
                    else{
                        attacker.sendMessage(ChatColor.RED + "You may not do this for " + ChatColor.BOLD + HCF.getRemaining(BACKSTACK_COOLDOWN - cooldown, true, true));
                    }
                }
            }
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
