package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.StaffPriority;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.util.BukkitUtils;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockAction;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VanishListener implements Listener {
    private static final String CHEST_INTERACT_PERMISSION = "vanish.chestinteract";
    private static final String INVENTORY_INTERACT_PERMISSION = "vanish.inventorysee";
    private static final String FAKE_CHEST_PREFIX = "[F] ";
    private static final String BLOCK_INTERACT_PERMISSION = "vanish.build";

    public static void handleFakeChest(final Player player, Chest chest, final boolean open) {
        final Inventory chestInventory = chest.getInventory();
        if (chestInventory instanceof DoubleChestInventory) {
            chest = (Chest) ((DoubleChestInventory) chestInventory).getHolder().getLeftSide();
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutBlockAction(chest.getX(), chest.getY(), chest.getZ(), Blocks.CHEST, 1, (open ? 1 : 0)));
        player.playSound(chest.getLocation(), open ? Sound.CHEST_OPEN : Sound.CHEST_CLOSE, 1.0f, 1.0f);
    }
    private final Map<UUID, Location> fakeChestLocationMap;
    private final BasePlugin plugin;

    public VanishListener(final BasePlugin plugin) {
        this.fakeChestLocationMap = new HashMap<>();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        BaseUser baseUser = plugin.getUserManager().getUser(player.getUniqueId());
        baseUser.updateVanishedState(player, baseUser.isVanished());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        BaseUser baseUser = this.plugin.getUserManager().getUser(player.getUniqueId());
        baseUser.updateVanishedState(player, baseUser.isVanished());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (this.plugin.getUserManager().getUser(event.getPlayer().getUniqueId()).isVanished()) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        if (entity instanceof Player) {
            final Player player = event.getPlayer();
            if (!player.isSneaking() && player.hasPermission("vanish.inventorysee") && this.plugin.getUserManager().getUser(player.getUniqueId()).isVanished()) {
                player.openInventory(((Player) entity).getInventory());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityTarget(final EntityTargetEvent event) {
        if (event.getReason() == EntityTargetEvent.TargetReason.CUSTOM) {
            return;
        }
        final Entity target = event.getTarget();
        final Entity entity = event.getEntity();
        if ((entity instanceof ExperienceOrb || entity instanceof LivingEntity) && target instanceof Player) {
            Player targetPlayer = (Player) target;
            if (targetPlayer.isOnline()) {
                BaseUser baseUser = this.plugin.getUserManager().getUser(targetPlayer.getUniqueId());
                if (baseUser == null || baseUser.isVanished()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDropItem(final PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if(player.isOnline()) {
            if (this.plugin.getUserManager().getUser(player.getUniqueId()).isVanished()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if(player.isOnline()) {
        if (this.plugin.getUserManager().getUser(player.getUniqueId()).isVanished()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(player.isOnline()) {
            if (this.plugin.getUserManager().getUser(player.getUniqueId()).isVanished()) {
                event.setDeathMessage(null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityDamage(final EntityDamageEvent event) {
        final EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.SUICIDE) {
            return;
        }
        final Entity entity = event.getEntity();
        if (entity instanceof Player) {
            final Player attacked = (Player) entity;
            if(attacked.isOnline()) {
                final BaseUser attackedUser = this.plugin.getUserManager().getUser(attacked.getUniqueId());
                final Player attacker = BukkitUtils.getFinalAttacker(event, true);
                if (attackedUser.isVanished()) {
                    if (attacker != null && StaffPriority.of(attacked) != StaffPriority.NONE) {
                        attacker.sendMessage(ChatColor.RED + "That player is vanished.");
                    }
                    event.setCancelled(true);
                    return;
                }
                if (attacker != null && this.plugin.getUserManager().getUser(attacker.getUniqueId()).isVanished() && !attacker.hasPermission("staff.kill")) {
                    attacker.sendMessage(ChatColor.RED + "You cannot attack players whilst vanished.");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final BaseUser baseUser = this.plugin.getUserManager().getUser(player.getUniqueId());
        if (baseUser.isVanished() && !player.hasPermission("vanish.build")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot build whilst vanished.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final BaseUser baseUser = this.plugin.getUserManager().getUser(player.getUniqueId());
        if (baseUser.isVanished() && !player.hasPermission("vanish.build")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot build whilst vanished.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        final Player player = event.getPlayer();
        final BaseUser baseUser = this.plugin.getUserManager().getUser(player.getUniqueId());
        if (baseUser.isVanished() && !player.hasPermission("vanish.build")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot build whilst vanished.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        switch (event.getAction()) {
            case PHYSICAL: {
                if (this.plugin.getUserManager().getUser(uuid).isVanished()) {
                    event.setCancelled(true);
                    break;
                }
                break;
            }
            case RIGHT_CLICK_BLOCK: {
                final org.bukkit.block.Block block = event.getClickedBlock();
                final BlockState state = block.getState();
                if (!(state instanceof Chest)) {
                    break;
                }
                if (!this.plugin.getUserManager().getUser(uuid).isVanished()) {
                    break;
                }
                final Chest chest = (Chest) state;
                final Location chestLocation = chest.getLocation();
                final InventoryType type = chest.getInventory().getType();
                if (type == InventoryType.CHEST && this.fakeChestLocationMap.putIfAbsent(uuid, chestLocation) == null) {
                    final ItemStack[] contents = chest.getInventory().getContents();
                    final Inventory fakeInventory = Bukkit.createInventory((InventoryHolder) null, contents.length, "[F] " + type.getDefaultTitle());
                    fakeInventory.setContents(contents);
                    event.setCancelled(true);
                    player.openInventory(fakeInventory);
                    handleFakeChest(player, chest, true);
                    this.fakeChestLocationMap.put(uuid, chestLocation);
                    break;
                }
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onInventoryClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final Location chestLocation;
        if ((chestLocation = this.fakeChestLocationMap.remove(player.getUniqueId())) != null) {
            final BlockState blockState = chestLocation.getBlock().getState();
            if (blockState instanceof Chest) {
                handleFakeChest(player, (Chest) blockState, false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onInventoryClick(final InventoryClickEvent event) {
        final HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            final Player player = (Player) humanEntity;
            if (this.fakeChestLocationMap.containsKey(player.getUniqueId())) {
                final ItemStack stack = event.getCurrentItem();
                if (stack != null && stack.getType() != Material.AIR && !player.hasPermission("vanish.chestinteract")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot interact with fake chest inventories.");
                }
            }
        }
    }
}
