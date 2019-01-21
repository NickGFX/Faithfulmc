package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.cuboid.CoordinatePair;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;
import net.minecraft.server.v1_7_R4.MobSpawnerAbstract;
import net.minecraft.server.v1_7_R4.TileEntityMobSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
//import org.bukkit.event.player.PlayerBreedEntityEvent;
//import org.bukkit.event.player.PlayerTemptEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.Map;

public class MobstackListener extends BukkitRunnable implements Listener {
    private static final int NATURAL_STACK_RADIUS = 20;
    private static final int MAX_STACKED_QUANTITY = 150;
    private static final int OTHER_STACK_RADIUS = 8;
    public static final String STACKED_PREFIX = BaseConstants.GOLD.toString() + "x";

    private final Table<CoordinatePair, EntityType, Integer> naturalSpawnStacks;
    private final BasePlugin plugin;
    private Map<MobSpawnerAbstract, Integer> mobSpawnerAbstractIntegerMap = Maps.newHashMap();

    public MobstackListener(final BasePlugin plugin) {
        this.naturalSpawnStacks = HashBasedTable.create();
        this.plugin = plugin;
        runTaskTimer(plugin, 40, 20 * 60);
    }

    private CoordinatePair fromLocation(final Location location) {
        return new CoordinatePair(location.getWorld(), Math.round(location.getBlockX() / NATURAL_STACK_RADIUS), Math.round(location.getBlockZ() / NATURAL_STACK_RADIUS));
    }

    public void run() {
        long now = System.currentTimeMillis();
        for (final World world : Bukkit.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.THE_END) {
                for (final LivingEntity entity : world.getLivingEntities()) {
                    if (entity.isValid() && !entity.isDead()) {
                        if (!(entity instanceof Animals || entity instanceof Monster)) {
                            continue;
                        }
                        for (final Entity nearby : entity.getNearbyEntities(OTHER_STACK_RADIUS, OTHER_STACK_RADIUS, OTHER_STACK_RADIUS)) {
                            if (nearby != null && nearby instanceof LivingEntity && !nearby.isDead() && nearby.isValid()) {
                                if (!(nearby instanceof Animals || nearby instanceof Monster)) {
                                    continue;
                                }
                                if (stack((LivingEntity) nearby, entity)) {
                                    if (naturalSpawnStacks.containsValue(entity.getEntityId())) {
                                        for (Map.Entry<CoordinatePair, Integer> entry : naturalSpawnStacks.column(entity.getType()).entrySet()) {
                                            if (entry.getValue() == entity.getEntityId()) {
                                                naturalSpawnStacks.put(entry.getKey(), entity.getType(), nearby.getEntityId());
                                                break;
                                            }
                                        }
                                    } else if (mobSpawnerAbstractIntegerMap.containsValue(entity.getEntityId())) {
                                        for (Map.Entry<MobSpawnerAbstract, Integer> entry : mobSpawnerAbstractIntegerMap.entrySet()) {
                                            if (entry.getValue() == entity.getEntityId()) {
                                                mobSpawnerAbstractIntegerMap.put(entry.getKey(), nearby.getEntityId());
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerTemptEntity(final PlayerTemptEntityEvent event) {
        if (canRemove(event.getEntity())) {
            final int stackedQuantity = this.getStackedQuantity((LivingEntity) event.getEntity());
            if (stackedQuantity >= MAX_STACKED_QUANTITY) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "This entity is already max stacked.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerBreedEntity(final PlayerBreedEntityEvent event) {
        if (event.getEntity() instanceof Horse) {
            return;
        }
        if (canRemove(event.getEntity())) {
            final LivingEntity chosen = plugin.getRandom().nextBoolean() ? event.getFirstParent() : event.getSecondParent();
            int stackedQuantity = this.getStackedQuantity(chosen);
            if (stackedQuantity == -1) {
                stackedQuantity = 1;
            }
            this.setStackedQuantity(chosen, Math.min(MAX_STACKED_QUANTITY, stackedQuantity + 1));
            event.getPlayer().sendMessage(ChatColor.GREEN + "One of the adults bred has been increased a stack.");
            event.setCancelled(true);
        }
    }

    */

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!canRemove(event.getEntity())) {
            return;
        }
        if (event.getSpawner().getWorld().getEnvironment() != World.Environment.THE_END) {
            CreatureSpawner creatureSpawner = event.getSpawner();
            TileEntityMobSpawner tile = (TileEntityMobSpawner) ((CraftWorld) creatureSpawner.getWorld()).getTileEntityAt(creatureSpawner.getX(), creatureSpawner.getY(), creatureSpawner.getZ());
            Integer integer = mobSpawnerAbstractIntegerMap.get(tile.getSpawner());
            if (integer != null) {
                final net.minecraft.server.v1_7_R4.Entity nmsTarget = ((CraftWorld) creatureSpawner.getWorld()).getHandle().getEntity(integer);
                if (nmsTarget != null) {
                    Entity target = nmsTarget.getBukkitEntity();
                    if (target != null && target instanceof LivingEntity && target.isValid() && !target.isDead() && target.getLocation().distance(creatureSpawner.getBlock().getLocation()) < 10) {
                        event.setCancelled(true);
                        LivingEntity targetLiving = (LivingEntity) target;
                        int stackedQuantity = getStackedQuantity(targetLiving);
                        if (stackedQuantity == -1) {
                            stackedQuantity = 1;
                        }
                        setStackedQuantity(targetLiving, Math.min(MAX_STACKED_QUANTITY, stackedQuantity + 1));
                        return;
                    }
                }
            }
            mobSpawnerAbstractIntegerMap.put(tile.getSpawner(), event.getEntity().getEntityId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        final EntityType entityType = event.getEntityType();
        switch (event.getSpawnReason()) {
            case CHUNK_GEN:
            case NATURAL:
            case DEFAULT: {
                final Location location = event.getLocation();
                final CoordinatePair coordinatePair = this.fromLocation(location);
                final Optional<Integer> entityIdOptional = Optional.fromNullable(this.naturalSpawnStacks.get(coordinatePair, entityType));
                if (entityIdOptional.isPresent()) {
                    int entityId =  entityIdOptional.get();
                    Entity target = BasePlugin.getPlugin().getNmsProvider().getEntityFromID(location.getWorld(), entityId);
                    if (target != null && target instanceof LivingEntity) {
                        final LivingEntity targetLiving = (LivingEntity) target;
                        boolean canSpawn;
                        if (targetLiving instanceof Ageable) {
                            canSpawn = ((Ageable) targetLiving).isAdult();
                        } else {
                            canSpawn = (!(targetLiving instanceof Zombie) || !((Zombie) targetLiving).isBaby());
                        }
                        if (canSpawn) {
                            int stackedQuantity = this.getStackedQuantity(targetLiving);
                            if (stackedQuantity == -1) {
                                stackedQuantity = 1;
                            }
                            int stacked = Math.min(MAX_STACKED_QUANTITY, stackedQuantity + 1);
                            setStackedQuantity(targetLiving, stacked);
                            event.setCancelled(true);
                        }
                    }
                }
                this.naturalSpawnStacks.put(coordinatePair, entityType, event.getEntity().getEntityId());
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity livingEntity = event.getEntity();
        if (livingEntity == null) {
            return;
        }
        int stackedQuantity = getStackedQuantity(livingEntity);
        if (stackedQuantity > 1) {
            LivingEntity respawned = (LivingEntity) livingEntity.getWorld().spawnEntity(livingEntity.getLocation(), event.getEntityType());
            ((CraftLivingEntity) respawned).getHandle().fromMobSpawner = true;
            setStackedQuantity(respawned, Math.min(MAX_STACKED_QUANTITY, stackedQuantity - 1));
            if (respawned instanceof Ageable) {
                ((Ageable) respawned).setAdult();
            }
            if(respawned instanceof Slime){
                ((Slime) respawned).setSize(3);
            }
            if (respawned instanceof Zombie) {
                ((Zombie) respawned).setBaby(false);
            }
            if (mobSpawnerAbstractIntegerMap.containsValue(livingEntity.getEntityId())) {
                for (Map.Entry<MobSpawnerAbstract, Integer> entry : mobSpawnerAbstractIntegerMap.entrySet()) {
                    if (entry.getValue() == livingEntity.getEntityId()) {
                        mobSpawnerAbstractIntegerMap.put(entry.getKey(), respawned.getEntityId());
                        return;
                    }
                }
            } else if (naturalSpawnStacks.containsValue(livingEntity.getEntityId())) {
                for (Map.Entry<CoordinatePair, Integer> entry : naturalSpawnStacks.column(livingEntity.getType()).entrySet()) {
                    if (entry.getValue() == livingEntity.getEntityId()) {
                        naturalSpawnStacks.put(entry.getKey(), respawned.getType(), respawned.getEntityId());
                        return;
                    }
                }
            }
        } else {
            naturalSpawnStacks.values().remove(livingEntity.getEntityId());
            mobSpawnerAbstractIntegerMap.values().remove(livingEntity.getEntityId());
        }
    }

    private int getStackedQuantity(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return -1;
        }
        String customName = livingEntity.getCustomName();
        if (customName == null || !customName.contains(MobstackListener.STACKED_PREFIX)) {
            return -1;
        }
        customName = customName.replace(MobstackListener.STACKED_PREFIX, "");
        if (customName == null) {
            return -1;
        }
        customName = ChatColor.stripColor(customName);
        return Ints.tryParse(customName);
    }

    private boolean stack(LivingEntity tostack, LivingEntity toremove) {
        if (tostack == null || toremove == null || !tostack.isValid() || !toremove.isValid() || toremove.getType() != tostack.getType() || tostack instanceof MagmaCube || tostack instanceof Slime || tostack instanceof Villager) {
            return false;
        }
        Integer newStack = 1;
        Integer removeStack = 1;
        if (this.hasStack(tostack)) {
            newStack = this.getStackedQuantity(tostack);
        }
        if (this.hasStack(toremove)) {
            removeStack = this.getStackedQuantity(toremove);
        } else if (this.getStackedQuantity(toremove) == -1 && toremove.getCustomName() != null && toremove.getCustomName().contains(ChatColor.WHITE.toString())) {
            return false;
        }
        toremove.remove();
        tostack.eject();
        toremove.eject();
        setStackedQuantity(tostack, Math.min(MAX_STACKED_QUANTITY, newStack + removeStack));
        return true;
    }

    public boolean canRemove(Entity toremove) {
        return !(toremove instanceof MagmaCube || toremove instanceof Slime || toremove instanceof Villager);
    }

    private boolean hasStack(final LivingEntity livingEntity) {
        return this.getStackedQuantity(livingEntity) != -1;
    }

    private void setStackedQuantity(final LivingEntity livingEntity, int quantity) {
        livingEntity.eject();
        livingEntity.setPassenger(null);
        if (quantity <= 1) {
            livingEntity.setCustomName(null);
        } else {
            livingEntity.setCustomName(MobstackListener.STACKED_PREFIX + quantity);
            livingEntity.setCustomNameVisible(false);
        }
    }
}
