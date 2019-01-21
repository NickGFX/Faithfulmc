package com.faithfulmc.hardcorefactions.faction.claim;

import com.luxormc.block.BlockPosition;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.visualise.VisualBlock;
import com.faithfulmc.hardcorefactions.visualise.VisualType;
import com.google.common.base.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class ClaimWandListener implements Listener {
    private final HCF plugin;

    public ClaimWandListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.PHYSICAL || !event.hasItem() || !this.isClaimingWand(event.getItem())) {
            return;
        }
        final Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (action == Action.RIGHT_CLICK_AIR) {
            this.plugin.getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            player.sendMessage(ConfigurationService.RED + "You have cleared your claim selection.");
            return;
        }
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(uuid);
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK || !player.isSneaking()) {
            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                Location blockLocation = block.getLocation();
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                }
                if (this.plugin.getClaimHandler().canClaimHere(player, blockLocation)) {
                    ClaimSelection revert = new ClaimSelection(blockLocation.getWorld());
                    ClaimSelection claimSelection = this.plugin.getClaimHandler().claimSelectionMap.putIfAbsent(uuid, revert);
                    if (claimSelection == null) {
                        claimSelection = revert;
                    }
                    Location oldPosition = null;
                    Location opposite = null;
                    int selectionId = 0;
                    switch (action) {
                        case LEFT_CLICK_BLOCK: {
                            oldPosition = claimSelection.getPos1();
                            opposite = claimSelection.getPos2();
                            selectionId = 1;
                            break;
                        }
                        case RIGHT_CLICK_BLOCK: {
                            oldPosition = claimSelection.getPos2();
                            opposite = claimSelection.getPos1();
                            selectionId = 2;
                            break;
                        }
                        default: {
                            return;
                        }
                    }
                    int blockX = blockLocation.getBlockX();
                    int blockZ = blockLocation.getBlockZ();
                    if (oldPosition != null && blockX == oldPosition.getBlockX() && blockZ == oldPosition.getBlockZ()) {
                        return;
                    }
                    if (System.currentTimeMillis() - claimSelection.getLastUpdateMillis() <= 200) {
                        return;
                    }
                    if (opposite != null) {
                        int xDiff = Math.abs(opposite.getBlockX() - blockX) + 1;
                        int zDiff = Math.abs(opposite.getBlockZ() - blockZ) + 1;
                        if (xDiff < 5 || zDiff < 5) {
                            player.sendMessage((Object) ConfigurationService.RED + "Claim selections must be at least " + 5 + 'x' + 5 + " blocks.");
                            return;
                        }
                    }
                    if (oldPosition != null) {
                        final Location finalOldPosition = oldPosition;
                        this.plugin.getVisualiseHandler().clearVisualType(player, VisualType.CREATE_CLAIM_SELECTION, new Predicate<VisualBlock>() {

                            public boolean apply(VisualBlock visualBlock) {
                                BlockPosition location = visualBlock.getLocation();
                                return location.getX() == finalOldPosition.getBlockX() && location.getZ() == finalOldPosition.getBlockZ();
                            }
                        }, true);
                    }
                    if (selectionId == 1) {
                        claimSelection.setPos1(blockLocation);
                    }
                    if (selectionId == 2) {
                        claimSelection.setPos2(blockLocation);
                    }
                    player.sendMessage( ChatColor.GREEN + "Set the location of claim selection " + ConfigurationService.YELLOW + selectionId + (Object) ChatColor.GREEN + " to: " + (Object) ConfigurationService.GOLD + '(' + (Object) ConfigurationService.YELLOW + blockX + ", " + blockZ + (Object) ConfigurationService.GOLD + ')');
                    if (claimSelection.hasBothPositionsSet()) {
                        int selectionPrice;
                        Claim claim = claimSelection.toClaim(playerFaction);
                        player.sendMessage( ConfigurationService.YELLOW + "Claim selection cost: " +  ((selectionPrice = claimSelection.getPrice(playerFaction, false)) > playerFaction.getBalance() ? ConfigurationService.RED : ChatColor.GREEN) + '$' + selectionPrice + (Object) ConfigurationService.YELLOW + ". Current size: (" + (Object) ConfigurationService.WHITE + claim.getWidth() + ", " + claim.getLength() + (Object) ConfigurationService.YELLOW + "), " + (Object) ConfigurationService.WHITE + claim.getArea() + (Object) ConfigurationService.YELLOW + " blocks.");
                    }
                    int blockY = block.getY();
                    int maxHeight = player.getWorld().getMaxHeight();
                    final ArrayList<BlockPosition> locations = new ArrayList<>(maxHeight);
                    for (int i = blockY; i < maxHeight; ++i) {
                        locations.add(new BlockPosition(blockX, i, blockZ));
                    }
                    new BukkitRunnable() {

                        public void run() {
                            ClaimWandListener.this.plugin.getVisualiseHandler().addVisualType(player, locations, VisualType.CREATE_CLAIM_SELECTION, true);
                        }
                    }.runTask(this.plugin);
                }
            }
            return;
        }
        ClaimSelection claimSelection2 =  this.plugin.getClaimHandler().claimSelectionMap.get(uuid);
        if (claimSelection2 == null || !claimSelection2.hasBothPositionsSet()) {
            player.sendMessage( ConfigurationService.RED + "You have not set both positions of this claim selection.");
            return;
        }
        if (this.plugin.getClaimHandler().tryPurchasing(player, claimSelection2.toClaim(playerFaction))) {
            this.plugin.getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.isClaimingWand(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player player;
        if (event.getDamager() instanceof Player && this.isClaimingWand((player = (Player) event.getDamager()).getItemInHand())) {
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            this.plugin.getClaimHandler().clearClaimSelection(player);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.CLAIM_WAND);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.CLAIM_WAND);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (this.isClaimingWand(item.getItemStack())) {
            item.remove();
            this.plugin.getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (this.isClaimingWand(item.getItemStack())) {
            item.remove();
            this.plugin.getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().remove(ClaimHandler.CLAIM_WAND)) {
            this.plugin.getClaimHandler().clearClaimSelection(event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            player.getInventory().remove(ClaimHandler.CLAIM_WAND);
            this.plugin.getClaimHandler().clearClaimSelection(player);
        }
    }

    public boolean isClaimingWand(ItemStack stack) {
        return stack != null && stack.isSimilar(ClaimHandler.CLAIM_WAND);
    }

}