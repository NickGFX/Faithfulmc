package com.faithfulmc.hardcorefactions.user;

import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.events.EventCapture;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerLevel;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.util.mongo.SimpleEntryIntInt;
import com.faithfulmc.hardcorefactions.util.mongo.SimpleEntryUUIDInt;
import com.faithfulmc.hardcorefactions.util.mongo.SimpleEntryUUIDLong;
import com.faithfulmc.util.GenericUtils;
import com.faithfulmc.util.PersistableLocation;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.*;
import java.util.stream.Collectors;

@Entity(value = "user")
@Indexes({@Index(fields = @Field(value = "name", type = IndexType.TEXT)), @Index(fields = @Field(value = "kills")), @Index(fields = @Field(value = "healthBrewed")), @Index(fields = @Field(value = "deaths")), @Index(fields = @Field(value = "killStreak")), @Index(fields = @Field(value = "lives")), @Index(fields = @Field(value = "balance")), @Index(fields = @Field(value = "total_ores")),})
public class FactionUser implements ConfigurationSerializable {
    @Embedded
    private final Set<UUID> factionChatSpying = new HashSet<>();
    @Transient
    private final Map<Integer, Integer> ores = new HashMap<>();
    @Transient
    private final Map<Integer, Integer> mobs = new HashMap<>();
    @Transient
    private final Map<UUID, Integer> kitUses = new HashMap<>();
    @Transient
    private final Map<UUID, Long> kitCooldowns = new HashMap<>();
    @Id
    private UUID userUUID;
    @Embedded
    private List<SimpleEntryIntInt> ores_storage = null;
    @Embedded
    private List<SimpleEntryIntInt> mobs_storage = null;
    private Integer total_ores = null;
    @Embedded
    private List<SimpleEntryUUIDInt> kitUses_storage = null;
    @Embedded
    private List<SimpleEntryUUIDLong> kitCooldowns_storage = null;
    private String name = null;
    private boolean capzoneEntryAlerts = false;
    private boolean showClaimMap = false;
    private boolean showLightning = true;
    private boolean oreInventory = true;
    private PersistableLocation backLocation;
    private Deathban deathban = null;
    private long lastFactionLeaveMillis = 0;
    private int kills = 0;
    private int healthBrewed = 0;
    private int deaths = 0;
    private int killStreak = 0;
    private int lives = 0;
    private int balance = 0;
    private boolean online = false;
    private long lastRevive = 0;
    private boolean reclaimed = false;
    private boolean nocobble = false;
    private boolean nomobdrops = false;
    private boolean fdalerts = true;
    private boolean lffalerts = true;
    private String factionname;
    private long playtime_storage = 0;
    @Transient
    private long playtime = 0;
    @Transient
    private long temporyPlaytime = 0;
    private long lastSeen = 0;
    @Reference
    private Faction faction = null;
    @Embedded
    private List<Deathban> deathbanHistory = new ArrayList<>();
    private MinerLevel minerLevel = MinerLevel.DEFAULT;
    private int spawncredits = 0;
    @Embedded
    private List<EventCapture> eventCaptures = new ArrayList<>();
    @Embedded
    private Set<String> previousFactions = new HashSet<>();
    private long lastPlaytimeReclaim = 0;
    private long lastPanic = 0;
    private boolean deathMessages = true;
    private long lastDisguise = 0;

    public FactionUser(UUID userUUID) {
        this.userUUID = userUUID;
        fdalerts = true;
        lffalerts = true;
        nocobble = false;
        reclaimed = false;
        deathbanHistory = new ArrayList<>();
    }

    public FactionUser() {
    }

    public FactionUser(final Map<String, Object> map) {
        this.factionChatSpying.addAll((Collection<? extends UUID>) GenericUtils.createList(map.get("faction-chat-spying"), String.class).stream().map(UUID::fromString).collect(Collectors.toList()));
        this.userUUID = UUID.fromString((String) map.get("userUUID"));
        this.capzoneEntryAlerts = (Boolean) map.get("capzoneEntryAlerts");
        this.showLightning = (Boolean) map.get("showLightning");
        this.deathban = (Deathban) map.get("deathban");
        this.lastFactionLeaveMillis = Long.parseLong((String) map.get("lastFactionLeaveMillis"));
        this.healthBrewed = (Integer) map.get("brewed");
        this.kills = (Integer) map.get("kills");
        this.deaths = (Integer) map.get("deaths");
        this.lastRevive = ((Number) map.getOrDefault("lastRevive", 0L)).longValue();
        this.reclaimed = (Boolean) map.getOrDefault("reclaimed", false);
        this.nocobble = (Boolean) map.getOrDefault("nocobble", false);
        this.killStreak = (Integer) map.getOrDefault("killStreak", 0);
        lives = (Integer) map.getOrDefault("lives", 0);
        balance = (Integer) map.getOrDefault("balance", 0);
        fdalerts = (Boolean) map.getOrDefault("fdalerts", true);
        lffalerts = (Boolean) map.getOrDefault("lffalerts", true);
        lastSeen = Long.parseLong((String) map.getOrDefault("lastSeen", "0"));
        name = (String) map.get("name");
        deathbanHistory = GenericUtils.createList(map.getOrDefault("deathbanHistory", Collections.emptyList()), Deathban.class);
        ores.putAll(GenericUtils.castMap(map.getOrDefault("ores", Maps.newHashMap()), Integer.class, Integer.class));
        kitUses.putAll(GenericUtils.castMap(map.getOrDefault("kitUses", Maps.newHashMap()), UUID.class, Integer.class));
        kitCooldowns.putAll(GenericUtils.castMap(map.getOrDefault("kitCooldowns", Maps.newHashMap()), UUID.class, Long.class));
        playtime = Long.parseLong((String) map.getOrDefault("playtime", "0"));
        mobs.putAll(GenericUtils.castMap(map.getOrDefault("mobs", Maps.newHashMap()), Integer.class, Integer.class));
        backLocation = (PersistableLocation) map.get("backlocation");
        nomobdrops = (Boolean) map.getOrDefault("mobdrops", false);
        minerLevel = MinerLevel.values()[(Integer) map.getOrDefault("minerLevel", 0)];
        spawncredits = ((Number) map.getOrDefault("spawncredits", 0)).intValue();
        oreInventory = (Boolean) map.getOrDefault("oreInventory", true);
        lastPlaytimeReclaim = playtime = Long.parseLong((String) map.getOrDefault("lastPlaytimeReclaim", "0"));
        lastPanic = Long.parseLong((String) map.getOrDefault("lastPanic", "0"));
        deathMessages = (Boolean) map.getOrDefault("deathMessages", true);
        lastDisguise = Long.parseLong((String) map.getOrDefault("lastDisguise", "0"));
    }

    @PrePersist
    public void PrePersistMethod() {
        ores_storage = ores.entrySet().stream().map(m -> new SimpleEntryIntInt(m.getKey(), m.getValue())).collect(Collectors.toList());
        mobs_storage = mobs.entrySet().stream().map(m -> new SimpleEntryIntInt(m.getKey(), m.getValue())).collect(Collectors.toList());
        kitUses_storage = kitUses.entrySet().stream().map(k -> new SimpleEntryUUIDInt(k.getKey(), k.getValue())).collect(Collectors.toList());
        kitCooldowns_storage = kitCooldowns.entrySet().stream().map(k -> new SimpleEntryUUIDLong(k.getKey(), k.getValue())).collect(Collectors.toList());
        total_ores = 0;
        for (Integer integer : ores.values()) {
            total_ores += integer;
        }
        playtime_storage = playtime + (online ? temporyPlaytime : 0);
        factionname = faction == null ? null : faction.getName();
    }

    @PostLoad
    public void postloadMethod() {
        if (ores_storage != null) {
            ores.clear();
            for (SimpleEntryIntInt entry : ores_storage) {
                ores.put(entry.getKey(), entry.getValue());
            }
        }
        if (mobs_storage != null) {
            mobs.clear();
            for (SimpleEntryIntInt entry : mobs_storage) {
                mobs.put(entry.getKey(), entry.getValue());
            }
        }
        if (kitUses_storage != null) {
            kitUses.clear();
            for (SimpleEntryUUIDInt entry : kitUses_storage) {
                kitUses.put(entry.getKey(), entry.getValue());
            }
        }
        if (kitCooldowns_storage != null) {
            kitCooldowns.clear();
            for (SimpleEntryUUIDLong entry : kitCooldowns_storage) {
                kitCooldowns.put(entry.getKey(), entry.getValue());
            }
        }
        playtime = playtime_storage;
    }

    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        map.put("name", this.name);
        map.put("faction-chat-spying", this.factionChatSpying.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("userUUID", this.userUUID.toString());
        map.put("brewed", this.healthBrewed);
        map.put("capzoneEntryAlerts", this.capzoneEntryAlerts);
        map.put("showClaimMap", this.showClaimMap);
        map.put("showLightning", this.showLightning);
        map.put("deathban", this.deathban);
        map.put("lastFactionLeaveMillis", Long.toString(this.lastFactionLeaveMillis));
        map.put("kills", this.kills);
        map.put("deaths", this.deaths);
        map.put("lastRevive", this.lastRevive);
        map.put("reclaimed", reclaimed);
        map.put("nocobble", nocobble);
        map.put("fdalerts", fdalerts);
        map.put("lffalerts", lffalerts);
        map.put("killStreak", killStreak);
        map.put("deathbanHistory", deathbanHistory);
        map.put("lives", lives);
        map.put("balance", balance);
        map.put("ores", ores);
        map.put("kitUses", kitUses);
        map.put("kitCooldowns", kitCooldowns);
        map.put("lastSeen", Long.toString(lastSeen));
        map.put("playtime", Long.toString(playtime));
        map.put("mobs", mobs);
        if (backLocation != null && backLocation.getWorld() != null) {
            map.put("backlocation", backLocation);
        }
        map.put("mobdrops", nomobdrops);
        map.put("minerLevel", minerLevel.ordinal());
        map.put("spawncredits", spawncredits);
        map.put("oreInventory", oreInventory);
        map.put("lastPlaytimeReclaim", Long.toString(lastPlaytimeReclaim));
        map.put("lastPanic", Long.toString(lastPanic));
        map.put("deathMessages", deathMessages);
        map.put("lastDisguise", lastDisguise);
        return map;
    }

    public boolean isDeathMessages() {
        return deathMessages;
    }

    public void setDeathMessages(boolean deathMessages) {
        this.deathMessages = deathMessages;
    }

    public long getLastPanic() {
        return lastPanic;
    }

    public void setLastPanic(long lastPanic) {
        this.lastPanic = lastPanic;
    }

    public int getKillStreak() {
        return killStreak;
    }

    public void setKillStreak(int killStreak) {
        this.killStreak = killStreak;
    }

    public boolean isFdalerts() {
        return fdalerts;
    }

    public void setFdalerts(boolean fdalerts) {
        this.fdalerts = fdalerts;
    }

    public boolean isLffalerts() {
        return lffalerts;
    }

    public void setLffalerts(boolean lffalerts) {
        this.lffalerts = lffalerts;
    }

    public boolean isNocobble() {
        return nocobble;
    }

    public void setNocobble(boolean nocobble) {
        this.nocobble = nocobble;
    }

    public boolean isCapzoneEntryAlerts() {
        return this.capzoneEntryAlerts;
    }

    public void setCapzoneEntryAlerts(boolean capzoneEntryAlerts) {
        this.capzoneEntryAlerts = capzoneEntryAlerts;
    }

    public boolean isShowClaimMap() {
        return this.showClaimMap;
    }

    public void setShowClaimMap(boolean showClaimMap) {
        this.showClaimMap = showClaimMap;
    }

    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getKDR() {
        if (kills == 0) {
            return 0.0;
        }
        if (deaths == 0) {
            return 1.0;
        }
        return (double) kills / (double) deaths;
    }

    public int getDiamondsMined() {
        return ores.getOrDefault(Material.DIAMOND_ORE.getId(), 0);
    }

    public int getHealthBrewed() {
        return this.healthBrewed;
    }

    public void setHealthBrewed(int healthBrewed) {
        this.healthBrewed = healthBrewed;
    }

    public Deathban getDeathban() {
        return this.deathban;
    }

    public void setDeathban(Deathban deathban) {
        this.deathban = deathban;
    }

    public void removeDeathban() {
        if (deathban != null) {
            if (deathbanHistory == null) {
                deathbanHistory = new ArrayList<>();
            }
            deathbanHistory.add(deathban);
        }
        this.deathban = null;
    }

    public long getLastFactionLeaveMillis() {
        return this.lastFactionLeaveMillis;
    }

    public void setLastFactionLeaveMillis(long lastFactionLeaveMillis) {
        this.lastFactionLeaveMillis = lastFactionLeaveMillis;
    }

    public boolean isShowLightning() {
        return this.showLightning;
    }

    public void setShowLightning(boolean showLightning) {
        this.showLightning = showLightning;
    }

    public Set<UUID> getFactionChatSpying() {
        return Collections.synchronizedSet(this.factionChatSpying);
    }

    public UUID getUserUUID() {
        return this.userUUID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.userUUID);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Deathban> getDeathbanHistory() {
        return Collections.synchronizedList(deathbanHistory);
    }

    public void setDeathbanHistory(List<Deathban> deathbanHistory) {
        this.deathbanHistory = new ArrayList<>(deathbanHistory);
    }

    public long getLastRevive() {
        return lastRevive;
    }

    public void setLastRevive(long lastRevive) {
        this.lastRevive = lastRevive;
    }

    public boolean isReclaimed() {
        return reclaimed;
    }

    public void setReclaimed(boolean reclaimed) {
        this.reclaimed = reclaimed;
    }

    public int getLives() {
        return lives;
    }

    public int setLives(int lives) {
        int previous = this.lives;
        this.lives = lives;
        return previous;
    }

    public int getBalance() {
        return balance;
    }

    public int setBalance(int balance) {
        int previous = this.balance;
        this.balance = balance;
        return previous;
    }

    public Faction getFaction() {
        return faction;
    }

    public PlayerFaction getPlayerFaction() {
        return (PlayerFaction) faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
        if(faction == null){
            this.factionname = null;
        }
        else{
            this.factionname = faction.getName();
            this.previousFactions.add(factionname);
        }
    }

    public Map<Integer, Integer> getOres() {
        return ores;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Map<UUID, Integer> getKitUses() {
        return kitUses;
    }

    public Map<UUID, Long> getKitCooldowns() {
        return kitCooldowns;
    }

    public int getUses(Kit kit) {
        return kitUses.getOrDefault(kit.getUniqueID(), 0);
    }

    public void setUses(Kit kit, int uses) {
        kitUses.put(kit.getUniqueID(), uses);
    }

    public void incrementKitUses(Kit kit) {
        setUses(kit, getUses(kit) + 1);
    }

    public long getLastUse(Kit kit) {
        return kitCooldowns.getOrDefault(kit.getUniqueID(), 0L);
    }

    public void setLastUse(Kit kit, long lastUse) {
        kitCooldowns.put(kit.getUniqueID(), lastUse);
    }

    public void updateKitCooldown(Kit kit) {
        setLastUse(kit, System.currentTimeMillis());
    }

    public long getRemainingKitCooldown(Kit kit) {
        return (getLastUse(kit) + kit.getDelayMillis()) - System.currentTimeMillis();
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public void calcPlaytime(long now) {
        if (online) {
            temporyPlaytime = now - lastSeen;
        } else {
            temporyPlaytime = 0;
        }
    }

    public long getCurrentPlaytime(){
        calcPlaytime(System.currentTimeMillis());
        return playtime + temporyPlaytime;
    }

    public int getCreepersKilled() {
        return mobs.getOrDefault(EntityType.CREEPER.getTypeId(), 0);
    }

    public int getEnderKilled() {
        return mobs.getOrDefault(EntityType.ENDERMAN.getTypeId(), 0);
    }

    public Map<Integer, Integer> getMobs() {
        return mobs;
    }

    public PersistableLocation getBackLocation() {
        return backLocation;
    }

    public void setBackLocation(PersistableLocation backLocation) {
        this.backLocation = backLocation;
    }

    public boolean isNomobdrops() {
        return nomobdrops;
    }

    public void setNomobdrops(boolean nomobdrops) {
        this.nomobdrops = nomobdrops;
    }

    public MinerLevel getMinerLevel() {
        return minerLevel;
    }

    public void setMinerLevel(MinerLevel minerLevel) {
        this.minerLevel = minerLevel;
    }

    public int getSpawncredits() {
        return spawncredits;
    }

    public void setSpawncredits(int spawncredits) {
        this.spawncredits = spawncredits;
    }

    public List<EventCapture> getEventCaptures() {
        return eventCaptures;
    }

    public Set<String> getPreviousFactions() {
        return previousFactions;
    }

    public boolean isOreInventory() {
        return oreInventory;
    }

    public void setOreInventory(boolean oreInventory) {
        this.oreInventory = oreInventory;
    }

    public long getLastPlaytimeReclaim() {
        return lastPlaytimeReclaim;
    }

    public void setLastPlaytimeReclaim(long lastPlaytimeReclaim) {
        this.lastPlaytimeReclaim = lastPlaytimeReclaim;
    }

    public Relation getRelation(FactionUser other){
        if(faction == null){
            return Relation.ENEMY;
        }
        return faction.getRelation(other);
    }

    public long getLastDisguise() {
        return lastDisguise;
    }

    public void setLastDisguise(long lastDisguise) {
        this.lastDisguise = lastDisguise;
    }
}
