package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.util.ExperienceManager;
import com.google.common.collect.Lists;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BottledExpListener implements Listener {
    private static final String BOTTLED_EXP_DISPLAY_NAME = ConfigurationService.YELLOW.toString() + "Bottled Exp";

    public BottledExpListener() {
        Bukkit.addRecipe(new ShapelessRecipe(createExpBottle(1)).addIngredient(Material.GLASS_BOTTLE));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if ((event.hasItem()) && ((action == Action.RIGHT_CLICK_AIR) || ((action == Action.RIGHT_CLICK_BLOCK) && (!event.isCancelled())))) {
            ItemStack stack = event.getItem();
            if (!isBottledExperience(stack)) {
                return;
            }
            ItemMeta meta = stack.getItemMeta();
            List<String> lore = meta.getLore();
            Integer amount = null;
            for (String loreLine : lore) {
                if ((amount = Ints.tryParse(ChatColor.stripColor(loreLine).split(" ")[0])) != null) {
                    break;
                }
            }
            if (amount != null) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                int previousLevel = player.getLevel();
                new ExperienceManager(player).changeExp(amount.intValue());
                if (player.getLevel() - previousLevel > 5) {
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                }
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.setItemInHand(new ItemStack(Material.GLASS_BOTTLE, 1));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if ((event.getInventory().getHolder() instanceof Player)) {
            CraftingInventory inventory = event.getInventory();
            Player player = (Player) inventory.getHolder();
            if (player.hasPermission("hcf.createbottle")) {
                if (isBottledExperience(inventory.getResult())) {
                    int exp = new ExperienceManager(player).getCurrentExp();
                    inventory.setResult(exp > 0 ? createExpBottle(exp) : new ItemStack(Material.AIR, 1));
                }
            } else {
                inventory.setResult(new ItemStack(Material.AIR, 1));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraftItem(CraftItemEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if ((humanEntity instanceof Player)) {
            Player player = (Player) humanEntity;
            if ((event.getSlotType() == InventoryType.SlotType.RESULT) && (isBottledExperience(event.getCurrentItem()))) {
                player.setLevel(0);
                player.setExp(0.0F);
            }
        }
    }

    private ItemStack createExpBottle(int experience) {
        ItemStack stack = new ItemStack(Material.EXP_BOTTLE, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(BOTTLED_EXP_DISPLAY_NAME);
        meta.setLore(Lists.newArrayList(new String[]{ConfigurationService.WHITE.toString() + experience + ConfigurationService.GOLD + " Experience"}));
        stack.setItemMeta(meta);
        return stack;
    }

    private boolean isBottledExperience(ItemStack stack) {
        if ((stack == null) || (!stack.hasItemMeta())) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return (meta.hasDisplayName()) && (meta.getDisplayName().equals(BOTTLED_EXP_DISPLAY_NAME));
    }
}
