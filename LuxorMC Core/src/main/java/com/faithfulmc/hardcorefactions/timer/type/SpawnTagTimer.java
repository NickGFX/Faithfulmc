package com.faithfulmc.hardcorefactions.timer.type;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.PlayerClaimEnterEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerJoinFactionEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerLeaveFactionEvent;
import com.faithfulmc.hardcorefactions.kit.event.KitApplyEvent;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.event.TimerClearEvent;
import com.faithfulmc.hardcorefactions.timer.event.TimerStartEvent;
import com.faithfulmc.hardcorefactions.visualise.VisualType;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.base.Optional;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class SpawnTagTimer extends PlayerTimer implements Listener {
    private static final long NON_WEAPON_TAG = ConfigurationService.ORIGINS ? 15000L : 5000L;
    private final HCF plugin;


    public SpawnTagTimer(HCF plugin) {
        super("Spawn Tag", ConfigurationService.ORIGINS ? TimeUnit.SECONDS.toMillis(60) : TimeUnit.SECONDS.toMillis(30L), false);
        this.plugin = plugin;

    }


    public String getScoreboardPrefix() {
        return ChatColor.RED.toString() + ChatColor.BOLD;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onKitApply(KitApplyEvent event) {

        Player player = event.getPlayer();

        long remaining;

        if ((!event.isForce()) && ((remaining = getRemaining(player)) > 0L)) {

            event.setCancelled(true);

            player.sendMessage(ConfigurationService.RED + "You cannot apply kits whilst your " + getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");

        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStop(TimerClearEvent event) {

        if (event.getTimer().equals(this)) {
                    Optional<UUID> optionalUserUUID = event.getUserUUID();
                    if (optionalUserUUID.isPresent()) {

                onExpire((UUID) optionalUserUUID.get());

            }

        }

    }


    public void onExpire(UUID userUUID) {

        Player player = Bukkit.getPlayer(userUUID);

        if (player == null) {

            return;

        }


        plugin.getVisualiseHandler().clearVisualType(player, VisualType.SPAWN_BORDER, true);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionJoin(PlayerJoinFactionEvent event) {

        Optional<Player> optional = event.getPlayer();

        if (optional.isPresent()) {

            Player player = (Player) optional.get();

            long remaining = getRemaining(player);

            if (remaining > 0L) {

                event.setCancelled(true);

                player.sendMessage(ConfigurationService.RED + "You cannot join factions whilst your " + getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(getRemaining(player), true, false) + ConfigurationService.RED + " remaining]");

            }

        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionLeave(PlayerLeaveFactionEvent event) {

        Optional<Player> optional = event.getPlayer();

        if (optional.isPresent()) {

            Player player = (Player) optional.get();

            if (getRemaining(player) > 0L) {

                event.setCancelled(true);

                player.sendMessage(ConfigurationService.RED + "You cannot join factions whilst your " + getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(getRemaining(player), true, false) + ConfigurationService.RED + " remaining]");

            }

        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPreventClaimEnter(PlayerClaimEnterEvent event) {

        if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT) {

            return;

        }

        Player player = event.getPlayer();

        if ((!event.getFromFaction().isSafezone()) && (event.getToFaction().isSafezone()) && (getRemaining(player) > 0L)) {

            event.setCancelled(true);

            player.sendMessage(ConfigurationService.RED + "You cannot enter " + event.getToFaction().getDisplayName(player) + ConfigurationService.RED + " whilst your " + getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(getRemaining(player), true, false) + ConfigurationService.RED + " remaining]");

        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        Player attacker = BukkitUtils.getFinalAttacker(event, true);

        Entity entity;

        if ((attacker != null) && (((entity = event.getEntity()) instanceof Player))) {

            Player attacked = (Player) entity;

            boolean weapon = event.getDamager() instanceof Arrow;

            if (!weapon) {

                ItemStack stack = attacker.getItemInHand();

                weapon = (stack != null) && ((EnchantmentTarget.WEAPON.includes(stack)) || stack.getType() == Material.FISHING_ROD);

            }

            long duration = weapon ? this.defaultCooldown : NON_WEAPON_TAG;

            setCooldown(attacked, attacked.getUniqueId(), Math.max(getRemaining(attacked), ConfigurationService.ORIGINS ? TimeUnit.SECONDS.toMillis(7) : duration), true);

            setCooldown(attacker, attacker.getUniqueId(), Math.max(getRemaining(attacker), duration), true);

            EntityPlayer entityPlayer = ((CraftPlayer) attacked).getHandle();
            if(entityPlayer.isDisguised()){
                attacked.sendMessage(ConfigurationService.YELLOW + "Your " + ConfigurationService.GRAY + entityPlayer.getDisguiseType().name().toLowerCase().replace("_", " ") + ChatColor.YELLOW + " disguise was removed due to taking damage");

                entityPlayer.disguise(null);
            }
        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStart(TimerStartEvent event) {

        if (event.getTimer().equals(this)) {

            Optional<Player> optional = event.getPlayer();

            if (optional.isPresent()) {

                Player player = (Player) optional.get();

                player.sendMessage(ConfigurationService.YELLOW + "You are now spawn tagged for " + ConfigurationService.GOLD + DurationFormatUtils.formatDurationWords(event.getDuration(), true, true));
            }

        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        clearCooldown(event.getPlayer().getUniqueId());

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPreventClaimEnterMonitor(PlayerClaimEnterEvent event) {

        if ((event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT) && (!event.getFromFaction().isSafezone()) && (event.getToFaction().isSafezone())) {

            clearCooldown(event.getPlayer());

        }

    }

}
