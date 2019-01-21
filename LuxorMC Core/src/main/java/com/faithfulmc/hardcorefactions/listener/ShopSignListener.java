package com.faithfulmc.hardcorefactions.listener;


import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.Crowbar;
import com.faithfulmc.util.InventoryUtils;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;


public class ShopSignListener implements org.bukkit.event.Listener {
    private static final long SIGN_TEXT_REVERT_TICKS = 40L;
    private static final Pattern ALPHANUMERIC_REMOVER = Pattern.compile("[^A-Za-z0-9]");
    private final HCF plugin;


    public ShopSignListener(HCF plugin) {
        this.plugin = plugin;

    }


    @EventHandler(ignoreCancelled = false, priority = org.bukkit.event.EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            if ((state instanceof Sign)) {
                Sign sign = (Sign) state;
                String[] lines = sign.getLines();
                Integer quantity = Ints.tryParse(lines[2]);
                if (quantity == null) {
                    return;
                }

                Integer price = Ints.tryParse(ALPHANUMERIC_REMOVER.matcher(lines[3]).replaceAll(""));
                if (price == null) {
                    return;
                }

                ItemStack stack;
                Kit kit = null;

                if (lines[1].equalsIgnoreCase("Crowbar")) {
                    stack = new Crowbar().getItemIfPresent();
                }
                else if ((stack = BasePlugin.getPlugin().getItemDb().getItem(ALPHANUMERIC_REMOVER.matcher(lines[1]).replaceAll(""), quantity)) == null) {
                    if ((kit = plugin.getKitManager().getKit(lines[1])) == null) {
                        return;
                    }
                }

                Player player = event.getPlayer();
                String[] fakeLines = Arrays.copyOf(sign.getLines(), 4);
                if (((lines[0].contains("Sell")) && (lines[0].contains(ChatColor.RED.toString()))) || (lines[0].contains(ChatColor.AQUA.toString()))) {
                    FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                    if(stack != null) {
                        int sellQuantity = Math.min(quantity, InventoryUtils.countAmount(player.getInventory(), stack.getType(), stack.getDurability()));
                        if (sellQuantity <= 0) {
                            fakeLines[0] = (ChatColor.RED + "Not carrying any");
                            fakeLines[2] = (ChatColor.RED + "on you.");
                            fakeLines[3] = "";
                        } else {
                            int newPrice = price / quantity * sellQuantity;
                            fakeLines[0] = (ChatColor.GREEN + "Sold " + sellQuantity);
                            fakeLines[3] = (ChatColor.GREEN + "for " + '$' + newPrice);
                            factionUser.setBalance(factionUser.getBalance() + newPrice);
                            InventoryUtils.removeItem(player.getInventory(), stack.getType(), (short) stack.getData().getData(), sellQuantity);
                            player.updateInventory();
                        }
                    }

                } else {
                    if ((!lines[0].contains("Buy")) || (!lines[0].contains(ChatColor.GREEN.toString()))) {
                        return;
                    }
                    FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                    if (price > factionUser.getBalance()) {
                        fakeLines[0] = (ChatColor.RED + "Cannot afford");
                    } else {
                        fakeLines[0] = (ChatColor.GREEN + "Item bought");
                        fakeLines[3] = (ChatColor.GREEN + "for " + '$' + price);
                        factionUser.setBalance(factionUser.getBalance() - price);

                        if (stack != null) {
                            World world = player.getWorld();
                            Location location = player.getLocation();
                            Map<Integer, ItemStack> excess = player.getInventory().addItem(stack);
                            for (Map.Entry<Integer, ItemStack> excessItemStack : excess.entrySet()) {
                                world.dropItemNaturally(location, excessItemStack.getValue());
                            }
                            player.updateInventory();
                        } else if (kit != null) {
                            kit.applyTo(player, true, false);
                        }
                    }
                }
                event.setCancelled(true);
                BasePlugin.getPlugin().getSignHandler().showLines(player, sign, fakeLines, SIGN_TEXT_REVERT_TICKS, true);
            }
        }
    }
}