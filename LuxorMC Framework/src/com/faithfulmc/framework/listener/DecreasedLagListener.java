package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DecreasedLagListener implements Listener {
    private static final String COMMAND;

    static {
        COMMAND = "stoplag";
    }

    private final BasePlugin plugin;

    public DecreasedLagListener(final BasePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            final Player player = event.getPlayer();
            final BaseCommand baseCommand = this.plugin.getCommandManager().getCommand(DecreasedLagListener.COMMAND);
            if (player.hasPermission(baseCommand.getPermission())) {
                event.getPlayer().sendMessage(BaseConstants.YELLOW + "Intensive server activity is currently prevented. Use /" + baseCommand.getName() + " to toggle.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(final BlockBurnEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeavesDecay(final LeavesDecayEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(final BlockFormEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(final BlockSpreadEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(final ExplosionPrimeEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (this.plugin.getServerHandler().isDecreasedLagMode()) {
            switch (event.getSpawnReason()) {
                case SPAWNER:
                case SPAWNER_EGG:
                case BUILD_SNOWMAN:
                case BUILD_IRONGOLEM:
                case BUILD_WITHER:
                case DISPENSE_EGG: {
                    break;
                }
                default: {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
