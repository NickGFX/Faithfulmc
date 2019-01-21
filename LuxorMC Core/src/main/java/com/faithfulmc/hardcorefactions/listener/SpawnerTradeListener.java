package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.ItemBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class SpawnerTradeListener implements Listener {
    public static String capitalizeString(String string) {
        string = string.replace("_", " ");
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                found = false;
            }
        } return String.valueOf(chars);
    }

    private final HCF hcf;
    private final String[] lines = {ChatColor.GREEN + "[SpawnerShop]", "", ChatColor.BLACK + "Click to buy", ""};
    private final String[] error = {ChatColor.GREEN + "[SpawnerShop]", ConfigurationService.RED + "Error", "", ""};
    private final ItemStack BLANK = new ItemBuilder(Material.STAINED_GLASS_PANE).data(DyeColor.GRAY.getData()).displayName(" ").build();
    private final ImmutableMap<EntityType, Integer> spawners = new ImmutableMap.Builder<EntityType, Integer>().put(EntityType.SKELETON, ConfigurationService.SPAWNER_PRICE).put(EntityType.SPIDER, ConfigurationService.SPAWNER_PRICE).put(EntityType.ZOMBIE, ConfigurationService.SPAWNER_PRICE).put(EntityType.CAVE_SPIDER, ConfigurationService.SPAWNER_PRICE).build();

    private final ImmutableMap<EntityType, Integer> SPAWNERITEMS = new ImmutableMap.Builder<EntityType, Integer>().put(EntityType.SKELETON, (1 * 9) + 1).put(EntityType.SPIDER, (1 * 9) + 3).put(EntityType.ZOMBIE, (1 * 9) + 5).put(EntityType.CAVE_SPIDER, (1 * 9) + 7).build();
    private Inventory inventory;

    public SpawnerTradeListener(HCF hcf) {
        this.hcf = hcf;
        inventory = Bukkit.createInventory(null, 3 * 9, ChatColor.GREEN + "Spawner Shop");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, BLANK);
        }
        for (Map.Entry<EntityType, Integer> entry : spawners.entrySet()) {
            EntityType entityType = entry.getKey();
            int value = entry.getValue();
            String name = ConfigurationService.YELLOW + capitalizeString(entityType.name());
            String costline = ConfigurationService.WHITE + "Cost: " + ConfigurationService.GRAY + value;
            ItemStack itemStack = new ItemBuilder(Material.MOB_SPAWNER).data(entityType.getTypeId()).displayName(name).loreLine(costline).build();
            Integer slot = SPAWNERITEMS.get(entityType);
            if (slot != null) {
                inventory.setItem(slot, itemStack);
            }
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent e) {
        if (e.getLine(0).equals("[SpawnerShop]")) {
            Player player = e.getPlayer();
            if (player.hasPermission("spawnershop.create")) {
                for (int i = 0; i < lines.length; i++) {
                    e.setLine(i, lines[i]);
                }
            } else {
                for (int i = 0; i < error.length; i++) {
                    e.setLine(i, error[i]);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (e.useInteractedBlock() == Event.Result.ALLOW && block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            for (int i = 0; i < lines.length; i++) {
                if (!sign.getLine(i).equals(lines[i])) {
                    return;
                }
            }
            player.openInventory(inventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player && e.getView() != null && e.getView().getTopInventory() != null && e.getView().getTopInventory().equals(inventory)) {
            e.setCancelled(true);
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(inventory)) {
                Player player = (Player) e.getWhoClicked();
                int slot = e.getSlot();
                EntityType entityType = null;
                for (Map.Entry<EntityType, Integer> entry : SPAWNERITEMS.entrySet()) {
                    if (entry.getValue() == slot) {
                        entityType = entry.getKey();
                        break;
                    }
                }
                if (entityType != null) {
                    int cost = spawners.get(entityType);
                    FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
                    int balance = factionUser.getBalance();
                    if (cost > balance) {
                        player.sendMessage(ConfigurationService.RED + "You can't afford this!");
                        player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1f, 1f);
                    } else {
                        factionUser.setBalance(balance - cost);
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                        player.sendMessage(ConfigurationService.RED + "Successful purchase!");
                        ItemStack stack = new ItemBuilder(Material.MOB_SPAWNER)
                                .displayName(ChatColor.GREEN + "Spawner")
                                .data(entityType.getTypeId())
                                .loreLine(ConfigurationService.WHITE + WordUtils.capitalizeFully(entityType.name())).build();
                        for (ItemStack itemStack : player.getInventory().addItem(stack).values()) {
                            player.getWorld().dropItem(player.getLocation(), itemStack);
                        }
                    }
                }
            }
        }
    }

    public void closeInventory(Player player) {
        new BukkitRunnable() {
            public void run() {
                player.closeInventory();
            }
        }.runTask(hcf);
    }
}
