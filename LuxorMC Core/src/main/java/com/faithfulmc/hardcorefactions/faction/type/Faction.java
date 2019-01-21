package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionRenameEvent;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.MongoSerializable;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.Map;
import java.util.UUID;

@Entity(value = "faction")
@Indexes({
        @Index(fields = @Field(value = "balance", type = IndexType.ASC)),
        @Index(fields = @Field(value = "total_kills", type = IndexType.ASC)),
        @Index(fields = @Field(value = "online_members", type = IndexType.ASC)),
        @Index(fields = @Field(value = "name", type = IndexType.TEXT)),
})
public abstract class Faction implements ConfigurationSerializable, MongoSerializable {
    public long lastRenameMillis = 0;
    @Id
    protected UUID uniqueID;
    protected String name = null;
    protected long creationMillis = 0;
    protected double dtrLossMultiplier = 1.0D;
    protected double deathbanMultiplier = 1.0D;
    protected boolean safezone = false;
    protected Integer balance = null;
    protected Integer total_kills = null;
    protected Integer online_members = null;
    protected Double deathsUntilRaidable = null;

    public Faction(String name) {
        this.uniqueID = UUID.randomUUID();
        this.name = name;
    }

    public Faction() {
    }

    public Faction(Map map) {
        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.name = ((String) map.get("name"));
        this.creationMillis = Long.parseLong((String) map.get("creationMillis"));
        this.lastRenameMillis = Long.parseLong((String) map.get("lastRenameMillis"));
        this.deathbanMultiplier = (Double) map.get("deathbanMultiplier");
        this.safezone = (Boolean) map.get("safezone");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("class", getClass().getName());
        map.put("uniqueID", this.uniqueID.toString());
        map.put("name", this.name);
        map.put("creationMillis", Long.toString(this.creationMillis));
        map.put("lastRenameMillis", Long.toString(this.lastRenameMillis));
        map.put("deathbanMultiplier", this.deathbanMultiplier);
        map.put("safezone", this.safezone);
        return map;
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public String getName() {
        return this.name;
    }

    public boolean setName(String name) {
        return setName(name, Bukkit.getConsoleSender());
    }

    public boolean setName(String name, CommandSender sender) {
        if (this.name.equals(name)) {
            return false;
        }
        FactionRenameEvent event = new FactionRenameEvent(this, sender, this.name, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        this.lastRenameMillis = System.currentTimeMillis();
        this.name = name;
        return true;
    }

    public Relation getFactionRelation(Faction faction) {
        if (faction == null) {
            return Relation.ENEMY;
        }
        if ((faction instanceof PlayerFaction)) {
            PlayerFaction playerFaction = (PlayerFaction) faction;
            if (playerFaction.equals(this)) {
                return Relation.MEMBER;
            }
            if (playerFaction.getAllied().contains(this.uniqueID)) {
                return Relation.ALLY;
            }
        }
        return Relation.ENEMY;
    }

    public Relation getRelation(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return Relation.ENEMY;
        }
        Player player = (Player) sender;
        return getFactionRelation(HCF.getInstance().getFactionManager().getPlayerFaction(player));
    }

    public Relation getRelation(FactionUser user){
        return getFactionRelation(user.getFaction());
    }

    public String getDisplayName(CommandSender sender) {
        return (this.safezone ? ConfigurationService.SAFEZONE_COLOUR : getRelation(sender).toChatColour()) + this.name;
    }

    public String getDisplayName(Faction other) {
        return getFactionRelation(other).toChatColour() + this.name;
    }

    public void printDetails(CommandSender sender) {
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(' ' + getDisplayName(sender));
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public boolean isDeathban() {
        return (!this.safezone) && (this.deathbanMultiplier > 0.0D);
    }

    public void setDeathban(boolean deathban) {
        if (deathban != isDeathban()) {
            this.deathbanMultiplier = (deathban ? 1.0D : 0.0D);
        }
    }

    public double getDeathbanMultiplier() {
        return this.deathbanMultiplier;
    }

    public void setDeathbanMultiplier(double deathbanMultiplier) {
        Preconditions.checkArgument(deathbanMultiplier >= 0.0D, "Deathban multiplier may not be negative");
        this.deathbanMultiplier = deathbanMultiplier;
    }

    public double getDtrLossMultiplier() {
        return this.dtrLossMultiplier;
    }

    public void setDtrLossMultiplier(double dtrLossMultiplier) {
        this.dtrLossMultiplier = dtrLossMultiplier;
    }

    public boolean isSafezone() {
        return this.safezone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Faction)) {
            return false;
        }
        Faction faction = (Faction) o;
        if (this.creationMillis != faction.creationMillis) {
            return false;
        }
        if (this.lastRenameMillis != faction.lastRenameMillis) {
            return false;
        }
        if (Double.compare(faction.dtrLossMultiplier, this.dtrLossMultiplier) != 0) {
            return false;
        }
        if (Double.compare(faction.deathbanMultiplier, this.deathbanMultiplier) != 0) {
            return false;
        }
        if (this.safezone != faction.safezone) {
            return false;
        }
        if (this.uniqueID != null ? !this.uniqueID.equals(faction.uniqueID) :

                faction.uniqueID != null) {
            return false;
        }
        if (this.name != null) {
            if (!this.name.equals(faction.name)) {
                return false;
            }
        } else if (faction.name != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.uniqueID != null ? this.uniqueID.hashCode() : 0;
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + (int) (this.creationMillis ^ this.creationMillis >>> 32);
        result = 31 * result + (int) (this.lastRenameMillis ^ this.lastRenameMillis >>> 32);
        long temp = Double.doubleToLongBits(this.dtrLossMultiplier);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.deathbanMultiplier);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        result = 31 * result + (this.safezone ? 1 : 0);
        return result;
    }

    public Integer getTotal_kills() {
        return total_kills;
    }

    public void setTotal_kills(Integer total_kills) {
        this.total_kills = total_kills;
    }
}
