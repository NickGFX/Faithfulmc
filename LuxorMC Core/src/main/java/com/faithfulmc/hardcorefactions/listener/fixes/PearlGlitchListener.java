package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Openable;

public class PearlGlitchListener implements Listener {
    private final ImmutableSet<Material> blockedPearlTypes;
    private final HCF plugin;

    public PearlGlitchListener(HCF plugin) {
        this.blockedPearlTypes = Sets.immutableEnumSet(Material.THIN_GLASS, new Material[]{Material.IRON_FENCE, Material.FENCE, Material.NETHER_FENCE, Material.FENCE_GATE, Material.ACACIA_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_WOOD_STAIRS, Material.NETHER_BRICK_STAIRS, Material.QUARTZ_STAIRS, Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.WOOD_STAIRS});
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.hasItem()) && (event.getItem().getType() == Material.ENDER_PEARL)) {
            Block block = event.getClickedBlock();
            Material type = block.getType();
            if ((type.isSolid()) && (!(block.getState() instanceof InventoryHolder))) {
                if(((CraftServer) Bukkit.getServer()).isNoTrapping() && type == Material.FENCE_GATE && block.getState().getData() instanceof Openable){
                    if(((Openable) block.getState().getData()).isOpen()){
                        return;
                    }
                }
                Faction factionAt = HCF.getInstance().getFactionManager().getFactionAt(block.getLocation());
                if (!(factionAt instanceof ClaimableFaction)) {
                    return;
                }
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.setItemInHand(event.getItem());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPearlClip(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Location to = event.getTo();
            Block block = to.getBlock();
            Material type = block.getType();
            if (this.blockedPearlTypes.contains(type)) {
                if(((CraftServer) Bukkit.getServer()).isNoTrapping() && type == Material.FENCE_GATE && block.getState().getData() instanceof Openable){
                    if(((Openable) block.getState().getData()).isOpen()){
                        return;
                    }
                }
                Block above = block.getRelative(BlockFace.UP);
                Material aboveType = above.getType();
                if(this.blockedPearlTypes.contains(aboveType) || aboveType.isSolid()) {
                    Player player = event.getPlayer();
                    player.sendMessage(ChatColor.YELLOW + "Pearl glitching detected, your pearl has been refunded");
                    event.setCancelled(true);
               }
                else{
                    to.add(0, 1, 0);
                    event.setTo(to);
                }
            }
        }
    }
}
