package com.faithfulmc.hardcorefactions.events;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.*;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.event.CaptureZoneEnterEvent;
import com.faithfulmc.hardcorefactions.faction.event.CaptureZoneLeaveEvent;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.listener.EventSignListener;
import com.faithfulmc.hardcorefactions.timer.GlobalTimer;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.Config;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EventTimer extends GlobalTimer implements Listener {
    private static final long RESCHEDULE_FREEZE_MILLIS = TimeUnit.SECONDS.toMillis(15L);;
    private static final String RESCHEDULE_FREEZE_WORDS = DurationFormatUtils.formatDurationWords(EventTimer.RESCHEDULE_FREEZE_MILLIS, true, true);;
    public static long EVENT_FREQUENCY = TimeUnit.HOURS.toMillis(4);

    private final HCF plugin;
    private long startStamp;
    private long lastContestedEventMillis;
    private EventFaction eventFaction;
    private boolean nextCancelled = false;
    private Long lastEvent = null;
    private Long nextEvent = null;
    private EventFaction lastEventFaction;
    private EventFaction nextEventFaction;
    private boolean justAnnounced = false;
    private int sotwDay = -1;

    public EventTimer(final HCF plugin) {
        super("Event", 0L);
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Faction faction = plugin.getFactionManager().getFaction("Citadel");
            if (faction != null && faction instanceof CitadelFaction) {
                CitadelFaction citadelFaction = (CitadelFaction) faction;
                long now = System.currentTimeMillis();
                if (citadelFaction.canChestReset(now)) {
                    citadelFaction.setLastChestReset(now);
                    citadelFaction.fillChests(true);
                }
            }
        }, 20 * 60, 20 * 30);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (EventTimer.this.eventFaction != null) {
                EventTimer.this.eventFaction.getEventType().getEventTracker().tick(EventTimer.this, EventTimer.this.eventFaction);
            }
        }, 20, 20);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            if(ConfigurationService.ORIGINS){
                if(Bukkit.getServer().hasWhitelist()) {
                    nextEventFaction = null;
                    nextEvent = null;
                }
                else{
                    if(nextEvent == null) {
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int loops = 0;
                        while (nextEvent == null) {
                            int day = calendar.get(Calendar.DAY_OF_WEEK);
                            calendar.set(Calendar.HOUR_OF_DAY, 11);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            if (calendar.getTimeInMillis() < now) {
                                nextEvent = calendar.getTimeInMillis();
                                List<KothFaction> factions = plugin.getFactionManager().getFactions().stream().filter(faction -> faction instanceof KothFaction && !faction.getName().equalsIgnoreCase("palace") && !faction.getName().equalsIgnoreCase("hell") && faction != lastEventFaction).map(faction -> (KothFaction) faction).collect(Collectors.toList());
                                int size = factions.size();
                                if (size == 0) {
                                    plugin.getLogger().warning("No available KOTH factions found");
                                } else {
                                    if (size == 1) {
                                        nextEventFaction = factions.iterator().next();
                                    } else {
                                        nextEventFaction = factions.get(ThreadLocalRandom.current().nextInt(size));
                                    }
                                    return;
                                }
                            }

                            if (day == Calendar.FRIDAY) {
                                calendar.set(Calendar.HOUR_OF_DAY, 17);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                if (calendar.getTimeInMillis() < now) {
                                    nextEvent = calendar.getTimeInMillis();
                                    nextEventFaction = (EventFaction) plugin.getFactionManager().getFaction("Hell");
                                    return;
                                }
                            }
                            else if (day == Calendar.SATURDAY) {
                                calendar.set(Calendar.HOUR_OF_DAY, 15);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                if (calendar.getTimeInMillis() < now) {
                                    nextEvent = calendar.getTimeInMillis();
                                    nextEventFaction = (EventFaction) plugin.getFactionManager().getFaction("Palace");
                                    return;
                                }
                            }
                            else{
                                calendar.set(Calendar.HOUR_OF_DAY, 16);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                if (calendar.getTimeInMillis() < now) {
                                    nextEvent = calendar.getTimeInMillis();
                                    List<KothFaction> factions = plugin.getFactionManager().getFactions().stream().filter(faction -> faction instanceof KothFaction && !faction.getName().equalsIgnoreCase("palace") && !faction.getName().equalsIgnoreCase("hell") && faction != lastEventFaction).map(faction -> (KothFaction) faction).collect(Collectors.toList());
                                    int size = factions.size();
                                    if (size == 0) {
                                        plugin.getLogger().warning("No available KOTH factions found");
                                    } else {
                                        if (size == 1) {
                                            nextEventFaction = factions.iterator().next();
                                        } else {
                                            nextEventFaction = factions.get(ThreadLocalRandom.current().nextInt(size));
                                        }
                                        return;
                                    }
                                }

                                calendar.set(Calendar.HOUR_OF_DAY, 21);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                if (calendar.getTimeInMillis() < now) {
                                    nextEvent = calendar.getTimeInMillis();
                                    List<KothFaction> factions = plugin.getFactionManager().getFactions().stream().filter(faction -> faction instanceof KothFaction && !faction.getName().equalsIgnoreCase("palace") && !faction.getName().equalsIgnoreCase("hell") && faction != lastEventFaction).map(faction -> (KothFaction) faction).collect(Collectors.toList());
                                    int size = factions.size();
                                    if (size == 0) {
                                        plugin.getLogger().warning("No available KOTH factions found");
                                    } else {
                                        if (size == 1) {
                                            nextEventFaction = factions.iterator().next();
                                        } else {
                                            nextEventFaction = factions.get(ThreadLocalRandom.current().nextInt(size));
                                        }
                                        return;
                                    }
                                }
                            }
                            loops++;
                            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
                            if(loops > 1){
                                break;
                            }
                        }
                    }
                }
            }
            else {
                if (Bukkit.getServer().hasWhitelist()) {
                    nextCancelled = true;
                    plugin.getLogger().info("Cancelling next event due to whitelist");
                } else if (plugin.getTimerManager().sotw.getRemaining() > 0) {
                    nextCancelled = true;
                    nextEventFaction = null;
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(new Date(now));
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + ConfigurationService.FIRST_KOTH_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, ConfigurationService.FIRST_KOTH_HOUR);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    sotwDay = calendar.get(Calendar.DAY_OF_YEAR);
                    nextEvent = calendar.getTimeInMillis();
                    plugin.getLogger().info("Cancelling next event and moving till tomorow due to SOTW");
                } else if (getEventFaction() == null) {
                    if (lastEvent == null) {
                        lastEvent = now;
                        plugin.getLogger().info("Last event time assumed");
                    }
                    if (nextEvent == null) {
                        Calendar calendar = new GregorianCalendar();
                        int day = calendar.get(Calendar.DAY_OF_YEAR);
                        Faction faction;
                        if(day == sotwDay + ConfigurationService.CONQUEST_DAY && (faction = plugin.getFactionManager().getFaction("Conquest")) != null && faction instanceof ConquestFaction){
                            calendar.set(Calendar.HOUR_OF_DAY, ConfigurationService.CONQUEST_HOUR);
                            if(calendar.getTimeInMillis() > now) {
                                nextEventFaction = (EventFaction) faction;
                            } else{
                                calendar.setTime(new Date(lastEvent + EVENT_FREQUENCY));
                            }
                        }
                        else {
                            calendar.setTime(new Date(lastEvent + EVENT_FREQUENCY));
                        }
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        nextEvent = calendar.getTimeInMillis();
                        if (!nextCancelled) {
                            plugin.getLogger().info("Next event will go live in " + DurationFormatUtils.formatDurationWords(nextEvent - now, true, true));
                        }
                    }
                    if (nextEventFaction == null) {
                        List<EventFaction> factions = plugin.getFactionManager().getFactions().stream().filter(faction -> faction instanceof KothFaction && !(faction instanceof CitadelFaction) && faction != lastEventFaction).map(faction -> (EventFaction) faction).collect(Collectors.toList());
                        int size = factions.size();
                        if (size == 0) {
                            plugin.getLogger().warning("No available event factions found");
                        } else {
                            if (size == 1) {
                                nextEventFaction = factions.iterator().next();
                            } else {
                                nextEventFaction = factions.get(ThreadLocalRandom.current().nextInt(size));
                            }
                            plugin.getLogger().info("Next Event Faction: " + nextEventFaction.getName());
                        }
                    }
                } else if (lastEventFaction != getEventFaction()) {
                    nextCancelled = true;
                    plugin.getLogger().warning("Cancelling next event because a different event is still running");
                }
            }
            if(nextEvent != null) {
                if (now >= nextEvent) {
                    if (!nextCancelled) {
                        if (!tryContesting(nextEventFaction, Bukkit.getConsoleSender())) {
                            plugin.getLogger().warning("Failed to start event " + nextEventFaction.getName());
                        }
                    }
                    lastEventFaction = nextEventFaction;
                    nextCancelled = false;
                    nextEventFaction = null;
                    nextEvent = null;
                    justAnnounced = true;
                }
                if (nextEvent != null && nextEventFaction != null) {
                    long timeTill = nextEvent - now;
                    if (timeTill > 0) {
                        if (!nextCancelled && !justAnnounced) {
                            if (timeTill > TimeUnit.MINUTES.toMillis(1) && (timeTill % TimeUnit.HOURS.toMillis(1) < TimeUnit.MINUTES.toMillis(1) || (timeTill < TimeUnit.HOURS.toMillis(1) && timeTill % TimeUnit.MINUTES.toMillis(15) < TimeUnit.MINUTES.toMillis(1)))) {
                                broadcastWarning(nextEventFaction, timeTill);
                                justAnnounced = true;
                            }
                        }
                    }
                    justAnnounced = false;
                }
            }
        }, 20 * 60, 20 * 60);
    }

    public static void broadcastWarning(EventFaction eventFaction, long timeTill) {
        if(HCF.getInstance().getTimerManager().eventTimer.getRemaining() <= 0) {
            timeTill = (timeTill / 1000 / 60) * 1000 * 60;
            Bukkit.broadcastMessage(eventFaction.getEventType().getPrefix() + ChatColor.LIGHT_PURPLE + eventFaction.getName() + ConfigurationService.YELLOW + " is starting in " + DurationFormatUtils.formatDurationWords(timeTill, true, true));
        }
    }

    public void setNextCancelled(boolean nextCancelled) {
        this.nextCancelled = nextCancelled;
    }

    public boolean isNextCancelled() {
        return nextCancelled;
    }

    public Long getLastEvent() {
        return lastEvent;
    }

    public Long getNextEvent() {
        return nextEvent;
    }

    public EventFaction getLastEventFaction() {
        return lastEventFaction;
    }

    public EventFaction getNextEventFaction() {
        return nextEventFaction;
    }

    public EventFaction getEventFaction() {
        return this.eventFaction;
    }

    public String getScoreboardPrefix() {
        if (getEventFaction().getName().equalsIgnoreCase("eotw")) {
            return ChatColor.DARK_RED + ChatColor.BOLD.toString();
        }
        else if(getEventFaction().getName().equalsIgnoreCase("Hell")){
            return ChatColor.RED + ChatColor.BOLD.toString();
        }
        else if(getEventFaction().getName().equalsIgnoreCase("Palace")){
            return ChatColor.DARK_AQUA + ChatColor.BOLD.toString();
        }
        else if(getEventFaction() instanceof CitadelFaction){
            return ChatColor.DARK_AQUA + ChatColor.BOLD.toString();
        }
        return ChatColor.BLUE.toString() + ChatColor.BOLD.toString();
    }

    public String getName() {
        return (this.eventFaction == null) ? "Event" : this.eventFaction.getName();
    }

    @Override
    public boolean clearCooldown() {
        boolean result = super.clearCooldown();
        if (this.eventFaction != null) {
            for (final CaptureZone captureZone : this.eventFaction.getCaptureZones()) {
                captureZone.setCappingPlayer(null);
            }
            this.eventFaction.setDeathban(true);
            this.eventFaction.getEventType().getEventTracker().stopTiming();
            this.eventFaction = null;
            this.startStamp = -1L;
            result = true;
        }
        return result;
    }

    @Override
    public long getRemaining() {
        if (this.eventFaction == null) {
            return 0L;
        }
        if (this.eventFaction instanceof KothFaction) {
            return ((KothFaction) this.eventFaction).getCaptureZone().getRemainingCaptureMillis();
        }
        return super.getRemaining();
    }

    @Override
    public long getRemaining(long now) {
        if (this.eventFaction == null) {
            return 0L;
        }
        if (this.eventFaction instanceof KothFaction) {
            return ((KothFaction) this.eventFaction).getCaptureZone().getRemainingCaptureMillis(now);
        }
        return super.getRemaining();
    }

    public void finishEvent(final Player winner) {
        if (this.eventFaction == null) {
            return;
        }
        lastEvent = System.currentTimeMillis();
        final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(winner);
        final World world = winner.getWorld();
        final Location location = winner.getLocation();
        if (eventFaction.getName().equalsIgnoreCase("eotw")) {
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "disablechat");
            Bukkit.broadcastMessage(this.eventFaction.getEventType().getPrefix() + ChatColor.LIGHT_PURPLE + ((playerFaction == null) ? winner.getName() : playerFaction.getName()) + ConfigurationService.YELLOW + " has won " + ChatColor.DARK_RED + ChatColor.BOLD.toString() + "EOTW");
        } else {
            Bukkit.broadcastMessage(this.eventFaction.getEventType().getPrefix() +  ChatColor.LIGHT_PURPLE + ((playerFaction == null) ? winner.getName() : playerFaction.getName()) + ConfigurationService.YELLOW + " has captured " + ChatColor.LIGHT_PURPLE + this.eventFaction.getName() + ConfigurationService.YELLOW + " after " + ConfigurationService.GOLD + DurationFormatUtils.formatDurationWords(this.getUptime(), true, true) + ConfigurationService.YELLOW + " of up-time.");
            if(ConfigurationService.ORIGINS){
                int points;
                int keys;
                String keyType;
                if(eventFaction.getName().equalsIgnoreCase("hell")){
                    points = 15;
                    keys = 2;
                    keyType = "KOTH";
                } else if(eventFaction.getName().equalsIgnoreCase("palace")){
                    points = 25;
                    keys = 2;
                    keyType = "PALACE";
                } else{
                    points = 5;
                    keys = 1;
                    keyType = "KOTH";
                }
                if(playerFaction != null) playerFaction.setPoints(playerFaction.getPoints() + points);
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "crate givekey " + winner.getName() + " " + keyType + " " + keys);
            }
            else if(eventFaction instanceof CitadelFaction){
                CitadelFaction citadelFaction = (CitadelFaction) eventFaction;
                CitadelCapture citadelCapture = new CitadelCapture(playerFaction, System.currentTimeMillis());
                citadelFaction.setCitadelCapture(citadelCapture);
                citadelFaction.fillChests();
            }
            else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "crate givekey " + winner.getName() + " " + ChatColor.stripColor(this.eventFaction.getEventType().getDisplayName()) + " 10");
            }
            Map<Integer, ItemStack> excess =  winner.getInventory().addItem(EventSignListener.getEventSign(this.eventFaction.getName(), winner.getName()));
            for (ItemStack entry : excess.values()) {
                world.dropItemNaturally(location, entry);
            }
        }
        if(!ConfigurationService.ORIGINS && playerFaction != null){
            if(eventFaction instanceof KothFaction){
                playerFaction.setPoints(playerFaction.getPoints() + 10);
            }
            else if(eventFaction instanceof ConquestFaction){
                playerFaction.setPoints(playerFaction.getPoints() + 150);
            }
        }
        EventCapture eventCapture = new EventCapture(eventFaction.getName(),
                eventFaction.getEventType(),
                playerFaction == null ? null : playerFaction.getName(),
                winner.getName(),
                playerFaction == null ? Collections.singletonList(winner.getName()) :
                        playerFaction.getOnlineMembers(winner)
                        .values()
                        .stream()
                        .map(FactionMember::getName)
                        .collect(Collectors.toList())
                );
        if(playerFaction != null){
            playerFaction.getCaptures().add(eventCapture);
            for(FactionMember factionMember: playerFaction.getOnlineMembers(winner).values()){
                FactionUser factionUser = factionMember.getFactionUser();
                factionUser.getEventCaptures().add(eventCapture);
            }
        }
        FactionUser factionUser = plugin.getUserManager().getUser(winner.getUniqueId());
        if(!factionUser.getEventCaptures().contains(eventCapture)) {
            factionUser.getEventCaptures().add(eventCapture);
        }
        this.clearCooldown();
    }

    public boolean tryContesting(final EventFaction eventFaction, final CommandSender sender) {
        if (this.eventFaction != null) {
            sender.sendMessage(ConfigurationService.RED + "There is already an active event, use /event cancel to end it.");
            return false;
        }
        if (eventFaction instanceof KothFaction) {
            final KothFaction kothFaction = (KothFaction) eventFaction;
            if (kothFaction.getCaptureZone() == null) {
                sender.sendMessage(ConfigurationService.RED + "Cannot schedule " + eventFaction.getName() + " as its' capture zone is not set.");
                return false;
            }
        } else if (eventFaction instanceof ConquestFaction) {
            final ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
            final Collection<ConquestFaction.ConquestZone> zones = conquestFaction.getConquestZones();
            for (final ConquestFaction.ConquestZone zone : ConquestFaction.ConquestZone.values()) {
                if (!zones.contains(zone)) {
                    sender.sendMessage(ConfigurationService.RED + "Cannot schedule " + eventFaction.getName() + " as capture zone '" + zone.getDisplayName() + ConfigurationService.RED + "' is not set.");
                    return false;
                }
            }
        }
        final long millis = System.currentTimeMillis();
        if (this.lastContestedEventMillis + EventTimer.RESCHEDULE_FREEZE_MILLIS - millis > 0L) {
            sender.sendMessage(ConfigurationService.RED + "Cannot reschedule events within " + EventTimer.RESCHEDULE_FREEZE_WORDS + '.');
            return false;
        }
        this.lastContestedEventMillis = millis;
        this.startStamp = millis;
        this.eventFaction = eventFaction;
        eventFaction.getEventType().getEventTracker().onContest(eventFaction, this);
        if (eventFaction instanceof ConquestFaction) {
            this.setRemaining(1000L, true);
            this.setPaused(true);
        }
        Collection<CaptureZone> captureZones = eventFaction.getCaptureZones();
        for (CaptureZone captureZone : captureZones) {
            if (captureZone.isActive()) {
                Player player = Iterables.getFirst(captureZone.getCuboid().getPlayers(), null);
                if (player == null) {
                    continue;
                }
                if (!eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone)) {
                    continue;
                }
                captureZone.setCappingPlayer(player);
            }
        }
        eventFaction.setDeathban(true);
        return true;
    }

    public long getUptime() {
        return System.currentTimeMillis() - this.startStamp;
    }

    public long getStartStamp() {
        return this.startStamp;
    }

    private void handleDisconnect(final Player player) {
        Preconditions.checkNotNull((Object) player);
        if (this.eventFaction == null) {
            return;
        }
        final Collection<CaptureZone> captureZones = this.eventFaction.getCaptureZones();
        for (final CaptureZone captureZone : captureZones) {
            if (Objects.equal((Object) captureZone.getCappingPlayer(), (Object) player)) {
                this.eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, this.eventFaction);
                captureZone.setCappingPlayer(null);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        this.handleDisconnect(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(final PlayerQuitEvent event) {
        this.handleDisconnect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(final PlayerKickEvent event) {
        this.handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(final CaptureZoneEnterEvent event) {
        if (this.eventFaction == null) {
            return;
        }
        final CaptureZone captureZone = event.getCaptureZone();
        if (!this.eventFaction.getCaptureZones().contains(captureZone)) {
            return;
        }
        final Player player = event.getPlayer();
        if (captureZone.getCappingPlayer() == null && this.eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone)) {
            captureZone.setCappingPlayer(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(final CaptureZoneLeaveEvent event) {
        if (Objects.equal(event.getFaction(), this.eventFaction)) {
            final Player player = event.getPlayer();
            final CaptureZone captureZone = event.getCaptureZone();
            if (Objects.equal(player, captureZone.getCappingPlayer()) && this.eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, this.eventFaction)) {
                captureZone.setCappingPlayer(null);
                for (final Player target : captureZone.getCuboid().getPlayers()) {
                    if (target != null && !target.equals(player) && this.eventFaction.getEventType().getEventTracker().onControlTake(target, captureZone)) {
                        captureZone.setCappingPlayer(target);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void load(Config config) {
        if(config.contains("eventTimer")) {
            ConfigurationSection configurationSection = config.getConfigurationSection("eventTimer");
            UUID factionUID = UUID.fromString(configurationSection.getString("factionUID"));
            Faction faction = plugin.getFactionManager().getFaction(factionUID);
            if(faction != null && faction instanceof EventFaction){
                this.eventFaction = ((EventFaction) faction);
            }
            startStamp = configurationSection.getLong("startStamp");
            lastContestedEventMillis = configurationSection.getLong("lastContestedEventMillis");
        }
        if(config.contains("nextEvent")) {
            ConfigurationSection nextSection = config.getConfigurationSection("nextEvent");
            String nextFactionUID = nextSection.getString("nextFactionUID");
            if (nextFactionUID != null) {
                nextEventFaction = (EventFaction) plugin.getFactionManager().getFaction(UUID.fromString(nextFactionUID));
            }
            String lastFactionUID = nextSection.getString("lastFactionUID");
            if (lastFactionUID != null) {
                lastEventFaction = (EventFaction) plugin.getFactionManager().getFaction(UUID.fromString(lastFactionUID));
            }
            lastEvent = nextSection.getLong("lastEvent");
            nextEvent = nextSection.getLong("nextEvent");
        }
        sotwDay = config.getInt("sotwDay", sotwDay);
    }

    @Override
    public void save(Config config) {
        if(eventFaction != null) {
            config.set("eventTimer.factionUID", eventFaction.getUniqueID().toString());
            config.set("eventTimer.startStamp", startStamp);
            config.set("eventTimer.lastContestedEventMillis", lastContestedEventMillis);
        }
        else{
            config.set("eventTimer", null);
        }
        config.set("nextEvent.nextFactionUID", nextEventFaction == null ? null : nextEventFaction.getUniqueID().toString());
        config.set("nextEvent.lastFactionUID", lastEventFaction == null ? null : lastEventFaction.getUniqueID().toString());
        config.set("nextEvent.lastEvent", lastEvent);
        config.set("nextEvent.nextEvent", nextEvent);
        config.set("sotwDay", sotwDay);
    }
}