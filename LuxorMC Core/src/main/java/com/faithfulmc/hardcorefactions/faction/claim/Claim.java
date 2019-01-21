package com.faithfulmc.hardcorefactions.faction.claim;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.GenericUtils;
import com.faithfulmc.util.cuboid.Cuboid;
import com.faithfulmc.util.cuboid.NamedCuboid;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

import java.util.*;

@Embedded
public class Claim extends NamedCuboid implements Cloneable, ConfigurationSerializable {
    @Transient
    private CaseInsensitiveMap<String, Subclaim> subclaims = new CaseInsensitiveMap<>();
    @Embedded
    private Map<String, Subclaim> subclaims_storage = null;
    private UUID claimUniqueID;
    private UUID factionUUID;
    @Transient
    private Faction faction = null;
    @Transient
    private boolean loaded = false;

    @PrePersist
    public void PrePersistMethod(){
        if(!subclaims.isEmpty()) {
            subclaims_storage = new HashMap<>(subclaims);
        }
        else{
            subclaims_storage = null;
        }
    }

    @PostLoad
    public void postloadMethod(){
        if(subclaims_storage != null) {
            subclaims = new CaseInsensitiveMap<>(subclaims_storage);
        }
    }

    public Claim() {

    }

    public Claim(final Map map) {
        super(map);
        this.subclaims = new CaseInsensitiveMap<String, Subclaim>();
        this.loaded = false;
        this.name = (String) map.get("name");
        this.claimUniqueID = UUID.fromString((String) map.get("claimUUID"));
        this.factionUUID = UUID.fromString((String) map.get("factionUUID"));
        for (final Subclaim subclaim : GenericUtils.createList(map.get("subclaims"), Subclaim.class)) {
            this.subclaims.put(subclaim.getName(), subclaim);
        }
    }

    public Claim(final Faction faction, final Location location) {
        super(location, location);
        this.subclaims = new CaseInsensitiveMap<String, Subclaim>();
        this.loaded = false;
        this.name = "";
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(final Faction faction, final Location location1, final Location location2) {
        super(location1, location2);
        this.subclaims = new CaseInsensitiveMap<String, Subclaim>();
        this.loaded = false;
        this.name = "";
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(final Faction faction, final World world, final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        super(world, x1, y1, z1, x2, y2, z2);
        this.subclaims = new CaseInsensitiveMap<String, Subclaim>();
        this.loaded = false;
        this.name = "";
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(final Faction faction, final Cuboid cuboid) {
        super(cuboid);
        this.subclaims = new CaseInsensitiveMap<String, Subclaim>();
        this.loaded = false;
        this.name = "";
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Map<String, Object> serialize() {
        final Map<String, Object> map = (Map<String, Object>) super.serialize();
        map.put("name", this.name);
        map.put("claimUUID", this.claimUniqueID.toString());
        map.put("factionUUID", this.factionUUID.toString());
        map.put("subclaims", new ArrayList<>(this.subclaims.values()));
        return map;
    }

    public UUID getClaimUniqueID() {
        return this.claimUniqueID;
    }

    public ClaimableFaction getFaction() {
        if (!this.loaded && this.faction == null && this.factionUUID != null) {
            this.faction = HCF.getInstance().getFactionManager().getFaction(this.factionUUID);
            this.loaded = true;
        }
        return (this.faction instanceof ClaimableFaction) ? ((ClaimableFaction) this.faction) : null;
    }

    public Collection<Subclaim> getSubclaims() {
        return this.subclaims.values();
    }

    public Subclaim getSubclaim(final String name) {
        return this.subclaims.get(name);
    }

    public String getFormattedName() {
        return this.getName() + ": (" + this.worldName + ", " + this.x1 + ", " + this.y1 + ", " + this.z1 + ") - (" + this.worldName + ", " + this.x2 + ", " + this.y2 + ", " + this.z2 + ')';
    }

    public Claim clone() {
        return (Claim) super.clone();
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Claim blocks = (Claim) o;
        if (this.loaded != blocks.loaded) {
            return false;
        }
        Label_0080:
        {
            if (this.subclaims != null) {
                if (this.subclaims.equals(blocks.subclaims)) {
                    break Label_0080;
                }
            } else if (blocks.subclaims == null) {
                break Label_0080;
            }
            return false;
        }
        Label_0116:
        {
            if (this.claimUniqueID != null) {
                if (this.claimUniqueID.equals(blocks.claimUniqueID)) {
                    break Label_0116;
                }
            } else if (blocks.claimUniqueID == null) {
                break Label_0116;
            }
            return false;
        }
        Label_0152:
        {
            if (this.factionUUID != null) {
                if (this.factionUUID.equals(blocks.factionUUID)) {
                    break Label_0152;
                }
            } else if (blocks.factionUUID == null) {
                break Label_0152;
            }
            return false;
        }
        if (this.faction != null) {
            if (!this.faction.equals(blocks.faction)) {
                return false;
            }
        } else if (blocks.faction != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = (this.subclaims != null) ? this.subclaims.hashCode() : 0;
        result = 31 * result + ((this.claimUniqueID != null) ? this.claimUniqueID.hashCode() : 0);
        result = 31 * result + ((this.factionUUID != null) ? this.factionUUID.hashCode() : 0);
        result = 31 * result + ((this.faction != null) ? this.faction.hashCode() : 0);
        result = 31 * result + (this.loaded ? 1 : 0);
        return result;
    }
}