package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class DeathSignListener implements Listener {
    private static final String DEATH_SIGN_ITEM_NAME;

    public static ItemStack getDeathSign(final String playerName, final String killerName) {
        final ItemStack stack = new ItemStack(Material.SIGN, 1);
        final ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(DeathSignListener.DEATH_SIGN_ITEM_NAME);
        meta.setLore(Lists.newArrayList(new String[]{ConfigurationService.RED + playerName, ConfigurationService.YELLOW + "slain by", ConfigurationService.RED + killerName, DateTimeFormats.DAY_MTH_HR_MIN_SECS.format(System.currentTimeMillis())}));
        stack.setItemMeta(meta);
        return stack;
    }

    static {
        DEATH_SIGN_ITEM_NAME = ConfigurationService.GOLD + "Death Sign";
    }

    public DeathSignListener(final HCF plugin) {
        if (!plugin.getConfig().getBoolean("death-signs", true)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> HandlerList.unregisterAll(this), 5L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(final SignChangeEvent event) {
        if (this.isDeathSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (this.isDeathSign(block)) {
            final BlockState state = block.getState();
            final Sign sign = (Sign) state;
            final ItemStack stack = new ItemStack(Material.SIGN, 1);
            final ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(DeathSignListener.DEATH_SIGN_ITEM_NAME);
            meta.setLore((List) Arrays.asList(sign.getLines()));
            stack.setItemMeta(meta);
            final Player player = event.getPlayer();
            final World world = player.getWorld();
            if (player.getGameMode() != GameMode.CREATIVE && world.isGameRule("doTileDrops")) {
                world.dropItemNaturally(block.getLocation(), stack);
            }
            event.setCancelled(true);
            block.setType(Material.AIR);
            state.update();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final ItemStack stack = event.getItemInHand();
        final BlockState state = event.getBlock().getState();
        if (state instanceof Sign && stack.hasItemMeta()) {
            final ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals(DeathSignListener.DEATH_SIGN_ITEM_NAME)) {
                final Sign sign = (Sign) state;
                final List<String> lore = (List<String>) meta.getLore();
                int count = 0;
                for (final String loreLine : lore) {
                    sign.setLine(count++, loreLine);
                    if (count == 4) {
                        break;
                    }
                }
                sign.update();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Player killer = player.getKiller();
        if (killer != null && (!killer.equals(player) & true)) {
            event.getDrops().add(getDeathSign(player.getName(), killer.getName()));
        }
    }

    private boolean isDeathSign(final Block block) {
        final BlockState state = block.getState();
        if (state instanceof Sign) {
            final String[] lines = ((Sign) state).getLines();
            return lines.length > 0 && lines[1] != null && lines[1].equals(ConfigurationService.YELLOW + "slain by");
        }
        return false;
    }
}