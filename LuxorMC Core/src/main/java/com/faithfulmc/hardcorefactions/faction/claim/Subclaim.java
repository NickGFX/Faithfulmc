package com.faithfulmc.hardcorefactions.faction.claim;

import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.GenericUtils;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Embedded;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Embedded
public class Subclaim extends Claim implements Cloneable, ConfigurationSerializable {
    @Embedded
    private final Set<UUID> accessibleMembers = new HashSet<UUID>();

    public Subclaim() {

    }

    public Subclaim(Map<String, Object> map) {
        super(map);
        this.accessibleMembers.addAll(GenericUtils.createList((Object) map.get("accessibleMembers"), String.class).stream().map(UUID::fromString).collect(Collectors.toList()));
    }

    public Subclaim(Faction faction, Location location) {
        super(faction, location, location);
    }

    public Subclaim(Faction faction, Location location1, Location location2) {
        super(faction, location1, location2);
    }

    public Subclaim(Faction faction, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(faction, world, x1, y1, z1, x2, y2, z2);
    }

    public Subclaim(Faction faction, Cuboid cuboid) {
        this(faction, cuboid.getWorld(), cuboid.getX1(), cuboid.getY1(), cuboid.getZ1(), cuboid.getX2(), cuboid.getY2(), cuboid.getZ2());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.remove("subclaims");
        map.put("accessibleMembers", this.accessibleMembers.stream().map(UUID::toString).collect(Collectors.toList()));
        return map;
    }

    public Set<UUID> getAccessibleMembers() {
        return this.accessibleMembers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subclaim)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Subclaim blocks = (Subclaim) o;
        if (this.accessibleMembers != null ? !this.accessibleMembers.equals(blocks.accessibleMembers) : blocks.accessibleMembers != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.accessibleMembers != null ? this.accessibleMembers.hashCode() : 0);
        return result;
    }

    @Override
    public Subclaim clone() {
        return (Subclaim) super.clone();
    }
}