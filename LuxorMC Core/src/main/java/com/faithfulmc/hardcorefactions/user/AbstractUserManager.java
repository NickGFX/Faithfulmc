package com.faithfulmc.hardcorefactions.user;

import com.faithfulmc.hardcorefactions.HCF;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public abstract class AbstractUserManager implements Listener {
    protected final HCF plugin;
    protected final ConcurrentMap<UUID, FactionUser> inMemoryStorage;
    protected final ConcurrentMap<UUID, FactionUser> onlineStorage;
    protected final Map<String, UUID> uuidCache = Collections.synchronizedMap(new TreeMap<String, UUID>(String.CASE_INSENSITIVE_ORDER));
    private static final Pattern USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    public AbstractUserManager(HCF plugin) {
        inMemoryStorage = new ConcurrentHashMap<>();
        onlineStorage = new ConcurrentHashMap<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reloadUserData();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        FactionUser factionUser = inMemoryStorage.get(uuid);
        if(factionUser == null){
            factionUser = new FactionUser(uuid);
            inMemoryStorage.put(uuid, factionUser);
            saveUser(factionUser);
        }
        onlineStorage.put(uuid, factionUser);
        uuidCache.put(player.getName(), uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        onlineStorage.remove(uuid);
    }

    public ConcurrentMap<UUID, FactionUser> getUsers() {
        return inMemoryStorage;
    }

    public FactionUser getUser(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        FactionUser factionUser;
        return (factionUser = onlineStorage.get(uuid)) != null ? factionUser : (factionUser = inMemoryStorage.get(uuid)) != null ? factionUser : insertAndReturn(uuid, new FactionUser(uuid));
    }

    public FactionUser getIfContainedOffline(UUID uuid){
        Preconditions.checkNotNull(uuid);
        FactionUser factionUser;
        return (factionUser = onlineStorage.get(uuid)) != null ? factionUser : inMemoryStorage.get(uuid);
    }

    public FactionUser insertAndReturn(UUID uuid, FactionUser factionUser){
        inMemoryStorage.put(uuid, factionUser);
        return factionUser;
    }

    public FactionUser getIfContains(UUID uuid){
        return onlineStorage.get(uuid);
    }

    public UUID fetchUUID(String username){
        Player player = Bukkit.getPlayer(username);
        if(player != null){
            return player.getUniqueId();
        }
        if(USERNAME_REGEX.matcher(username).matches()){
            return uuidCache.get(username);
        }
        return null;
    }

    public ConcurrentMap<UUID, FactionUser> getOnlineStorage() {
        return onlineStorage;
    }

    public void saveUser(FactionUser user) {

    }

    public abstract void saveUserData();

    public abstract void reloadUserData();
}
