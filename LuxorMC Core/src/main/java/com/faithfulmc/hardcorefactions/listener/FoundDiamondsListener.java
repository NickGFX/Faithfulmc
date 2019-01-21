package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.util.location.BlockLocation;
import com.faithfulmc.util.Config;
import com.faithfulmc.util.GenericUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FoundDiamondsListener implements Listener {
    public static final Material SEARCH_TYPE = Material.DIAMOND_ORE;
    public final Set<BlockLocation> foundLocations = new HashSet<BlockLocation>();
    private final HCF plugin;
    private static final String METADATA_NAME = "HCF_FD";
    private static final boolean METADATA_BASED = ConfigurationService.DIAMONDS_METADATA;
    private static final int SEARCH_RADIUS = 3;

    public FoundDiamondsListener(HCF plugin) {
        this.plugin = plugin;
        if (!METADATA_BASED) {
            Config config = new Config(plugin, "diamonds");
            this.foundLocations.addAll(GenericUtils.createList(config.get("registered-diamonds", Collections.emptyList()), BlockLocation.class));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == SEARCH_TYPE) {
                addDiamond(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == SEARCH_TYPE) {
            addDiamond(block);
        }
    }

    public static final String NO_DIAMOND_ALERTS = "FD_ALERTS";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        if (block.getType() == SEARCH_TYPE && addDiamond(blockLocation)) {
            int count = 1;
            for (int x = -SEARCH_RADIUS; x < SEARCH_RADIUS; ++x) {
                for (int y = -SEARCH_RADIUS; y < SEARCH_RADIUS; ++y) {
                    for (int z = -SEARCH_RADIUS; z < SEARCH_RADIUS; ++z) {
                        Block otherBlock = blockLocation.clone().add((double) x, (double) y, (double) z).getBlock();
                        if (otherBlock.equals(block) || otherBlock.getType() != SEARCH_TYPE || !addDiamond(otherBlock)) {
                            continue;
                        }
                        ++count;
                    }
                }
            }
            String message = ChatColor.GRAY + "[" + ChatColor.AQUA + "*" + ChatColor.GRAY + "] " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " has found" + ChatColor.AQUA + " Diamonds " + ChatColor.GRAY + '[' + ChatColor.AQUA + count + ChatColor.GRAY + ']';
            for(Player other: Bukkit.getOnlinePlayers()){
                if(!other.hasMetadata(NO_DIAMOND_ALERTS)){
                    other.sendMessage(message);
                }
            }
        }
    }

    public void saveConfig() {
        if(!METADATA_BASED) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("registered-diamonds", new ArrayList<>(this.foundLocations));
            try {
                config.save(new File(plugin.getDataFolder(), "diamonds.yml"));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public boolean isDiamond(Block block){
        if(METADATA_BASED){
            return block.hasMetadata(METADATA_NAME);
        }
        else return foundLocations.contains(BlockLocation.fromLocation(block.getLocation()));
    }

    public boolean isDiamond(Location block){
        if(METADATA_BASED){
            return isDiamond(block.getBlock());
        }
        return foundLocations.contains(BlockLocation.fromLocation(block));
    }

    public boolean addDiamond(Block block){
        if(METADATA_BASED){
            if(!block.hasMetadata(METADATA_NAME)){
                block.setMetadata(METADATA_NAME, new FixedMetadataValue(plugin, true));
                return true;
            }
            return false;
        }
        return foundLocations.add(BlockLocation.fromLocation(block.getLocation()));
    }

    public boolean addDiamond(Location block){
        if(METADATA_BASED){
            return addDiamond(block.getBlock());
        }
        return foundLocations.add(BlockLocation.fromLocation(block));
    }
}