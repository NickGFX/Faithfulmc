package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.framework.command.SimpleCommandManager;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.command.LFFCommand;
import com.faithfulmc.hardcorefactions.command.ToggleDeathMessagesCommand;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreListener implements Listener {
    private final HCF plugin;

    public CoreListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event){
        if(event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_OTHER) {
            String reason = event.getKickMessage();
            if (reason == null || !reason.contains("restarting")) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("unqueue");
                out.writeUTF(event.getUniqueId().toString());
                List<? extends Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (playerList.isEmpty()) {
                    return;
                }
                Player player;
                if (playerList.size() == 1) {
                    player = playerList.get(0);
                } else {
                    player = playerList.get(ThreadLocalRandom.current().nextInt(playerList.size()));
                }
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        CreatureSpawner creatureSpawner = e.getSpawner();
        new BukkitRunnable() {
            public void run() {
                creatureSpawner.setDelay(creatureSpawner.getSpawnedType() == EntityType.GHAST ? 1000 : 200);
            }
        }.runTaskLater(plugin, 20);
    }

    @EventHandler
    public void onDamaging(EntityDamageByEntityEvent event) {
        if ((event.getDamager() instanceof Player)) {
            Player p = (Player) event.getDamager();
            if (p.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                        int level = effect.getAmplifier() + 1;
                        double newDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE) / (level * 1.45D + 1.1D) * (ConfigurationService.ORIGINS ? 1.4D : 1.5D);
                        double damagePercent = newDamage / event.getDamage(EntityDamageEvent.DamageModifier.BASE);
                        try {
                            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) * damagePercent);
                        } catch (Exception localException) {
                            localException.printStackTrace();
                        }
                        try {
                            event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) * damagePercent);
                        } catch (Exception localException1) {
                            localException1.printStackTrace();
                        }
                        try {
                            event.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE, event.getDamage(EntityDamageEvent.DamageModifier.RESISTANCE) * damagePercent);
                        } catch (Exception localException2) {
                            localException2.printStackTrace();
                        }
                        if(event.getEntity() instanceof Player) {
                            try {
                                event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) * damagePercent);
                            } catch (Exception localException3) {
                                localException3.printStackTrace();
                            }
                        }
                        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if ((args[0].contains(":")) && (!event.getPlayer().hasPermission("staff.bypass"))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(SimpleCommandManager.PERMISSION_MESSAGE);
        }
    }

    @EventHandler
    public void onMobKiller(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            FactionUser factionUser = plugin.getUserManager().getUser(killer.getUniqueId());
            int typeId = event.getEntity().getType().getTypeId();
            factionUser.getMobs().put(typeId, factionUser.getMobs().getOrDefault(typeId, 0) + 1);
        }
    }

    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {
                event.setCancelled(true);
                event.getWorld().setStorm(false);
                event.getWorld().setThundering(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);
        FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
        factionUser.setName(player.getName());
        factionUser.setLastSeen(System.currentTimeMillis());
        factionUser.setOnline(true);
        if(!factionUser.isFdalerts()){
            player.setMetadata(FoundDiamondsListener.NO_DIAMOND_ALERTS, new FixedMetadataValue(plugin, true));
        }
        if(!factionUser.isLffalerts()){
            player.setMetadata(LFFCommand.LFF_META, new FixedMetadataValue(plugin, true));
        }
        if(!factionUser.isOreInventory()){
            player.setMetadata(OreListener.NO_OREINVENTORY_META, new FixedMetadataValue(plugin, true));
        }
        if(factionUser.isNomobdrops()){
            player.setMetadata(PickupListener.NO_MOBDROPS_META, new FixedMetadataValue(plugin, true));
        }
        if(factionUser.isNocobble()){
            player.setMetadata(PickupListener.NO_COBBLE_META, new FixedMetadataValue(plugin, true));
        }
        if(!factionUser.isDeathMessages()){
            player.setMetadata(ToggleDeathMessagesCommand.NO_DEATH_MESSAGES_META, new FixedMetadataValue(plugin, true));
        }
        factionUser.setFaction(plugin.getFactionManager().getPlayerFaction(player));
        if(factionUser.getFaction() != null){
            PlayerFaction playerFaction = (PlayerFaction) factionUser.getFaction();
            FactionMember factionMember = playerFaction.getMember(player);
            if(factionMember != null) {
                factionMember.setFactionUser(factionUser);
                factionMember.setName(player.getName());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!player.isOp()) {
            Block block = event.getBlock();
            Material type = block.getType();
            if (type == Material.MOB_SPAWNER) {
                if (block.getWorld().getEnvironment() == World.Environment.NETHER) {
                    player.sendMessage(ConfigurationService.RED + "You cannot break mob spawners in the nether!");
                    event.setCancelled(true);
                } else if (block.getWorld().getEnvironment() == World.Environment.THE_END) {
                    player.sendMessage(ConfigurationService.RED + "You cannot break mob spawners in the end!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
        factionUser.setPlaytime(factionUser.getCurrentPlaytime());
        factionUser.setLastSeen(System.currentTimeMillis());
        factionUser.setShowClaimMap(false);
        factionUser.setFaction(plugin.getFactionManager().getPlayerFaction(player));
        factionUser.setOnline(false);
        this.plugin.getVisualiseHandler().clearAll(player, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        this.plugin.getUserManager().getUser(player.getUniqueId()).setShowClaimMap(false);
    }
}
