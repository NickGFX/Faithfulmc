package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullListener implements Listener {
    private static final String KILL_BEHEAD_PERMISSION = "hcf.kill.behead";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if ((killer != null) && (killer.hasPermission(KILL_BEHEAD_PERMISSION))) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(player.getName());
            skull.setItemMeta(meta);
            event.getDrops().add(skull);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            BlockState state = event.getClickedBlock().getState();
            if ((state instanceof Skull)) {
                Skull skull = (Skull) state;
                player.sendMessage(ConfigurationService.YELLOW + "This head belongs to " + ConfigurationService.WHITE + ((skull.getSkullType() == SkullType.PLAYER) && (skull.hasOwner()) ? skull.getOwner() : new StringBuilder().append("a ").append(WordUtils.capitalizeFully(skull.getSkullType().name())).append(" skull").toString()) + ConfigurationService.YELLOW + '.');
            }
        }
    }
}
