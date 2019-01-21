package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.command.OreAmountCommand;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerLevel;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerLevelEvent;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.google.common.collect.ImmutableSet;
import com.luxormc.event.PlayerMineItemsEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Iterator;
import java.util.Set;

public class OreListener implements Listener {
    private final HCF plugin;

    public OreListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMine(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Block block = e.getBlock();
        if (block == null) {
            return;
        }
        if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }
        Material type = block.getType();
        if (type == Material.GLOWING_REDSTONE_ORE) {
            type = Material.REDSTONE_ORE;
        }
        FactionUser factionUser = plugin.getUserManager().getIfContains(player.getUniqueId());
        if (factionUser != null && OreAmountCommand.ORES.containsKey(type)) {
            int amt = factionUser.getOres().getOrDefault(type.getId(), 0);
            amt++;
            factionUser.getOres().put(type.getId(), amt);
            if(type == Material.DIAMOND_ORE){
                MinerLevel level = factionUser.getMinerLevel();
                MinerLevel next = level.next();
                if(next != null && amt >= next.getAmount()){
                    Bukkit.getPluginManager().callEvent(new MinerLevelEvent(factionUser, next));
                    factionUser.setMinerLevel(next);
                }
            }
        }
    }

    public static final String NO_OREINVENTORY_META = "NO_OREINVENTORY";

    public static final Set<Material> ORES = ImmutableSet.of(
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.COAL_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.REDSTONE_ORE,
            Material.EMERALD_ORE,
            Material.QUARTZ_ORE,
            Material.IRON_INGOT,
            Material.GOLD_INGOT,
            Material.COAL,
            Material.INK_SACK,
            Material.DIAMOND,
            Material.REDSTONE,
            Material.EMERALD,
            Material.QUARTZ
    );

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMine(PlayerMineItemsEvent event){
        Player player = event.getPlayer();
        if(!player.hasMetadata(NO_OREINVENTORY_META)){
            PlayerInventory playerInventory = player.getInventory();
            Iterator<ItemStack> iterator = event.getStackCollection().iterator();
            while (iterator.hasNext()){
                ItemStack itemStack = iterator.next();
                if(itemStack != null){
                    Material type = itemStack.getType();
                    if(ORES.contains(type)){
                        if(playerInventory.addItem(itemStack).isEmpty()){
                            iterator.remove();
                        }
                        else{
                            player.sendMessage(ConfigurationService.YELLOW + "Your inventory is full");
                        }
                    }
                }
            }
        }
    }
}
