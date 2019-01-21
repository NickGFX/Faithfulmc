package com.faithfulmc.hardcorefactions.kit;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.event.KitRenameEvent;
import com.faithfulmc.util.Config;
import com.faithfulmc.util.GenericUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;

import java.util.*;

public class FlatFileKitManager implements KitManager, Listener {
    private static final int INV_WIDTH = 9;
    private final Map<String, Kit> kitNameMap;
    private final Map<UUID, Kit> kitUUIDMap;
    private final HCF plugin;
    private Config config;
    private List<Kit> kits;

    public FlatFileKitManager(HCF plugin) {
        this.plugin = plugin;
        kitNameMap = new CaseInsensitiveMap<>();
        kitUUIDMap = new HashMap<>();
        kits = new ArrayList<>();
        reloadKitData();
        Bukkit.getPluginManager().registerEvents(this, (Plugin) plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onKitRename(final KitRenameEvent event) {
        kitNameMap.remove(event.getOldName());
        kitNameMap.put(event.getNewName(), event.getKit());
    }

    public List<Kit> getKits() {
        return this.kits;
    }

    public Kit getKit(final UUID uuid) {
        return this.kitUUIDMap.get(uuid);
    }

    public Kit getKit(final String id) {
        return this.kitNameMap.get(id);
    }

    public boolean containsKit(final Kit kit) {
        return this.kits.contains(kit);
    }

    public void createKit(final Kit kit) {
        if (this.kits.add(kit)) {
            this.kitNameMap.put(kit.getName(), kit);
            this.kitUUIDMap.put(kit.getUniqueID(), kit);
        }
    }

    public void removeKit(final Kit kit) {
        if (kits.remove(kit)) {
            kitNameMap.remove(kit.getName());
            kitUUIDMap.remove(kit.getUniqueID());
        }
    }

    public Inventory getGui(final Player player) {
        final UUID uuid = player.getUniqueId();
        final Inventory inventory = Bukkit.createInventory((InventoryHolder) player, (this.kits.size() + 9 - 1) / 9 * 9, ChatColor.BLUE + "Kit Selector");
        for (Kit kit : kits) {
            ItemStack stack = kit.getImage();
            String description = kit.getDescription();
            String kitPermission = kit.getPermissionNode();
            List<String> lore;
            if (kitPermission != null && !player.hasPermission(kitPermission)) {
                lore = Lists.newArrayList(ConfigurationService.RED + "You do not own this kit.");
            } else {
                lore = new ArrayList<>();
                if (kit.isEnabled()) {
                    if (kit.getDelayMillis() > 0L) {
                        lore.add(ConfigurationService.YELLOW + kit.getDelayWords() + " cooldown");
                    }
                } else {
                    lore.add(ChatColor.RED + "Disabled");
                }
                final int cloned = kit.getMaximumUses();
                if (cloned != Integer.MAX_VALUE) {
                    lore.add(ConfigurationService.YELLOW + "Used " + this.plugin.getUserManager().getUser(uuid).getUses(kit) + '/' + cloned + " times.");
                }
                if (description != null) {
                    lore.add(" ");
                    for (final String part : ChatPaginator.wordWrap(description, 24)) {
                        lore.add(ConfigurationService.WHITE + part);
                    }
                }
            }
            final ItemStack var7 = stack.clone();
            final ItemMeta var8 = var7.getItemMeta();
            var8.setDisplayName(ChatColor.GREEN + kit.getName());
            var8.setLore((List) lore);
            var7.setItemMeta(var8);
            inventory.addItem(new ItemStack[]{var7});
        }
        return inventory;
    }

    public void reloadKitData() {
        config = new Config(plugin, "kits");
        final Object object = config.get("kits");
        if (object instanceof List) {
            kits = GenericUtils.createList(object, Kit.class);
            for (final Kit kit : kits) {
                kitNameMap.put(kit.getName(), kit);
                kitUUIDMap.put(kit.getUniqueID(), kit);
            }
        }
    }

    public void saveKitData() {
        config.set("kits", (Object) new LinkedList<>(this.kits));
        config.save();
    }
}
