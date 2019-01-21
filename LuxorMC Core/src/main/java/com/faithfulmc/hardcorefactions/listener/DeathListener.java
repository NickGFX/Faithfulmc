package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.Config;
import com.faithfulmc.util.JavaUtils;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import net.minecraft.server.v1_7_R4.EntityLightning;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DeathListener implements Listener {
    public static HashMap<UUID, ItemStack[]> inventoryContents = new HashMap<>();
    public static HashMap<UUID, ItemStack[]> armorContents = new HashMap<>();
    private final HCF plugin;

    public static final String DEATH_META = "DEATH_KILLER";

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ')', replacement);
    }

    public DeathListener(HCF plugin) {
        this.plugin = plugin;
        reloadData();
    }

    public void reloadData() {
        if(ConfigurationService.KIT_MAP){
            return;
        }
        Config deathdata = new Config(plugin, "death");
        if (deathdata.contains("inventories")) {
            for (String uid : deathdata.getConfigurationSection("inventories").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uid);
                    Object data = deathdata.get("inventories." + uid);
                    if (data instanceof ItemStack[]) {
                        ItemStack[] items = (ItemStack[]) data;
                        inventoryContents.put(uuid, items);
                    } else if (data instanceof List) {
                        List<ItemStack> list = ((List<ItemStack>) data);
                        ItemStack[] items = list.toArray(new ItemStack[list.size()]);
                        inventoryContents.put(uuid, items);
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        if (deathdata.contains("armor")) {
            try {
                for (String uid : deathdata.getConfigurationSection("armor").getKeys(false)) {
                    UUID uuid = UUID.fromString(uid);
                    Object data = deathdata.get("armor." + uid);
                    if (data instanceof ItemStack[]) {
                        ItemStack[] items = (ItemStack[]) data;
                        armorContents.put(uuid, items);
                    } else if (data instanceof List) {
                        List<ItemStack> list = ((List<ItemStack>) data);
                        ItemStack[] items = list.toArray(new ItemStack[list.size()]);
                        armorContents.put(uuid, items);
                    }
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeathKillIncrement(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        Entity entityKiller = killer;
        Player player = event.getEntity();
        if(killer == null){
            entityKiller = getKiller(event);
            if(entityKiller instanceof Player){
                killer = (Player) entityKiller;
            }
        }
        if(killer == null){
            EntityDamageEvent entityDamageEvent = player.getLastDamageCause();
            if(entityDamageEvent instanceof EntityDamageByEntityEvent){
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
                if(entityDamageByEntityEvent.getDamager() instanceof Player){
                    killer = (Player) entityDamageByEntityEvent.getDamager();
                }
                if(entityKiller == null){
                    entityKiller = entityDamageByEntityEvent.getDamager();
                }
            }
        }
        player.removeMetadata(DEATH_META, plugin);
        if(killer != null) {
            player.setMetadata(DEATH_META, new FixedMetadataValue(plugin, killer));
        }
        FactionUser slain = this.plugin.getUserManager().getUser(player.getUniqueId());
        slain.setKillStreak(0);
        slain.setDeaths(slain.getDeaths() + 1);
        if(slain.getFaction() != null && !ConfigurationService.ORIGINS){
            slain.getPlayerFaction().setPoints(Math.max(0, slain.getPlayerFaction().getPoints() - 1));
        }
        String originalMessage = event.getDeathMessage();
        if (killer != null) {
            if(ConfigurationService.STATTRACK) {
                stattrack(player, killer);
            }
            FactionUser user = this.plugin.getUserManager().getUser(killer.getUniqueId());
            user.setKills(user.getKills() + 1);
            user.setKillStreak(user.getKillStreak() + 1);
            if(user.getFaction() != null){
                user.getFaction().setTotal_kills(user.getFaction().getTotal_kills() + 1);
                if(!ConfigurationService.ORIGINS){
                    user.getPlayerFaction().setPoints(user.getPlayerFaction().getPoints() + 1);
                }
            }
        }
        event.setDeathMessage(getDeathMessage(originalMessage, player, entityKiller));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player.getUniqueId());
        if (playerFaction != null && !ConfigurationService.KIT_MAP) {
            Faction factionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());
            Role role = playerFaction.getMember(player.getUniqueId()).getRole();
            if (playerFaction.getDeathsUntilRaidable() >= -5.0D) {
                playerFaction.setDeathsUntilRaidable(playerFaction.getDeathsUntilRaidable() - factionAt.getDtrLossMultiplier());
                playerFaction.setRemainingRegenerationTime(ConfigurationService.DTR_FREEZE_TIME_MILLIS + playerFaction.getOnlinePlayers().size() * TimeUnit.MINUTES.toMillis(2L));
                playerFaction.broadcast(ConfigurationService.YELLOW + "A member of your faction died " + ConfigurationService.TEAMMATE_COLOUR + role.getAstrix() + player.getName() + ConfigurationService.YELLOW + ". DTR:" + ConfigurationService.GRAY + " [" + playerFaction.getDtrColour() + JavaUtils.format(playerFaction.getDeathsUntilRaidable()) + ConfigurationService.WHITE + '/' + ConfigurationService.WHITE + playerFaction.getMaximumDeathsUntilRaidable() + ConfigurationService.GRAY + "].");
            } else {
                playerFaction.setRemainingRegenerationTime(ConfigurationService.DTR_FREEZE_TIME_MILLIS + playerFaction.getOnlinePlayers().size() * TimeUnit.MINUTES.toMillis(2L));
                playerFaction.broadcast(ConfigurationService.YELLOW + "A member of your faction died " + ConfigurationService.TEAMMATE_COLOUR + role.getAstrix() + player.getName() + ConfigurationService.YELLOW + ". DTR:" + ConfigurationService.GRAY + " [" + playerFaction.getDtrColour() + JavaUtils.format(playerFaction.getDeathsUntilRaidable()) + ConfigurationService.WHITE + '/' + ConfigurationService.WHITE + playerFaction.getMaximumDeathsUntilRaidable() + ConfigurationService.GRAY + "].");
            }
        }
        if(!ConfigurationService.KIT_MAP) {
            inventoryContents.put(player.getUniqueId(), player.getInventory().getContents());
            armorContents.put(player.getUniqueId(), player.getInventory().getArmorContents());
        }
        Location location = player.getLocation();
        if (!ConfigurationService.KIT_MAP && Bukkit.spigot().getTPS()[0] > 16.0D) {
            WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
            EntityLightning entityLightning = new EntityLightning(worldServer, location.getX(), location.getY(), location.getZ(), false);
            PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(entityLightning);
            for (Player target : Bukkit.getOnlinePlayers()) {
                //if (this.plugin.getUserManager().getUser(target.getUniqueId()).isShowLightning()) {
                    ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet);
                    target.playSound(target.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                //}
            }

        }
        if(ConfigurationService.KIT_MAP && event.getNewExp() >= 0){
            new BukkitRunnable(){
                public void run() {
                    if(player.isOnline()) {
                        plugin.getTimerManager().spawnTagTimer.clearCooldown(player);
                        ((CraftPlayer) player).getHandle().setHidden(true);
                        player.spigot().respawn();
                    }
                }
            }.runTaskLater(plugin, 1);
        }
    }



    private Entity getKiller(PlayerDeathEvent event) {
        EntityLiving lastAttacker = ((CraftPlayer) event.getEntity()).getHandle().aX();
        return lastAttacker == null ? null : lastAttacker.getBukkitEntity();
    }

    private String getDeathMessage(String input, org.bukkit.entity.Entity entity, org.bukkit.entity.Entity killer) {
        input = input.replaceFirst("\\[", ConfigurationService.GRAY + "[" + ConfigurationService.GRAY);
        input = replaceLast(input, "]", ConfigurationService.GRAY + "]" + ConfigurationService.GRAY);
        if (entity != null) {
            input = input.replaceFirst("(?i)" + getEntityName(entity), ConfigurationService.RED + getDisplayName(entity) + (ConfigurationService.LUXOR ? ChatColor.RED : ConfigurationService.YELLOW));
        }
        if ((killer != null) && ((entity == null) || (!killer.equals(entity)))) {
            input = input.replaceFirst("(?i)" + getEntityName(killer), ConfigurationService.RED + getDisplayName(killer) + (ConfigurationService.LUXOR ? ChatColor.RED : ConfigurationService.YELLOW));
        }
        return input;
    }

    private String getStattrackMessage(org.bukkit.entity.Entity entity, org.bukkit.entity.Entity killer) {
        return ConfigurationService.RED + getEntityName(entity) + (ConfigurationService.LUXOR ? ChatColor.RED : ConfigurationService.YELLOW) + " was slain by " + ConfigurationService.RED + getEntityName(killer);
    }

    private String getEntityName(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity, "Entity cannot be null");
        return (entity instanceof Player) ? ((Player) entity).getName() : ((CraftEntity) entity).getHandle().getName();
    }

    private String getDisplayName(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity, "Entity cannot be null");
        if ((entity instanceof Player)) {
            Player player = (Player) entity;
            return player.getName() + ConfigurationService.GRAY + '[' + ConfigurationService.GRAY + this.plugin.getUserManager().getUser(player.getUniqueId()).getKills() + ConfigurationService.GRAY + ']';
        }
        return WordUtils.capitalizeFully(entity.getType().name().replace('_', ' '));
    }

    public static final String KILL_COUNTER_PREFIX = ChatColor.RESET.toString() + ChatColor.RED.toString() + ChatColor.YELLOW + ChatColor.BOLD.toString() + "Kill Counter: " + ChatColor.GRAY;

    public void stattrack(Player death, Player killer){
        ItemStack itemStack = killer.getItemInHand();
        if(itemStack != null){
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                List<String> lore = itemMeta.getLore();
                if(lore == null){
                    lore = new ArrayList<>();
                    lore.add(0, KILL_COUNTER_PREFIX + 1);
                }
                else{
                    boolean found = false;
                    for(int i = 0; i < lore.size(); i ++){
                        String loreLine = lore.get(i);
                        if(loreLine.startsWith(KILL_COUNTER_PREFIX)){
                            String killString = loreLine.substring(KILL_COUNTER_PREFIX.length(), loreLine.length());
                            int kills = Ints.tryParse(killString) + 1;
                            lore.remove(i);
                            lore.add(0, KILL_COUNTER_PREFIX + kills);
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        lore.add(0, KILL_COUNTER_PREFIX + 1);
                    }
                }
                lore.add(getStattrackMessage(death, killer));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                killer.setItemInHand(itemStack);
                killer.updateInventory();
            }
        }
    }

    public void saveData() {
        YamlConfiguration deathdata = new YamlConfiguration();
        for (Map.Entry<UUID, ItemStack[]> entry : inventoryContents.entrySet()) {
            deathdata.set("inventories." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, ItemStack[]> entry : armorContents.entrySet()) {
            deathdata.set("armor." + entry.getKey().toString(), entry.getValue());
        }
        try {
            deathdata.save(new File(plugin.getDataFolder(), "death.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
