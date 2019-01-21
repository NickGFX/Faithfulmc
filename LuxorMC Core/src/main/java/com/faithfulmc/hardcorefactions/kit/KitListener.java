package com.faithfulmc.hardcorefactions.kit;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.kit.event.KitApplyEvent;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.ParticleEffect;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class KitListener implements Listener {
    private final HCF plugin;
    private final BasePlugin basePlugin;

    public KitListener(final HCF plugin) {
        this.plugin = plugin;
        basePlugin = BasePlugin.getPlugin();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        if (inventory != null) {
            final String title = inventory.getTitle();
            if (title.contains("Kit Preview")) {
                event.setCancelled(true);
            }
            final HumanEntity humanEntity = event.getWhoClicked();
            if (title.contains("Kit Selector") && humanEntity instanceof Player) {
                event.setCancelled(true);
                if (!Objects.equals(event.getView().getTopInventory(), event.getClickedInventory())) {
                    return;
                }
                final ItemStack stack = event.getCurrentItem();
                if (stack == null || !stack.hasItemMeta()) {
                    return;
                }
                final ItemMeta meta = stack.getItemMeta();
                if (!meta.hasDisplayName()) {
                    return;
                }
                final Player player = (Player) humanEntity;
                final String name = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
                final Kit kit = this.plugin.getKitManager().getKit(name);
                if (kit == null) {
                    return;
                }
                kit.applyTo(player, false, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onKitSign(final PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Block block = event.getClickedBlock();
            final BlockState state = block.getState();
            if (!(state instanceof Sign)) {
                return;
            }
            final Sign sign = (Sign) state;
            final String[] lines = sign.getLines();
            if (lines.length >= 2 && lines[1].contains("Kit") && lines[1].contains(String.valueOf(ChatColor.COLOR_CHAR))) {
                final Kit kit = this.plugin.getKitManager().getKit((lines.length >= 3) ? lines[2] : null);
                if (kit == null) {
                    return;
                }
                event.setCancelled(true);
                final Player player = event.getPlayer();
                final String[] fakeLines = Arrays.copyOf(sign.getLines(), 4);
                final boolean applied = kit.applyTo(player, false, false);
                if (applied) {
                    fakeLines[0] = ChatColor.GREEN + "Successfully";
                    fakeLines[1] = ChatColor.GREEN + "equipped kit";
                    fakeLines[2] = kit.getDisplayName();
                    fakeLines[3] = "";
                } else {
                    fakeLines[0] = ConfigurationService.RED + "Failed to";
                    fakeLines[1] = ConfigurationService.RED + "equip kit";
                    fakeLines[2] = kit.getDisplayName();
                    fakeLines[3] = ConfigurationService.RED + "Check chat";
                }
                if (this.basePlugin.getSignHandler().showLines(player, sign, fakeLines, 15L, false) && applied) {
                    ParticleEffect.FIREWORK_SPARK.display(player, sign.getLocation().clone().add(0.5, 0.5, 0.5), 0.01f, 10);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onKitApply(final KitApplyEvent event) {
        if (!event.isForce()) {
            final Player player = event.getPlayer();
            final Kit kit = event.getKit();
            if (!player.isOp() && !kit.isEnabled()) {
                event.setCancelled(true);
                player.sendMessage(ConfigurationService.RED + "The " + kit.getDisplayName() + " kit is currently disabled.");
            } else {
                final String kitPermission = kit.getPermissionNode();
                if (kitPermission != null && !player.hasPermission(kitPermission)) {
                    event.setCancelled(true);
                    player.sendMessage(ConfigurationService.RED + "You do not have permission to use this kit.");
                } else {
                    final UUID uuid = player.getUniqueId();
                    final long minPlaytimeMillis = kit.getMinPlaytimeMillis();
                    FactionUser factionUser = plugin.getUserManager().getUser(uuid);
                    if (minPlaytimeMillis > 0L && factionUser.getCurrentPlaytime() < minPlaytimeMillis) {
                        player.sendMessage(ConfigurationService.RED + "You need at least " + kit.getMinPlaytimeWords() + " minimum playtime to use kit " + kit.getDisplayName() + '.');
                        event.setCancelled(true);
                    } else {
                        final long remaining = factionUser.getRemainingKitCooldown(kit);
                        if (remaining > 0L) {
                            player.sendMessage(ConfigurationService.RED + "You cannot use the " + kit.getDisplayName() + " kit for " + DurationFormatUtils.formatDurationWords(remaining, true, true) + '.');
                            event.setCancelled(true);
                        } else {
                            final int curUses = factionUser.getUses(kit);
                            final int maxUses = kit.getMaximumUses();
                            if (curUses >= maxUses && maxUses != Integer.MAX_VALUE) {
                                player.sendMessage(ConfigurationService.RED + "You have already used this kit " + curUses + '/' + maxUses + " times.");
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onKitApplyMonitor(final KitApplyEvent event) {
        if (!event.isForce()) {
            Player player = event.getPlayer();
            Kit kit = event.getKit();
            FactionUser factionUser = this.plugin.getUserManager().getUser(player.getUniqueId());
            factionUser.incrementKitUses(kit);
            factionUser.updateKitCooldown(kit);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onKitApplyHigh(KitApplyEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Faction factionAt = this.plugin.getFactionManager().getFactionAt(location);
        Faction playerFaction;
        if (((!factionAt.isSafezone()) && (((playerFaction = this.plugin.getFactionManager().getPlayerFaction(player)) == null) || (!playerFaction.equals(factionAt)))) && !player.isOp()) {
            player.sendMessage(ConfigurationService.RED + "Kits can only be applied in safe-zones or your own claims.");
            event.setCancelled(true);
        }
    }
}
