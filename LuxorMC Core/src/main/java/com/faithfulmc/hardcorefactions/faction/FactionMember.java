package com.faithfulmc.hardcorefactions.faction;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.google.common.collect.Maps;
import net.minecraft.util.com.google.common.base.Enums;
import net.minecraft.util.com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.util.Map;
import java.util.UUID;

@Entity
public class FactionMember implements ConfigurationSerializable {
    @Id
    private UUID uniqueID;
    private String name;
    private ChatChannel chatChannel;
    private Role role;
    @Reference
    private FactionUser factionUser;

    public FactionMember() {
    }

    public FactionMember(Player player, ChatChannel chatChannel, Role role) {
        getFactionUser();
        this.uniqueID = player.getUniqueId();
        this.chatChannel = chatChannel;
        this.role = role;
        this.name = player.getName();
    }

    public FactionMember(String name, UUID player, ChatChannel chatChannel, Role role) {
        this.uniqueID = player;
        this.chatChannel = chatChannel;
        this.role = role;
    }

    public FactionMember(Faction faction, Player player, ChatChannel chatChannel, Role role) {
        this.uniqueID = player.getUniqueId();
        this.chatChannel = chatChannel;
        this.role = role;
        this.name = player.getName();
    }

    public FactionMember(Map<String, Object> map) {
        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.chatChannel = (Enums.getIfPresent(ChatChannel.class, (String) map.get("chatChannel")).or(ChatChannel.PUBLIC));
        this.role = (Enums.getIfPresent(Role.class, (String) map.get("role")).or(Role.MEMBER));
        this.name = (String) map.get("name");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("uniqueID", this.uniqueID.toString());
        map.put("chatChannel", this.chatChannel.name());
        map.put("role", this.role.name());
        map.put("name", name);
        return map;
    }

    public String getName() {
        if(this.name == null){
            this.name = getFactionUser().getName();
        }
        return name;
    }

    public UUID getUniqueId() {
        return this.uniqueID;
    }

    public ChatChannel getChatChannel() {
        return this.chatChannel;
    }

    public void setChatChannel(ChatChannel chatChannel) {
        Preconditions.checkNotNull(chatChannel, "ChatChannel cannot be null");
        this.chatChannel = chatChannel;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFactionUser(FactionUser factionUser) {
        this.factionUser = factionUser;
    }

    public FactionUser getFactionUser(){
        if(factionUser != null){
            return factionUser;
        }
        else{
            factionUser = HCF.getInstance().getUserManager().getUser(uniqueID);
            return factionUser;
        }
    }

    public Player toOnlinePlayer() {
        return Bukkit.getPlayer(this.uniqueID);
    }
}
