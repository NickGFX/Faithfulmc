package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.listener.ExpMultiplierListener;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BottleCommand implements CommandExecutor, Listener {
    private final Map<ThrownExpBottle, Integer> thrownExpBottleIntegerMap;
    private final HCF hcf;
    private final String name = ConfigurationService.GOLD + "XP Bottle";

    public BottleCommand(HCF hcf) {
        this.hcf = hcf;
        hcf.getServer().getPluginManager().registerEvents(this, hcf);
        thrownExpBottleIntegerMap = new HashMap<>();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int exp = player.getTotalExperience();
            if (exp == 0) {
                player.sendMessage(ConfigurationService.RED + "You have no XP");
            } else {
                ItemStack bottle = createBottle(player);
                player.setTotalExperience(0);
                player.setLevel(0);
                player.setExp(0);
                if (player.getInventory().addItem(bottle).isEmpty()) {
                    player.sendMessage(ConfigurationService.RED + "Created an experience bottle");
                } else {
                    player.sendMessage(ConfigurationService.RED + "You have no space in your inventory");
                }
            }
        } else {
            sender.sendMessage(ConfigurationService.RED + "You must be a player to execute this command");
        }
        return true;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK && item != null && item.getType() == Material.EXP_BOTTLE) {
            ItemMeta meta = item.getItemMeta();
            String name = meta.getDisplayName();
            if (meta.hasDisplayName() && meta.hasLore() && name.equals(this.name) && meta.getLore().size() == 3) {
                String loreline = meta.getLore().get(2);
                e.setCancelled(true);
                Integer xp;
                try {
                    xp = Integer.parseInt(loreline.substring((ConfigurationService.YELLOW + "Experience: " + ConfigurationService.GRAY).length(), loreline.length()));
                } catch (Exception ex) {
                    player.sendMessage(ConfigurationService.RED + "Invalid XP bottle");
                    return;
                }
                ThrownExpBottle thrownExpBottle = player.launchProjectile(ThrownExpBottle.class);
                thrownExpBottleIntegerMap.put(thrownExpBottle, xp);

                ItemStack hand = item.clone();
                hand.setAmount(hand.getAmount() - 1);
                player.getInventory().setItemInHand(hand);
                player.updateInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onXPEvent(ExpBottleEvent e) {
        Integer xp = thrownExpBottleIntegerMap.remove(e.getEntity());
        if (xp != null) {
            e.setExperience((int) Math.round(xp / ExpMultiplierListener.DEFAULT_MULTIPLER));
        }
    }

    public int getExpToLevel(int expLevel) {
        return expLevel >= 30 ? 62 + (expLevel - 30) * 7 : (expLevel >= 15 ? 17 + (expLevel - 15) * 3 : 17);
    }

    public int fromXP(int levels, float exp) {
        int xp = 0;
        for (int i = levels; i >= 0; i--) {
            xp += getExpToLevel(i);
        }
        xp -= levels;
        return xp;
    }

    public ItemStack createBottle(Player player) {
        int exp = fromXP(player.getLevel(), player.getExp());
        int levels = player.getLevel();
        float remainder = player.getExp();
        ItemStack itemStack = new ItemStack(Material.EXP_BOTTLE, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Arrays.asList(ConfigurationService.YELLOW + "Owner: " + ConfigurationService.GRAY + player.getName(), ConfigurationService.YELLOW + "Worth: " + ConfigurationService.GRAY + new DecimalFormat("#.#").format(levels + remainder) + " Levels", ConfigurationService.YELLOW + "Experience: " + ConfigurationService.GRAY + exp));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
