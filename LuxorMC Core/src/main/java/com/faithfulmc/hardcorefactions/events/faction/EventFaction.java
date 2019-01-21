package com.faithfulmc.hardcorefactions.events.faction;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.mongodb.morphia.annotations.Entity;


@Entity(value = "faction")
public abstract class EventFaction extends ClaimableFaction {

    public EventFaction(){

    }

    public EventFaction(String name) {

        super(name);

        setDeathban(true);

    }


    public EventFaction(java.util.Map<String, Object> map) {

        super(map);

        setDeathban(true);

    }


    public String getDisplayName(Faction faction) {
        return getDisplayName((CommandSender) null);
    }


    public String getDisplayName(CommandSender sender) {
        if (getName().equalsIgnoreCase("eotw")) {
            return ChatColor.DARK_RED + getName();
        } else if(getName().equalsIgnoreCase("hell")){
            return ChatColor.RED + getName();
        } else if(getName().equalsIgnoreCase("palace")){
            return ChatColor.DARK_AQUA + getName();
        } else if (getEventType() == EventType.KOTH) {
            return ChatColor.LIGHT_PURPLE.toString() + getName() + ' ' + getEventType().getDisplayName();
        } else if (getEventType() == EventType.CONQUEST) {
            return ConfigurationService.GOLD.toString() + getEventType().getDisplayName();
        } else if(getEventType() == EventType.CITADEL){
            return ChatColor.DARK_AQUA.toString() + getEventType().getDisplayName();
        }
        return ChatColor.DARK_PURPLE + getEventType().getDisplayName();

    }


    public void setClaim(Cuboid cuboid, CommandSender sender) {

        removeClaims(getClaims(), sender);

        Location min = cuboid.getMinimumPoint();

        min.setY(0);

        Location max = cuboid.getMaximumPoint();
                max.setY(256);

        addClaim(new Claim(this, min, max), sender);

    }


    public abstract EventType getEventType();


    public abstract java.util.List<CaptureZone> getCaptureZones();

}