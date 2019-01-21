package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.event.FactionClaimChangeEvent;
import com.faithfulmc.hardcorefactions.faction.event.FactionClaimChangedEvent;
import com.faithfulmc.hardcorefactions.faction.event.cause.ClaimChangeCause;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.GenericUtils;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.*;

@Entity(value = "faction")
public class ClaimableFaction extends Faction {
    public static final ImmutableMap<World.Environment, String> ENVIRONMENT_MAPPINGS = ImmutableMap.of(World.Environment.NETHER, "Nether", World.Environment.NORMAL, "Overworld", World.Environment.THE_END, "The End");

    @Embedded
    protected List<Claim> claims = new ArrayList<>();

    public ClaimableFaction() {
    }

    public ClaimableFaction(String name) {
        super(name);
    }

    public ClaimableFaction(Map<String, Object> map) {
        super(map);
        claims.addAll(GenericUtils.createList(map.get("claims"), Claim.class));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("claims", new ArrayList<>(this.claims));
        return map;
    }

    public void printDetails(CommandSender sender) {
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(' ' + getDisplayName(sender));
        for (Claim claim : this.claims) {
            Location location = claim.getCenter();
            if (location != null && location.getWorld() != null && ENVIRONMENT_MAPPINGS.containsKey(location.getWorld().getEnvironment())) {
                sender.sendMessage(ConfigurationService.YELLOW + "  Location: " + ConfigurationService.GRAY.toString() + (String) ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ());
            }
        }
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public Collection<Claim> getClaims() {
        return this.claims;
    }

    public boolean addClaim(Claim claim, CommandSender sender) {
        return addClaims(Collections.singleton(claim), sender);
    }

    public boolean addClaims(Collection<Claim> adding, CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, ClaimChangeCause.CLAIM, adding, this);
        Bukkit.getPluginManager().callEvent(event);
        if ((event.isCancelled()) || (!this.claims.addAll(adding))) {
            return false;
        }
        Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.CLAIM, adding));
        return true;
    }

    public boolean removeClaim(Claim claim, CommandSender sender) {
        return removeClaims(Collections.singleton(claim), sender);
    }

    public void forceRemoveClaims(Collection<Claim> removing, CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        int previousClaims = this.claims.size();
        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, ClaimChangeCause.UNCLAIM, removing, this);
        Bukkit.getPluginManager().callEvent(event);
        claims.removeAll(removing);
        if ((this instanceof PlayerFaction)) {
            PlayerFaction playerFaction = (PlayerFaction) this;
            Location home = playerFaction.getHome();
            HCF plugin = HCF.getInstance();
            int refund = 0;
            for (Claim claim : removing) {
                refund += plugin.getClaimHandler().calculatePrice(claim, previousClaims, true);
                if (previousClaims > 0) {
                    previousClaims--;
                }
                if ((home != null) && (claim.contains(home))) {
                    playerFaction.setHome(null);
                    playerFaction.broadcast(ConfigurationService.RED.toString() + ChatColor.BOLD + "Your factions' home was unset as its residing claim was removed.");
                    break;
                }
            }
            FactionUser leader = playerFaction.getLeader().getFactionUser();
            leader.setBalance(leader.getBalance() + refund);
            playerFaction.broadcast(ConfigurationService.YELLOW + "Faction leader was refunded " + ChatColor.GREEN + '$' + refund + ConfigurationService.YELLOW + " due to a land unclaim.");
        }
        Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.UNCLAIM, removing));
    }

    public boolean removeClaims(Collection<Claim> removing, CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        int previousClaims = this.claims.size();
        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, ClaimChangeCause.UNCLAIM, removing, this);
        Bukkit.getPluginManager().callEvent(event);
        if ((event.isCancelled()) || (!this.claims.removeAll(removing))) {
            return false;
        }
        if ((this instanceof PlayerFaction)) {
            PlayerFaction playerFaction = (PlayerFaction) this;
            Location home = playerFaction.getHome();
            HCF plugin = HCF.getInstance();
            int refund = 0;
            for (Claim claim : removing) {
                refund += plugin.getClaimHandler().calculatePrice(claim, previousClaims, true);
                if (previousClaims > 0) {
                    previousClaims--;
                }
                if ((home != null) && (claim.contains(home))) {
                    playerFaction.setHome(null);
                    playerFaction.broadcast(ConfigurationService.RED.toString() + ChatColor.BOLD + "Your factions' home was unset as its residing claim was removed.");
                    break;
                }
            }
            FactionUser leader = playerFaction.getLeader().getFactionUser();
            leader.setBalance(leader.getBalance() + refund);
            playerFaction.broadcast(ConfigurationService.YELLOW + "Faction leader was refunded " + ChatColor.GREEN + '$' + refund + ConfigurationService.YELLOW + " due to a land unclaim.");
        }
        Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.UNCLAIM, removing));
        return true;
    }
}
