package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.events.tracker.ConquestTracker;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.event.*;
import com.faithfulmc.hardcorefactions.faction.struct.RegenStatus;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.google.common.base.Optional;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.concurrent.TimeUnit;

public class FactionListener implements Listener {
    private static final long FACTION_JOIN_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String FACTION_JOIN_WAIT_WORDS = DurationFormatUtils.formatDurationWords(FACTION_JOIN_WAIT_MILLIS, true, true);
    private static final String LAND_CHANGED_META_KEY = "landChangedMessage";
    private static final long LAND_CHANGE_MSG_THRESHOLD = 225L;
    private final HCF plugin;

    public FactionListener(HCF plugin) {
        this.plugin = plugin;
    }

    public String C(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public boolean containsBlocked(String phrase){
        phrase = phrase.toLowerCase();
        for(String word: ChatListener.blocked){
            if(phrase.contains(word)){
                return true;
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionCreate(FactionCreateEvent event) {
        Faction faction = event.getFaction();
        if (faction instanceof PlayerFaction) {
            PlayerFaction playerFaction = (PlayerFaction) faction;
            if (event.getSender() != null && event.getSender() instanceof Player) {
                FactionMember factionMember = playerFaction.getMember((Player) event.getSender());
                factionMember.getFactionUser().setFaction(playerFaction);
                faction.setTotal_kills(factionMember.getFactionUser().getKills());
            }
            CommandSender sender = event.getSender();
            String name = event.getFaction().getName();
            boolean blocked = containsBlocked(name);
            if (!blocked) {
                Bukkit.broadcastMessage(C("&6" + ConfigurationService.DOUBLEARROW + " &c" + event.getFaction().getName() + "&e has been created by &a" + sender.getName() + "&e."));
            } else {
                sender.sendMessage(C("&6" + ConfigurationService.DOUBLEARROW + " &c" + event.getFaction().getName() + "&e has been created by &a" + sender.getName() + "&e."));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != sender && player.hasPermission("staffchat.use")) {
                        player.sendMessage(C("&c" + ConfigurationService.DOUBLEARROW + " &c" + event.getFaction().getName() + "&e has been created by &a" + sender.getName() + "&e."));
                    }
                }
            }
            playerFaction.getOnlinePlayers();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event) {
        Faction faction = event.getFaction();
        if (faction instanceof PlayerFaction) {
            CommandSender sender = event.getSender();
            String name = event.getFaction().getName();
            boolean blocked = containsBlocked(name);
            if (!blocked) {
                Bukkit.broadcastMessage(C("&6" + ConfigurationService.DOUBLEARROW + " &c" + event.getFaction().getName() + "&e has been disbanded by &a" + sender.getName() + "&e."));
            } else {
                sender.sendMessage(C("&6" + ConfigurationService.DOUBLEARROW + " &c" + event.getFaction().getName() + "&e has been disbanded by &a" + sender.getName() + "&e."));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != sender && player.hasPermission("staffchat.use")) {
                        player.sendMessage(C("&c" + ConfigurationService.DOUBLEARROW + " &c" + event.getFaction().getName() + "&e has been disbanded by &a" + sender.getName() + "&e."));
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRename(FactionRenameEvent event) {
        Faction faction = event.getFaction();
        CommandSender sender = event.getSender();
        if (faction instanceof PlayerFaction) {
            String name = event.getFaction().getName();
            boolean blocked = containsBlocked(name);
            if (!blocked) {
                Bukkit.broadcastMessage(C("&6" + ConfigurationService.DOUBLEARROW + " &c" + event.getOriginalName() + "&e has been renamed to &c" + event.getNewName() + "&e by &a" + sender.getName() + "&e."));
            } else {
                sender.sendMessage(C("&6" + ConfigurationService.DOUBLEARROW + " &c" + event.getOriginalName() + "&e has been renamed to &c" + event.getNewName() + "&e by &a" + sender.getName() + "&e."));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != sender && player.hasPermission("staffchat.use")) {
                        player.sendMessage(C("&c" + ConfigurationService.DOUBLEARROW + " &c" + event.getOriginalName() + "&e has been renamed to &c" + event.getNewName() + "&e by &a" + sender.getName() + "&e."));
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRenameMonitor(FactionRenameEvent event) {
        Faction faction = event.getFaction();
        if ((faction instanceof KothFaction)) {
            ((KothFaction) faction).getCaptureZone().setName(event.getNewName());
        }
    }

    private long getLastLandChangedMeta(Player player) {
        MetadataValue value = player.getMetadata(LAND_CHANGED_META_KEY).iterator().hasNext() ? player.getMetadata(LAND_CHANGED_META_KEY).iterator().next() : null;
        long millis = System.currentTimeMillis();
        long remaining = value == null ? 0L : value.asLong() - millis;
        if (remaining <= 0L) {
            player.setMetadata(LAND_CHANGED_META_KEY, new FixedMetadataValue(this.plugin, millis + LAND_CHANGE_MSG_THRESHOLD));
        }
        return remaining;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(CaptureZoneEnterEvent event) {
        Player player = event.getPlayer();
        if ((getLastLandChangedMeta(player) <= 0L) && (this.plugin.getUserManager().getUser(player.getUniqueId()).isCapzoneEntryAlerts())) {
            if (event.getFaction().getEventType() == EventType.CONQUEST) {
                player.sendMessage(ConquestTracker.PREFIX + ConfigurationService.GOLD + "Now entering capture zone: " + event.getCaptureZone().getDisplayName());
            } else {
                player.sendMessage(ConfigurationService.YELLOW + "Now entering capture zone: " + event.getCaptureZone().getDisplayName() + ConfigurationService.YELLOW + '(' + event.getFaction().getName() + ConfigurationService.YELLOW + ')');
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(CaptureZoneLeaveEvent event) {
        Player player = event.getPlayer();
        if ((getLastLandChangedMeta(player) <= 0L) && (this.plugin.getUserManager().getUser(player.getUniqueId()).isCapzoneEntryAlerts())) {
            if (event.getFaction().getEventType() == EventType.CONQUEST) {
                player.sendMessage(ConquestTracker.PREFIX + ConfigurationService.GOLD + "Now leaving capture zone: " + event.getCaptureZone().getDisplayName());
            } else {
                player.sendMessage(ConfigurationService.YELLOW + "Now leaving capture zone: " + event.getCaptureZone().getDisplayName() + ConfigurationService.YELLOW + '(' + event.getFaction().getName() + ConfigurationService.YELLOW + ')');
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerClaimEnter(PlayerClaimEnterEvent event) {
        Faction toFaction = event.getToFaction();
        Player player = event.getPlayer();
        if (toFaction.isSafezone()) {
            player.setHealth(( player).getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setSaturation(4.0F);
            if(!player.hasMetadata("safezone")) {
                player.setMetadata("safezone", new FixedMetadataValue(plugin, true));
            }
        }
        else if(player.hasMetadata("safezone")){
            player.removeMetadata("safezone", plugin);
        }
        if (getLastLandChangedMeta(player) <= 0L) {
            Faction fromFaction = event.getFromFaction();
            player.sendMessage(ConfigurationService.YELLOW + "Now leaving: " + fromFaction.getDisplayName(player) + ConfigurationService.YELLOW + " (" + (fromFaction.isDeathban() ? ConfigurationService.RED + "Deathban" : ChatColor.GREEN + "Non-Deathban") + ConfigurationService.YELLOW + ')');
            player.sendMessage(ConfigurationService.YELLOW + "Now entering: " + toFaction.getDisplayName(player) + ConfigurationService.YELLOW + " (" + (toFaction.isDeathban() ? ConfigurationService.RED + "Deathban" : ChatColor.GREEN + "Non-Deathban") + ConfigurationService.YELLOW + ')');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event) {
        Optional<Player> optionalPlayer = event.getPlayer();
        if (optionalPlayer.isPresent()) {
            FactionUser factionUser = plugin.getUserManager().getUser((optionalPlayer.get()).getUniqueId());
            factionUser.setLastFactionLeaveMillis(System.currentTimeMillis());
            if(plugin.getTimerManager().teleportTimer.getRemaining(optionalPlayer.get()) > 0) {
                plugin.getTimerManager().teleportTimer.cancelTeleport(optionalPlayer.get(), null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPreFactionJoin(PlayerJoinFactionEvent event) {
        Faction faction = event.getFaction();
        Optional optionalPlayer = event.getPlayer();
        if (((faction instanceof PlayerFaction)) && (optionalPlayer.isPresent())) {
            Player player = (Player) optionalPlayer.get();
            PlayerFaction playerFaction = (PlayerFaction) faction;
            if ((!this.plugin.getEotwHandler().isEndOfTheWorld()) && (playerFaction.getRegenStatus() == RegenStatus.PAUSED)) {
                event.setCancelled(true);
                player.sendMessage(ConfigurationService.RED + "You cannot join factions that are not regenerating DTR.");
                return;
            }
            long difference = this.plugin.getUserManager().getUser(player.getUniqueId()).getLastFactionLeaveMillis() - System.currentTimeMillis() + FACTION_JOIN_WAIT_MILLIS;
            if ((difference > 0L) && (!player.hasPermission("hcf.faction.argument.staff.forcejoin"))) {
                event.setCancelled(true);
                player.sendMessage(ConfigurationService.RED + "You cannot join factions after just leaving within " + FACTION_JOIN_WAIT_WORDS + ". " + "You gotta wait another " + DurationFormatUtils.formatDurationWords(difference, true, true) + '.');
                return;
            }
            if(ConfigurationService.ORIGINS && plugin.getHcfClassManager().getEquippedClass(player) instanceof ArcherClass && playerFaction.getOnlineArchers().size() >= 2){
                plugin.getHcfClassManager().setEquippedClass(player, null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionLeave(PlayerLeaveFactionEvent event) {
        Faction faction = event.getFaction();
        if ((faction instanceof PlayerFaction)) {
            Optional<Player> optional = event.getPlayer();
            if (optional.isPresent()) {
                Player player = optional.get();
                if (this.plugin.getFactionManager().getFactionAt(player.getLocation()).equals(faction)) {
                    event.setCancelled(true);
                    player.sendMessage(ConfigurationService.RED + "You cannot leave your faction whilst you remain in its' territory.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction != null) {
            playerFaction.printDetails(player);
            playerFaction.broadcast(ConfigurationService.YELLOW + "A member of your faction came online " + ChatColor.GREEN + playerFaction.getMember(player).getRole().getAstrix() + player.getName() + ConfigurationService.GOLD + '.', player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction != null) {
            playerFaction.broadcast(ConfigurationService.YELLOW + "A member of your faction went offline " + ChatColor.GREEN + playerFaction.getMember(player).getRole().getAstrix() + player.getName() + ConfigurationService.GOLD + '.');
        }
    }
}
