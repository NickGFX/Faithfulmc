package com.faithfulmc.hardcorefactions.faction.claim;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionManager;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.*;
import com.faithfulmc.hardcorefactions.visualise.VisualType;
import com.faithfulmc.util.ItemBuilder;
import com.faithfulmc.util.cuboid.Cuboid;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class ClaimHandler {
    public static final int MIN_CLAIM_HEIGHT = 0;
    public static final int MAX_CLAIM_HEIGHT = 256;
    public static final ItemStack CLAIM_WAND = new ItemBuilder(Material.DIAMOND_HOE)
            .displayName(ConfigurationService.GOLD.toString() + "Claim Wand")
            .lore(new String[]{
                    ConfigurationService.YELLOW + "Left or Right Click " + ConfigurationService.GOLD + "a Block" + ConfigurationService.YELLOW + " to:",
                    ConfigurationService.GRAY + "Set the first and second position of ",
                    ConfigurationService.GRAY + "your Claim selection.",
                    "",
                    ConfigurationService.YELLOW + "Right Click " + ConfigurationService.GOLD + "the Air" + ConfigurationService.YELLOW + " to:",
                    ConfigurationService.GRAY + "Clear your current Claim selection.",
                    "",
                    ConfigurationService.YELLOW + "Shift Left Click " + ConfigurationService.GOLD + "the Air or a Block" + ConfigurationService.YELLOW + " to:",
                    ConfigurationService.GRAY + "Purchase your current Claim selection."}
                    ).build();

    public static final int MIN_CLAIM_RADIUS = 5;
    public static final int MAX_CHUNKS_PER_LIMIT = 32;
    public static final int NEXT_PRICE_MULTIPLIER_AREA = 250;
    public static final int NEXT_PRICE_MULTIPLIER_CLAIM = 500;
    public static final double CLAIM_SELL_MULTIPLIER = 0.8;
    public static double CLAIM_PRICE_PER_BLOCK = 0.25;

    public static int NEARBY_CLAIM_RADIUS = 2;
    public static final int MAX_CLAIMS_PER_FACTION = 8;

    public final ConcurrentMap<UUID, ClaimSelection> claimSelectionMap;
    private final HCF plugin;

    public ClaimHandler(HCF plugin) {
        this.plugin = plugin;
        CacheLoader<UUID, ClaimSelection> loader = new CacheLoader<UUID, ClaimSelection>() {
            public ClaimSelection load(UUID uuid) throws Exception {
                return ClaimHandler.this.claimSelectionMap.get(uuid);
            }
        };
        this.claimSelectionMap = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build(loader).asMap();
    }

    public int calculatePrice(Cuboid claim, int currentClaims, boolean selling) {
        if (currentClaims == -1 || !claim.hasBothPositionsSet()) {
            return 0;
        }
        int multiplier = 1;
        int remaining = claim.getArea();
        double price = 0.0;
        while (remaining > 0) {
            if (--remaining % NEXT_PRICE_MULTIPLIER_AREA == 0) {
                ++multiplier;
            }
            price += CLAIM_PRICE_PER_BLOCK * (double) multiplier;
        }
        if (currentClaims != 0) {
            currentClaims = Math.max(currentClaims + (selling ? -1 : 0), 0);
            price += (double) (currentClaims * NEXT_PRICE_MULTIPLIER_CLAIM);
        }
        if (selling) {
            price *= CLAIM_SELL_MULTIPLIER;
        }
        return (int) price;
    }

    public boolean clearClaimSelection(Player player) {
        ClaimSelection claimSelection = this.plugin.getClaimHandler().claimSelectionMap.remove(player.getUniqueId());
        if (claimSelection != null) {
            this.plugin.getVisualiseHandler().clearVisualType(player, VisualType.CREATE_CLAIM_SELECTION, true);
            return true;
        }
        return false;
    }

    public boolean canSubclaimHere(Player player, Location location) {
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            player.sendMessage(ConfigurationService.RED + "You must be in a faction to subclaim land.");
            return false;
        }
        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ConfigurationService.RED + "You must be an officer to claim land.");
            return false;
        }
        if (!this.plugin.getFactionManager().getFactionAt(location).equals(playerFaction)) {
            player.sendMessage(ConfigurationService.RED + "This location is not part of your factions' territory.");
            return false;
        }
        return true;
    }

    public boolean canClaimHere(Player player, Location location) {
        World world = location.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ConfigurationService.RED + "You can only claim land in the Overworld.");
            return false;
        }
        if (!(this.plugin.getFactionManager().getFactionAt(location) instanceof WildernessFaction)) {
            player.sendMessage(ConfigurationService.RED + "You can only claim land in the " + ConfigurationService.WILDERNESS_COLOUR + "Wilderness" + ConfigurationService.RED + ". " + "Make sure you are past " + ConfigurationService.WARZONE_RADIUS+ " blocks from spawn..");
            return false;
        }
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            player.sendMessage(ConfigurationService.RED + "You must be in a faction to claim land.");
            return false;
        }
        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ConfigurationService.RED + "You must be an officer to claim land.");
            return false;
        }
        if (playerFaction.getClaims().size() >= MAX_CLAIMS_PER_FACTION) {
            player.sendMessage(ConfigurationService.RED + "Your faction has maximum claims - " + MAX_CLAIMS_PER_FACTION);
            return false;
        }
        int locX = location.getBlockX();
        int locZ = location.getBlockZ();
        FactionManager factionManager = this.plugin.getFactionManager();
        for (int x = locX - NEARBY_CLAIM_RADIUS; x < locX + NEARBY_CLAIM_RADIUS; ++x) {
            for (int z = locZ - NEARBY_CLAIM_RADIUS; z < locZ + NEARBY_CLAIM_RADIUS; ++z) {
                Faction factionAtNew = factionManager.getFactionAt(world, x, z);
                if (!(factionAtNew instanceof RoadFaction)) {
                    if (playerFaction.equals(factionAtNew) || !(factionAtNew instanceof ClaimableFaction)) {
                        continue;
                    }
                }
                player.sendMessage(ConfigurationService.RED + "This position contains enemy claims within a " + NEARBY_CLAIM_RADIUS + " block buffer radius.");
                return false;
            }
        } return true;
    }

    public boolean tryPurchasing(Player player, Claim claim) {
        int z;
        int x;
        Preconditions.checkNotNull(claim, "Claim is null");
        World world = claim.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ConfigurationService.RED + "You can only claim land in the Overworld.");
            return false;
        }
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            player.sendMessage(ConfigurationService.RED + "You must be in a faction to claim land.");
            return false;
        }
        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ConfigurationService.RED + "You must be an officer to claim land.");
            return false;
        }
        if (playerFaction.getClaims().size() >= MAX_CLAIMS_PER_FACTION) {
            player.sendMessage(ConfigurationService.RED + "Your faction has maximum claims - " + MAX_CLAIMS_PER_FACTION);
            return false;
        }
        int factionBalance = playerFaction.getBalance();
        int claimPrice = this.calculatePrice(claim, playerFaction.getClaims().size(), false);
        if (claimPrice > factionBalance) {
            player.sendMessage(ConfigurationService.RED + "Your faction bank only has " + '$' + factionBalance + ", the price of this claim is " + '$' + claimPrice + '.');
            return false;
        }
        if (claim.getChunks().size() > MAX_CHUNKS_PER_LIMIT) {
            player.sendMessage(ConfigurationService.RED + "Claims cannot exceed " + MAX_CHUNKS_PER_LIMIT + " chunks.");
            return false;
        }
        if (claim.getWidth() < MIN_CLAIM_RADIUS || claim.getLength() < MIN_CLAIM_RADIUS) {
            player.sendMessage(ConfigurationService.RED + "Claims must be at least " + MIN_CLAIM_RADIUS + 'x' + MIN_CLAIM_RADIUS + " blocks.");
            return false;
        }
        int minimumX = claim.getMinimumX();
        int maximumX = claim.getMaximumX();
        int minimumZ = claim.getMinimumZ();
        int maximumZ = claim.getMaximumZ();
        FactionManager factionManager = this.plugin.getFactionManager();
        for (x = minimumX; x < maximumX; ++x) {
            for (z = minimumZ; z < maximumZ; ++z) {
                Faction factionAt = factionManager.getFactionAt(world, x, z);
                if (factionAt == null || factionAt instanceof WildernessFaction) {
                    continue;
                }
                player.sendMessage(ConfigurationService.RED + "This claim contains a location not within the " + ConfigurationService.GRAY + "Wilderness" + ConfigurationService.RED + '.');
                return false;
            }
        }
        for (x = minimumX - NEARBY_CLAIM_RADIUS; x < maximumX + NEARBY_CLAIM_RADIUS; ++x) {
            for (z = minimumZ - NEARBY_CLAIM_RADIUS; z < maximumZ + NEARBY_CLAIM_RADIUS; ++z) {
                Faction factionAtNew = factionManager.getFactionAt(world, x, z);
                if (!(factionAtNew instanceof RoadFaction)) {
                    if (playerFaction.equals(factionAtNew) || !(factionAtNew instanceof ClaimableFaction)) {
                        continue;
                    }
                }
                player.sendMessage(ConfigurationService.RED + "This claim contains enemy claims within a " + NEARBY_CLAIM_RADIUS + " block buffer radius.");
                return false;
            }
        }
        Location minimum = claim.getMinimumPoint();
        Location maximum = claim.getMaximumPoint();
        Collection<Claim> otherClaims = playerFaction.getClaims();
        boolean conjoined = otherClaims.isEmpty();
        if (!conjoined) {
            player.sendMessage(ConfigurationService.RED + "Use /f unclaim to resize your faction claims.");
            return false;
        }
        claim.setY1(MIN_CLAIM_HEIGHT);
        claim.setY2(MAX_CLAIM_HEIGHT);
        if (!playerFaction.addClaim(claim, player)) {
            return false;
        }
        Location center = claim.getCenter();
        player.sendMessage(ConfigurationService.YELLOW + "Claim has been purchased for " + ChatColor.GREEN + '$' + claimPrice + ConfigurationService.YELLOW + '.');
        playerFaction.setBalance(factionBalance - claimPrice);
        playerFaction.broadcast(ConfigurationService.GOLD + player.getName() + ChatColor.GREEN + " claimed land for your faction at " + ConfigurationService.GOLD + '(' + center.getBlockX() + ", " + center.getBlockZ() + ')' + ChatColor.GREEN + '.', player.getUniqueId());
        return true;
    }
}


