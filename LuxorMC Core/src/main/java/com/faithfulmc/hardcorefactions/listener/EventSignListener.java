package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EventSignListener implements Listener {
    private static final String EVENT_SIGN_ITEM_NAME = ConfigurationService.GOLD + "Event Sign";

    public static ItemStack getEventSign(String playerName, String kothName) {
        ItemStack stack = new ItemStack(Material.SIGN, 1);
        ItemMeta meta = stack.getItemMeta();
        String name = ChatColor.AQUA + kothName;
        meta.setDisplayName(EVENT_SIGN_ITEM_NAME);
        meta.setLore(Lists.newArrayList(new String[]{ConfigurationService.YELLOW.toString() + HCF.getInstance().getFactionManager().getFaction(playerName).getName(), ConfigurationService.WHITE + "captured by", ConfigurationService.YELLOW + ChatColor.stripColor(name), DateTimeFormats.DAY_MTH_HR_MIN_SECS.format(System.currentTimeMillis())}));
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (isEventSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (isEventSign(block)) {
            BlockState state = block.getState();
            Sign sign = (Sign) state;
            ItemStack stack = new ItemStack(Material.SIGN, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(EVENT_SIGN_ITEM_NAME);
            meta.setLore(Arrays.asList(sign.getLines()));
            stack.setItemMeta(meta);
            Player player = event.getPlayer();
            World world = player.getWorld();
            if ((player.getGameMode() != GameMode.CREATIVE) && (world.isGameRule("doTileDrops"))) {
                world.dropItemNaturally(block.getLocation(), stack);
            }
            event.setCancelled(true);
            block.setType(Material.AIR);
            state.update();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack stack = event.getItemInHand();
        BlockState state = event.getBlock().getState();
        if (((state instanceof Sign)) && (stack.hasItemMeta())) {
            ItemMeta meta = stack.getItemMeta();
            if ((meta.hasDisplayName()) && (meta.getDisplayName().equals(EVENT_SIGN_ITEM_NAME))) {
                Sign sign = (Sign) state;
                List<String> lore = meta.getLore();
                int count = 0;
                Iterator<String> var8 = lore.iterator();
                while (var8.hasNext()) {
                    String loreLine = (String) var8.next();
                    sign.setLine(count++, loreLine);
                    if (count == 4) {
                        break;
                    }
                }
                sign.update();
            }
        }
    }

    private boolean isEventSign(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return false;
        }
        String[] lines = ((Sign) state).getLines();
        return (lines.length > 0) && (lines[1] != null) && (lines[1].equals(ConfigurationService.WHITE + "captured by"));
    }
}
