package com.faithfulmc.hardcorefactions.logger;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.google.common.base.Function;
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoggerEntity extends EntityVillager {
    private static final Function DAMAGE_FUNCTION;

    private static PlayerNmsResult getResult(World world, UUID playerUUID) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID); // Use bukkit to load this
        if (offlinePlayer.hasPlayedBefore()) {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            EntityPlayer entityPlayer = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), worldServer, new GameProfile(playerUUID, offlinePlayer.getName()), new PlayerInteractManager(worldServer));
            CraftPlayer player = entityPlayer.getBukkitEntity();
            if (player != null) {
                player.loadData();
                return new PlayerNmsResult(player, entityPlayer);
            }
        }
        return null;
    }

    static {
        DAMAGE_FUNCTION = (f1 -> 0.0);
    }

    private final UUID playerUUID;

    public LoggerEntity(final World world, final Location location, final Player player) {
        super(((CraftWorld) world).getHandle());
        this.lastDamager = ((CraftPlayer) player).getHandle().lastDamager;
        final double x = player.getLocation().getX();
        final double y = player.getLocation().getY();
        final double z = player.getLocation().getZ();
        final String playerName = player.getName();

        this.playerUUID = player.getUniqueId();
        getBukkitEntity().setMaxHealth(player.getMaxHealth());
        getBukkitEntity().setHealth(player.getHealth());
        setInvisible(false);
        this.setCustomName(ConfigurationService.GRAY + "(Combat Logger) " + ConfigurationService.RED + playerName);
        this.setCustomNameVisible(true);
        setPositionRotation(x, y, z, location.getYaw(), location.getPitch());
        fallDistance = player.getFallDistance();
        this.world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        /*
        PacketPlayOutSpawnEntityLiving entitySpawn = new PacketPlayOutSpawnEntityLiving(this);
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING, entitySpawn);
        for (org.bukkit.entity.Entity entity : getBukkitEntity().getNearbyEntities(15, 15, 15)) {
            if (entity instanceof Player) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket((Player) entity, container);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        */
        retrack();
    }

    public EntityAgeable createChild(EntityAgeable entityAgeable) {
        return null;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void move(final double d0, final double d1, final double d2) {
        super.move(0, d1, 0);
    }

    public void b(final int i) {
    }

    public void dropDeathLoot(boolean flag, int i) {
    }

    public Entity findTarget() {
        return null;
    }

    public boolean damageEntity(final DamageSource damageSource, final float amount) {
        PlayerNmsResult nmsResult = getResult(this.world.getWorld(), this.playerUUID);
        if (nmsResult == null) {
            return true;
        }
        EntityPlayer entityPlayer = nmsResult.entityPlayer;
        if (entityPlayer != null) {
            entityPlayer.setPosition(this.locX, this.locY, this.locZ);
            EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(entityPlayer, damageSource, (double) amount, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, LoggerEntity.DAMAGE_FUNCTION, LoggerEntity.DAMAGE_FUNCTION, LoggerEntity.DAMAGE_FUNCTION, LoggerEntity.DAMAGE_FUNCTION, LoggerEntity.DAMAGE_FUNCTION, LoggerEntity.DAMAGE_FUNCTION);
            if (event.isCancelled()) {
                return false;
            }
        }
        return super.damageEntity(damageSource, amount);
    }

    public boolean a(EntityHuman entityHuman) {
        return false;
    }

    public void h() {
        super.h();
    }

    public void collide(Entity entity) {
    }

    public void die(DamageSource damageSource) {
        final PlayerNmsResult playerNmsResult = getResult((World) this.world.getWorld(), this.playerUUID);
        if (playerNmsResult != null) {
            Player player = playerNmsResult.player;
            PlayerInventory inventory = player.getInventory();
            boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");
            List<ItemStack> drops = new ArrayList<>();
            if (!keepInventory) {
                for (ItemStack loggerDeathEvent : inventory.getContents()) {
                    if (loggerDeathEvent != null && loggerDeathEvent.getType() != Material.AIR) {
                        drops.add(loggerDeathEvent);
                    }
                }
                for (ItemStack loggerDeathEvent : inventory.getArmorContents()) {
                    if (loggerDeathEvent != null && loggerDeathEvent.getType() != Material.AIR) {
                        drops.add(loggerDeathEvent);
                    }
                }
            }
            String var13 = this.combatTracker.b().c();
            EntityPlayer var14 = playerNmsResult.entityPlayer;
            var14.combatTracker = this.combatTracker;
            if (Bukkit.getPlayer(var14.getName()) != null) {
                Bukkit.getPlayer(var14.getUniqueID()).kickPlayer(var13);
            }
            PlayerDeathEvent var15 = CraftEventFactory.callPlayerDeathEvent(var14, drops, var13, keepInventory);
            var13 = var15.getDeathMessage();
            if (var13 != null && !var13.isEmpty()) {
                Bukkit.broadcastMessage(var13);
            }
            super.die(damageSource);
            LoggerDeathEvent var16 = new LoggerDeathEvent(this);
            Bukkit.getPluginManager().callEvent(var16);
            if (!var15.getKeepInventory()) {
                inventory.clear();
                inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
            }
            var14.setLocation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            var14.setHealth(0.0f);
            player.saveData();
            ((WorldServer)world).getTracker().untrackEntity(this);
            if(!playerNmsResult.player.isOnline()) {
                ((WorldServer) playerNmsResult.entityPlayer.world).getTracker().untrackEntity(playerNmsResult.entityPlayer);
            }
        }
    }

    public CraftLivingEntity getBukkitEntity() {
        return (CraftLivingEntity) super.getBukkitEntity();
    }

    public static final class PlayerNmsResult {
        public final Player player;
        public final EntityPlayer entityPlayer;

        public PlayerNmsResult(Player player, EntityPlayer entityPlayer) {
            this.player = player;
            this.entityPlayer = entityPlayer;
        }
    }
}