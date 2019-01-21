package com.faithfulmc.hardcorefactions.faction;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.event.*;
import com.faithfulmc.hardcorefactions.faction.event.cause.ClaimChangeCause;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.*;
import com.faithfulmc.hardcorefactions.mountain.GlowstoneFaction;
import com.faithfulmc.hardcorefactions.mountain.OreFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.location.ChunkPosition;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.cache.CacheCleanerThread;
import com.faithfulmc.util.cuboid.CoordinatePair;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public abstract class AbstractFactionManager implements FactionManager, Listener {
    protected final WarzoneFaction warzone;
    protected final WildernessFaction wilderness;
    private final CacheCleanerThread cacheCleanerThread;
    protected final Table<CoordinatePair, ChunkPosition, Claim> claimPositionTable;
    protected final LoadingCache<CoordinatePair, Optional<Claim>> positionCache;
    protected final Map<UUID, UUID> factionPlayerUuidMap;
    protected final Map<UUID, Faction> factionUUIDMap;
    protected final Map<String, UUID> factionNameMap;
    protected final HCF plugin;

    protected AbstractFactionManager(HCF plugin) {
        this.plugin = plugin;
        claimPositionTable = HashBasedTable.create();
        positionCache = CacheBuilder.newBuilder()
                .maximumSize(8000)
                .build(new CacheLoader<CoordinatePair, Optional<Claim>>() {
            public Optional<Claim> load(CoordinatePair coordinatePair) {
                int chunkX = coordinatePair.getX() >> 4;
                int chunkZ = coordinatePair.getZ() >> 4;
                int posX = coordinatePair.getX() % 16;
                int posZ = coordinatePair.getZ() % 16;
                synchronized (claimPositionTable) {
                    return Optional.ofNullable(claimPositionTable.get(new CoordinatePair(coordinatePair.getWorldName(), chunkX, chunkZ), new ChunkPosition((byte)posX, (byte)posZ)));
                }
            }
        });
        cacheCleanerThread = new CacheCleanerThread(1000, plugin, positionCache);
        this.factionPlayerUuidMap = new ConcurrentHashMap<>();
        this.factionUUIDMap = new HashMap<>();
        this.factionNameMap = new CaseInsensitiveMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.warzone = new WarzoneFaction(plugin);
        this.wilderness = new WildernessFaction(plugin);
        reloadFactionData();
        cacheCleanerThread.start();
    }

    public void setFaction(UUID uuid, Faction faction){
        setFaction(plugin.getUserManager().getUser(uuid), faction);
    }

    public void setFaction(FactionUser factionUser, Faction faction){
        factionUser.setFaction(faction);
        if(faction == null){
            factionPlayerUuidMap.remove(factionUser.getUserUUID());
        }
        else {
            factionPlayerUuidMap.put(factionUser.getUserUUID(), faction.getUniqueID());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoinedFaction(PlayerJoinedFactionEvent event) {
        setFaction(event.getUniqueID(), event.getFaction());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event) {
        setFaction(event.getUniqueID(), null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRename(FactionRenameEvent event) {
        this.factionNameMap.remove(event.getOriginalName());
        this.factionNameMap.put(event.getNewName(), event.getFaction().getUniqueID());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionClaim(FactionClaimChangedEvent event) {
        for (Claim claim : event.getAffectedClaims()) {
            cacheClaim(claim, event.getCause());
        }
    }

    @Deprecated
    public Map<String, UUID> getFactionNameMap() {
        return this.factionNameMap;
    }

    public List<Faction> getFactions() {
        List<Faction> asd = new ArrayList<>();
        for (Faction fac : this.factionUUIDMap.values()) {
            asd.add(fac);
        }
        return asd;
    }

    public Claim getClaimAt(World world, int x, int z) {
        try {
            return positionCache.get(new CoordinatePair(world, x, z)).orElse(null);
        } catch (ExecutionException e) {
            e.printStackTrace();
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            byte posX = (byte)(x % 16);
            byte posZ = (byte)(z % 16);
            synchronized (claimPositionTable) {
                return claimPositionTable.get(new CoordinatePair(world, chunkX, chunkZ), new ChunkPosition(posX, posZ));
            }
        }
    }

    public Collection<Claim> getClaimsAtChunk(CoordinatePair coordinatePair){
        synchronized (claimPositionTable) {
            return claimPositionTable.row(coordinatePair).values();
        }
    }
    public Set<Claim> getNearbyClaims(CoordinatePair chunkCoords){
        Set<Claim> claims = new HashSet<>();
        for(int x = -1; x <= 1; x++){
            for(int z = -1; z <= 1; z++){
                CoordinatePair coordinatePair = new CoordinatePair(chunkCoords.getWorld(), chunkCoords.getX() + x, chunkCoords.getZ() + z);
                claims.addAll(getClaimsAtChunk(coordinatePair));
            }
        }
        return claims;
    }

    public Claim getClaimAt(Location location) {
        return getClaimAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public Faction getFactionAt(World world, int x, int z) {
        World.Environment environment = world.getEnvironment();
        Claim claim = getClaimAt(world, x, z);
        if (claim != null) {
            Faction faction = claim.getFaction();
            if (faction != null) {
                return faction;
            }
        }
        if (environment == World.Environment.THE_END) {
            return this.warzone;
        }
        int warzoneRadius = ConfigurationService.WARZONE_RADIUS;
        if (environment == World.Environment.NETHER) {
            warzoneRadius /= 8;
        }
        return (Math.abs(x) > warzoneRadius) || (Math.abs(z) > warzoneRadius) ? this.wilderness : this.warzone;
    }

    public Faction getFactionAt(Location location) {
        return getFactionAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public Faction getFactionAt(Block block) {
        return getFactionAt(block.getLocation());
    }

    public Faction getFaction(String factionName) {
        UUID uuid = this.factionNameMap.get(factionName);
        return uuid == null ? null : this.factionUUIDMap.get(uuid);
    }

    public Faction getFaction(UUID factionUUID) {
        return this.factionUUIDMap.get(factionUUID);
    }

    public PlayerFaction getPlayerFaction(UUID playerUUID) {
        UUID uuid = this.factionPlayerUuidMap.get(playerUUID);
        Faction faction = uuid == null ? null : this.factionUUIDMap.get(uuid);
        return (faction instanceof PlayerFaction) ? (PlayerFaction) faction : null;
    }

    public PlayerFaction getPlayerFaction(Player player) {
        return getPlayerFaction(player.getUniqueId());
    }

    public PlayerFaction getContainingPlayerFaction(String search) {
        UUID target = JavaUtils.isUUID(search) ? UUID.fromString(search) : plugin.getUserManager().fetchUUID(search);
        return target == null ? null: getPlayerFaction(target);
    }

    public Faction getContainingFaction(String search) {
        Faction faction = getFaction(search);
        if (faction != null) {
            return faction;
        }
        UUID playerUUID = plugin.getUserManager().fetchUUID(search);
        if (playerUUID != null) {
            return getPlayerFaction(playerUUID);
        }
        return null;
    }

    public boolean containsFaction(Faction faction) {
        return this.factionNameMap.containsKey(faction.getName());
    }

    public boolean createFaction(Faction faction) {
        return createFaction(faction, Bukkit.getConsoleSender());
    }

    public boolean createFaction(Faction faction, CommandSender sender) {
        if (this.factionUUIDMap.putIfAbsent(faction.getUniqueID(), faction) != null) {
            return false;
        }
        this.factionNameMap.put(faction.getName(), faction.getUniqueID());
        if (((faction instanceof PlayerFaction)) && ((sender instanceof Player))) {
            Player player = (Player) sender;
            PlayerFaction playerFaction = (PlayerFaction) faction;
            if (!playerFaction.setMember(player, new FactionMember(playerFaction, player, ChatChannel.PUBLIC, Role.LEADER))) {
                return false;
            }
        }
        FactionCreateEvent createEvent = new FactionCreateEvent(faction, sender);
        Bukkit.getPluginManager().callEvent(createEvent);
        return !createEvent.isCancelled();
    }

    public boolean removeFaction(Faction faction, CommandSender sender) {
        if (this.factionUUIDMap.remove(faction.getUniqueID()) == null) {
            return false;
        }
        this.factionNameMap.remove(faction.getName());
        FactionRemoveEvent removeEvent = new FactionRemoveEvent(faction, sender);
        Bukkit.getPluginManager().callEvent(removeEvent);
        if (removeEvent.isCancelled()) {
            return false;
        }
        if ((faction instanceof ClaimableFaction)) {
            Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.UNCLAIM, ((ClaimableFaction) faction).getClaims()));
        }
        PlayerFaction playerFaction;
        if ((faction instanceof PlayerFaction)) {
            playerFaction = (PlayerFaction) faction;
            for (PlayerFaction ally : playerFaction.getAlliedFactions()) {
                Bukkit.getPluginManager().callEvent(new FactionRelationRemoveEvent(playerFaction, ally, Relation.ENEMY));
                ally.getRelations().remove(faction.getUniqueID());
            }
            for (UUID uuid : playerFaction.getMembers().keySet()) {
                playerFaction.setMember(uuid, null, true);
            }
        }
        return true;
    }

    public void cacheClaim(Claim claim, ClaimChangeCause cause) {
        Preconditions.checkNotNull(claim, "Claim cannot be null");
        Preconditions.checkNotNull(cause, "Cause cannot be null");
        Preconditions.checkArgument(cause != ClaimChangeCause.RESIZE, "Cannot cache claims of resize miner");
        World world = claim.getWorld();
        if (world == null) {
            return;
        }
        int minX = Math.min(claim.getX1(), claim.getX2());
        int maxX = Math.max(claim.getX1(), claim.getX2());
        int minZ = Math.min(claim.getZ1(), claim.getZ2());
        int maxZ = Math.max(claim.getZ1(), claim.getZ2());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                CoordinatePair worldPosition = new CoordinatePair(world, x, z);
                CoordinatePair chunkPair = new CoordinatePair(world, x >> 4, z >> 4);
                ChunkPosition chunkPosition = new ChunkPosition((byte)(x % 16), (byte)(z % 16));
                synchronized (claimPositionTable) {
                    if (cause == ClaimChangeCause.CLAIM) {
                        claimPositionTable.put(chunkPair, chunkPosition, claim);
                    } else {
                        claimPositionTable.remove(chunkPair, chunkPosition);
                    }
                }
                positionCache.invalidate(worldPosition);
            }
        }
    }

    protected void addDefaults() {
        Set<Faction> adding = new HashSet<>();
        if (!this.factionNameMap.containsKey("Warzone")) {
            adding.add(new WarzoneFaction(plugin));
        }
        if (!this.factionNameMap.containsKey("Spawn")) {
            adding.add(new SpawnFaction(plugin));
        }
        if (!this.factionNameMap.containsKey("NorthRoad")) {
            adding.add(new NorthRoadFaction(plugin));
        }
        if (!this.factionNameMap.containsKey("EastRoad")) {
            adding.add(new EastRoadFaction(plugin));
        }
        if (!this.factionNameMap.containsKey("SouthRoad")) {
            adding.add(new SouthRoadFaction(plugin));
        }
        if (!this.factionNameMap.containsKey("WestRoad")) {
            adding.add(new WestRoadFaction(plugin));
        }
        if (!ConfigurationService.KIT_MAP && !this.factionNameMap.containsKey("Glowstone")) {
            adding.add(new GlowstoneFaction(plugin));
        }
        if (!ConfigurationService.KIT_MAP && !this.factionNameMap.containsKey("OreFaction")) {
            adding.add(new OreFaction(plugin));
        }
        if (!ConfigurationService.KIT_MAP && !this.factionNameMap.containsKey("EndPortal")) {
            adding.add(new EndPortalFaction(plugin));
        }
        /*
        if(ConfigurationService.ORIGINS && !this.factionNameMap.containsKey("EndSafezone")){
            adding.add(new EndSafezoneFaction(plugin));
        }
        */
        for (Faction added : adding) {
            cacheFaction(added);
            Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "Faction " + added.getName() + " not found, created.");
        }
    }

    public void cacheFaction(Faction faction) {
        this.factionNameMap.put(faction.getName(), faction.getUniqueID());
        this.factionUUIDMap.put(faction.getUniqueID(), faction);
        ClaimableFaction claimableFaction;
        if ((faction instanceof ClaimableFaction)) {
            claimableFaction = (ClaimableFaction) faction;
            for (Claim claim : claimableFaction.getClaims()) {
                cacheClaim(claim, ClaimChangeCause.CLAIM);
            }
        }
        if ((faction instanceof PlayerFaction)) {
            PlayerFaction playerFaction = (PlayerFaction) faction;
            for (FactionMember factionMember : playerFaction.getMembers().values()) {
                setFaction(factionMember.getFactionUser(), faction);
            }
        }
    }

    public void updateFaction(Faction faction){

    }
}
