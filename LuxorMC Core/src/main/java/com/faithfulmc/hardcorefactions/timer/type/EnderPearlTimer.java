package com.faithfulmc.hardcorefactions.timer.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.TimerRunnable;
import com.faithfulmc.util.Config;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.util.com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class EnderPearlTimer extends PlayerTimer implements org.bukkit.event.Listener {
    private static final long REFRESH_DELAY_TICKS = 2L;
    private static final long REFRESH_DELAY_TICKS_18 = 20L;
    private static final long EXPIRE_SHOW_MILLISECONDS = 1500L;
    private final ConcurrentMap<Object, Object> itemNameFakes;
    private final JavaPlugin plugin;

    public EnderPearlTimer(HCF plugin) {
        super("Enderpearl", TimeUnit.SECONDS.toMillis(15L), false);
        this.plugin = plugin;
        this.itemNameFakes = CacheBuilder.newBuilder().expireAfterWrite(this.defaultCooldown + 1500L + 5000L, TimeUnit.MILLISECONDS).build().asMap();
    }

    public String getScoreboardPrefix() {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD;
    }

    public void load(Config config) {
        super.load(config);
        java.util.Collection<UUID> cooldowned = this.cooldowns.keySet();
        for (UUID uuid : cooldowned) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                startDisplaying(player);
            }
        }
    }

    public void onExpire(UUID userUUID) {
        super.onExpire(userUUID);
        Player player = Bukkit.getPlayer(userUUID);
        if (player != null) {
            player.sendMessage(ChatColor.RED + "Your " + getDisplayName() + ChatColor.RED + " cooldown has expired.");
        }
    }

    public TimerRunnable clearCooldown(UUID playerUUID) {
        TimerRunnable runnable = super.clearCooldown(playerUUID);
        if (runnable != null) {
            this.itemNameFakes.remove(playerUUID);
            return runnable;
        }
        return null;
    }

    public void clearCooldown(Player player) {
        stopDisplaying(player);
        super.clearCooldown(player);
    }

    public void refund(Player player) {
        player.getInventory().addItem(new ItemStack[]{new org.bukkit.inventory.ItemStack(Material.ENDER_PEARL, 1)});
        clearCooldown(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(PlayerInteractEvent event) {
        Action action = event.getAction();
        if(event.useItemInHand() != Event.Result.DENY && action == Action.RIGHT_CLICK_AIR) {
            ItemStack itemStack = event.getItem();
            if (itemStack != null && itemStack.getType() == Material.ENDER_PEARL) {
                Player shooter = event.getPlayer();
                long remaining = getRemaining(shooter);
                if (remaining > 0L) {
                    shooter.sendMessage(ConfigurationService.RED + "You still have a " + getDisplayName() + ConfigurationService.RED + " cooldown for another " + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + '.');
                    event.setUseItemInHand(Event.Result.DENY);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        org.bukkit.entity.Projectile projectile = event.getEntity();
        if ((projectile instanceof EnderPearl)) {
            EnderPearl enderPearl = (EnderPearl) projectile;
            org.bukkit.projectiles.ProjectileSource source = enderPearl.getShooter();
            if ((source instanceof Player)) {
                Player shooter = (Player) source;
                long remaining = getRemaining(shooter);
                if (remaining > 0L) {
                    shooter.sendMessage(ConfigurationService.RED + "You still have a " + getDisplayName() + ConfigurationService.RED + " cooldown for another " + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + '.');
                    shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                    event.setCancelled(true);
                    return;
                }
                if (setCooldown(shooter, shooter.getUniqueId(), this.defaultCooldown, true)) {
                    startDisplaying(shooter);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PearlNameFaker pearlNameFaker = (PearlNameFaker) this.itemNameFakes.get(player.getUniqueId());
        if (pearlNameFaker != null) {
            int previousSlot = event.getPreviousSlot();
            org.bukkit.inventory.ItemStack item = player.getInventory().getItem(previousSlot);
            if (item == null) {
                return;
            }
            pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(item), previousSlot);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Player player;
        PearlNameFaker pearlNameFaker;
        if ((humanEntity instanceof Player)) {
            player = (Player) humanEntity;
            pearlNameFaker = (PearlNameFaker) this.itemNameFakes.get(player.getUniqueId());
            if (pearlNameFaker == null) {
                return;
            }
            for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry : event.getNewItems().entrySet()) {
                if (entry.getKey() == player.getInventory().getHeldItemSlot()) {
                    pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(player.getItemInHand()), player.getInventory().getHeldItemSlot());
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if ((humanEntity instanceof Player)) {
            final Player player = (Player) humanEntity;
            PearlNameFaker pearlNameFaker = (PearlNameFaker) this.itemNameFakes.get(player.getUniqueId());
            if (pearlNameFaker == null) {
                return;
            }
            int heldSlot = player.getInventory().getHeldItemSlot();
            if (event.getSlot() == heldSlot) {
                pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(player.getItemInHand()), heldSlot);
            } else if (event.getHotbarButton() == heldSlot) {
                pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(event.getCurrentItem()), event.getSlot());
                new BukkitRunnable() {
                    public void run() {
                        player.updateInventory();
                    }
                }.runTask(this.plugin);
            }
        }
    }

    public void startDisplaying(Player player) {
        PearlNameFaker pearlNameFaker;
        if ((getRemaining(player) > 0L) && (this.itemNameFakes.putIfAbsent(player.getUniqueId(), pearlNameFaker = new PearlNameFaker(this, player)) == null)) {
            long ticks = ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() >= 47 ? 20L : 2L;
            pearlNameFaker.runTaskTimerAsynchronously(this.plugin, ticks, ticks);
        }
    }

    public void stopDisplaying(Player player) {
        PearlNameFaker pearlNameFaker = (PearlNameFaker) this.itemNameFakes.remove(player.getUniqueId());
        if (pearlNameFaker != null) {
            pearlNameFaker.cancel();
        }
    }

    public static class PearlNameFaker extends BukkitRunnable {
        private final PlayerTimer timer;
        private Player player;

        public PearlNameFaker(PlayerTimer timer, Player player) {
            this.timer = timer;
            this.player = player;
        }

        public void run() {
            if(player == null || !player.isOnline()){
                cancel();
            }
            else {
                org.bukkit.inventory.ItemStack stack = this.player.getItemInHand();
                if ((stack != null) && (stack.getType() == Material.ENDER_PEARL)) {
                    long remaining = this.timer.getRemaining(this.player);
                    net.minecraft.server.v1_7_R4.ItemStack item = CraftItemStack.asNMSCopy(stack);
                    if (remaining > 0L) {
                        item = item.cloneItemStack();
                        item.c(ConfigurationService.YELLOW + "Enderpearl Cooldown: " + ChatColor.GRAY + HCF.getRemaining(remaining, true, true));
                        setFakeItem(item, this.player.getInventory().getHeldItemSlot());
                    } else {
                        cancel();
                    }
                }
            }
        }

        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            if(player != null && player.isOnline()) {
                setFakeItem(CraftItemStack.asNMSCopy(this.player.getItemInHand()), this.player.getInventory().getHeldItemSlot());
            }
            player = null;
        }

        public void setFakeItem(net.minecraft.server.v1_7_R4.ItemStack nms, int index) {
            if (player == null || !player.isOnline()) {
                return;
            }
            EntityPlayer entityPlayer = ((CraftPlayer) this.player).getHandle();
            if (index < net.minecraft.server.v1_7_R4.PlayerInventory.getHotbarSize()) {
                index += 36;
            } else if (index > 35) {
                index = 8 - (index - 36);
            }
            entityPlayer.playerConnection.sendPacket(new net.minecraft.server.v1_7_R4.PacketPlayOutSetSlot(entityPlayer.activeContainer.windowId, index, nms));
        }
    }
}