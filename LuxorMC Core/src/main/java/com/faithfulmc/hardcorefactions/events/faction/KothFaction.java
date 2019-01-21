package com.faithfulmc.hardcorefactions.events.faction;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.tracker.KothTracker;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;
import java.util.Map;


@Entity(value = "faction")
public class KothFaction extends CapturableFaction implements ConfigurationSerializable {
    protected CaptureZone captureZone;

    public KothFaction(){

    }


    public KothFaction(String name) {

        super(name);

        setDeathban(true);

    }


    public KothFaction(Map<String, Object> map) {

        super(map);

        setDeathban(true);

        this.captureZone = ((CaptureZone) map.get("captureZone"));
        if (ConfigurationService.KIT_MAP) {
            captureZone.setDefaultCaptureMillis(KothTracker.DEFAULT_CAP_MILLIS);
        }

    }


    public Map<String, Object> serialize() {

        Map<String, Object> map = super.serialize();

        map.put("captureZone", this.captureZone);

        return map;

    }


    public List<CaptureZone> getCaptureZones() {

        return this.captureZone == null ? ImmutableList.of() : ImmutableList.of(this.captureZone);

    }


    public EventType getEventType() {

        return EventType.KOTH;

    }


    public void printDetails(CommandSender sender) {

        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);

        sender.sendMessage(getDisplayName(sender));

        for (Claim claim : this.claims) {

            Location location = claim.getCenter();

            sender.sendMessage(ConfigurationService.YELLOW + "  Location: " + ConfigurationService.RED + '(' + (String) ClaimableFaction.ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ() + ')');

        }

        if (this.captureZone != null) {

            long remainingCaptureMillis = this.captureZone.getRemainingCaptureMillis();

            long defaultCaptureMillis = this.captureZone.getDefaultCaptureMillis();

            if ((remainingCaptureMillis > 0L) && (remainingCaptureMillis != defaultCaptureMillis)) {

                sender.sendMessage(ConfigurationService.YELLOW + "  Remaining Time: " + ConfigurationService.RED + DurationFormatUtils.formatDurationWords(remainingCaptureMillis, true, true));

            }

            sender.sendMessage(ConfigurationService.YELLOW + "  Capture Delay: " + ConfigurationService.RED + this.captureZone.getDefaultCaptureWords());

            if ((this.captureZone.getCappingPlayer() != null) && (sender.hasPermission("hcf.koth.checkcapper"))) {

                Player capping = this.captureZone.getCappingPlayer();

                PlayerFaction playerFaction = HCF.getInstance().getFactionManager().getPlayerFaction(capping);
                sender.sendMessage(ConfigurationService.YELLOW + "  Current Capper: " + ConfigurationService.GOLD + (capping == null ? "None" : capping.getName() + (playerFaction != null ? ConfigurationService.GRAY + " [" + playerFaction.getName() + "]" : "")));

            }

        }

        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);

    }

    public String getPrefix(){
        return KothTracker.PREFIX;
    }


    public CaptureZone getCaptureZone() {

        return this.captureZone;

    }


    public void setCaptureZone(CaptureZone captureZone) {

        this.captureZone = captureZone;
        if (getName().equalsIgnoreCase("eotw")) {
            captureZone.setDefaultCaptureMillis(30 * 60 * 1000);
        }

    }

}