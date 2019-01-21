package com.faithfulmc.framework.user;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.event.PlayerVanishEvent;
import com.faithfulmc.framework.user.util.NameHistory;
import com.faithfulmc.util.GenericUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.net.InetAddresses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Transient;

import java.util.*;

@Entity(value = "globaluser")
public class BaseUser extends ServerParticipator {
    @Embedded private List<String> addressHistories = new ArrayList<>();
    @Embedded private List<NameHistory> nameHistories = new ArrayList<>();
    @Embedded private List<String> notes = new ArrayList<>();
    private Long firstJoined = null;
    private boolean messagingSounds = true;
    @Transient private boolean vanished = false;
    private long lastSeen;
    private boolean online = false;
    private String lastServer = null;
    private String group = null;
    private ChatColor chatColor = null;
    private String nickName = null;
    @Transient private boolean loaded = true;

    @PostLoad
    public void postLoadMethod(){
        if(firstJoined == null){
            firstJoined = lastSeen;
        }
    }

    public BaseUser(){
    }

    public BaseUser(UUID uniqueID, String name) {
        this(uniqueID, true);
        setName(name);
    }

    public BaseUser(UUID uniqueID, boolean loaded) {
        super(uniqueID);
        this.lastSeen = System.currentTimeMillis();
        firstJoined = lastSeen;
        this.loaded = loaded;
    }

    public BaseUser(Map<String, Object> map) {
        super(map);
        this.notes.addAll(GenericUtils.createList(map.get("notes"), String.class));
        this.addressHistories.addAll(GenericUtils.createList(map.get("addressHistories"), String.class));
        this.lastSeen = ((Number) map.getOrDefault("lastSeen", -1L)).longValue();
        Object object = map.get("nameHistories");
        if (object != null) {
            this.nameHistories.addAll(GenericUtils.createList(object, NameHistory.class));
        }
        if ((object = map.get("messagingSounds")) instanceof Boolean) {
            this.messagingSounds = (boolean) object;
        }
        if ((object = map.get("vanished")) instanceof Boolean) {
            this.vanished = (boolean) object;
        }
        if(map.containsKey("chatColor")){
            chatColor = ChatColor.valueOf((String) map.get("chatColor"));
        }
        if(map.containsKey("nickName")){
            nickName = (String) map.get("nickName");
        }
        group = (String) map.getOrDefault("group", "default");
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = (Map<String, Object>) super.serialize();
        map.put("lastSeen", lastSeen);
        map.put("addressHistories", this.addressHistories);
        map.put("notes", this.notes);
        map.put("nameHistories", this.nameHistories);
        map.put("messagingSounds", this.messagingSounds);
        map.put("vanished", this.vanished);
        map.put("group", group);
        if(chatColor != null) map.put("chatColor", chatColor.name());
        if(nickName != null) map.put("nickName", nickName);
        return map;
    }

    public List<NameHistory> getNameHistories() {
        return this.nameHistories;
    }

    public List<String> getNotes() {
        return this.notes;
    }

    public ChatColor getChatColor(){
        return chatColor;
    }

    public void setChatColor(ChatColor chatColor){
        if(!Objects.equals(this.chatColor, chatColor)){
            this.chatColor = chatColor;
            update();
        }
    }

    public String getNickName(){
        return nickName;
    }

    public void setNickName(String nickName){
        if(!Objects.equals(this.nickName, nickName)){
            this.nickName = nickName;
            update();
        }
    }

    public void setNote(final String note) {
        this.notes.add(note);
        update();
    }

    public boolean tryRemoveNote() {
        this.notes.clear();
        update();
        return true;
    }

    public List<String> getAddressHistories() {
        return this.addressHistories;
    }


    public boolean tryLoggingAddress(final String address) {
        Preconditions.checkNotNull((Object) address, (Object) "Cannot log null address");
        if (!this.addressHistories.contains(address)) {
            Preconditions.checkArgument(InetAddresses.isInetAddress(address), (Object) "Not an Inet address");
            this.addressHistories.add(address);
            update();
            return true;
        }
        return false;
    }

    public boolean isMessagingSounds() {
        return this.messagingSounds;
    }

    public void setMessagingSounds(final boolean messagingSounds) {
        this.messagingSounds = messagingSounds;
        update();
    }

    public boolean isVanished() {
        return this.vanished;
    }

    public void setVanished(final boolean vanished) {
        this.setVanished(vanished, true);
    }

    public void setVanished() {
        this.setVanished(!this.isVanished(), true);
    }

    public void setVanished(final boolean vanished, final boolean update) {
        this.setVanished(Bukkit.getPlayer(this.getUniqueId()), vanished, update);
    }

    public boolean setVanished(final Player player, final boolean vanished, final boolean notifyPlayerList) {
        if(BasePlugin.PRACTICE){
            return false;
        }
        if (this.vanished != vanished) {
            if (player != null) {
                final PlayerVanishEvent event = new PlayerVanishEvent(player, notifyPlayerList ? new HashSet<>(Bukkit.getOnlinePlayers()) : Collections.emptySet(), vanished);
                Bukkit.getPluginManager().callEvent((Event) event);
                if (event.isCancelled()) {
                    return false;
                }
                if (notifyPlayerList) {
                    this.updateVanishedState(player, event.getViewers(), vanished);
                }
            }
            this.vanished = vanished;
            return true;
        }
        return false;
    }

    public void updateVanishedState(Player player, boolean vanished) {
        this.updateVanishedState(player, new ArrayList<>(Bukkit.getOnlinePlayers()), vanished);
    }

    public void updateVanishedState(Player player, Collection<Player> viewers, boolean vanished) {
        if(BasePlugin.PRACTICE){
            return;
        }
        player.spigot().setCollidesWithEntities(!vanished);
        for (Player target : viewers) {
            if (player.equals(target)) {
                continue;
            }
            BaseUser targetUser = BasePlugin.getPlugin().getUserManager().getUser(target.getUniqueId());
            if (targetUser.isVanished()) {
                if (!vanished) {
                    player.hidePlayer(target);
                } else {
                    player.showPlayer(target);
                }
            } else if (vanished) {
                target.hidePlayer(player);
            } else{
                target.showPlayer(player);
            }
        }
    }

    public String getLastKnownName() {
        return (Iterables.getLast(this.nameHistories)).getName();
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        if(!Objects.equals(this.lastSeen, lastSeen)) {
            this.lastSeen = lastSeen;
            update();
        }
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        if (!Objects.equals(this.group, group)) {
            this.group = group;
            update();
        }
    }

    public void setName(String name) {
        if(!Objects.equals(getName(), name)) {
            boolean log = true;
            for (NameHistory nameHistory : this.nameHistories) {
                if (nameHistory.getName().equals(name)) {
                    log = false;
                    break;
                }
            }
            if(log) {
                nameHistories.add(new NameHistory(name, System.currentTimeMillis()));
            }
            update();
        }
        super.setName(name);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        if(!Objects.equals(this.online, online)) {
            this.online = online;
            update();
        }
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        if(!Objects.equals(this.lastServer, lastServer)){
            this.lastServer = lastServer;
            update();
        }
    }

    public Long getFirstJoined() {
        return firstJoined;
    }

    public void setFirstJoined(Long firstJoined) {
        if(!Objects.equals(this.firstJoined, firstJoined)) {
            this.firstJoined = firstJoined;
            update();
        }
    }

    public void update(){
        BasePlugin basePlugin = BasePlugin.getPlugin();
        if (basePlugin == null || basePlugin.getDatastore() == null || basePlugin.getCursorThread() == null) {
            return;
        }
        if(getUniqueId() != null){
            Bukkit.getScheduler().runTaskAsynchronously(basePlugin, () -> {
                basePlugin.getUserManager().save(this);
                basePlugin.getCursorThread().createUpdate(getUniqueId());
            });
        }
    }

    @Override
    public void merge(ServerParticipator self) {
        super.merge(self);
        if(self instanceof BaseUser){
            BaseUser selfBase = (BaseUser) self;
            this.loaded = selfBase.loaded;
            this.addressHistories = selfBase.addressHistories;
            this.nameHistories = selfBase.nameHistories;
            this.notes = selfBase.notes;
            this.firstJoined = selfBase.firstJoined;
            this.messagingSounds = selfBase.messagingSounds;
            selfBase.vanished = this.vanished;
            this.lastSeen = selfBase.lastSeen;
            this.online = selfBase.online;
            this.lastServer = selfBase.lastServer;
            this.group = selfBase.group;
            this.nickName = selfBase.nickName;
            this.chatColor = selfBase.chatColor;
        }
    }


}
