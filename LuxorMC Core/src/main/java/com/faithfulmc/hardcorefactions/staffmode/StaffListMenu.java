package com.faithfulmc.hardcorefactions.staffmode;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.ItemBuilder;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class StaffListMenu implements Runnable, Listener {
    private static final ItemStack BLANK = new ItemBuilder(Material.STAINED_GLASS_PANE).displayName(" ").data(DyeColor.GRAY.getData()).build();
    private static final ItemStack UP = new ItemBuilder(Material.WOOD_BUTTON).displayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "UP").lore(ConfigurationService.GRAY + "Click to go up a page").build();
    private static final ItemStack DOWN = new ItemBuilder(Material.WOOD_BUTTON).displayName(ConfigurationService.RED + ChatColor.BOLD.toString() + "DOWN").lore(ConfigurationService.GRAY + "Click to go down a page").build();

    private final HCF hcf;
    private final List<Inventory> inventoryList = Collections.synchronizedList(new ArrayList<>());
    private List<Player> playerList = Collections.synchronizedList(new ArrayList<>());

    public StaffListMenu(HCF hcf) {
        this.hcf = hcf;
        Bukkit.getPluginManager().registerEvents(this, hcf);
        Bukkit.getScheduler().runTaskTimer(hcf, this, 0, 20);
    }

    public void run() {
        List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("staffmode.use")).collect(Collectors.toList());
        Iterator<Player> playerIterator = onlinePlayers.iterator();
        int size = onlinePlayers.size();
        int rowsNeeded = (size + 8) / 9;
        int inventoriesNeeded = (rowsNeeded + 2) / 3;
        int currentInventorySize = inventoryList.size();
        if (currentInventorySize > inventoriesNeeded) {
            while (inventoryList.size() > inventoriesNeeded) {
                Inventory removed = inventoryList.remove(inventoryList.size() - 1);
                if (inventoryList.isEmpty()) {
                    new ArrayList<>(removed.getViewers()).forEach(HumanEntity::closeInventory);
                } else {
                    Inventory before = inventoryList.get(inventoryList.size() - 1);
                    new ArrayList<>(removed.getViewers()).forEach(viewer -> viewer.openInventory(before));
                }
            }
        } else if (currentInventorySize < inventoriesNeeded) {
            while (inventoryList.size() < inventoriesNeeded) {
                inventoryList.add(Bukkit.createInventory(null, 9 * 5, ConfigurationService.GOLD + "Staff"));
            }
        }
        int inventoryIndex = 0;
        for (Inventory inventory : inventoryList) {
            inventory.clear();
            int slot = 9;
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, BLANK);
                inventory.setItem(i + (4 * 9), BLANK);
            }
            if (inventoryIndex > 0) {
                inventory.setItem(4, UP);
            }
            if (inventoryIndex < inventoriesNeeded - 1) {
                inventory.setItem(4 + (4 * 9), DOWN);
            }
            while (playerIterator.hasNext() && slot < 4 * 9) {
                Player player = playerIterator.next();
                FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
                long playtime = factionUser.getCurrentPlaytime();
                long afktime = BukkitUtils.getIdleTime(player);
                if (player.isOnline()) {
                    ItemBuilder itemBuilder = new ItemBuilder(Material.SKULL_ITEM);
                    itemBuilder.data((short) 3);
                    SkullMeta skullMeta = (SkullMeta) itemBuilder.getStack().getItemMeta();
                    skullMeta.setOwner(player.getName());
                    itemBuilder.setMeta(skullMeta);
                    String rank = ChatColor.translateAlternateColorCodes('&', "&e" + HCF.getChat().getPlayerPrefix(player)).replace("_", " ");
                    itemBuilder.displayName(rank + player.getDisplayName());
                    itemBuilder.lore("", ConfigurationService.WHITE + "IGN: " + ConfigurationService.GRAY + player.getName(), ConfigurationService.WHITE + "PlayTime: " + ConfigurationService.GRAY + DurationFormatUtils.formatDurationWords((int) Math.ceil(playtime / (1000.0 * 60.0)) * (1000 * 60), true, true), ConfigurationService.WHITE + "AFK: " + ConfigurationService.GRAY + ((afktime > 60000) ? DurationFormatUtils.formatDurationWords((int) Math.ceil(afktime / (1000 * 60.0)) * 1000 * 60, true, true) : "No"), "", ConfigurationService.GOLD + "Left Click " + ConfigurationService.DOUBLEARROW + ConfigurationService.GRAY + " Teleport");
                    inventory.setItem(slot, itemBuilder.build());
                    slot++;
                }
            }
            inventoryIndex++;
        }
        this.playerList = onlinePlayers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            Inventory clicked = e.getClickedInventory();
            Inventory top = e.getView().getTopInventory();
            if (top != null && inventoryList.contains(top)) {
                if (!hcf.getStaffModeListener().isStaff(player)) {
                    closeInventory(player);
                    e.setCancelled(true);
                }
                e.setCancelled(true);
                if (clicked != null && top.equals(clicked)) {
                    int inventoryIndex = inventoryList.indexOf(top);
                    int slot = e.getSlot();
                    if (slot == 4 && inventoryIndex > 0) {
                        openInventory(player, inventoryList.get(inventoryIndex - 1));
                    } else if (slot == 4 + (4 * 9) && inventoryIndex < inventoryList.size() - 1) {
                        openInventory(player, inventoryList.get(inventoryIndex + 1));
                    } else if (slot >= 9 && slot < 4 * 9) {
                        int currentPlayer = inventoryIndex * (3 * 9) + slot - 9;
                        if (playerList.size() > currentPlayer) {
                            Player other = playerList.get(currentPlayer);
                            if (other.isOnline()) {
                                if (e.getClick() == ClickType.RIGHT) {
                                    hcf.getStaffModeListener().examin(player, other);
                                } else if (e.getClick() == ClickType.LEFT) {
                                    Bukkit.dispatchCommand(player, "tp " + other.getName());
                                    closeInventory(player);
                                }
                            } else {
                                player.sendMessage(ConfigurationService.RED + "Player is no longer online");
                            }
                        }
                    }
                }
            }
        }
    }

    public void openInventory(Player player, Inventory inventory) {
        new BukkitRunnable() {
            public void run() {
                player.openInventory(inventory);
            }
        }.runTask(hcf);
    }

    public void closeInventory(Player player) {
        new BukkitRunnable() {
            public void run() {
                player.closeInventory();
            }
        }.runTask(hcf);
    }

    public void open(Player player) {
        if (inventoryList.isEmpty()) {
            player.sendMessage(ConfigurationService.RED + "No staff online");
        } else {
            player.openInventory(inventoryList.iterator().next());
        }
    }
}
