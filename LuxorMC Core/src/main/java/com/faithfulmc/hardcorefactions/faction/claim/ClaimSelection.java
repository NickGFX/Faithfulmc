package com.faithfulmc.hardcorefactions.faction.claim;


import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;


public class ClaimSelection implements Cloneable {
    private final UUID uuid;
    private final World world;
    private long lastUpdateMillis;
    private Location pos1;
    private Location pos2;


    public ClaimSelection(World world) {

        this.uuid = UUID.randomUUID();

        this.world = world;

    }


    public ClaimSelection(World world, Location pos1, Location pos2) {

        this.uuid = UUID.randomUUID();

        this.world = world;

        this.pos1 = pos1;

        this.pos2 = pos2;

    }


    public UUID getUuid() {

        return this.uuid;

    }


    public World getWorld() {

        return this.world;

    }


    public int getPrice(PlayerFaction playerFaction, boolean selling) {

        Preconditions.checkNotNull(playerFaction, "Player faction cannot be null");

        return (this.pos1 == null) || (this.pos2 == null) ? 0 : HCF.getInstance().getClaimHandler().calculatePrice(new com.faithfulmc.util.cuboid.Cuboid(this.pos1, this.pos2), playerFaction.getClaims().size(), selling);

    }


    public Claim toClaim(Faction faction) {

        Preconditions.checkNotNull(faction, "Faction cannot be null");

        return (this.pos1 == null) || (this.pos2 == null) ? null : new Claim(faction, this.pos1, this.pos2);

    }


    public long getLastUpdateMillis() {

        return this.lastUpdateMillis;

    }


    public Location getPos1() {

        return this.pos1;

    }


    public void setPos1(Location location) {

        Preconditions.checkNotNull(location, "The location cannot be null");

        this.pos1 = location;

        this.lastUpdateMillis = System.currentTimeMillis();

    }


    public Location getPos2() {

        return this.pos2;

    }


    public void setPos2(Location location) {

        Preconditions.checkNotNull(location, "The location is null");

        this.pos2 = location;

        this.lastUpdateMillis = System.currentTimeMillis();

    }


    public boolean hasBothPositionsSet() {

        return (this.pos1 != null) && (this.pos2 != null);

    }


    public boolean equals(Object o) {

        if (this == o) {

            return true;

        }

        if (!(o instanceof ClaimSelection)) {

            return false;

        }

        ClaimSelection that = (ClaimSelection) o;

        if (this.uuid != null ?
        !this.uuid.equals(that.uuid):
           that.uuid != null)
        {

            return false;

        }

        if (this.world != null ? !this.world.equals(that.world):
           that.world != null)
        {

            return false;

        }

        if (this.pos1 != null ? !this.pos1.equals(that.pos1): that.pos1 != null)
        {

            return false;

        }

        if (this.pos2 != null) {

            if (!this.pos2.equals(that.pos2)) {

                return false;

            }

        }

        else if (that.pos2 != null) {

            return false;

        }

        return true;

    }


    public int hashCode() {

        int result = this.uuid != null ? this.uuid.hashCode() : 0;

        result = 31 * result + (this.world != null ? this.world.hashCode() : 0);

        result = 31 * result + (this.pos1 != null ? this.pos1.hashCode() : 0);
                result = 31 * result + (this.pos2 != null ? this.pos2.hashCode() : 0);

        return result;

    }


    public ClaimSelection clone() {

        try {

            return (ClaimSelection) super.clone();

        } catch (CloneNotSupportedException ex) {

            throw new RuntimeException(ex);

        }

    }

}