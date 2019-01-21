package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;

public class BorderListener implements Listener {
    private static final int BORDER_OFFSET_TELEPORTS = 50;

    public static boolean isWithinBorder(Location location) {
        int borderSize = ((Integer) ConfigurationService.BORDER_SIZES.get(location.getWorld().getEnvironment())).intValue();
        return (Math.abs(location.getBlockX()) <= borderSize) && (Math.abs(location.getBlockZ()) <= borderSize);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreaturePreSpawn(CreatureSpawnEvent event) {
        if (!ConfigurationService.KIT_MAP && !isWithinBorder(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPearl(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Player player = e.getPlayer();
            if (!isWithinBorder(e.getTo()) && (ConfigurationService.KIT_MAP || !HCF.getInstance().getEotwHandler().isEndOfTheWorld())) {
                HCF.getInstance().getTimerManager().enderPearlTimer.refund(player);
                player.sendMessage(ConfigurationService.RED + "You may not pearl outside the border");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketFillEvent event) {
        if (!isWithinBorder(event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ConfigurationService.RED + "You cannot fill buckets past the border.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!isWithinBorder(event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ConfigurationService.RED + "You cannot empty buckets past the border.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isWithinBorder(event.getBlock().getLocation()) && (ConfigurationService.KIT_MAP || !HCF.getInstance().getEotwHandler().isEndOfTheWorld())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ConfigurationService.RED + "You cannot place blocks past the border.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isWithinBorder(event.getBlock().getLocation()) && (ConfigurationService.KIT_MAP || !HCF.getInstance().getEotwHandler().isEndOfTheWorld())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ConfigurationService.RED + "You cannot break blocks past the border.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if ((from.getBlockX() == to.getBlockX()) && (from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        if ((!isWithinBorder(to)) && (isWithinBorder(from))) {
            Player player = event.getPlayer();
            player.sendMessage(ConfigurationService.RED + "You cannot go past the border.");
            event.setTo(from);
            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                vehicle.eject();
                vehicle.teleport(from);
                vehicle.setPassenger(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location to = event.getTo();
        if (!isWithinBorder(to)) {
            PlayerTeleportEvent.TeleportCause cause = event.getCause();
            if ((cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) || ((cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) && (isWithinBorder(event.getFrom())))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ConfigurationService.RED + "You cannot go past the border.");
            } else {
                World.Environment toEnvironment = to.getWorld().getEnvironment();
                if (toEnvironment != World.Environment.NORMAL) {
                    return;
                }
                int x = to.getBlockX();
                int z = to.getBlockZ();
                int borderSize = ConfigurationService.BORDER_SIZES.get(toEnvironment);
                boolean extended = false;
                if (Math.abs(x) > borderSize) {
                    to.setX(x > 0 ? borderSize - BORDER_OFFSET_TELEPORTS : -borderSize + BORDER_OFFSET_TELEPORTS);
                    extended = true;
                }
                if (Math.abs(z) > borderSize) {
                    to.setZ(z > 0 ? borderSize - BORDER_OFFSET_TELEPORTS : -borderSize + BORDER_OFFSET_TELEPORTS);
                    extended = true;
                }
                if (extended) {
                    to.add(0.5D, 0.0D, 0.5D);
                    event.setTo(to);
                    event.getPlayer().sendMessage(ConfigurationService.RED + "This portals travel location was over the border. It has been moved inwards.");
                }
            }
        }
    }
}
