package com.faithfulmc.hardcorefactions.events.faction;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.*;

import java.util.*;


@Entity(value = "faction")

public class ConquestFaction extends CapturableFaction implements ConfigurationSerializable {
    @Transient
    private EnumMap<ConquestZone, CaptureZone> captureZones = new EnumMap<>(ConquestZone.class);;
    @Embedded
    private Map<Object, CaptureZone> captureZones_storage = null;

    @PrePersist
    public void PrePersistMethod(){
        captureZones_storage = new HashMap<>();
        captureZones.entrySet().forEach(entry -> captureZones_storage.put(entry.getKey().ordinal(), entry.getValue()));
    }

    @PostLoad
    public void postloadMethod(){
        if(captureZones_storage != null) {
            captureZones_storage.entrySet().forEach(entry -> {
                int key;
                if (entry.getKey() instanceof Number) {
                    key = ((Number) entry.getKey()).intValue();
                } else if (entry.getKey() instanceof String) {
                    key = Integer.parseInt((String) entry.getKey());
                } else {
                    return;
                }
                captureZones.put(ConquestZone.values()[key], entry.getValue());
            });
        }
    }

    public ConquestFaction(){
    }


    public ConquestFaction(String name) {

        super(name);

        setDeathban(true);

        this.captureZones = new EnumMap<>(ConquestZone.class);

    }


    public ConquestFaction(Map<String, Object> map) {

        super(map);

        setDeathban(true);

        this.captureZones = new EnumMap<>(ConquestZone.class);

        Object object;

        if (((object = map.get("red")) instanceof CaptureZone)) {

            this.captureZones.put(ConquestZone.RED, (CaptureZone) object);

        }

        if (((object = map.get("green")) instanceof CaptureZone)) {

            this.captureZones.put(ConquestZone.GREEN, (CaptureZone) object);

        }

        if (((object = map.get("blue")) instanceof CaptureZone)) {

            this.captureZones.put(ConquestZone.BLUE, (CaptureZone) object);

        }

        if (((object = map.get("yellow")) instanceof CaptureZone)) {

            this.captureZones.put(ConquestZone.YELLOW, (CaptureZone) object);

        }

        if (((object = map.get("main")) instanceof CaptureZone)) {

            this.captureZones.put(ConquestZone.MAIN, (CaptureZone) object);

        }


    }


    public Map<String, Object> serialize() {

        Map<String, Object> map = super.serialize();

        for (Map.Entry<ConquestZone, CaptureZone> entry : this.captureZones.entrySet()) {

            map.put(((ConquestZone) entry.getKey()).name().toLowerCase(), entry.getValue());

        }

        return map;

    }


    public EventType getEventType() {

        return EventType.CONQUEST;

    }

    public ConquestZone getZone(CaptureZone captureZone) {
        for (Map.Entry<ConquestZone, CaptureZone> captureZoneEntry : captureZones.entrySet()) {
            if (captureZoneEntry.getValue() == captureZone) {
                return captureZoneEntry.getKey();
            }
        }
        return null;
    }


    public void printDetails(CommandSender sender) {

        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);

        sender.sendMessage(getDisplayName(sender));

        for (Claim claim : this.claims) {

            Location location = claim.getCenter();

            sender.sendMessage(ConfigurationService.YELLOW + "  Location: " + ConfigurationService.RED + '(' + (String) ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ() + ')');

        }

        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);

    }


    public void setZone(ConquestZone conquestZone, CaptureZone captureZone) {

        switch (conquestZone) {
            case MAIN:
                this.captureZones.put(ConquestZone.MAIN, captureZone);

                break;

            case RED:

                this.captureZones.put(ConquestZone.RED, captureZone);

                break;

            case BLUE:

                this.captureZones.put(ConquestZone.BLUE, captureZone);

                break;

            case GREEN:

                this.captureZones.put(ConquestZone.GREEN, captureZone);

                break;

            case YELLOW:

                this.captureZones.put(ConquestZone.YELLOW, captureZone);

                break;

            default:
                        throw new AssertionError("Unsupported operation");

        }

    }


    public CaptureZone getRed() {

        return (CaptureZone) this.captureZones.get(ConquestZone.RED);

    }


    public CaptureZone getGreen() {

        return (CaptureZone) this.captureZones.get(ConquestZone.GREEN);

    }


    public CaptureZone getBlue() {

        return (CaptureZone) this.captureZones.get(ConquestZone.BLUE);

    }


    public CaptureZone getYellow() {
                return (CaptureZone) this.captureZones.get(ConquestZone.YELLOW);

    }

    public CaptureZone getMain() {
        return captureZones.get(ConquestZone.MAIN);
    }


    public Collection<ConquestZone> getConquestZones() {
                return ImmutableSet.copyOf(this.captureZones.keySet());

    }


    public List<CaptureZone> getCaptureZones() {

        return ImmutableList.copyOf(this.captureZones.values());

    }


    @Embedded
    public static enum ConquestZone{
        MAIN(ChatColor.LIGHT_PURPLE,"Main"),
        RED(ChatColor.RED,"Red"),
        BLUE(ChatColor.BLUE,"Blue"),
        YELLOW(ChatColor.YELLOW,"Yellow"),
        GREEN(ChatColor.GREEN,"Green");

    private static final Map<String, ConquestZone> BY_NAME;

                public static ConquestZone getByName(String name) {

            return (ConquestZone) BY_NAME.get(name.toUpperCase());

        }

        public static Collection<String> getNames() {

            return new ArrayList(BY_NAME.keySet());

        }

        static {

            ImmutableMap.Builder<String, ConquestZone> builder = ImmutableMap.builder();

            for (ConquestZone zone : values()) {

                builder.put(zone.name().toUpperCase(), zone);

            }

            BY_NAME = builder.build();

        }

        private final String name;
        private final ChatColor color;


        private ConquestZone(ChatColor color, String name) {

            this.color = color;

            this.name = name;

        }


        public ChatColor getColor() {

            return this.color;

        }


        public String getName() {

            return this.name;

        }


        public String getDisplayName() {

            return this.color.toString() + this.name;

        }

    }

}