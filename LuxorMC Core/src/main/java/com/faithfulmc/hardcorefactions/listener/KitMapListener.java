package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.SpawnFaction;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.kit.event.KitApplyEvent;
import com.google.common.cache.CacheBuilder;
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KitMapListener implements Listener {
    private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(5);
    private static final String[] KITS = {"PvP", "Bard", "Archer", "Rogue", "Builder"};
    private static final ChatColor[] COLOURS = {ChatColor.AQUA, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.RED, ChatColor.GOLD};
    final HCF plugin;
    private final String longlines = ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + StringUtils.repeat('-', 11);
    private final ConcurrentMap lastClicks = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).concurrencyLevel(1).build().asMap();

    public KitMapListener(HCF plugin) {
        this.plugin = plugin;
    }

    public int randomTick(){
        return ThreadLocalRandom.current().nextInt(40);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (item != null) {
            new BukkitRunnable() {
                public void run() {
                    item.remove();
                }
            }.runTaskLater(plugin, (20 * 5) + randomTick());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getWorld().getEnvironment() == World.Environment.NORMAL && to.getBlockY() < from.getBlockY()){
            Block block = to.getBlock();
            Material material = block.getType();
            if(material == Material.WATER || material == Material.STATIONARY_WATER){
                Faction faction = plugin.getFactionManager().getFactionAt(block);
                if(faction instanceof SpawnFaction){
                    Material type = block.getType();
                    if (type == Material.STATIONARY_WATER || type == Material.WATER) {
                        new BukkitRunnable() {
                            public void run() {
                                player.setFallDistance(0);
                                player.setNoDamageTicks(0);
                                player.teleport(plugin.getEndSpawn());
                            }
                        }.runTask(plugin);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKitApply(KitApplyEvent event) {
        if(Arrays.asList(KITS).contains(event.getKit().getName())) {
            event.setCancelled(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (item != null) {
            new BukkitRunnable() {
                public void run() {
                    item.remove();
                }
            }.runTaskLater(plugin, (20 * 15) + randomTick());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignUpdate(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("hcf.kitmap.createsign")) {
            int i = 0;
            for (String kit : KITS) {
                if (e.getLine(0).equalsIgnoreCase("[" + kit + "]")) {
                    ChatColor chatColor = COLOURS[i];
                    e.setLine(0, longlines);
                    e.setLine(1, chatColor + "Kit");
                    e.setLine(2, chatColor + kit);
                    e.setLine(3, longlines);
                    return;
                }
                i++;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignClick(PlayerInteractEvent e) {
        if (e.useInteractedBlock() == Event.Result.ALLOW) {
            Block block = e.getClickedBlock();
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(longlines) && sign.getLine(3).equals(longlines)) {
                    String name = ChatColor.stripColor(sign.getLine(2));
                    Kit kit;
                    if ((kit = plugin.getKitManager().getKit(name)) != null){
                        Player player = e.getPlayer();
                        if (!plugin.getStaffModeListener().isStaff(player)) {
                            long now = System.currentTimeMillis();
                            long diff = now - (Long) lastClicks.getOrDefault(player.getUniqueId(), 0L);
                            if (diff > COOLDOWN) {
                                for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                                    player.removePotionEffect(potionEffect.getType());
                                }
                                player.getInventory().clear();
                                player.getInventory().setArmorContents(new ItemStack[4]);
                                kit.applyTo(player, true, true);
                                lastClicks.put(player.getUniqueId(), now);
                                player.updateInventory();
                            } else {
                                player.sendMessage(ConfigurationService.RED + "You are on cooldown for " + ChatColor.BOLD + HCF.getRemaining(COOLDOWN - diff, true, true));
                            }
                        }
                    }
                }
            }
        }
    }

    private final Set<UUID> dyingList = new HashSet<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event){
        if(!String.valueOf(BasePlugin.getPlugin().getGlobalMessager().getId()).toLowerCase().contains("dev")) return;
        Entity entity = event.getEntity();
        if(entity instanceof Player){
            Player player = (Player) entity;
            if(event.getFinalDamage() > player.getHealth()){
                event.setDamage(0.0);

                synchronized (dyingList){
                    if(dyingList.add(player.getUniqueId())){
                        Runnable runnable = () -> {
                            synchronized (dyingList){
                                if(!dyingList.remove(player.getUniqueId())){
                                    return;
                                }
                            }
                            Location deathLocation = player.getLocation();
                            EntityPlayer oldPlayer = ((CraftPlayer) player).getHandle();
                            EntityPlayer entityPlayer = new EntityPlayer(MinecraftServer.getServer(), (WorldServer) oldPlayer.getWorld(), oldPlayer.getProfile(), new PlayerInteractManager(oldPlayer.getWorld()));
                            entityPlayer.setPositionRotation(oldPlayer.locX, oldPlayer.locY, oldPlayer.locZ, oldPlayer.yaw, oldPlayer.pitch);
                            Vector velocity = player.getKiller() != null ? player.getLocation().clone().subtract(player.getKiller().getLocation()).toVector().normalize().multiply(0.5) : player.getLocation().getDirection().multiply(-0.5);
                            velocity.setY(0.25);
                            entityPlayer.motX = velocity.getX();
                            entityPlayer.motY = velocity.getY();
                            entityPlayer.motZ = velocity.getZ();
                            ((WorldServer) entityPlayer.world).tracker.a(oldPlayer, new PacketPlayOutNamedEntitySpawn(entityPlayer));
                            entityPlayer.setHealth(0.0f);
                            ((WorldServer) entityPlayer.world).tracker.a(oldPlayer, new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true));
                            ((WorldServer) entityPlayer.world).tracker.a(oldPlayer, new PacketPlayOutNamedSoundEffect("game.player.die", entityPlayer.locX, entityPlayer.locY, entityPlayer.locZ, 0.75f, 1f));
                            List<ItemStack> itemStackList = new ArrayList<>();
                            itemStackList.addAll(Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).collect(Collectors.toList()));
                            itemStackList.addAll(Arrays.stream(player.getInventory().getArmorContents()).filter(Objects::nonNull).collect(Collectors.toList()));
                            oldPlayer.setHidden(true);
                            player.getInventory().clear();
                            player.getInventory().setArmorContents(new ItemStack[4]);
                            for(PotionEffect potionEffect: player.getActivePotionEffects()){
                                player.removePotionEffect(potionEffect.getType());
                            }
                            player.setHealth(player.getMaxHealth());
                            player.setFoodLevel(20);
                            player.setSaturation(1f);
                            player.setTotalExperience(0);
                            player.setExp(0f);
                            player.setLevel(0);
                            player.updateInventory();
                            plugin.getTimerManager().spawnTagTimer.clearCooldown(player);
                            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                            PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent(player, itemStackList, 0, -1, ((EntityLiving)((CraftEntity)entity).getHandle()).combatTracker.b().c());
                            Bukkit.getPluginManager().callEvent(playerDeathEvent);
                            for(ItemStack itemStack: playerDeathEvent.getDrops()){
                                deathLocation.getWorld().dropItem(deathLocation, itemStack);
                            }
                        };

                        if(Bukkit.isPrimaryThread()) runnable.run();
                        else Bukkit.getScheduler().runTask(plugin, runnable);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event){
        event.setCancelled(true);
    }
}
