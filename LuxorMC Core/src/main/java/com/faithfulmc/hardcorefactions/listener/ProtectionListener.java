package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.faction.CapturableFaction;
import com.faithfulmc.hardcorefactions.events.faction.CitadelCapture;
import com.faithfulmc.hardcorefactions.events.faction.CitadelFaction;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.event.CaptureZoneEnterEvent;
import com.faithfulmc.hardcorefactions.faction.event.CaptureZoneLeaveEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerClaimEnterEvent;
import com.faithfulmc.hardcorefactions.faction.struct.Raidable;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.*;
import com.faithfulmc.hardcorefactions.mountain.GlowstoneFaction;
import com.faithfulmc.hardcorefactions.mountain.MountainFaction;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.cuboid.Cuboid;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_7_R4.TileEntityPiston;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.material.Cauldron;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;

public class ProtectionListener implements Listener {
    public static final String PROTECTION_BYPASS_PERMISSION = "hcf.faction.protection.bypass";
    private static final ImmutableMultimap<Object, Object> ITEM_BLOCK_INTERACTABLES;
    private static final ImmutableSet BLOCK_INTERACTABLES;

    public static boolean attemptBuild(final Entity entity, final Location location, final String denyMessage) {
        return attemptBuild(entity, location, denyMessage, false);
    }

    public static boolean attemptBuild(final Entity entity, final Location location, final String denyMessage, final boolean isInteraction) {
        return attemptBuild(entity, location, denyMessage, isInteraction, false);
    }

    public static boolean attemptBuild(final Entity entity, final Location location, final String denyMessage, final boolean isInteraction, boolean br) {
        if (entity == null) {
            return false;
        }
        boolean result = false;
        if (entity instanceof Player) {
            final Player player = (Player) entity;
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(PROTECTION_BYPASS_PERMISSION)) {
                return true;
            }
            if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                player.sendMessage(ConfigurationService.RED + "You cannot build in the end.");
                return false;
            }

            Faction factionAt = HCF.getInstance().getFactionManager().getFactionAt(location);
            if(factionAt instanceof CitadelFaction && isInteraction && location.getBlock().getState() instanceof Chest){
                CitadelFaction citadelFaction = (CitadelFaction) factionAt;
                CitadelCapture citadelCapture = citadelFaction.getCitadelCapture();
                if(citadelCapture != null && citadelCapture.hasControl()) {
                    PlayerFaction playerFaction = HCF.getInstance().getFactionManager().getPlayerFaction(player);
                    if(playerFaction != null && Objects.equals(citadelCapture.getFactionUUID(), playerFaction.getUniqueID())){
                        return true;
                    }
                }
            }
            if (!(factionAt instanceof ClaimableFaction)) {
                result = true;
            } else if (factionAt instanceof Raidable && ((Raidable) factionAt).isRaidable()) {
                result = true;
            }
            if(ConfigurationService.ORIGINS && !br && !isInteraction && (factionAt instanceof WildernessFaction || factionAt instanceof WarzoneFaction)){
                if(location.getBlockY() >= 100){
                    player.sendMessage(ChatColor.RED + "You may not build above " + ChatColor.BOLD + "Y100" + ChatColor.RED + " in the wilderness.");
                    return false;
                }
            }
            if (factionAt instanceof PlayerFaction) {
                final PlayerFaction playerFaction = HCF.getInstance().getFactionManager().getPlayerFaction(player);
                if (playerFaction != null && playerFaction.equals(factionAt)) {
                    result = true;
                }
            }
            if(br && factionAt instanceof MountainFaction && ((MountainFaction)factionAt).allowed(location.getBlock().getType())){
                result = true;
            }
            if(factionAt instanceof WarzoneFaction && ConfigurationService.KIT_MAP){
                result = true;
            }
            if (result) {
                if (!isInteraction && Math.abs(location.getBlockX()) <= ConfigurationService.SPAWN_BUFFER && Math.abs(location.getBlockZ()) <= ConfigurationService.SPAWN_BUFFER) {
                    if (denyMessage != null) {
                        player.sendMessage(ConfigurationService.YELLOW + "You cannot build within " + ChatColor.GREEN + ConfigurationService.SPAWN_BUFFER + ConfigurationService.YELLOW + " blocks from spawn.");
                    }
                    return false;
                }
            } else if (denyMessage != null) {
                player.sendMessage(String.format(denyMessage, factionAt.getDisplayName(player)));
            }
        }
        return result;
    }

    public static boolean canBuildAt(final Location from, final Location to) {
        final Faction toFactionAt = HCF.getInstance().getFactionManager().getFactionAt(to);
        if (toFactionAt instanceof Raidable && !((Raidable) toFactionAt).isRaidable()) {
            final Faction fromFactionAt = HCF.getInstance().getFactionManager().getFactionAt(from);
            if (!toFactionAt.equals(fromFactionAt)) {
                return false;
            }
        }
        return true;
    }

    static {
        ITEM_BLOCK_INTERACTABLES = ImmutableMultimap.builder().put(Material.DIAMOND_HOE, Material.GRASS).put(Material.GOLD_HOE, Material.GRASS).put(Material.IRON_HOE, Material.GRASS).put(Material.STONE_HOE, Material.GRASS).put(Material.WOOD_HOE, Material.GRASS).build();
        BLOCK_INTERACTABLES = Sets.immutableEnumSet(Material.BED, Material.BED_BLOCK, Material.BEACON, Material.FENCE_GATE, Material.IRON_DOOR, Material.TRAP_DOOR, Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.CHEST, Material.TRAPPED_CHEST, Material.BREWING_STAND, Material.HOPPER, Material.DROPPER, Material.DISPENSER, Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.LEVER, Material.FIRE, Material.FURNACE, Material.BURNING_FURNACE, Material.REDSTONE_COMPARATOR, Material.REDSTONE_COMPARATOR_ON, Material.REDSTONE_COMPARATOR_OFF, Material.DIODE, Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF);
    }

    private final HCF plugin;

    public ProtectionListener(final HCF plugin) {
        this.plugin = plugin;
    }

    private void handleMove(final PlayerMoveEvent event, final PlayerClaimEnterEvent.EnterCause enterCause) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        final Player player = event.getPlayer();
        boolean cancelled = false;
        final Faction fromFaction = this.plugin.getFactionManager().getFactionAt(from);
        final Faction toFaction = this.plugin.getFactionManager().getFactionAt(to);
        if (!Objects.equals(fromFaction, toFaction)) {
            final PlayerClaimEnterEvent calledEvent = new PlayerClaimEnterEvent(player, from, to, fromFaction, toFaction, enterCause);
            Bukkit.getPluginManager().callEvent((Event) calledEvent);
            cancelled = calledEvent.isCancelled();
        } else if (toFaction instanceof CapturableFaction) {
            final CapturableFaction capturableFaction = (CapturableFaction) toFaction;
            for (final CaptureZone captureZone : capturableFaction.getCaptureZones()) {
                final Cuboid cuboid = captureZone.getCuboid();
                if (cuboid != null) {
                    final boolean containsFrom = cuboid.contains(from);
                    final boolean containsTo = cuboid.contains(to);
                    if (containsFrom && !containsTo) {
                        final CaptureZoneLeaveEvent calledEvent2 = new CaptureZoneLeaveEvent(player, capturableFaction, captureZone);
                        Bukkit.getPluginManager().callEvent((Event) calledEvent2);
                        cancelled = calledEvent2.isCancelled();
                        break;
                    }
                    if (!containsFrom && containsTo) {
                        final CaptureZoneEnterEvent calledEvent3 = new CaptureZoneEnterEvent(player, capturableFaction, captureZone);
                        Bukkit.getPluginManager().callEvent((Event) calledEvent3);
                        cancelled = calledEvent3.isCancelled();
                        break;
                    }
                    continue;
                }
            }
        }
        if (cancelled) {
            if (enterCause == PlayerClaimEnterEvent.EnterCause.TELEPORT) {
                event.setCancelled(true);
            } else {
                from.add(0.5, 0.0, 0.5);
                event.setTo(from);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(final PlayerMoveEvent event) {
        this.handleMove(event, PlayerClaimEnterEvent.EnterCause.MOVEMENT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            Faction factionAt = plugin.getFactionManager().getFactionAt(entity.getLocation());
            if (factionAt instanceof GlowstoneFaction) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(final PlayerTeleportEvent event) {
        this.handleMove(event, PlayerClaimEnterEvent.EnterCause.TELEPORT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (((factionAt instanceof ClaimableFaction)) && (!(factionAt instanceof PlayerFaction))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonExtend(final BlockPistonExtendEvent event) {
        final Block block = event.getBlock();
        if (block == null) {
            return;
        }

        final Block targetBlock = block.getRelative(event.getDirection(), event.getLength() + 1);
        if (targetBlock == null) {
            return;
        }

        Faction factionAt = this.plugin.getFactionManager().getFactionAt(block);
        if (factionAt == null) {
            return;
        }

        Faction targetFaction = this.plugin.getFactionManager().getFactionAt(targetBlock.getLocation());
        if (targetFaction == null) {
            return;
        }

        if (targetFaction instanceof Raidable && !((Raidable) targetFaction).isRaidable() && !targetFaction.equals(factionAt)) {
            event.setCancelled(true);

            int targetType = targetBlock.getTypeId();
            byte data = targetBlock.getData();
            block.setType(Material.AIR);

            Bukkit.getScheduler().runTask(plugin, () -> {
                targetBlock.setTypeIdAndData(targetType, data, false);
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonRetract(final BlockPistonRetractEvent event) {
        if (!event.isSticky()) {
            return;
        }
        final Location retractLocation = event.getRetractLocation();
        final Block retractBlock = retractLocation.getBlock();
        if (retractBlock == null) {
            return;
        }
        if (!retractBlock.isEmpty() && !retractBlock.isLiquid()) {
            final Block block = event.getBlock();
            final Faction targetFaction = this.plugin.getFactionManager().getFactionAt(retractLocation);
            if (targetFaction == null) {
                return;
            }
            Faction factionAt = this.plugin.getFactionManager().getFactionAt(block);
            if (factionAt == null) {
                return;
            }
            if (targetFaction instanceof Raidable && !((Raidable) targetFaction).isRaidable() && !targetFaction.equals(factionAt)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFromTo(final BlockFromToEvent event) {
        final Block toBlock = event.getToBlock();
        final Block fromBlock = event.getBlock();
        final Material fromType = fromBlock.getType();
        final Material toType = toBlock.getType();
        if ((toType == Material.REDSTONE_WIRE || toType == Material.TRIPWIRE) && (fromType == Material.AIR || fromType == Material.STATIONARY_LAVA || fromType == Material.LAVA)) {
            toBlock.setType(Material.AIR);
        }
        if ((toBlock.getType() == Material.WATER || toBlock.getType() == Material.STATIONARY_WATER || toBlock.getType() == Material.LAVA || toBlock.getType() == Material.STATIONARY_LAVA) && !canBuildAt(fromBlock.getLocation(), toBlock.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent e) {
        final Block toBlock = e.getNewState().getBlock();
        final Block fromBlock = e.getBlock();
        final Material fromType = fromBlock.getType();
        final Material toType = e.getNewState().getType();
        if ((toType == Material.REDSTONE_WIRE || toType == Material.TRIPWIRE) && (fromType == Material.AIR || fromType == Material.STATIONARY_LAVA || fromType == Material.LAVA)) {
            toBlock.setType(Material.AIR);
        }
        if ((toBlock.getType() == Material.WATER || toBlock.getType() == Material.STATIONARY_WATER || toBlock.getType() == Material.LAVA || toBlock.getType() == Material.STATIONARY_LAVA) && !canBuildAt(fromBlock.getLocation(), toBlock.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            final Faction toFactionAt = this.plugin.getFactionManager().getFactionAt(event.getTo());
            if (toFactionAt.isSafezone() && !this.plugin.getFactionManager().getFactionAt(event.getFrom()).isSafezone()) {
                final Player player = event.getPlayer();
                player.sendMessage(ConfigurationService.RED + "You cannot Enderpearl into safe-zones, used Enderpearl has been refunded.");
                this.plugin.getTimerManager().enderPearlTimer.refund(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PortalCreateEvent event) {
        Block block = Iterables.getFirst(event.getBlocks(), null);
        if(block != null && block.getWorld().getEnvironment() == World.Environment.NETHER) {
            if(event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
                event.setCancelled(true);
            }
            else if(Math.abs(block.getX()) < ConfigurationService.SPAWN_BUFFER && Math.abs(block.getZ()) < ConfigurationService.SPAWN_BUFFER){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(final PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Player player = event.getPlayer();
            Faction fromFac = this.plugin.getFactionManager().getFactionAt(from);
            if (fromFac.isSafezone() || (
                    from.getWorld().getEnvironment() == World.Environment.NETHER
                    && Math.abs(from.getBlockX()) <= ConfigurationService.SPAWN_RADIUS_MAP.get(World.Environment.NETHER)
                    && Math.abs(from.getBlockZ()) <= ConfigurationService.SPAWN_RADIUS_MAP.get(World.Environment.NETHER)
            )) {
                event.setTo(to.getWorld().getSpawnLocation().add(0.5, 0.0, 0.5));
                event.useTravelAgent(false);
                player.sendMessage(ConfigurationService.YELLOW + "You were teleported to the spawn of target world as you were in a safe-zone.");
                return;
            }
            /*
            if (event.useTravelAgent() && to.getWorld().getEnvironment() == World.Environment.NORMAL) {
                Location foundPortal;
                if (from.getBlock().hasMetadata("PORTAL")) {
                    return;
                }
                else {
                    TravelAgent travelAgent = event.getPortalTravelAgent();
                    if (!travelAgent.getCanCreatePortal()) {
                        return;
                    }
                    foundPortal = travelAgent.findPortal(to);
                    if (foundPortal != null) {
                        from.getBlock().setMetadata("PORTAL", new FixedMetadataValue(plugin, "PORTAL"));
                        return;
                    }
                }
                Faction factionAt = this.plugin.getFactionManager().getFactionAt(to);
                if (factionAt instanceof ClaimableFaction) {
                    Faction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                    if (playerFaction != null && playerFaction.equals(factionAt)) {
                        return;
                    }
                    player.sendMessage(ConfigurationService.YELLOW + "Portal would have created portal in territory of " + factionAt.getDisplayName(player) + ConfigurationService.YELLOW + '.');
                    event.setCancelled(true);
                }
            }
            */
        }
    }

    @EventHandler( priority = EventPriority.HIGH)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        switch (reason){
            case SPAWNER_EGG:
            case CUSTOM: {
                event.setCancelled(false);
                return;
            }
        }
        if(!event.isCancelled()) {
            final Location location = event.getLocation();
            final Faction factionAt = this.plugin.getFactionManager().getFactionAt(location);
            if (!(factionAt.isSafezone() && reason == CreatureSpawnEvent.SpawnReason.SPAWNER) && location.getWorld().getEnvironment() == World.Environment.NORMAL) {
                int x = Math.abs(location.getBlockX());
                int z = Math.abs(location.getBlockZ());
                if (x < ConfigurationService.WARZONE_RADIUS && z < ConfigurationService.WARZONE_RADIUS) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof Player) {
            final Player player = (Player) entity;
            final Faction playerFactionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());
            final EntityDamageEvent.DamageCause cause = event.getCause();
            if (playerFactionAt.isSafezone() && cause != EntityDamageEvent.DamageCause.SUICIDE) {
                event.setCancelled(true);
            }
            final Player attacker = BukkitUtils.getFinalAttacker(event, true);
            if (attacker != null) {
                final Faction attackerFactionAt = this.plugin.getFactionManager().getFactionAt(attacker.getLocation());
                if (attackerFactionAt.isSafezone()) {
                    event.setCancelled(true);
                    attacker.sendMessage(ConfigurationService.RED + "You cannot attack players whilst in safe-zones.");
                    return;
                }
                if (playerFactionAt.isSafezone()) {
                    attacker.sendMessage(ConfigurationService.RED + "You cannot attack players that are in safe-zones.");
                    return;
                }
                final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                final PlayerFaction attackerFaction;
                if (playerFaction != null && (attackerFaction = this.plugin.getFactionManager().getPlayerFaction(attacker)) != null) {
                    final Role role = playerFaction.getMember(player).getRole();
                    final String astrix = role.getAstrix();
                    if (attackerFaction.equals(playerFaction)) {
                        attacker.sendMessage(ConfigurationService.TEAMMATE_COLOUR + astrix + player.getName() + ConfigurationService.YELLOW + " is in your faction.");
                        event.setCancelled(true);
                    } else if (attackerFaction.getAllied().contains(playerFaction.getUniqueID())) {
                        attacker.sendMessage(ChatColor.AQUA + "You cannot hit " + ConfigurationService.TEAMMATE_COLOUR + astrix + player.getName() + ChatColor.AQUA + " as they are an ally.");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        final Entity entered = event.getEntered();
        if (entered instanceof Player) {
            final Vehicle vehicle = event.getVehicle();
            if (vehicle instanceof Horse) {
                final Horse horse = (Horse) event.getVehicle();
                final AnimalTamer owner = horse.getOwner();
                if (owner != null && !owner.equals(entered)) {
                    ((Player) entered).sendMessage(ChatColor.GREEN + "You cannot enter a Horse that belongs to " + ConfigurationService.RED + owner.getName() + ConfigurationService.YELLOW + '.');
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        final Entity entity = (Entity) event.getEntity();
        if (entity instanceof Player && ((Player) entity).getFoodLevel() < event.getFoodLevel() && this.plugin.getFactionManager().getFactionAt(entity.getLocation()).isSafezone()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionSplash(final PotionSplashEvent event) {
        final ThrownPotion potion = event.getEntity();
        if (!BukkitUtils.isDebuff(potion)) {
            return;
        }
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(potion.getLocation());
        if (factionAt.isSafezone()) {
            event.setCancelled(true);
            return;
        }
        final ProjectileSource source = potion.getShooter();
        if (source instanceof Player) {
            final Player player = (Player) source;
            for (final LivingEntity affected : event.getAffectedEntities()) {
                if (affected instanceof Player && !player.equals(affected)) {
                    final Player target = (Player) affected;
                    if (target.equals(source)) {
                        continue;
                    }
                    if (!this.plugin.getFactionManager().getFactionAt(target.getLocation()).isSafezone()) {
                        continue;
                    }
                    event.setIntensity(affected, 0.0);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityTarget(final EntityTargetEvent event) {
        switch (event.getReason()) {
            case CLOSEST_PLAYER:
            case RANDOM_TARGET: {
                final Entity target = event.getTarget();
                if (!(event.getEntity() instanceof LivingEntity)) {
                    break;
                }
                if (!(target instanceof Player)) {
                    break;
                }
                final Faction factionAt = this.plugin.getFactionManager().getFactionAt(target.getLocation());
                final Faction playerFaction;
                if (factionAt.isSafezone() || ((playerFaction = this.plugin.getFactionManager().getPlayerFaction((Player) target)) != null && factionAt.equals(playerFaction))) {
                    event.setCancelled(true);
                    break;
                }
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();
        if (action == Action.PHYSICAL && !attemptBuild(event.getPlayer(), block.getLocation(), null, ConfigurationService.KIT_MAP)) {
            event.setCancelled(true);
        }
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Material type = block.getType();
            boolean canBuild = !ProtectionListener.BLOCK_INTERACTABLES.contains(type);
            if (canBuild) {
                final Material itemType = event.hasItem() ? event.getItem().getType() : null;
                if (itemType != null && ProtectionListener.ITEM_BLOCK_INTERACTABLES.containsKey((Object) itemType) && ProtectionListener.ITEM_BLOCK_INTERACTABLES.get((Object) itemType).contains((Object) event.getClickedBlock().getType())) {
                    canBuild = false;
                } else {
                    final MaterialData materialData = block.getState().getData();
                    if (materialData instanceof Cauldron) {
                        final Cauldron cauldron = (Cauldron) materialData;
                        if (!cauldron.isEmpty() && event.hasItem() && event.getItem().getType() == Material.GLASS_BOTTLE) {
                            canBuild = false;
                        }
                    }
                }
            }
            if (!canBuild && !attemptBuild(event.getPlayer(), block.getLocation(), ConfigurationService.YELLOW + "You cannot do this in the territory of %1$s" + ConfigurationService.YELLOW + '.', true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBurn(final BlockBurnEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof WarzoneFaction || factionAt instanceof RoadFaction || (factionAt instanceof Raidable && !((Raidable) factionAt).isRaidable())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFade(final BlockFadeEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLeavesDelay(final LeavesDecayEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockForm(final BlockFormEvent event) {
        final Faction factionAt = this.plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && !attemptBuild(entity, event.getBlock().getLocation(), null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!attemptBuild((Entity) event.getPlayer(), event.getBlock().getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.', false, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (ConfigurationService.KIT_MAP && event.getItemInHand() != null && event.getItemInHand().getType() == Material.WEB) {
            Faction faction = plugin.getFactionManager().getFactionAt(event.getBlockPlaced());
            if (faction == null || (!faction.isSafezone() && !(faction instanceof EventFaction))) {
                return;
            }
        }
        Block blockPlaced = event.getBlockPlaced();
        if (!attemptBuild(event.getPlayer(), blockPlaced.getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        if (!attemptBuild((Entity) event.getPlayer(), event.getBlockClicked().getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (!attemptBuild((Entity) event.getPlayer(), event.getBlockClicked().getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent event) {
        final Entity remover = event.getRemover();
        if (remover instanceof Player && !attemptBuild(remover, event.getEntity().getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingPlace(final HangingPlaceEvent event) {
        if (!attemptBuild((Entity) event.getPlayer(), event.getEntity().getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingDamageByEntity(final EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof Hanging) {
            final Player attacker = BukkitUtils.getFinalAttacker((EntityDamageEvent) event, false);
            if (!attemptBuild((Entity) attacker, entity.getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onHangingInteractByPlayer(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        if (entity instanceof Hanging && !attemptBuild((Entity) event.getPlayer(), entity.getLocation(), ConfigurationService.YELLOW + "You cannot build in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player && e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof BlockState) {
            BlockState blockState = (BlockState) e.getInventory().getHolder();
            Block block = blockState.getBlock();
            if (!attemptBuild(e.getPlayer(), block.getLocation(), ConfigurationService.YELLOW + "You cannot do this in the territory of %1$s" + ConfigurationService.YELLOW + '.', true)) {
                e.setCancelled(true);
            }
        }
    }
}