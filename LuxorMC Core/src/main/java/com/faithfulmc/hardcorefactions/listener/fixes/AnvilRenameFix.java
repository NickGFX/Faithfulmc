package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.framework.command.module.essential.RenameCommand;
import com.faithfulmc.framework.event.AnvilRepairEvent;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class AnvilRenameFix implements Listener {
    private final HCF hcf;

    public AnvilRenameFix(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler
    public void onAnvilRepair(AnvilRepairEvent event){
        if(event.getHumanEntity() instanceof Player) {
            Player player = (Player) event.getHumanEntity();
            ItemStack stack = event.getResult();
            if (stack != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
                if (stack.getType() == Material.PAPER || stack.getType() == Material.DISPENSER) {
                    event.setCancelled(true);
                } else {
                    ItemMeta meta = stack.getItemMeta();
                    String lower = meta.getDisplayName().toLowerCase();
                    for (String word : RenameCommand.DISALLOWED) {
                        if (lower.contains(word)) {
                            player.sendMessage(ConfigurationService.RED + "You may not use that word, you will now be muted");
                            if(!player.hasMetadata("MUTED")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute -s " + player.getName() + " Offensive Item Renaming 3h");
                                player.setMetadata("MUTED", new FixedMetadataValue(hcf, "MUTED"));
                            }
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}
