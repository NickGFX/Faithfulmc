package com.faithfulmc.hardcorefactions.scoreboard.provider;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.eotw.EotwHandler;
import com.faithfulmc.hardcorefactions.events.faction.ConquestFaction;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.events.tracker.ConquestTracker;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import com.faithfulmc.hardcorefactions.hcfclass.bard.BardClass;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerClass;
import com.faithfulmc.hardcorefactions.hcfclass.rogue.RogueClass;
import com.faithfulmc.hardcorefactions.scoreboard.SidebarEntry;
import com.faithfulmc.hardcorefactions.scoreboard.SidebarProvider;
import com.faithfulmc.hardcorefactions.timer.GlobalTimer;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.Timer;
import com.faithfulmc.hardcorefactions.timer.type.SpawnTagTimer;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.Cooldowns;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public class TimerSidebarProvider implements SidebarProvider {
    protected static final String STRAIGHT_LINE = BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 13);

    private static String handleBardFormat(long millis, boolean trailingZero, boolean showMillis) {
        return ((showMillis ? trailingZero ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS : DateTimeFormats.SECONDS).get()).format(millis * 0.001D);
    }

    private final HCF plugin;
    private final BasePlugin basePlugin;

    public TimerSidebarProvider(HCF plugin) {
        this.plugin = plugin;
        this.basePlugin = BasePlugin.getPlugin();
    }

    public String getTitle() {
        return ConfigurationService.SCOREBOARD_TITLE;
    }

    public String getColour(boolean b) {
        return b ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
    }

    public List<SidebarEntry> getLines(Player player, long now) {
        List<SidebarEntry> lines = new ArrayList<>();
        HCFClass pvpClass = this.plugin.getHcfClassManager().getEquippedClass(player);
        EventTimer eventTimer = this.plugin.getTimerManager().eventTimer;
        EventFaction eventFaction = eventTimer.getEventFaction();
        BaseUser baseUser = basePlugin.getUserManager().getUser(player.getUniqueId());
        FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
        if (ConfigurationService.KIT_MAP && factionUser != null && (plugin.getTimerManager().eventTimer.getEventFaction() == null || !(plugin.getTimerManager().eventTimer.getEventFaction() instanceof ConquestFaction))) {
            lines.add(new SidebarEntry(ChatColor.DARK_RED.toString() + ChatColor.BOLD, "Kills", ChatColor.GRAY + ": " + factionUser.getKills()));
            lines.add(new SidebarEntry(ChatColor.DARK_RED.toString() + ChatColor.BOLD, "Deaths", ChatColor.GRAY + ": " + factionUser.getDeaths()));
            lines.add(new SidebarEntry(ChatColor.DARK_RED.toString() + ChatColor.BOLD, "Balance", ChatColor.GRAY + ": $" + factionUser.getBalance()));
        }
        if (plugin.getStaffModeListener().isStaff(player)) {
            lines.add(new SidebarEntry(ConfigurationService.GOLD + ChatColor.BOLD.toString(), "Staff Mode", ChatColor.GRAY + ChatColor.BOLD.toString() + ":"));
            if (baseUser != null) {
                lines.add(new SidebarEntry(ConfigurationService.GOLD + "  " + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.YELLOW, "Vanish", ChatColor.GRAY + ": " + getColour(baseUser.isVanished()) + (baseUser.isVanished() ? "Enabled" : "Disabled")));
            }
            lines.add(new SidebarEntry(ConfigurationService.GOLD + "  " + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.YELLOW, "Gamemode", ChatColor.GRAY + ": " + getColour(player.getGameMode() == GameMode.CREATIVE) + (player.getGameMode() == GameMode.CREATIVE ? "Creative" : "Survival")));
            if (baseUser != null) {
                lines.add(new SidebarEntry(ConfigurationService.GOLD + "  " + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.YELLOW, "Staff Chat", ChatColor.GRAY + ": " + getColour(baseUser.isInStaffChat()) + (baseUser.isInStaffChat() ? "Enabled" : "Disabled")));
            }
        }
        if (pvpClass != null) {
            if (pvpClass instanceof MinerClass && !ConfigurationService.KIT_MAP) {
                lines.add(new SidebarEntry(ChatColor.BOLD + pvpClass.getDisplayName() + ConfigurationService.GRAY + ": "));
                lines.add(new SidebarEntry(ConfigurationService.ARROW_COLOR + " " + ConfigurationService.DOUBLEARROW + " ", ChatColor.AQUA + "Diamonds", ConfigurationService.GRAY + ": " + ConfigurationService.RED + factionUser.getDiamondsMined()));
            }
            if ((pvpClass instanceof BardClass)) {
                BardClass bardClass = (BardClass) pvpClass;
                lines.add(new SidebarEntry(ChatColor.BOLD + bardClass.getDisplayName() + ConfigurationService.GRAY + ": "));
                lines.add(new SidebarEntry(ConfigurationService.GOLD + " " + ConfigurationService.DOUBLEARROW + " ", ConfigurationService.YELLOW + "Energy", ConfigurationService.GRAY + ": " + ConfigurationService.RED + handleBardFormat(bardClass.getEnergyMillis(player, now), true, false)));
                long cooldown = bardClass.getRemainingBuffDelay(player, now);
                if (cooldown > 0L) {
                    lines.add(new SidebarEntry(ConfigurationService.GOLD + " " + ConfigurationService.DOUBLEARROW + " ", ConfigurationService.YELLOW + "Buff Delay", ConfigurationService.GRAY + ": " + ConfigurationService.RED + HCF.getRemaining(cooldown, false)));
                }
            }
            if (pvpClass instanceof ArcherClass || pvpClass instanceof RogueClass) {
                int size = lines.size();
                boolean display = false;
                if(pvpClass.hasCooldown(player)){
                    int cooldown = Cooldowns.getCooldownForPlayerInt(pvpClass.getCooldown(), player, now);
                    lines.add(new SidebarEntry(ConfigurationService.ARROW_COLOR + " " + ConfigurationService.DOUBLEARROW + " ", ConfigurationService.YELLOW + "Buff Delay", ConfigurationService.GRAY + ": " + ConfigurationService.RED + cooldown));
                    display = true;
                }
                if(pvpClass instanceof ArcherClass) {
                    if (ArcherClass.TAGGED.containsValue(player.getUniqueId())) {
                        for (UUID uuid : ArcherClass.TAGGED.keySet()) {
                            Player tagged;
                            if (((ArcherClass.TAGGED.get(uuid)).equals(player.getUniqueId())) && ((tagged = Bukkit.getPlayer(uuid)) != null)) {
                                String name = tagged.getName();
                                if (name.length() > 14) {
                                    name = name.substring(0, 14);
                                }
                                lines.add(new SidebarEntry(ConfigurationService.GOLD.toString() + " " + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.YELLOW.toString(), ConfigurationService.YELLOW + name, ""));
                            }
                        }
                        display = true;
                    }
                }
                if(display){
                    lines.add(size, new SidebarEntry(ChatColor.BOLD + pvpClass.getDisplayName() + ConfigurationService.GRAY + ": "));
                }
            }
        }
        Collection<Timer> timers = this.plugin.getTimerManager().getTimers();
        for (Timer timer : timers) {
            if (timer instanceof EventTimer) {
                EventTimer etimer = (EventTimer) timer;
                if (etimer.getEventFaction() != null && etimer.getEventFaction().getEventType() == EventType.CONQUEST) {
                    continue;
                }
            }
            if ((timer instanceof PlayerTimer)) {
                PlayerTimer playerTimer = (PlayerTimer) timer;
                long remaining3 = playerTimer.getRemaining(player, now);
                if (remaining3 > 0L) {
                    String timerName = playerTimer.getName();
                    if (timerName.length() > 14) {
                        timerName = timerName.substring(0, timerName.length());
                    }
                    lines.add(new SidebarEntry(playerTimer.getScoreboardPrefix(), timerName + ChatColor.GRAY, ": " + ChatColor.RED + HCF.getRemaining(remaining3, !(playerTimer instanceof SpawnTagTimer))));
                }
            } else if ((timer instanceof GlobalTimer)) {
                GlobalTimer globalTimer = (GlobalTimer) timer;
                long remaining3 = globalTimer.getRemaining(now);
                if (remaining3 > 0L) {
                    String timerName = globalTimer.getName();
                    if (timerName.length() > 14) {
                        timerName = timerName.substring(0, timerName.length());
                    }
                    lines.add(new SidebarEntry(globalTimer.getScoreboardPrefix(), timerName + ChatColor.GRAY, ": " + ChatColor.RED + HCF.getRemaining(remaining3, false)));
                }
            }
        }
        if (!ConfigurationService.KIT_MAP) {
            EotwHandler.EotwRunnable eotwRunnable = this.plugin.getEotwHandler().getRunnable();
            if (eotwRunnable != null) {
                long remaining4 = eotwRunnable.getTimeUntilStarting(now);
                if (remaining4 > 0L) {
                    lines.add(new SidebarEntry(ChatColor.DARK_RED.toString() + ChatColor.BOLD, "EOTW" + ConfigurationService.RED + " (Starts", " In) " + ConfigurationService.GRAY + ": " + ConfigurationService.RED + HCF.getRemaining(remaining4, false)));
                } else if ((remaining4 = eotwRunnable.getTimeUntilCappable()) > 0L) {
                    lines.add(new SidebarEntry(ChatColor.DARK_RED.toString() + ChatColor.BOLD, "EOTW" + ConfigurationService.RED + " (Cappable ", "In) " + ConfigurationService.GRAY + ": " + ConfigurationService.RED + HCF.getRemaining(remaining4, false)));
                }
            }
        }

        if ((eventFaction instanceof ConquestFaction)) {
            if (!lines.isEmpty()) {
                lines.add(new SidebarEntry(ConfigurationService.SCOREBOARD_COLOR, ConfigurationService.GRAY + TimerSidebarProvider.STRAIGHT_LINE, TimerSidebarProvider.STRAIGHT_LINE));
            }
            lines.add(new SidebarEntry(ConfigurationService.GOLD + ChatColor.BOLD.toString(), "Conquest", ConfigurationService.GRAY + ":"));
            ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
            ConquestTracker conquestTracker = (ConquestTracker) conquestFaction.getEventType().getEventTracker();
            List<Map.Entry<PlayerFaction, Integer>> entries = new ArrayList<>(conquestTracker.getFactionPointsMap().entrySet());
            int max = plugin.getStaffModeListener().isStaff(player) ? 1 : 3;
            if (entries.size() > max) {
                entries = entries.subList(0, max);
            }
            int i = 0;
            for (Map.Entry<PlayerFaction, Integer> entry : entries) {
                lines.add(new SidebarEntry(" " + ConfigurationService.GOLD + ChatColor.BOLD.toString() + (i + 1) + ". ", entry.getKey().getDisplayName(player), ConfigurationService.GRAY + ": " + entry.getValue()));
                i++;
            }
            if (!entries.isEmpty()) {
                lines.add(new SidebarEntry(ConfigurationService.SCOREBOARD_COLOR, TimerSidebarProvider.STRAIGHT_LINE + ConfigurationService.GRAY, TimerSidebarProvider.STRAIGHT_LINE));
            }
            for (CaptureZone captureZone : conquestFaction.getCaptureZones()) {
                ConquestFaction.ConquestZone conquestZone = conquestFaction.getZone(captureZone);
                long time = Math.max(captureZone.getRemainingCaptureMillis(now), 0);
                String left = HCF.getRemaining(time, false);
                lines.add(new SidebarEntry("  " + conquestZone.getColor() + ChatColor.BOLD, conquestZone.getName(), ConfigurationService.GRAY + ": " + left));
            }
        }
        if (!lines.isEmpty()) {
            if (lines.size() <= 14) {
                lines.add(0, new SidebarEntry(ConfigurationService.SCOREBOARD_COLOR, TimerSidebarProvider.STRAIGHT_LINE, TimerSidebarProvider.STRAIGHT_LINE));
                if (lines.size() < 14) {
                    lines.add(lines.size(), new SidebarEntry(ConfigurationService.SCOREBOARD_COLOR, ChatColor.STRIKETHROUGH + TimerSidebarProvider.STRAIGHT_LINE, TimerSidebarProvider.STRAIGHT_LINE));
                }
            }
        }
        return lines;
    }
}
