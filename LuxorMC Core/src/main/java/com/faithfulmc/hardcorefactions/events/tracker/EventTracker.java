package com.faithfulmc.hardcorefactions.events.tracker;

import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import org.bukkit.entity.Player;

public interface EventTracker {
    EventType getEventType();
    void tick(EventTimer paramEventTimer, EventFaction paramEventFaction);
    void onContest(EventFaction paramEventFaction, EventTimer paramEventTimer);
    boolean onControlTake(Player paramPlayer, CaptureZone paramCaptureZone);
    boolean onControlLoss(Player paramPlayer, CaptureZone paramCaptureZone, EventFaction paramEventFaction);
    void stopTiming();
}