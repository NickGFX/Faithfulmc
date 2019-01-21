package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.events.EventCapture;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.event.FactionDtrChangeEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerJoinedFactionEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerLeaveFactionEvent;
import com.faithfulmc.hardcorefactions.faction.event.PlayerLeftFactionEvent;
import com.faithfulmc.hardcorefactions.faction.event.cause.FactionLeaveCause;
import com.faithfulmc.hardcorefactions.faction.struct.Raidable;
import com.faithfulmc.hardcorefactions.faction.struct.RegenStatus;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import com.faithfulmc.hardcorefactions.timer.type.TeleportTimer;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.mongo.SimpleEntryUUIDRelation;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.GenericUtils;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.PersistableLocation;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Entity(value = "faction")
public class PlayerFaction extends ClaimableFaction implements Raidable {
    private static final UUID[] EMPTY_UUID_ARRAY = new UUID[0];
    @Transient
    protected final ConcurrentHashMap<UUID, Relation> requestedRelations = new ConcurrentHashMap<>();
    @Transient
    protected final ConcurrentHashMap<UUID, Relation> relations = new ConcurrentHashMap<>();
    @Transient
    protected final ConcurrentHashMap<UUID, FactionMember> members = new ConcurrentHashMap<>();
    @Transient
    protected TreeSet<String> invitedPlayerNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    protected PersistableLocation home = null;
    protected String announcement = null;
    protected boolean open = false;
    protected long regenCooldownTimestamp = 0;
    @Embedded
    public List<FactionMember> members_storage = null;
    @Embedded
    private List<SimpleEntryUUIDRelation> requestedRelations_storage = null;
    @Embedded
    private List<SimpleEntryUUIDRelation> relations_storage = null;
    protected Map<Role, Set<String>> members_strings;
    @Embedded
    private List<EventCapture> captures = new ArrayList<>();
    private long lastDtrUpdateTimestamp = 0;
    private boolean muted = false;
    private long mutetime = 0;
    private UUID focus = null;
    private String focusname = null;
    private RegenStatus regenStatus = null;
    private Double maxDeathsUntilRaidable;
    private int points = 0;

    {
        deathsUntilRaidable = 1.1;
    }

    @PrePersist
    public void PrePersistMethod(){
        regenStatus = getRegenStatus();
        maxDeathsUntilRaidable = getMaximumDeathsUntilRaidable();
        members_storage = new ArrayList<>(members.values());
        requestedRelations_storage = requestedRelations.entrySet().stream().map(r -> new SimpleEntryUUIDRelation(r.getKey(), r.getValue())).collect(Collectors.toList());
        relations_storage = relations.entrySet().stream().map(r -> new SimpleEntryUUIDRelation(r.getKey(), r.getValue())).collect(Collectors.toList());
        members_strings = new HashMap<>();
        for(FactionMember member: members_storage){
            Set<String> users = members_strings.getOrDefault(member.getRole(), new HashSet<>());
            users.add(member.getName());
            members_strings.putIfAbsent(member.getRole(), users);
        }
    }

    public PlayerFaction(String name) {
        super(name);
        this.deathsUntilRaidable = 1.1D;
        muted = false;
        mutetime = 0;
        balance = 0;
        focus = null;
        focusname = null;
    }

    public PlayerFaction() {
    }

    public PlayerFaction(Map map) {
        super(map);
        balance = 0;
        this.deathsUntilRaidable = 1.1D;
        Iterator object = GenericUtils.castMap(map.get("members"), String.class, FactionMember.class).entrySet().iterator();
        while (object.hasNext()) {
            Map.Entry entry = (Map.Entry) object.next();
            this.members.put(UUID.fromString((String) entry.getKey()), (FactionMember) entry.getValue());
        }
        this.invitedPlayerNames.addAll(GenericUtils.createList(map.get("invitedPlayerNames"), String.class));
        Object object1 = map.get("home");
        if (object1 != null) {
            this.home = ((PersistableLocation) object1);
        }
        object1 = map.get("announcement");
        if (object1 != null) {
            this.announcement = ((String) object1);
        }
        Iterator entry2 = GenericUtils.castMap(map.get("relations"), String.class, String.class).entrySet().iterator();
        while (entry2.hasNext()) {
            Map.Entry entry1 = (Map.Entry) entry2.next();
            this.relations.put(UUID.fromString((String) entry1.getKey()), Relation.valueOf((String) entry1.getValue()));
        }
        entry2 = GenericUtils.castMap(map.get("requestedRelations"), String.class, String.class).entrySet().iterator();
        while (entry2.hasNext()) {
            Map.Entry entry1 = (Map.Entry) entry2.next();
            this.requestedRelations.put(UUID.fromString((String) entry1.getKey()), Relation.valueOf((String) entry1.getValue()));
        }
        this.open = ((Boolean) map.get("open"));
        this.balance = ((Integer) map.get("balance"));
        this.deathsUntilRaidable = ((Double) map.get("deathsUntilRaidable"));
        this.regenCooldownTimestamp = Long.parseLong((String) map.get("regenCooldownTimestamp"));
        this.lastDtrUpdateTimestamp = Long.parseLong((String) map.get("lastDtrUpdateTimestamp"));
        muted = (Boolean) map.getOrDefault("muted", false);
        mutetime = Long.parseLong((String) map.getOrDefault("mutetime", "0"));
        if (map.containsKey("focus")) {
            focus = UUID.fromString((String) map.get("focus"));
            focusname = (String) map.get("focusname");
        }
        points = ((Integer) map.getOrDefault("points", 0));
    }

    @PostLoad
    public void postloadMethod() {
        if (members_storage != null) {
            members.clear();
            for (FactionMember factionMember : members_storage) {
                this.members.put(factionMember.getUniqueId(), factionMember);
            }
            members_storage = null;
        }
        if (requestedRelations_storage != null) {
            this.requestedRelations.clear();
            for (SimpleEntryUUIDRelation entry : requestedRelations_storage) {
                requestedRelations.put(entry.getKey(), entry.getValue());
            }
            requestedRelations_storage = null;
        }
        if (relations_storage != null) {
            this.relations.clear();
            for (SimpleEntryUUIDRelation entry : relations_storage) {
                relations.put(entry.getKey(), entry.getValue());
            }
            relations_storage = null;
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        HashMap<String, String> relationSaveMap = new HashMap(this.relations.size());
        Iterator requestedRelationsSaveMap = this.relations.entrySet().iterator();
        while (requestedRelationsSaveMap.hasNext()) {
            Map.Entry entrySet = (Map.Entry) requestedRelationsSaveMap.next();
            relationSaveMap.put(((UUID) entrySet.getKey()).toString(), ((Relation) entrySet.getValue()).name());
        }
        map.put("relations", relationSaveMap);
        HashMap<String, String> requestedRelationsSaveMap1 = new HashMap(this.requestedRelations.size());
        Iterator entrySet1 = this.requestedRelations.entrySet().iterator();
        while (entrySet1.hasNext()) {
            Map.Entry saveMap = (Map.Entry) entrySet1.next();
            requestedRelationsSaveMap1.put(saveMap.getKey().toString(), ((Relation) saveMap.getValue()).name());
        }
        map.put("requestedRelations", requestedRelationsSaveMap1);
        Set entrySet2 = this.members.entrySet();
        LinkedHashMap saveMap1 = new LinkedHashMap(this.members.size());
        Iterator var6 = entrySet2.iterator();
        while (var6.hasNext()) {
            Map.Entry entry = (Map.Entry) var6.next();
            saveMap1.put(entry.getKey().toString(), entry.getValue());
        }
        map.put("members", saveMap1);
        map.put("invitedPlayerNames", new ArrayList(this.invitedPlayerNames));
        if (this.home != null) {
            map.put("home", this.home);
        }
        if (this.announcement != null) {
            map.put("announcement", this.announcement);
        }
        map.put("open", Boolean.valueOf(this.open));
        map.put("balance", Integer.valueOf(this.balance));
        map.put("deathsUntilRaidable", Double.valueOf(this.deathsUntilRaidable));
        map.put("regenCooldownTimestamp", Long.toString(this.regenCooldownTimestamp));
        map.put("lastDtrUpdateTimestamp", Long.toString(this.lastDtrUpdateTimestamp));
        map.put("muted", muted);
        map.put("mutetime", Long.toString(mutetime));
        if (focus != null && focusname != null) {
            map.put("focus", focus.toString());
            map.put("focusname", focusname);
        }
        map.put("points", points);
        return map;
    }

    public List<Player> getOnlineArchers(){
        return getOnlinePlayers().stream().filter(player -> HCF.getInstance().getHcfClassManager().getEquippedClass(player) instanceof ArcherClass).collect(Collectors.toList());
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        if(points > this.points){
            broadcast(ChatColor.YELLOW + "Your faction was awarded " + ChatColor.GOLD + (points - this.points) + ChatColor.YELLOW + " points.");
        }
        else if(points < this.points){
            broadcast(ChatColor.YELLOW + "Your faction lost " + ChatColor.GOLD + (this.points - points) + ChatColor.YELLOW + " points.");
        }
        this.points = points;
    }

    public UUID getFocus() {
        return focus;
    }

    public void setFocus(UUID focus) {
        this.focus = focus;
    }

    public String getFocusname() {
        return focusname;
    }

    public void setFocusname(String focusname) {
        this.focusname = focusname;
    }

    public boolean setMember(UUID playerUUID, FactionMember factionMember) {
        return setMember(null, playerUUID, factionMember, false);
    }

    public boolean setMember(UUID playerUUID, FactionMember factionMember, boolean force) {
        return setMember(null, playerUUID, factionMember, force);
    }

    public boolean setMember(Player player, FactionMember factionMember) {
        return setMember(player, player.getUniqueId(), factionMember, false);
    }

    public boolean setMember(Player player, FactionMember factionMember, boolean force) {
        return setMember(player, player.getUniqueId(), factionMember, force);
    }

    private boolean setMember(Player player, UUID playerUUID, FactionMember factionMember, boolean force) {
        if (factionMember == null) {
            if (!force) {
                PlayerLeaveFactionEvent event = player == null ? new PlayerLeaveFactionEvent(playerUUID, this, FactionLeaveCause.LEAVE) : new PlayerLeaveFactionEvent(player, this, FactionLeaveCause.LEAVE);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return false;
                }
            }
            this.members.remove(playerUUID);
            setDeathsUntilRaidable(Math.min(this.deathsUntilRaidable, getMaximumDeathsUntilRaidable()));
            PlayerLeftFactionEvent event2 = player == null ? new PlayerLeftFactionEvent(playerUUID, this, FactionLeaveCause.LEAVE) : new PlayerLeftFactionEvent(player, this, FactionLeaveCause.LEAVE);
            Bukkit.getPluginManager().callEvent(event2);
            return true;
        }
        PlayerJoinedFactionEvent eventPre = player == null ? new PlayerJoinedFactionEvent(playerUUID, this) : new PlayerJoinedFactionEvent(player, this);
        Bukkit.getPluginManager().callEvent(eventPre);
        this.lastDtrUpdateTimestamp = System.currentTimeMillis();
        this.invitedPlayerNames.remove(factionMember.getName());
        this.members.put(playerUUID, factionMember);
        return true;
    }

    public boolean isMuted() {
        if (muted) {
            if (mutetime == -1 || System.currentTimeMillis() < mutetime) {
                return true;
            }
            muted = false;
        }
        return false;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public long getMutetime() {
        return mutetime;
    }

    public void setMutetime(long mutetime) {
        this.mutetime = mutetime;
    }

    public Collection<UUID> getAllied() {
        return ConfigurationService.MAX_ALLIES_PER_FACTION == 0 ? Collections.emptyList() :
                relations.entrySet().stream().filter(uuidRelationEntry -> uuidRelationEntry.getValue() == Relation.ALLY).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<PlayerFaction> getAlliedFactions() {
        Collection<UUID> allied = getAllied();
        if(allied.isEmpty()){
            return Collections.emptyList();
        }
        Iterator<UUID> iterator = allied.iterator();
        List<PlayerFaction> results = new ArrayList<>(allied.size());
        while (iterator.hasNext()) {
            Faction faction = HCF.getInstance().getFactionManager().getFaction(iterator.next());
            if ((faction instanceof PlayerFaction)) {
                results.add((PlayerFaction) faction);
            } else {
                iterator.remove();
            }
        }
        return results;
    }

    public Map<UUID, Relation> getRequestedRelations() {
        return this.requestedRelations;
    }

    public Map<UUID, Relation> getRelations() {
        return this.relations;
    }

    public void setRelation(Faction other, Relation relation){
        relations.put(other.getUniqueID(), relation);
    }

    public Map<UUID, FactionMember> getMembers() {
        return ImmutableMap.copyOf(this.members);
    }

    public Set<Player> getOnlinePlayers() {
        return getOnlinePlayers((CommandSender) null);
    }

    public Set<Player> getOnlinePlayers(CommandSender sender) {
        Set<Map.Entry<UUID, FactionMember>> entrySet = getOnlineMembers(sender).entrySet();
        Set<Player> results = new HashSet<>(entrySet.size());
        Iterator<Map.Entry<UUID, FactionMember>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FactionMember> entry = iterator.next();
            FactionUser factionUser = entry.getValue().getFactionUser();
            if(factionUser != null && factionUser.isOnline()) {
                Player player = factionUser.getPlayer();
                if (player != null && player.isOnline()) {
                    results.add(player);
                }
            }
        }
        online_members = results.size();
        return results;
    }

    public Map<UUID, FactionMember> getOnlineMembers() {
        return getOnlineMembers(null);
    }

    public Map<UUID, FactionMember> getOnlineMembers(CommandSender sender) {
        Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
        HashMap<UUID, FactionMember> results = new HashMap<>();
        Iterator<Map.Entry<UUID, FactionMember>> iterator = this.members.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FactionMember> entry = iterator.next();
            Player target = Bukkit.getPlayer(entry.getKey());
            if ((target != null) && ((senderPlayer == null) || (senderPlayer.canSee(target)))) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }

    public FactionMember getLeader() {
        Map<UUID, FactionMember> members = this.members;
        Iterator<Map.Entry<UUID, FactionMember>> iterator = members.entrySet().iterator();
        Map.Entry<UUID, FactionMember> entry;
        do {
            if (!iterator.hasNext()) {
                return null;
            }
        } while (((entry = iterator.next()).getValue()).getRole() != Role.LEADER);
        return entry.getValue();
    }

    public Set<FactionMember> getCoLeaders() {
        return members.values().stream().filter(member -> member.getRole() == Role.COLEADER).collect(Collectors.toSet());
    }

    public FactionMember getMember(HCF hcf, String memberName) {
        UUID uuid = hcf.getUserManager().fetchUUID(memberName);
        if (uuid == null) {
            return null;
        }
        return  this.members.get(uuid);
    }

    public FactionMember getMember(Player player) {
        return getMember(player.getUniqueId());
    }

    public FactionMember getMember(UUID memberUUID) {
        return (FactionMember) this.members.get(memberUUID);
    }

    public Set<String> getInvitedPlayerNames() {
        return this.invitedPlayerNames;
    }

    public Location getHome() {
        return this.home == null ? null : this.home.getLocation();
    }

    public void setHome(Location home) {
        if ((home == null) && (this.home != null)) {
            TeleportTimer timer = HCF.getInstance().getTimerManager().teleportTimer;
            Iterator<Player> var3 = getOnlinePlayers().iterator();
            while (var3.hasNext()) {
                Player player = (Player) var3.next();
                Location destination = (Location) timer.getDestination(player);
                if (Objects.equal(destination, this.home.getLocation())) {
                    timer.clearCooldown(player);
                    player.sendMessage(ConfigurationService.RED + "Your home was unset, so your " + timer.getDisplayName() + ConfigurationService.RED + " timer has been cancelled");
                }
            }
        }
        this.home = (home == null ? null : new PersistableLocation(home));
    }

    public String getAnnouncement() {
        return this.announcement;
    }

    public void setAnnouncement(@Nullable String announcement) {
        this.announcement = announcement;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public boolean isRaidable() {
        return ConfigurationService.KIT_MAP ? HCF.getInstance().getEotwHandler().isEndOfTheWorld() : this.deathsUntilRaidable <= 0.0D;
    }

    public double getDeathsUntilRaidable() {
        return getDeathsUntilRaidable(true);
    }

    public double getMaximumDeathsUntilRaidable() {
        int size = this.members.size();
        if (size == 1) {
            return 1.1;
        }
        else if(size == 2){
            return 2.1;
        }
        else if(ConfigurationService.FACTION_PLAYER_LIMIT == 3 && size == 3){
            return 3.1;
        }
        double defaultDTR = 5;
        if(ConfigurationService.ORIGINS){
            defaultDTR = 5.5;
        }
        else {
            if (size >= 24) {
                defaultDTR = 7.5;
            } else if (size >= 20) {
                defaultDTR = 7;
            } else if (size >= 16) {
                defaultDTR = 6.5;
            } else if (size >= 12) {
                defaultDTR = 6;
            } else if (size >= 8) {
                defaultDTR = 5.5;
            }
        }
        return Math.min(defaultDTR, size * 0.9D);
    }

    public double getDeathsUntilRaidable(boolean updateLastCheck) {
        if (updateLastCheck) {
            updateDeathsUntilRaidable();
        }
        return this.deathsUntilRaidable;
    }

    public ChatColor getDtrColour() {
        updateDeathsUntilRaidable();
        if (this.deathsUntilRaidable <= 0.0D) {
            return ConfigurationService.RED;
        }
        if (this.deathsUntilRaidable < 1.0D) {
            return ConfigurationService.YELLOW;
        }
        return ChatColor.GREEN;
    }

    private void updateDeathsUntilRaidable() {
        if (getRegenStatus() == RegenStatus.REGENERATING) {
            long now = System.currentTimeMillis();
            long millisPassed = now - this.lastDtrUpdateTimestamp;
            if (millisPassed >= ConfigurationService.DTR_MILLIS_BETWEEN_UPDATES) {
                long remainder = millisPassed % ConfigurationService.DTR_MILLIS_BETWEEN_UPDATES;
                int multiplier = (int) ((millisPassed + remainder) / ConfigurationService.DTR_MILLIS_BETWEEN_UPDATES);
                double increase = multiplier * 0.1D;
                this.lastDtrUpdateTimestamp = (now - remainder);
                setDeathsUntilRaidable(this.deathsUntilRaidable + increase);
            }
        }
    }

    public double setDeathsUntilRaidable(double deathsUntilRaidable) {
        return setDeathsUntilRaidable(deathsUntilRaidable, true);
    }

    private double setDeathsUntilRaidable(double deathsUntilRaidable, boolean limit) {
        deathsUntilRaidable = deathsUntilRaidable * 100.0D / 100.0D;
        if (limit) {
            deathsUntilRaidable = Math.min(deathsUntilRaidable, getMaximumDeathsUntilRaidable());
        }
        if (deathsUntilRaidable - this.deathsUntilRaidable != 0.0D) {
            FactionDtrChangeEvent event = new FactionDtrChangeEvent(FactionDtrChangeEvent.DtrUpdateCause.REGENERATION, this, this.deathsUntilRaidable, deathsUntilRaidable);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                deathsUntilRaidable = event.getNewDtr();
                if ((deathsUntilRaidable > 0.0D) && (deathsUntilRaidable <= 0.0D)) {
                    HCF.getInstance().getLogger().info("Faction " + getName() + " is now raidable.");
                }
                this.lastDtrUpdateTimestamp = System.currentTimeMillis();
                return this.deathsUntilRaidable = deathsUntilRaidable;
            }
        }
        return this.deathsUntilRaidable;
    }

    protected long getRegenCooldownTimestamp() {
        return this.regenCooldownTimestamp;
    }

    public long getRemainingRegenerationTime() {
        return this.regenCooldownTimestamp == 0L ? 0L : this.regenCooldownTimestamp - System.currentTimeMillis();
    }

    public void setRemainingRegenerationTime(long millis) {
        long systemMillis = System.currentTimeMillis();
        this.regenCooldownTimestamp = (systemMillis + millis);
        this.lastDtrUpdateTimestamp = (systemMillis + ConfigurationService.DTR_MILLIS_BETWEEN_UPDATES * 2L);
    }

    public RegenStatus getRegenStatus() {
        if (getRemainingRegenerationTime() > 0L) {
            return RegenStatus.PAUSED;
        }
        if (getMaximumDeathsUntilRaidable() > this.deathsUntilRaidable) {
            return RegenStatus.REGENERATING;
        }
        return RegenStatus.FULL;
    }

    public void printStats(CommandSender sender) {
        int combinedKills = 0;
        int combinedDiamonds = 0;
        int combinedDeaths = 0;
        int combinedEndermen = 0;
        int combinedCreepers = 0;
        long combinedPlaytime = 0L;
        String name1 = getDisplayName(sender);
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ConfigurationService.GOLD + ChatColor.stripColor(name1));
        for(Map.Entry<UUID, FactionMember> entry: this.members.entrySet()){
            FactionUser user = entry.getValue().getFactionUser();
            combinedKills += user.getKills();
            combinedDeaths += user.getDeaths();
            combinedDiamonds += user.getDiamondsMined();
            combinedPlaytime += user.getPlaytime();
            combinedEndermen += user.getEnderKilled();
            combinedCreepers += user.getCreepersKilled();
        }
        sender.sendMessage(ConfigurationService.YELLOW + "  Kills: " + ConfigurationService.GRAY + combinedKills + ConfigurationService.YELLOW + "  Deaths: " + ConfigurationService.GRAY + combinedDeaths);
        if(!ConfigurationService.KIT_MAP) {
            sender.sendMessage(ConfigurationService.YELLOW + "  Enderman: " + ConfigurationService.GRAY + combinedEndermen + ConfigurationService.YELLOW + "  Creepers: " + ConfigurationService.GRAY + combinedCreepers);
            sender.sendMessage(ConfigurationService.YELLOW + "  Diamonds: " + ConfigurationService.GRAY + combinedDiamonds);
        }
        sender.sendMessage(ConfigurationService.YELLOW + "  Points: " + ConfigurationService.GRAY + points);
        sender.sendMessage(ConfigurationService.YELLOW + "  PlayTime: " + ConfigurationService.GRAY + DurationFormatUtils.formatDurationWords(combinedPlaytime, true, true));
    }

    public void printDetails(CommandSender sender) {
        String leaderName = null;
        Set<String> allyNames = new HashSet<>(ConfigurationService.MAX_ALLIES_PER_FACTION);
        Iterator<Map.Entry<UUID, Relation>> relationsIterator = this.relations.entrySet().iterator();
        while (relationsIterator.hasNext()) {
            Map.Entry<UUID, Relation> memberNames = relationsIterator.next();
            Faction relationFaction = HCF.getInstance().getFactionManager().getFaction(memberNames.getKey());
            if ((relationFaction instanceof PlayerFaction)) {
                PlayerFaction playerFaction = (PlayerFaction) relationFaction;
                allyNames.add(playerFaction.getDisplayName(sender) + ConfigurationService.GRAY + '[' + ConfigurationService.GRAY + playerFaction.getOnlinePlayers(sender).size() + ConfigurationService.GRAY + '/' + ConfigurationService.GRAY + playerFaction.members.size() + ConfigurationService.GRAY + ']');
            }
        }
        int killCounter = 0;
        Set<String> memberNames1 = new HashSet<>();
        Set<String> coLeaderNames = new HashSet<>();
        Set<String> captainNames1 = new HashSet<>();
        Iterator<Map.Entry<UUID, FactionMember>> playerFaction1 = this.members.entrySet().iterator();
        while (playerFaction1.hasNext()) {
            Map.Entry<UUID, FactionMember> entry = playerFaction1.next();
            FactionMember factionMember = entry.getValue();
            Player target = factionMember.toOnlinePlayer();
            FactionUser user = factionMember.getFactionUser();
            int kills = user.getKills();
            killCounter += kills;
            Deathban deathban = user.getDeathban();
            ChatColor colour;
            if ((deathban != null) && (deathban.isActive())) {
                colour = ConfigurationService.RED;
            } else if ((target != null) && ((!(sender instanceof Player)) || (((Player) sender).canSee(target)))) {
                colour = ChatColor.GREEN;
            } else {
                colour = ConfigurationService.GRAY;
            }
            String memberName = colour + factionMember.getName() + ConfigurationService.YELLOW + '[' + ChatColor.GREEN + kills + ConfigurationService.YELLOW + ']';
            if (factionMember.getRole() == Role.LEADER) {
                leaderName = memberName;
            }
            else if (factionMember.getRole() == Role.COLEADER) {
                coLeaderNames.add(memberName);
            }
            else if (factionMember.getRole() == Role.CAPTAIN) {
                captainNames1.add(memberName);
            }
            else {
                memberNames1.add(memberName);
            }
        }
        total_kills = killCounter;
        String name1 = getDisplayName(sender);
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ConfigurationService.GOLD + ChatColor.stripColor(name1) + ConfigurationService.GRAY + " [" + getOnlineMembers().size() + "/" + getMembers().size() + "] " + ConfigurationService.YELLOW + " Home: " + ConfigurationService.GRAY + (home == null ? "Not set" : this.home.getLocation().getBlockX() + ", " + this.home.getLocation().getBlockZ()));
        if (leaderName != null) {
            sender.sendMessage(ConfigurationService.YELLOW + "  Leader: " + ConfigurationService.RED + leaderName);
        }
        if (!coLeaderNames.isEmpty()) {
            sender.sendMessage(ConfigurationService.YELLOW + "  Co-Leader" + (coLeaderNames.size() == 1 ? "s" : "") + ": " + ConfigurationService.RED + StringUtils.join(coLeaderNames, new StringBuilder().append(ConfigurationService.GRAY).append(", ").toString()));
        }
        if (!captainNames1.isEmpty()) {
            sender.sendMessage(ConfigurationService.YELLOW + "  Captains: " + ConfigurationService.RED + StringUtils.join(captainNames1, new StringBuilder().append(ConfigurationService.GRAY).append(", ").toString()));
        }
        if (!memberNames1.isEmpty()) {
            sender.sendMessage(ConfigurationService.YELLOW + "  Members: " + ConfigurationService.RED + StringUtils.join(memberNames1, new StringBuilder().append(ConfigurationService.GRAY).append(", ").toString()));
        }
        sender.sendMessage(ConfigurationService.YELLOW + "  Total Kills: " + ConfigurationService.GRAY + killCounter);
        sender.sendMessage(ConfigurationService.YELLOW + "  Balance: " + ConfigurationService.GRAY + '$' + this.balance);
        if (!ConfigurationService.KIT_MAP) {
            long dtrRegenRemaining = getRemainingRegenerationTime();
            sender.sendMessage(ConfigurationService.YELLOW + "  DTR: " + getDtrColour() + JavaUtils.format(getDeathsUntilRaidable(false)) + getRegenStatus().getSymbol() + (dtrRegenRemaining > 0L ? ConfigurationService.YELLOW + " Regen: " + ConfigurationService.GRAY + DurationFormatUtils.formatDurationWords(dtrRegenRemaining, true, true) : ""));
        }
        sender.sendMessage(ConfigurationService.YELLOW + "  Points: " + ConfigurationService.GRAY + points);
        if (!allyNames.isEmpty()) {
            if (allyNames.size() == 1) {
                sender.sendMessage(ConfigurationService.YELLOW + "  Ally: " + StringUtils.join(allyNames, new StringBuilder().append(ConfigurationService.GRAY).append(", ").toString()));
            } else {
                sender.sendMessage(ConfigurationService.YELLOW + "  Allies: " + StringUtils.join(allyNames, new StringBuilder().append(ConfigurationService.GRAY).append(", ").toString()));
            }
        }
        int kothCaptures = 0;
        int conquestCaptures = 0;
        int citadelCaptures = 0;
        for(EventCapture eventCapture: captures) {
            switch (eventCapture.getEventType()) {
                case KOTH: {
                    kothCaptures++;
                    break;
                }
                case CONQUEST: {
                    conquestCaptures++;
                    break;
                }
                case CITADEL:
                    citadelCaptures++;
                    break;
            }
        }
        if(kothCaptures > 0){
            sender.sendMessage(ConfigurationService.YELLOW + "  Koth Captures: " + ConfigurationService.GRAY + kothCaptures);
        }
        if(conquestCaptures > 0){
            sender.sendMessage(ConfigurationService.YELLOW + "  Conquest Captures: " + ConfigurationService.GRAY + conquestCaptures);
        }
        if(citadelCaptures > 0){
            sender.sendMessage(ConfigurationService.YELLOW + "  Citadel Captures: " + ConfigurationService.GRAY + citadelCaptures);
        }
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public void broadcast(String message) {
        broadcast(message, EMPTY_UUID_ARRAY);
    }

    public void broadcast(String[] messages) {
        broadcast(messages, EMPTY_UUID_ARRAY);
    }

    public void broadcast(String message, @Nullable UUID... ignore) {
        broadcast(new String[]{message}, ignore);
    }

    public void broadcast(String[] messages, UUID... ignore) {
        Preconditions.checkNotNull(messages, "Messages cannot be null");
        Preconditions.checkArgument(messages.length > 0, "Message array cannot be empty");
        Collection<Player> players = getOnlinePlayers();
        Collection<UUID> ignores = ignore.length == 0 ? Collections.emptySet() : Sets.newHashSet(ignore);
        for (Player player : players) {
            if (!ignores.contains(player.getUniqueId())) {
                player.sendMessage(messages);
            }
        }
    }

    public long getLastDtrUpdateTimestamp() {
        return this.lastDtrUpdateTimestamp;
    }

    public List<EventCapture> getCaptures() {
        return captures;
    }
}
