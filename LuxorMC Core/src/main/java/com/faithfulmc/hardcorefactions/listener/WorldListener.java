package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.RoadFaction;
import com.faithfulmc.hardcorefactions.faction.type.WarzoneFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.material.EnderChest;
import org.bukkit.metadata.FixedMetadataValue;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class WorldListener implements Listener {
    public static final String DEFAULT_WORLD_NAME = "world";
    private final HCF plugin;

    public WorldListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
        if ((event.getEntity() instanceof EnderDragon) || (event.getEntity() instanceof Creeper)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockChange(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityPortalEnter(EntityPortalEvent event) {
        if ((event.getEntity() instanceof EnderDragon)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(ConfigurationService.RED + "Beds are disabled on this server.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onWitherChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (((entity instanceof Wither)) || ((entity instanceof EnderDragon))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        switch (event.getBlock().getType()) {
            case SNOW:
            case ICE:
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(Bukkit.getWorld(DEFAULT_WORLD_NAME).getSpawnLocation().add(0.5D, 0.0D, 0.5D));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
            factionUser.setBalance(factionUser.getBalance() + ConfigurationService.STARTING_MONEY);
            event.setSpawnLocation(Bukkit.getWorld(DEFAULT_WORLD_NAME).getSpawnLocation().add(0.5D, 0.0D, 0.5D));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if ((event.getInventory() instanceof EnderChest)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if ((event.getEntity() instanceof Squid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            e.setCancelled(true);
        }
        if (e.getEntity() instanceof Player && !((Player) e.getEntity()).isOnline() && (e.getCause() == EntityDamageEvent.DamageCause.LAVA || e.getCause() == EntityDamageEvent.DamageCause.DROWNING)) {
            e.setCancelled(true);
        }
    }

    public static String SAFEZONE = "SAFEZONE";

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDecay(LeavesDecayEvent event) {
        if (event.getBlock().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);
        }
        else if(event.getBlock().hasMetadata(SAFEZONE)){
            event.setCancelled(true);
        }
        else{
            Faction faction = plugin.getFactionManager().getFactionAt(event.getBlock());
            if(faction.isSafezone() || faction instanceof EventFaction || faction instanceof RoadFaction){
                event.getBlock().setMetadata(SAFEZONE, new FixedMetadataValue(plugin, SAFEZONE));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDecay(BlockSpreadEvent event) {
        if (event.getBlock().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);
        }
        else if(event.getBlock().hasMetadata(SAFEZONE)){
            event.setCancelled(true);
        }
        else{
            Faction faction = plugin.getFactionManager().getFactionAt(event.getBlock());
            if(faction.isSafezone() || faction instanceof EventFaction || faction instanceof RoadFaction || faction instanceof WarzoneFaction){
                event.getBlock().setMetadata(SAFEZONE, new FixedMetadataValue(plugin, SAFEZONE));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDecay(BlockGrowEvent event) {
        if (event.getBlock().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);
        }
        else if(event.getBlock().hasMetadata(SAFEZONE)){
            event.setCancelled(true);
        }
        else{
            Faction faction = plugin.getFactionManager().getFactionAt(event.getBlock());
            if(faction.isSafezone() || faction instanceof EventFaction || faction instanceof RoadFaction || faction instanceof WarzoneFaction){
                event.getBlock().setMetadata(SAFEZONE, new FixedMetadataValue(plugin, SAFEZONE));
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDecay(BlockFadeEvent event) {
        if (event.getBlock().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);
        }
        else if(event.getBlock().hasMetadata(SAFEZONE)){
            event.setCancelled(true);
        }
        else{
            Faction faction = plugin.getFactionManager().getFactionAt(event.getBlock());
            if(faction.isSafezone() || faction instanceof EventFaction || faction instanceof RoadFaction || faction instanceof WarzoneFaction){
                event.getBlock().setMetadata(SAFEZONE, new FixedMetadataValue(plugin, SAFEZONE));
                event.setCancelled(true);
            }
        }
    }
}
