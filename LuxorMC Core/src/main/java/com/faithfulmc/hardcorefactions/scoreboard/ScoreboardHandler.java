package com.faithfulmc.hardcorefactions.scoreboard;

import com.luxormc.event.PotionEffectAddEvent;
import com.luxormc.event.PotionEffectRemoveEvent;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionRelationCreateEvent;
import com.faithfulmc.hardcorefactions.faction.event.FactionRelationRemoveEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerJoinedFactionEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerLeftFactionEvent;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.scoreboard.provider.TimerSidebarProvider;
import com.google.common.base.Optional;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ScoreboardHandler implements Listener {
    private final Map<UUID, PlayerBoard> playerBoards;
    private final TimerSidebarProvider timerSidebarProvider;
    private final HCF plugin;
    private ScoreboardThread[] scoreboardThreads = new ScoreboardThread[1];
    private final Set<UUID> invisibilityFixed = Collections.synchronizedSet(new HashSet<>());
    private volatile int currentThread = 0;

    public ScoreboardHandler(HCF plugin) {
        this.playerBoards = new HashMap<>();
        this.plugin = plugin;
        this.timerSidebarProvider = new TimerSidebarProvider(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for(int i = 0; i < scoreboardThreads.length; i ++){
            scoreboardThreads[i] = new ScoreboardThread(plugin);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerBoard playerBoard = new PlayerBoard(plugin, player);
            playerBoard.init(Bukkit.getOnlinePlayers());
            setPlayerBoard(player.getUniqueId(), playerBoard);
        }
    }

    public ScoreboardThread getNextThread(){
        ScoreboardThread scoreboardThread = scoreboardThreads[currentThread];
        currentThread++;
        currentThread %= scoreboardThreads.length;
        return scoreboardThread;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        synchronized (this.playerBoards) {
            for (PlayerBoard playerBoard : this.playerBoards.values()) {
                playerBoard.init(player);
            }
        }
        PlayerBoard playerBoard = new PlayerBoard(this.plugin, player);
        playerBoard.init(Bukkit.getOnlinePlayers());
        setPlayerBoard(uuid, playerBoard);
        if(PlayerBoard.INVISIBILITYFIX &&
                player.hasPotionEffect(PotionEffectType.INVISIBILITY) &&
                plugin.getTimerManager().sotw.getRemaining() <= 0 &&
                MinecraftServer.getServer().tps1.getAverage() >= 18 &&
                invisibilityFixed.add(player.getUniqueId())) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        invisibilityFixed.remove(event.getPlayer().getUniqueId());
        synchronized (this.playerBoards) {
            this.playerBoards.remove(event.getPlayer().getUniqueId()).remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoinedFaction(PlayerJoinedFactionEvent event) {
        Optional<Player> optional = event.getPlayer();
        Player player;
        if (optional.isPresent()) {
            player = optional.get();
            Collection<Player> players = event.getFaction().getOnlinePlayers();
            PlayerBoard playerBoard = getPlayerBoard(event.getUniqueID());
            if(playerBoard != null) {
                playerBoard.setMembers(players);
                List<PlayerFaction> alliedFactions = event.getFaction().getAlliedFactions();
                for(PlayerFaction playerFaction: alliedFactions){
                    playerBoard.setAllies(playerFaction.getOnlinePlayers());
                }
            }
            for (Player other: players) {
                PlayerBoard otherBoard = getPlayerBoard(other.getUniqueId());
                otherBoard.setMembers(Collections.singleton(player));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event) {
        Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            Player player = optional.get();
            Collection<Player> players = event.getFaction().getOnlinePlayers();
            PlayerBoard playerBoard = getPlayerBoard(event.getUniqueID());
            if(playerBoard != null){
                playerBoard.setNeutrals(players);
                List<PlayerFaction> alliedFactions = event.getFaction().getAlliedFactions();
                for(PlayerFaction playerFaction: alliedFactions){
                    playerBoard.setNeutrals(playerFaction.getOnlinePlayers());
                }
            }
            for(Player other: players) {
                PlayerBoard otherBoard = getPlayerBoard(other.getUniqueId());
                otherBoard.setNeutrals(Collections.singleton(player));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionAllyCreate(FactionRelationCreateEvent event) {
        for(Player player: event.getSenderFaction().getOnlinePlayers()){
            PlayerBoard playerBoard = getPlayerBoard(player.getUniqueId());
            playerBoard.setAllies(event.getTargetFaction().getOnlinePlayers());
        }
        for(Player player: event.getTargetFaction().getOnlinePlayers()){
            PlayerBoard playerBoard = getPlayerBoard(player.getUniqueId());
            playerBoard.setAllies(event.getSenderFaction().getOnlinePlayers());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionAllyRemove(FactionRelationRemoveEvent event) {
        for(Player player: event.getSenderFaction().getOnlinePlayers()){
            PlayerBoard playerBoard = getPlayerBoard(player.getUniqueId());
            playerBoard.setNeutrals(event.getTargetFaction().getOnlinePlayers());
        }
        for(Player player: event.getTargetFaction().getOnlinePlayers()){
            PlayerBoard playerBoard = getPlayerBoard(player.getUniqueId());
            playerBoard.setNeutrals(event.getSenderFaction().getOnlinePlayers());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInvisibilityExpire(PotionEffectRemoveEvent event){
        if(PlayerBoard.INVISIBILITYFIX &&
           event.getEntity() instanceof Player &&
           event.getEffect().getType().getId() == PotionEffectType.INVISIBILITY.getId() &&
           invisibilityFixed.remove(event.getEntity().getUniqueId())){
            Player player = (Player) event.getEntity();
            new BukkitRunnable(){
                public void run() {
                    if(player.isOnline() && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        synchronized (playerBoards) {
                            for (PlayerBoard playerBoard : playerBoards.values()) {
                                playerBoard.init(player);
                            }
                        }
                    }
                }
            }.runTask(plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInvisibleDrink(PotionEffectAddEvent event) {
        if (PlayerBoard.INVISIBILITYFIX &&
            event.getEntity() instanceof Player &&
            event.getEffect().getType().getId() == PotionEffectType.INVISIBILITY.getId() &&
            plugin.getTimerManager().sotw.getRemaining() <= 0 &&
            MinecraftServer.getServer().tps1.getAverage() >= 18 &&
            invisibilityFixed.add(event.getEntity().getUniqueId())
                ) {
            Player player = (Player) event.getEntity();
            new BukkitRunnable() {
                public void run() {
                    if (player.isOnline() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        synchronized (playerBoards) {
                            for (PlayerBoard playerBoard : playerBoards.values()) {
                                playerBoard.removeAll(player);
                            }
                        }
                    }
                }
            }.runTask(plugin);
        }
    }

    public Set<UUID> getInvisibilityFixed() {
        return invisibilityFixed;
    }

    public PlayerBoard getPlayerBoard(UUID uuid) {
        synchronized (this.playerBoards) {
            return this.playerBoards.get(uuid);
        }
    }

    public void setPlayerBoard(UUID uuid, PlayerBoard board) {
        synchronized (this.playerBoards) {
            this.playerBoards.put(uuid, board);
        }
        board.setSidebarVisible(true);
        board.setDefaultSidebar(this.timerSidebarProvider);
        board.setScoreboardThread(getNextThread());
    }

    public void disable(){
        for(ScoreboardThread scoreboardThread: scoreboardThreads){
            scoreboardThread.setRunning(false);
        }
        clearBoards();
    }

    public void clearBoards() {
        synchronized (this.playerBoards) {
            Iterator<PlayerBoard> iterator = this.playerBoards.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().remove();
                iterator.remove();
            }
        }
    }

    public Map<UUID, PlayerBoard> getPlayerBoards() {
        return playerBoards;
    }
}
