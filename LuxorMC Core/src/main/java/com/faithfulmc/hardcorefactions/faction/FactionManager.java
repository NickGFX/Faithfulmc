package com.faithfulmc.hardcorefactions.faction;

import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.event.cause.ClaimChangeCause;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface FactionManager extends Listener {
    long MAX_DTR_REGEN_MILLIS = TimeUnit.HOURS.toMillis(3L);
    String MAX_DTR_REGEN_WORDS = DurationFormatUtils.formatDurationWords(MAX_DTR_REGEN_MILLIS, true, true);
    Map<String, ?> getFactionNameMap();
    Collection<Faction> getFactions();
    Claim getClaimAt(Location paramLocation);
    Claim getClaimAt(World paramWorld, int paramInt1, int paramInt2);
    Faction getFactionAt(Location paramLocation);
    Faction getFactionAt(Block paramBlock);
    Faction getFactionAt(World paramWorld, int paramInt1, int paramInt2);
    Faction getFaction(String paramString);
    Faction getFaction(UUID paramUUID);
    PlayerFaction getContainingPlayerFaction(String paramString);
    PlayerFaction getPlayerFaction(Player paramPlayer);
    PlayerFaction getPlayerFaction(UUID paramUUID);
    Faction getContainingFaction(String paramString);
    boolean containsFaction(Faction paramFaction);
    boolean createFaction(Faction paramFaction);
    boolean createFaction(Faction paramFaction, CommandSender paramCommandSender);
    boolean removeFaction(Faction paramFaction, CommandSender paramCommandSender);
    void reloadFactionData();
    void saveFactionData();
    void updateFaction(Faction faction);
    void cacheClaim(Claim claim, ClaimChangeCause cause);
}
