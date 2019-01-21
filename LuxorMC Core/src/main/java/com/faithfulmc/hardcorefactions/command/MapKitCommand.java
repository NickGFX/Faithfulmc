package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;

public class MapKitCommand implements CommandExecutor, TabCompleter, Listener, InventoryHolder {
    private final ItemStack BLANK = new ItemBuilder(Material.STAINED_GLASS_PANE).data(DyeColor.GRAY.getData()).displayName("").build();
    private final ItemStack LIGHTER = new ItemBuilder(Material.STAINED_GLASS_PANE).data(DyeColor.WHITE.getData()).displayName("").build();
    private Inventory inventory;

    public MapKitCommand(HCF plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        inventory = Bukkit.createInventory(this, 9 * 6, ConfigurationService.GOLD + ChatColor.BOLD.toString() + ConfigurationService.MAP_TITLE + (ConfigurationService.KIT_MAP ? "" : " " + ConfigurationService.RED + "Map " + ConfigurationService.MAP_NUMBER));
        update();
    }

    public void update() {
        inventory.setContents(fetchContents());
    }

    private ItemStack[] fetchContents() {
        ItemStack[] contents = new ItemStack[inventory.getSize()];
        Arrays.fill(contents, BLANK);
        Arrays.fill(contents, (9 * 1) + 2, (9 * 1) + 7, LIGHTER);
        Arrays.fill(contents, (9 * 2) + 2, (9 * 2) + 7, LIGHTER);
        Arrays.fill(contents, (9 * 3) + 2, (9 * 3) + 7, LIGHTER);
        Arrays.fill(contents, (9 * 4) + 2, (9 * 4) + 7, LIGHTER);
        int protection = ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_ENVIRONMENTAL.getMaxLevel());
        int unbreaking = ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.DURABILITY, Enchantment.DURABILITY.getMaxLevel());
        int sharpness = ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ALL.getMaxLevel());
        int looting = ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.LOOT_BONUS_MOBS, Enchantment.LOOT_BONUS_MOBS.getMaxLevel());
        int power = ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.ARROW_DAMAGE, Enchantment.ARROW_DAMAGE.getMaxLevel());
        int flame = ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(Enchantment.ARROW_FIRE, Enchantment.ARROW_FIRE.getMaxLevel());
        ItemStack HELMET = new ItemBuilder(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection).enchant(Enchantment.DURABILITY, unbreaking).build();
        ItemStack CHESTPLATE = new ItemBuilder(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection).enchant(Enchantment.DURABILITY, unbreaking).build();
        ItemStack LEGGINGS = new ItemBuilder(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection).enchant(Enchantment.DURABILITY, unbreaking).build();
        ItemStack BOOTS = new ItemBuilder(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, protection).enchant(Enchantment.DURABILITY, unbreaking).build();
        ItemStack SWORD = new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, sharpness).enchant(Enchantment.LOOT_BONUS_MOBS, looting).enchant(Enchantment.DURABILITY, unbreaking).build();
        ItemStack BOW = new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE, power).enchant(Enchantment.ARROW_FIRE, flame).enchant(Enchantment.DURABILITY, unbreaking).build();
        int posion = ConfigurationService.POTION_LIMITS.getOrDefault(PotionType.POISON, 1);
        ItemStack POSION = new ItemBuilder(new Potion(PotionType.POISON, posion, true, false).toItemStack(1)).lore(ConfigurationService.SCOREBOARD_COLOR + "Tier " + ConfigurationService.RED + posion, ConfigurationService.SCOREBOARD_COLOR + "Longer Duration " + ChatColor.RED + "Disabled").build();
        int invisibility = ConfigurationService.POTION_LIMITS.getOrDefault(PotionType.INVISIBILITY, 1);
        ItemStack INVIS = new ItemBuilder(new Potion(PotionType.INVISIBILITY, Math.max(invisibility, 1), false, true).toItemStack(1)).lore(invisibility > 0 ? ConfigurationService.SCOREBOARD_COLOR + "Longer Duration " + ChatColor.GREEN + "Enabled" : ConfigurationService.SCOREBOARD_COLOR + "Invisibility " + ChatColor.RED + "Disabled").build();
        contents[(9 * 1) + 4] = HELMET.clone();
        contents[(9 * 2) + 3] = POSION.clone();
        contents[(9 * 2) + 4] = CHESTPLATE.clone();
        contents[(9 * 2) + 5] = SWORD.clone();
        contents[(9 * 3) + 3] = INVIS.clone();
        contents[(9 * 3) + 4] = LEGGINGS.clone();
        contents[(9 * 3) + 5] = BOW.clone();
        contents[(9 * 4) + 4] = BOOTS.clone();
        return contents;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        ((Player) sender).openInventory(inventory);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (view != null && view.getTopInventory() != null && view.getTopInventory().getHolder() == this) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onKitMap(PlayerCommandPreprocessEvent e) {
        if (ConfigurationService.KIT_MAP && e.getMessage().startsWith("/kitmap")) {
            e.setMessage(e.getMessage().replace("kitmap", "mapkit"));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (!inventory.getViewers().isEmpty()) {
            HashSet<?> viewers = new HashSet<>(inventory.getViewers());
            Iterator<?> var5 = viewers.iterator();
            while (var5.hasNext()) {
                HumanEntity viewer = (HumanEntity) var5.next();
                viewer.closeInventory();
            }
        }
    }
}
