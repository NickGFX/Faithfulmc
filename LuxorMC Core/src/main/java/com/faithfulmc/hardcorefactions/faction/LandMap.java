package com.faithfulmc.hardcorefactions.faction;

import com.luxormc.block.BlockPosition;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.visualise.VisualType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;

public class LandMap {
    private static final int FACTION_MAP_RADIUS_BLOCKS = 22;
    private static final Material[] BLACKLISK = {Material.LEAVES, Material.LEAVES_2, Material.FENCE_GATE, Material.WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.STATIONARY_WATER};

    public static boolean updateMap(Player player, HCF plugin, VisualType visualType, boolean inform) {
        Location location = player.getLocation();
        World world = player.getWorld();
        int locationX = location.getBlockX();
        int locationZ = location.getBlockZ();
        int minimumX = locationX - FACTION_MAP_RADIUS_BLOCKS;
        int minimumZ = locationZ - FACTION_MAP_RADIUS_BLOCKS;
        int maximumX = locationX + FACTION_MAP_RADIUS_BLOCKS;
        int maximumZ = locationZ + FACTION_MAP_RADIUS_BLOCKS;
        Set<Claim> board = new LinkedHashSet<>();
        boolean subclaimBased;
        if (visualType == VisualType.SUBCLAIM_MAP) {
            subclaimBased = true;
        } else {
            if (visualType != VisualType.CLAIM_MAP) {
                player.sendMessage(ConfigurationService.RED + "Not supported: " + visualType.name().toLowerCase() + '.');
                return false;
            }
            subclaimBased = false;
        }
        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                Claim claim = plugin.getFactionManager().getClaimAt(world, x, z);
                if (claim != null) {
                    if (subclaimBased) {
                        board.addAll(claim.getSubclaims());
                    } else {
                        board.add(claim);
                    }
                }
            }
        }
        if (board.isEmpty()) {
            player.sendMessage(ConfigurationService.RED + "No claims are in your visual range to display.");
            return false;
        }
        for (Claim claim2 : board) {
            if (claim2 == null) {
                continue;
            }
            int maxHeight = Math.min(world.getMaxHeight(), 256);
            Location[] corners = claim2.getCornerLocations();
            List<BlockPosition> shown = new ArrayList<>(maxHeight * corners.length);
            for (Location corner : corners) {
                for (int y = 0; y < maxHeight; y++) {
                    shown.add(new BlockPosition(corner.getBlockX(), y, corner.getBlockZ()));
                }
            }
            Map<BlockPosition, MaterialData> dataMap = plugin.getVisualiseHandler().addVisualType(player, shown, visualType, true);
            if (!dataMap.isEmpty()) {
                String materialName = BasePlugin.getPlugin().getItemDb().getName(new ItemStack((dataMap.entrySet().iterator().next().getValue()).getItemType(), 1));
                if (inform && claim2.getFaction() != null) {
                    player.sendMessage(ConfigurationService.YELLOW + claim2.getFaction().getDisplayName(player) + ConfigurationService.YELLOW + " owns land " + ConfigurationService.GRAY + " (displayed with " + materialName + ")" + ConfigurationService.YELLOW + '.');
                }
            }
        }
        return true;
    }

    public static Location getNearestSafePosition(Player player, Location origin, int searchRadius) {
        return getNearestSafePosition(player, origin, searchRadius, false);
    }


    public static Location getNearestSafePosition(Player player, Location origin, int searchRadius, boolean stuck) {
        FactionManager factionManager = HCF.getInstance().getFactionManager();
        Faction playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        int max = ConfigurationService.BORDER_SIZES.get(origin.getWorld().getEnvironment());
        int originalX = Math.max(Math.min(origin.getBlockX(), max), -max);
        int originalZ = Math.max(Math.min(origin.getBlockZ(), max), -max);
        int minX = Math.max(originalX - searchRadius, -max) - originalX;
        int maxX = Math.min(originalX + searchRadius, max) - originalX;
        int minZ = Math.max(originalZ - searchRadius, -max) - originalZ;
        int maxZ = Math.min(originalZ + searchRadius, max) - originalZ;
        for (int x = 0; x < searchRadius; x++) {
            if (x > maxX || -x < minX) {
                continue;
            }
            for (int z = 0; z < searchRadius; z++) {
                if (z > maxZ || -z < minZ) {
                    continue;
                }
                Location atPos = origin.clone().add(x, 0.0D, z);
                Faction factionAtPos = factionManager.getFactionAt(atPos);
                if (factionAtPos == null || (!stuck && playerFaction != null && playerFaction.equals(factionAtPos)) || !(factionAtPos instanceof PlayerFaction)) {
                    Location safe = getSafeLocation(origin.getWorld(), atPos.getBlockX(), atPos.getBlockZ());
                    if (safe != null) {
                        return safe.add(0.5, 0.5, 0.5);
                    }
                }
                Location atNeg = origin.clone().add(x, 0.0D, z);
                Faction factionAtNeg = factionManager.getFactionAt(atNeg);
                if (factionAtNeg == null || (!stuck && playerFaction != null && playerFaction.equals(factionAtNeg)) || !(factionAtNeg instanceof PlayerFaction)) {
                    Location safe = getSafeLocation(origin.getWorld(), atNeg.getBlockX(), atNeg.getBlockZ());
                    if (safe != null) {
                        return safe.add(0.5, 0.5, 0.5);
                    }
                }
            }
        }
        return null;
    }

    private static Location getSafeLocation(World world, int x, int z) {
        Block highest = world.getHighestBlockAt(x, z);
        Material type = highest.getType();
        if (Arrays.asList(BLACKLISK).contains(type)) {
            return null;
        }
        while (!type.isSolid()) {
            if (highest.getY() <= 1 || Arrays.asList(BLACKLISK).contains(type)) {
                return null;
            }
            highest = highest.getRelative(BlockFace.DOWN);
            type = highest.getType();
        }
        return highest.getRelative(BlockFace.UP).getLocation();
    }
}
