package com.faithfulmc.hardcorefactions.listener;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.scoreboard.FunctionTabRow;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.tablist.tab.BlankTabRow;
import com.faithfulmc.tablist.tab.StaticTabRow;
import com.faithfulmc.tablist.tab.Tab;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TabListener implements Listener {
    private final HCF plugin;
    private final Map<UUID, Tab> tabMap = new HashMap<>();
    private final Map<UUID, BukkitTask> taskMap = new HashMap<>();

    public TabListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Tab tab = createTab(player);
        if (((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() >= 47) {
            tab.sendTab();
        } else {
            tab.sendTab7();
        }
        BukkitTask bukkitTask = plugin.getServer().getScheduler().runTaskTimer(plugin, tab::updateTab, 0, 10);
        taskMap.put(player.getUniqueId(), bukkitTask);
        tabMap.put(player.getUniqueId(), tab);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        tabMap.remove(player.getUniqueId());
        BukkitTask bukkitTask = taskMap.remove(player.getUniqueId());
        if(bukkitTask != null){
            bukkitTask.cancel();
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event){
        Player player = event.getPlayer();
        tabMap.remove(player.getUniqueId());
        BukkitTask bukkitTask = taskMap.remove(player.getUniqueId());
        if(bukkitTask != null){
            bukkitTask.cancel();
        }
    }

    public Tab createTab(Player player){
        FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
        Tab tab = new Tab(player, ConfigurationService.SCOREBOARD_TITLE, ChatColor.YELLOW + "www.luxormc.com");
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new StaticTabRow(ChatColor.AQUA + ChatColor.BOLD.toString() + "Player Info"));
        tab.addTabRow(new FunctionTabRow("Balance: " + ChatColor.WHITE, ChatColor.GRAY::toString, () -> "$" + String.valueOf(factionUser.getBalance())));
        tab.addTabRow(new FunctionTabRow("Kills: " + ChatColor.WHITE, ChatColor.GRAY::toString, () -> String.valueOf(factionUser.getKills())));
        tab.addTabRow(new FunctionTabRow("Deaths: " + ChatColor.WHITE, ChatColor.GRAY::toString, () -> String.valueOf(factionUser.getDeaths())));
        tab.addTabRow(new FunctionTabRow("Lives: " + ChatColor.WHITE, ChatColor.GRAY::toString, () -> String.valueOf(factionUser.getLives())));
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new StaticTabRow(ChatColor.AQUA + ChatColor.BOLD.toString() + "Location"));
        tab.addTabRow(new FunctionTabRow("", () -> {
            Location location = player.getLocation();
            int blockX = Math.abs(location.getBlockX()) > 99999 ? 0 : location.getBlockX();
            int blockZ = Math.abs(location.getBlockZ()) > 99999 ? 0 : location.getBlockZ();
            return "( " + blockX + ", " + blockZ + " )";
        }, () -> ""));
        tab.addTabRow(new FunctionTabRow("", () -> ChatColor.GRAY + "[" + getDirection(player.getLocation().getYaw()) + "]", () -> ""));
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());

        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new StaticTabRow(ChatColor.AQUA + ChatColor.BOLD.toString() + ConfigurationService.MAP_TITLE));
        tab.addTabRow(new StaticTabRow(ChatColor.GRAY + "luxormc.com"));
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new StaticTabRow(ChatColor.AQUA + ChatColor.BOLD.toString() + "Faction"));
        tab.addTabRow(new FunctionTabRow("", () -> factionUser.getFaction() == null ? "" : ChatColor.GRAY + "Name: ", () -> (factionUser.getFaction() == null ? "None" : factionUser.getFaction().getName())));
        tab.addTabRow(new FunctionTabRow("", () -> factionUser.getFaction() == null ? "" : ChatColor.GRAY + "Role: ", () -> factionUser.getFaction() == null ? "" : WordUtils.capitalize(factionUser.getPlayerFaction().getMember(player).getRole().name().toLowerCase())));
        if(ConfigurationService.KIT_MAP) tab.addTabRow(new BlankTabRow());
        else tab.addTabRow(new FunctionTabRow("", () -> factionUser.getFaction() == null ? "" : ChatColor.GRAY + "DTR: ", () -> factionUser.getFaction() == null ? "" : factionUser.getPlayerFaction().getDtrColour().toString().replace(ChatColor.GRAY.toString(), ChatColor.GRAY.toString()) + new DecimalFormat("#.#").format(factionUser.getPlayerFaction().getDeathsUntilRaidable())));
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());

        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new StaticTabRow(ChatColor.AQUA + ChatColor.BOLD.toString() + "Map Info"));
        tab.addTabRow(new StaticTabRow(ChatColor.GRAY + "Map Kit: " + ChatColor.WHITE + "P" + ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.PROTECTION_ENVIRONMENTAL, 1) + " S" + ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.DAMAGE_ALL, 1)));
        tab.addTabRow(new StaticTabRow(ChatColor.GRAY + "Factions: " + ChatColor.WHITE + ConfigurationService.FACTION_PLAYER_LIMIT + " Man"));
        tab.addTabRow(new StaticTabRow(ChatColor.GRAY + "Allies: " + ChatColor.WHITE + (ConfigurationService.MAX_ALLIES_PER_FACTION != 1 ? (ConfigurationService.MAX_ALLIES_PER_FACTION == 0 ? "No" : ConfigurationService.MAX_ALLIES_PER_FACTION) + " Allies" : ConfigurationService.MAX_ALLIES_PER_FACTION + " Ally")));
        tab.addTabRow(new StaticTabRow(ChatColor.GRAY + "Border: " + ChatColor.WHITE + ConfigurationService.BORDER_SIZES.getOrDefault(World.Environment.NORMAL, 3000)));
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new StaticTabRow(ChatColor.AQUA + ChatColor.BOLD.toString() + "Next KOTH"));
        tab.addTabRow(new FunctionTabRow("", () -> "", () -> plugin.getTimerManager().eventTimer.getNextEventFaction() == null ? "None scheduled" : plugin.getTimerManager().eventTimer.getNextEventFaction().getName()));
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        tab.addTabRow(new BlankTabRow());
        return tab;
    }

    private static String getDirection(float yaw) {
        yaw -= 180;
        yaw = (yaw % 360);
        if(yaw < 0) yaw += 360;

        if (0 <= yaw && yaw < 22.5) {
            return "NORTH";
        } else if (22.5 <= yaw && yaw < 67.5) {
            return "NORTH-EAST";
        } else if (67.5 <= yaw && yaw < 112.5) {
            return "EAST";
        } else if (112.5 <= yaw && yaw < 157.5) {
            return "SOUTH-EAST";
        } else if (157.5 <= yaw && yaw < 202.5) {
            return "SOUTH";
        } else if (202.5 <= yaw && yaw < 247.5) {
            return "SOUTH-WEST";
        } else if (247.5 <= yaw && yaw < 292.5) {
            return "WEST";
        } else if (292.5 <= yaw && yaw < 337.5) {
            return "NORTH-WEST";
        } else if (337.5 <= yaw && yaw < 360.0) {
            return "NORTH";
        } else {
            return "?";
        }
    }
}
