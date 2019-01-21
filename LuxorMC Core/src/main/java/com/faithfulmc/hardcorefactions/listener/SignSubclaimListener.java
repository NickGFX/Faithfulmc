package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.Lists;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class SignSubclaimListener implements Listener {
    private static final int MAX_SIGN_LINE_CHARS = 16;
    private static final String SUBCLAIM_PREFIX;
    private static final BlockFace[] SIGN_FACES;

    static {
        SUBCLAIM_PREFIX = ChatColor.AQUA.toString() + "[Subclaim]";
        SIGN_FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};
    }

    private final HCF plugin;

    public SignSubclaimListener(final HCF plugin) {
        this.plugin = plugin;
    }

    private boolean isSubclaimable(final Block block) {
        final Material type = block.getType();
        return type == Material.FENCE_GATE || type == Material.TRAP_DOOR || block.getState() instanceof InventoryHolder;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(final SignChangeEvent event) {
        String[] lines = event.getLines();
        if (!StringUtils.containsIgnoreCase(lines[0], "subclaim")) {
            return;
        }
        Block block = event.getBlock();
        MaterialData materialData = block.getState().getData();
        if (materialData instanceof Sign && block.getState() instanceof org.bukkit.block.Sign) {
            Sign sign = (Sign) materialData;
            org.bukkit.block.Sign signBlock = (org.bukkit.block.Sign) block.getState();
            Block attatchedBlock = block.getRelative(sign.getAttachedFace());
            if (this.isSubclaimable(attatchedBlock)) {
                Player player = event.getPlayer();
                PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                Role role;
                if (playerFaction == null || (role = playerFaction.getMember(player).getRole()) == Role.MEMBER) {
                    return;
                }
                final Collection<org.bukkit.block.Sign> attachedSigns = getAttachedSigns(attatchedBlock);
                new BukkitRunnable() {
                    public void run() {
                        Faction factionAt = plugin.getFactionManager().getFactionAt(block.getLocation());
                        if (playerFaction.equals(factionAt)) {
                            for (org.bukkit.block.Sign attachedSign : attachedSigns) {
                                if (attachedSign.getLine(0).equals(SignSubclaimListener.SUBCLAIM_PREFIX)) {
                                    player.sendMessage(ConfigurationService.RED + "There is already a subclaim sign on this " + attatchedBlock.getType().toString() + '.');
                                    return;
                                }
                            }
                            for (int i = 0; i < lines.length; i++) {
                                signBlock.setLine(i, lines[i]);
                            }
                            List<String> memberList = new ArrayList<String>(3);
                            for (int i = 1; i < lines.length; ++i) {
                                String line = lines[i];
                                if (StringUtils.isNotBlank(line)) {
                                    memberList.add(line);
                                }
                            }

                            if (memberList.isEmpty()) {
                                signBlock.setLine(1, player.getName());
                                player.sendMessage(ConfigurationService.YELLOW + "Since no name was specified, this subclaim is now for you.");
                            }
                            boolean leaderChest = lines[1].equals(Role.LEADER.getAstrix()) || StringUtils.containsIgnoreCase((CharSequence) lines[1], (CharSequence) "leader");
                            boolean captainChest = lines[1].equals(Role.CAPTAIN.getAstrix()) || StringUtils.containsIgnoreCase((CharSequence) lines[1], (CharSequence) "captain");
                            if (captainChest) {
                                signBlock.setLine(2, null);
                                signBlock.setLine(3, null);
                                signBlock.setLine(1, ConfigurationService.YELLOW + "Captains Only");
                            }
                            if (leaderChest) {
                                if (role != Role.LEADER && role != Role.COLEADER) {
                                    player.sendMessage(ConfigurationService.RED + "Only faction leaders can create leader subclaimed objects.");
                                    return;
                                }
                                signBlock.setLine(2, null);
                                signBlock.setLine(3, null);
                                signBlock.setLine(1, ChatColor.DARK_RED + "Leaders Only");
                            }
                            signBlock.setLine(0, SignSubclaimListener.SUBCLAIM_PREFIX);
                            signBlock.update();
                            List actualMembers = memberList.stream().filter(member -> playerFaction.getMember(plugin, member) != null).collect(Collectors.toList());
                            playerFaction.broadcast(ConfigurationService.TEAMMATE_COLOUR + player.getName() + ConfigurationService.YELLOW + " has created a subclaim on block miner " + ChatColor.LIGHT_PURPLE + attatchedBlock.getType().toString() + ConfigurationService.YELLOW + " at " + ConfigurationService.WHITE + '[' + attatchedBlock.getX() + ", " + attatchedBlock.getZ() + ']' + ConfigurationService.YELLOW + " for " + (leaderChest ? "leaders" : (actualMembers.isEmpty() ? "captains" : ("members " + ConfigurationService.GRAY + '[' + ChatColor.DARK_GREEN + StringUtils.join((Iterable) actualMembers, ", ") + ConfigurationService.GRAY + ']'))));
                        }
                    }
                }.runTaskAsynchronously(plugin);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (this.plugin.getEotwHandler().isEndOfTheWorld()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("hcf.faction.protection.bypass")) {
            return;
        }
        Block block = event.getBlock();
        BlockState state = block.getState();
        if (state instanceof org.bukkit.block.Sign || this.isSubclaimable(block)) {
            PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                return;
            }
            Role role = playerFaction.getMember(player).getRole();
            if(role == Role.LEADER || role == Role.COLEADER) return;
            if (state instanceof org.bukkit.block.Sign) {
                final org.bukkit.block.Sign sign = (org.bukkit.block.Sign) state;
                if (sign.getLine(0).equals(SignSubclaimListener.SUBCLAIM_PREFIX)) {
                    if(!sign.getLine(1).equals(ChatColor.DARK_RED + "Leaders Only") && role == Role.CAPTAIN) return;
                    event.setCancelled(true);
                    player.sendMessage(ConfigurationService.RED + "You cannot break subclaim signs");
                }
                return;
            }
            Faction factionAt = this.plugin.getFactionManager().getFactionAt(block);
            String search = this.getShortenedName(player.getName());
            if (playerFaction.equals(factionAt) && !playerFaction.isRaidable()) {
                Collection<org.bukkit.block.Sign> attachedSigns = this.getAttachedSigns(block);
                for (org.bukkit.block.Sign attachedSign : attachedSigns) {
                    String[] lines = attachedSign.getLines();
                    if (!lines[0].equals(SignSubclaimListener.SUBCLAIM_PREFIX)) {
                        continue;
                    }
                    if(!lines[1].equals(ChatColor.DARK_RED + "Leaders Only") && role == Role.CAPTAIN) return;
                    for (int i = 1; i < lines.length; ++i) {
                        if (lines[i].contains(search)) {
                            return;
                        }
                    }
                    event.setCancelled(true);
                    player.sendMessage(ConfigurationService.RED + "You cannot break this subclaimed " + block.getType().toString() + '.');
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMoveItem(final InventoryMoveItemEvent event) {
        if (this.plugin.getEotwHandler().isEndOfTheWorld()) {
            return;
        }
        final InventoryHolder holder = event.getSource().getHolder();
        Collection<Block> sourceBlocks;
        if (holder instanceof Chest) {
            sourceBlocks = Collections.singletonList(((Chest) holder).getBlock());
        } else {
            if (!(holder instanceof DoubleChest)) {
                return;
            }
            final DoubleChest doubleChest = (DoubleChest) holder;
            sourceBlocks = Lists.newArrayList(((Chest) doubleChest.getLeftSide()).getBlock(), ((Chest) doubleChest.getRightSide()).getBlock());
        }
        if(event.getDestination() != null &&
                event.getDestination().getHolder() != null && 
                event.getDestination().getHolder() instanceof HopperMinecart){
            event.setCancelled(true); // Fix minecart issue
            return;
        }
        for (final Block block : sourceBlocks) {
            if (block.hasMetadata("SUBCLAIM_META")) {
                MetadataValue fixedMetadataValue = BukkitUtils.getMetaData(block, "SUBCLAIM_META", plugin);
                if(fixedMetadataValue != null) {
                    boolean subclaim = fixedMetadataValue.asBoolean();
                    if (subclaim) {
                        event.setCancelled(true);
                        return;
                    }
                }
                continue;
            }
            final Collection<org.bukkit.block.Sign> attachedSigns = this.getAttachedSigns(block);
            for (final org.bukkit.block.Sign attachedSign : attachedSigns) {
                if (attachedSign.getLine(0).equals(SignSubclaimListener.SUBCLAIM_PREFIX)) {
                    block.setMetadata("SUBCLAIM_META", new FixedMetadataValue(plugin, true));
                    event.setCancelled(true);
                    return;
                }
            }
            block.setMetadata("SUBCLAIM_META", new FixedMetadataValue(plugin, false));
        }
    }

    private String getShortenedName(String originalName) {
        if (originalName.length() == 16) {
            originalName = originalName.substring(0, 15);
        }
        return originalName;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("hcf.faction.protection.bypass")) {
            return;
        }
        if (this.plugin.getEotwHandler().isEndOfTheWorld()) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (this.isSubclaimable(block)) {
            final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null || playerFaction.isRaidable()) {
                return;
            }
            final Role role = playerFaction.getMember(player).getRole();
            if (role == Role.LEADER || role == Role.COLEADER) {
                return;
            }
            if (playerFaction.equals(this.plugin.getFactionManager().getFactionAt(block))) {
                final Collection<org.bukkit.block.Sign> attachedSigns = this.getAttachedSigns(block);
                if (attachedSigns.isEmpty()) {
                    return;
                }
                final String search = this.getShortenedName(player.getName());
                for (final org.bukkit.block.Sign attachedSign : attachedSigns) {
                    final String[] lines = attachedSign.getLines();
                    if (!lines[0].equals(SignSubclaimListener.SUBCLAIM_PREFIX)) {
                        continue;
                    }
                    if (!Role.LEADER.getAstrix().equals(lines[1])) {
                        for (int i = 1; i < lines.length; ++i) {
                            if (lines[i].contains(search)) {
                                return;
                            }
                        }
                    }
                    if (role != Role.CAPTAIN) {
                        event.setCancelled(true);
                        player.sendMessage(ConfigurationService.RED + "You do not have access to this subclaimed " + block.getType().toString() + '.');
                        break;
                    }
                    if (lines[1].contains("Leader")) {
                        event.setCancelled(true);
                        player.sendMessage(ConfigurationService.RED + "You do not have access to this subclaimed " + block.getType().toString() + '.');
                        break;
                    }
                }
            }
        }
    }

    public Collection<org.bukkit.block.Sign> getAttachedSigns(final Block block) {
        final Set<org.bukkit.block.Sign> results = new HashSet<org.bukkit.block.Sign>();
        this.getSignsAround(block, results);
        final BlockState state = block.getState();
        if (state instanceof Chest) {
            final Inventory chestInventory = ((Chest) state).getInventory();
            if (chestInventory instanceof DoubleChestInventory) {
                final DoubleChest doubleChest = ((DoubleChestInventory) chestInventory).getHolder();
                final Block left = ((Chest) doubleChest.getLeftSide()).getBlock();
                final Block right = ((Chest) doubleChest.getRightSide()).getBlock();
                this.getSignsAround(left.equals(block) ? right : left, results);
            }
        }
        return results;
    }

    private Set<org.bukkit.block.Sign> getSignsAround(final Block block, final Set<org.bukkit.block.Sign> results) {
        for (final BlockFace face : SignSubclaimListener.SIGN_FACES) {
            final Block relative = block.getRelative(face);
            final BlockState relativeState = relative.getState();
            if (relativeState instanceof org.bukkit.block.Sign) {
                final Sign materialSign = (Sign) relativeState.getData();
                if (relative.getRelative(materialSign.getAttachedFace()).equals(block)) {
                    results.add((org.bukkit.block.Sign) relative.getState());
                }
            }
        }
        return results;
    }
}