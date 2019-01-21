package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.util.Crowbar;
import com.faithfulmc.util.ItemBuilder;
import com.faithfulmc.util.ParticleEffect;
import net.minecraft.util.com.google.common.base.Enums;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class CrowbarListener implements Listener {
    private final HCF plugin;

    public CrowbarListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.hasItem())) {
            com.google.common.base.Optional<Crowbar> crowbarOptional = Crowbar.fromStack(event.getItem());
            if (crowbarOptional.isPresent()) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                World world = player.getWorld();
                if (world.getEnvironment() != World.Environment.NORMAL) {
                    player.sendMessage(ConfigurationService.RED + "Crowbars may only be used in the overworld.");
                    return;
                }
                Block block = event.getClickedBlock();
                Location blockLocation = block.getLocation();
                if (!ProtectionListener.attemptBuild(player, blockLocation, ConfigurationService.YELLOW + "You cannot do this in the territory of %1$s" + ConfigurationService.YELLOW + '.')) {
                    return;
                }
                Crowbar crowbar = (Crowbar) crowbarOptional.get();
                BlockState blockState = block.getState();
                if ((blockState instanceof CreatureSpawner)) {
                    int remainingUses = crowbar.getSpawnerUses();
                    if (remainingUses <= 0) {
                        player.sendMessage(ConfigurationService.RED + "This crowbar has no more Spawner uses.");
                        return;
                    }
                    crowbar.setSpawnerUses(remainingUses - 1);
                    player.setItemInHand(crowbar.getItemIfPresent());
                    CreatureSpawner spawner = (CreatureSpawner) blockState;
                    block.setType(Material.AIR);
                    blockState.update();
                    world.dropItemNaturally(blockLocation, new ItemBuilder(Material.MOB_SPAWNER).displayName(ChatColor.GREEN + "Spawner").data((short) spawner.getData().getData()).loreLine(ConfigurationService.WHITE + WordUtils.capitalizeFully(spawner.getSpawnedType().name())).build());
                } else {
                    if (block.getType() != Material.ENDER_PORTAL_FRAME) {
                        return;
                    }
                    int remainingUses = crowbar.getEndFrameUses();
                    if (remainingUses <= 0) {
                        player.sendMessage(ConfigurationService.RED + "This crowbar has no more End Portal Frame uses.");
                        return;
                    }
                    boolean destroyed = false;
                    int blockX = blockLocation.getBlockX();
                    int blockY = blockLocation.getBlockY();
                    int blockZ = blockLocation.getBlockZ();
                    int searchRadius = 4;
                    for (int x = blockX - searchRadius; x <= blockX + searchRadius; x++) {
                        for (int z = blockZ - searchRadius; z <= blockZ + searchRadius; z++) {
                            Block next = world.getBlockAt(x, blockY, z);
                            if (next.getType() == Material.ENDER_PORTAL) {
                                next.setType(Material.AIR);
                                next.getState().update();
                                destroyed = true;
                            }
                        }
                    }
                    if (destroyed) {
                        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                        player.sendMessage(ConfigurationService.RED.toString() + ConfigurationService.GOLD + "Ender Portal is no longer active");
                        if (playerFaction != null) {
                            boolean informFaction = false;
                            for (Claim claim : playerFaction.getClaims()) {
                                if (claim.contains(blockLocation)) {
                                    informFaction = true;
                                    break;
                                }
                            }
                            if (informFaction) {
                                FactionMember factionMember = playerFaction.getMember(player);
                                String astrix = factionMember.getRole().getAstrix();
                                playerFaction.broadcast(ConfigurationService.TEAMMATE_COLOUR + astrix + factionMember.getName() + ChatColor.YELLOW + " has used a Crowbar de-activating one of the factions' end portals.", new UUID[]{player.getUniqueId()});
                            }
                        }
                    }
                    crowbar.setEndFrameUses(remainingUses - 1);
                    player.setItemInHand(crowbar.getItemIfPresent());
                    block.setType(Material.AIR);
                    blockState.update();
                    world.dropItemNaturally(blockLocation, new ItemStack(Material.ENDER_PORTAL_FRAME, 1));
                }
                if (event.getItem().getType() == Material.AIR) {
                    player.playSound(blockLocation, Sound.ITEM_BREAK, 1.0F, 1.0F);
                } else {
                    ParticleEffect.FIREWORK_SPARK.display(player, blockLocation, 0.125F, 50);
                    player.playSound(blockLocation, Sound.LEVEL_UP, 1.0F, 1.0F);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        ItemStack stack = event.getItemInHand();
        Player player = event.getPlayer();
        if (((block.getState() instanceof CreatureSpawner)) && (stack.hasItemMeta())) {
            ItemMeta meta = stack.getItemMeta();
            if ((meta.hasLore()) && (meta.hasDisplayName())) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                List<String> lore = meta.getLore();
                if (!lore.isEmpty()) {
                    String spawnerName = ChatColor.stripColor(((String) lore.get(0)).toUpperCase());
                    net.minecraft.util.com.google.common.base.Optional<EntityType> entityTypeOptional = Enums.getIfPresent(EntityType.class, spawnerName);
                    if (entityTypeOptional.isPresent()) {
                        spawner.setSpawnedType((EntityType) entityTypeOptional.get());
                        spawner.update(true, true);
                        player.sendMessage(ConfigurationService.YELLOW + "Placed a " + ConfigurationService.GRAY + spawnerName + ConfigurationService.YELLOW + " spawner.");
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareCrowbarCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        if ((event.isRepair()) && (event.getRecipe().getResult().getType() == Crowbar.CROWBAR_TYPE)) {
            int endFrameUses = 0;
            int spawnerUses = 0;
            boolean changed = false;
            ItemStack[] matrix2;
            ItemStack[] matrix = matrix2 = inventory.getMatrix();
            for (ItemStack ingredient : matrix2) {
                com.google.common.base.Optional<Crowbar> crowbarOptional = Crowbar.fromStack(ingredient);
                if (crowbarOptional.isPresent()) {
                    Crowbar crowbar = (Crowbar) crowbarOptional.get();
                    spawnerUses += crowbar.getSpawnerUses();
                    endFrameUses += crowbar.getEndFrameUses();
                    changed = true;
                }
            }
            if (changed) {
                inventory.setResult(new Crowbar(spawnerUses, endFrameUses).getItemIfPresent());
            }
        }
    }
}
