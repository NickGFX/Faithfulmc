package com.faithfulmc.hardcorefactions.visualise;

import com.luxormc.block.BlockPosition;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.RoadFaction;
import com.faithfulmc.hardcorefactions.util.location.MemoryBlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class AsyncWallBorderListener extends BukkitRunnable implements Listener {
    private static final int WALL_BORDER_HEIGHT_BELOW_DIFF = 4;
    private static final int WALL_BORDER_HEIGHT_ABOVE_DIFF = 5;
    private static final int WALL_BORDER_HORIZONTAL_DISTANCE = 7;
    private final HCF plugin;
    private final Queue<VisualTask> visualTasks = new ConcurrentLinkedQueue<>();
    private AsyncServerWarpTimer timer;

    public AsyncWallBorderListener(HCF plugin) {
        this.plugin = plugin;
        timer = new AsyncServerWarpTimer(this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            timer.addPlayer(player);
        }
        timer.runTaskTimerAsynchronously(plugin, 4, 4);
        runTaskTimer(plugin, 1, 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        timer.removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        timer.addPlayer(player);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event){
        plugin.getVisualiseHandler().clearAll(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        Location to = event.getTo();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           MemoryBlockLocation memoryBlockLocation = timer.previous.get(player);
           if(memoryBlockLocation != null && (to.getBlockX() != memoryBlockLocation.getX() || to.getBlockY() != memoryBlockLocation.getY() || to.getBlockZ() != memoryBlockLocation.getZ())){
               timer.previous.put(player, MemoryBlockLocation.fromLocation(to));
               handlePositionChanged(player, to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ());
           }
        });
    }

    public void run() {
        VisualTask visualTask;
        while ((visualTask = visualTasks.poll()) != null){
            plugin.getVisualiseHandler().setVisualType(visualTask.getPlayer(), visualTask.getBlockPositions(), visualTask.getVisualType(), true);
        }
    }

    private void handlePositionChanged(Player player, final World toWorld, final int toX, final int toY, final int toZ) {
        Collection<Claim> added = new HashSet<Claim>();
        int minHeight = toY - WALL_BORDER_HEIGHT_BELOW_DIFF;
        int maxHeight = toY + WALL_BORDER_HEIGHT_ABOVE_DIFF;
        VisualType visualType;
        if (this.plugin.getTimerManager().spawnTagTimer.getRemaining(player) > 0L) {
            visualType = VisualType.SPAWN_BORDER;
            ClaimableFaction claimableFaction = null;
            if(toWorld.getEnvironment() == World.Environment.THE_END){
                if(ConfigurationService.ORIGINS){
                    claimableFaction = (ClaimableFaction) plugin.getFactionManager().getFaction("EndSafezone");
                }
            }
            else{
                claimableFaction = (ClaimableFaction) plugin.getFactionManager().getFaction("Spawn");
            }
            if(claimableFaction != null) {
                Collection<Claim> claims = claimableFaction.getClaims();
                for (Claim claim : claims) {
                    if (claim.getWorld() == player.getWorld()) {
                        added.add(claim);
                    }
                }
            }
        } else {
            if (ConfigurationService.KIT_MAP || this.plugin.getTimerManager().pvpProtectionTimer.getRemaining(player) <= 0L) {
                return;
            }
            int minX = toX - WALL_BORDER_HORIZONTAL_DISTANCE;
            int maxX = toX + WALL_BORDER_HORIZONTAL_DISTANCE;
            int minZ = toZ - WALL_BORDER_HORIZONTAL_DISTANCE;
            int maxZ = toZ + WALL_BORDER_HORIZONTAL_DISTANCE;
            visualType = VisualType.CLAIM_BORDER;
            for (int x = minX; x < maxX; ++x) {
                for (int z = minZ; z < maxZ; ++z) {
                    Faction faction = this.plugin.getFactionManager().getFactionAt(toWorld, x, z);
                    if (faction != null && faction instanceof ClaimableFaction) {
                        if (faction.isSafezone() || faction instanceof RoadFaction) {
                            continue;
                        }
                        Collection<Claim> claims = ((ClaimableFaction) faction).getClaims();
                        for (Claim claim : claims) {
                            if (toWorld.equals(claim.getWorld())) {
                                added.add(claim);
                            }
                        }
                    }
                }
            }
        }

        List<BlockPosition> blockPositions = new ArrayList<>();

        if (!added.isEmpty()) {
            Iterator<Claim> iterator = added.iterator();
            while (iterator.hasNext()) {
                Claim claim2 = iterator.next();
                List<Vector> edges = claim2.edges();
                for (Vector edge : edges) {
                    if (Math.abs(edge.getBlockX() - toX) > WALL_BORDER_HORIZONTAL_DISTANCE) {
                        continue;
                    }
                    if (Math.abs(edge.getBlockZ() - toZ) > WALL_BORDER_HORIZONTAL_DISTANCE) {
                        continue;
                    }
                    Location location = edge.toLocation(toWorld);
                    if (location == null) {
                        continue;
                    }
                    for(int y = minHeight; y <= maxHeight; y++){
                        blockPositions.add(new BlockPosition(location.getBlockX(), y, location.getBlockZ()));
                    }
                }
                iterator.remove();
            }
        }
        if(player.isOnline()) {
            visualTasks.removeIf(visualTask -> visualTask.getPlayer() == player);
            visualTasks.add(new VisualTask(player, visualType, blockPositions));
        }
    }

    public static final class AsyncServerWarpTimer extends BukkitRunnable {
        private AsyncWallBorderListener listener;
        protected ConcurrentMap<Player, MemoryBlockLocation> previous = new ConcurrentHashMap<>();

        public AsyncServerWarpTimer(AsyncWallBorderListener listener) {
            this.listener = listener;
        }

        public void addPlayer(Player player) {
            previous.putIfAbsent(player, MemoryBlockLocation.fromLocation(player.getLocation()));
            listener.handlePositionChanged(player, player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        }

        public void removePlayer(Player player) {
            previous.remove(player);
        }

        public void run() {
            for (Map.Entry<Player, MemoryBlockLocation> playerBlockVectorEntry : new HashSet<>(previous.entrySet())) {
                Player player = playerBlockVectorEntry.getKey();
                if (!player.isOnline()) {
                    continue;
                }
                if (playerBlockVectorEntry.getValue() == null) {
                    continue;
                }
                MemoryBlockLocation from = playerBlockVectorEntry.getValue();
                MemoryBlockLocation to = MemoryBlockLocation.fromLocation(player.getLocation());
                if (!from.equals(to)) {
                    listener.handlePositionChanged(player, player.getWorld(), to.getX(), to.getY(), to.getZ());
                    this.previous.replace(player, to);
                }
            }
        }
    }

    private class VisualTask{
        private final Player player;
        private final VisualType visualType;
        private final List<BlockPosition> blockPositions;

        public VisualTask(Player player, VisualType visualType, List<BlockPosition> blockPositions) {
            this.player = player;
            this.visualType = visualType;
            this.blockPositions = blockPositions;
        }

        public Player getPlayer() {
            return player;
        }

        public VisualType getVisualType() {
            return visualType;
        }

        public List<BlockPosition> getBlockPositions() {
            return blockPositions;
        }
    }
}
