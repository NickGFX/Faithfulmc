package com.faithfulmc.hardcorefactions.events.faction;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.google.common.collect.Maps;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;
import java.util.UUID;

@Embedded
public class CitadelCapture implements ConfigurationSerializable{
    private long lastCapture = 0;
    private UUID factionUUID;
    @Transient
    private Faction faction;
    @Transient
    private boolean loaded = false;

    public CitadelCapture(){

    }

    public CitadelCapture(Faction capture, long time){
        lastCapture = time;
        factionUUID = capture.getUniqueID();
        faction = capture;
        loaded = true;
    }

    public CitadelCapture(Map map){
        lastCapture = (long) map.get("lastCapture");
        factionUUID = UUID.fromString((String) map.get("factionUUID"));
    }

    public Map<String, Object> serialize(){
        Map<String, Object> map = Maps.newHashMap();
        map.put("lastCapture", lastCapture);
        map.put("factionUUID", factionUUID);
        return map;
    }

    public Faction getFaction(){
        if(!loaded){
            faction = HCF.getInstance().getFactionManager().getFaction(factionUUID);
            loaded = true;
        }
        return faction;
    }

    public long getLastCapture() {
        return lastCapture;
    }

    public void setLastCapture(long lastCapture) {
        this.lastCapture = lastCapture;
    }

    public UUID getFactionUUID() {
        return factionUUID;
    }

    public void setFactionUUID(UUID factionUUID) {
        this.factionUUID = factionUUID;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public long getCaptureTime(){
        return (long) (1000 * 60 * 60 * 24 * ConfigurationService.CITADEL_CAPTURE_DAYS);
    }

    public long getCaptureEnd(){
        return lastCapture + getCaptureTime();
    }

    public long getRemainingTime(){
        return getRemainingTime(System.currentTimeMillis());
    }

    public long getRemainingTime(long now){
        return getCaptureEnd() - now;
    }

    public boolean hasControl(long now){
        return getRemainingTime(now) > 0;
    }

    public boolean hasControl(){
        return hasControl(System.currentTimeMillis());
    }
}
