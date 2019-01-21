package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EventPearlFix implements Listener {
    private final HCF plugin;

    public EventPearlFix(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerEnderpearl(PlayerInteractEvent event) {
        Action action = event.getAction();
        if(action == Action.RIGHT_CLICK_AIR) {
            ItemStack itemStack = event.getItem();
            if(itemStack != null && itemStack.getType() == Material.ENDER_PEARL) {
                Player player = event.getPlayer();
                Faction factionAt = plugin.getFactionManager().getFactionAt(player.getLocation());
                if (factionAt instanceof EventFaction) {
                    event.setUseItemInHand(Event.Result.DENY);
                    player.sendMessage(ConfigurationService.RED + "You may not " + plugin.getTimerManager().enderPearlTimer.getDisplayName() + ConfigurationService.RED + " in an active event zone.");
                }
            }
        }
    }
}
