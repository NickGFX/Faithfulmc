package com.faithfulmc.hardcorefactions.staffmode;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.logger.CombatLogListener;
import com.faithfulmc.hardcorefactions.staffmode.StaffListMenu;
import com.faithfulmc.util.ItemBuilder;
import com.faithfulmc.util.player.PlayerCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class StaffModeListener implements Listener {
    private static final ItemStack RANDOM_TP = new ItemBuilder(Material.WATCH).displayName(ConfigurationService.GOLD + "Random TP").lore(ConfigurationService.GRAY + "Right click to random tp").build();
    private static final ItemStack EXAMIN = new ItemBuilder(Material.BOOK).displayName(ConfigurationService.GOLD + "Examine Player").lore(ConfigurationService.GRAY + "Right click to examine a player").build();
    private static final ItemStack MINER_TP = new ItemBuilder(Material.BEACON).displayName(ConfigurationService.GOLD + "Miner TP").lore(ConfigurationService.GRAY + "Right click to tp to a miner").build();
    private static final ItemStack BLANK = new ItemBuilder(Material.STAINED_GLASS_PANE).displayName(" ").data(DyeColor.GRAY.getData()).build();
    private static final ItemStack ALTS = new ItemBuilder(Material.PAPER).displayName(ChatColor.GREEN + "Alts").build();
    private static final ItemStack TP = new ItemBuilder(Material.WATCH).displayName(ChatColor.BLUE + "Teleport").build();
    private static final ItemStack FREEZE = new ItemBuilder(Material.PACKED_ICE).displayName(ConfigurationService.GOLD + "Freeze Player").lore(ConfigurationService.GRAY + "Right click to freeze a player").build();
    private static final ItemStack LIST = new ItemBuilder(Material.NETHER_STAR).displayName(ConfigurationService.GOLD + "Online Staff").lore(ConfigurationService.GRAY + "Right click to view online staff").build();
    private static final ItemStack THRU = new ItemBuilder(Material.COMPASS).displayName(ConfigurationService.GOLD + "Phase Compass").lore(ConfigurationService.GRAY + "Right click to go through blocks").build();
    private final HCF hcf;
    private final BasePlugin basePlugin;
    private Set<Player> staffMode = new HashSet<>();
    private Map<Player, PlayerCache> playerCacheMap = new HashMap<>();
    private Map<Player, Player> examinMap = new HashMap<>();
    private String name = ConfigurationService.GOLD + "Staff Mode";
    private StaffListMenu staffModePlayers;

    public StaffModeListener(HCF hcf) {
        this.hcf = hcf;
        this.basePlugin = BasePlugin.getPlugin();
        staffModePlayers = new StaffListMenu(hcf);
    }

    public void onEnable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("mod-mode")) {
                if (player.hasPermission("staffmode.use")) {
                    enableStaff(player, true);
                }
                player.removeMetadata("mod-mode", hcf);
            }
        }
    }

    public void onDisable() {
        for (Player player : new HashSet<>(staffMode)) {
            player.setMetadata("mod-mode", new FixedMetadataValue(hcf, "mod-mode"));
            disableStaff(player, true);
        }
    }

    public void examin(Player staff, Player target) {
        if (staffMode.contains(target)) {
            staff.sendMessage(ConfigurationService.GOLD + target.getName() + ConfigurationService.YELLOW + " is in " + name);
            staff.closeInventory();
            return;
        }
        examinMap.put(staff, target);
        String targetName = target.getName();
        if (targetName.length() > 12) {
            targetName = targetName.substring(0, 12);
        }
        Inventory inventory = Bukkit.createInventory(null, 6 * 9, ConfigurationService.GOLD + "Examine " + ConfigurationService.YELLOW + targetName);
        ItemStack[] content = inventory.getContents();
        System.arraycopy(target.getInventory().getArmorContents(), 0, content, 2, target.getInventory().getArmorContents().length);
        System.arraycopy(target.getInventory().getContents(), 0, content, 9, target.getInventory().getContents().length);
        content[0] = BLANK.clone();
        content[1] = BLANK.clone();
        content[6] = BLANK.clone();
        content[7] = BLANK.clone();
        content[8] = BLANK.clone();
        for (int i = 5 * 9; i < 6 * 9; i++) {
            content[i] = BLANK.clone();
        }
        content[(5 * 9) + 1] = ALTS.clone();
        content[(5 * 9) + 7] = TP.clone();
        content[(5 * 9) + 3] = new ItemBuilder(Material.COOKED_BEEF, target.getFoodLevel()).displayName(ConfigurationService.YELLOW + "Hunger").lore(ConfigurationService.GRAY.toString() + target.getFoodLevel() + "/" + ChatColor.GREEN + "20").build();
        content[(5 * 9) + 5] = new ItemBuilder(Material.SPECKLED_MELON, (int) target.getHealth()).displayName(ConfigurationService.RED + "Health").lore(ConfigurationService.GRAY.toString() + target.getHealth() + "/" + ChatColor.GREEN.toString() + target.getMaxHealth()).build();
        inventory.setContents(content);
        staff.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("staffmode.use")) {
            enableStaff(player, false);
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (staffMode.contains(event.getPlayer()) && event.getNewGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (staffMode.contains(player)) {
            disableStaff(player, true);
            CombatLogListener.addSafeDisconnect(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && staffMode.contains(e.getDamager())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (staffMode.contains(player)) {
                event.setCancelled(true);
                if (event.getClickedInventory() == event.getView().getTopInventory()) {
                    Inventory clicked = event.getClickedInventory();
                    if (clicked != null && clicked.getName().startsWith(ConfigurationService.GOLD + "Examine " + ConfigurationService.YELLOW)) {
                        Player examining = examinMap.get(player);
                        int slot = event.getSlot();
                        if (slot == (5 * 9) + 1) {
                            Bukkit.dispatchCommand(player, "alts " + examining.getName());
                            closeInventory(player);
                        } else if (slot == (5 * 9) + 7) {
                            Bukkit.dispatchCommand(player, "tp " + examining.getName());
                            closeInventory(player);
                        }
                    }
                }
            }
        }
    }

    public List<Player> filter(List<Player> notStaff) {
        return notStaff.stream().filter(player -> !staffMode.contains(player)).collect(Collectors.toList());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractAt(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        if (staffMode.contains(player) && e.getRightClicked() instanceof Player) {
            ItemStack itemStack = e.getPlayer().getItemInHand();
            e.setCancelled(true);
            if (itemStack != null) {
                if (itemStack.isSimilar(EXAMIN)) {
                    examin(player, (Player) e.getRightClicked());
                } else if (itemStack.isSimilar(FREEZE)) {
                    Bukkit.dispatchCommand(player, "freeze " + ((Player) e.getRightClicked()).getName());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (isStaff(player)) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK && staffMode.contains(player)) {
                ItemStack item = e.getItem();
                if (item != null) {
                    if (item.isSimilar(RANDOM_TP)) {
                        e.setCancelled(true);
                        List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream().filter(other -> !staffMode.contains(other)).collect(Collectors.toList());
                        Player tp;
                        if (onlinePlayers.isEmpty()) {
                            player.sendMessage(ConfigurationService.RED + "No players online");
                            return;
                        } else if (onlinePlayers.size() == 1) {
                            tp = onlinePlayers.iterator().next();
                        } else {
                            int randInt = ThreadLocalRandom.current().nextInt(onlinePlayers.size());
                            tp = onlinePlayers.get(randInt);
                        }
                        Bukkit.dispatchCommand(player, "teleport " + tp.getName());
                    } else if (item.isSimilar(MINER_TP)) {
                        e.setCancelled(true);
                        List<Player> worldPlayers = player.getWorld().getPlayers().stream().filter(other -> !staffMode.contains(other) && other.getLocation().getY() < 45.0).collect(Collectors.toList());
                        Player tp;
                        if (worldPlayers.isEmpty()) {
                            player.sendMessage(ConfigurationService.RED + "No miners online");
                            return;
                        } else if (worldPlayers.size() == 1) {
                            tp = worldPlayers.iterator().next();
                        } else {
                            int randInt = ThreadLocalRandom.current().nextInt(worldPlayers.size());
                            tp = worldPlayers.get(randInt);
                        }
                        Bukkit.dispatchCommand(player, "teleport " + tp.getName());
                    } else if (item.isSimilar(LIST)) {
                        e.setCancelled(true);
                        staffModePlayers.open(player);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        if (staffMode.contains(player)) {
            disableStaff(player, true);
            enableStaff(player, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerDropItemEvent e) {
        if (staffMode.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(BlockPlaceEvent e) {
        if (staffMode.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerPickupItemEvent e) {
        if (staffMode.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(BlockBreakEvent e) {
        if (staffMode.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(BlockDamageEvent e) {
        if (staffMode.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player && staffMode.contains(e.getPlayer()) && e.getInventory().getName().startsWith(ConfigurationService.GOLD + "Examine " + ConfigurationService.YELLOW)) {
            examinMap.remove(e.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClickOther(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player && examinMap.containsValue(e.getWhoClicked())) {
            List<Player> staffPlayers = new ArrayList<>();
            for (Map.Entry<Player, Player> entry : examinMap.entrySet()) {
                if (entry.getValue() == e.getWhoClicked()) {
                    staffPlayers.add(entry.getKey());
                }
            }
            for (Player staff : staffPlayers) {
                if (staff.getOpenInventory() != null && staff.getOpenInventory().getTopInventory() != null && staff.getOpenInventory().getTopInventory().getName().startsWith(ConfigurationService.GOLD + "Examine " + ConfigurationService.YELLOW)) {
                    Inventory inventory = staff.getOpenInventory().getTopInventory();
                    Player target = (Player) e.getWhoClicked();
                    new BukkitRunnable() {
                        public void run() {
                            ItemStack[] content = inventory.getContents();
                            System.arraycopy(target.getInventory().getArmorContents(), 0, content, 2, target.getInventory().getArmorContents().length);
                            System.arraycopy(target.getInventory().getContents(), 0, content, 9, target.getInventory().getContents().length);
                            inventory.setContents(content);
                        }
                    }.runTask(hcf);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryDragOther(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player && examinMap.containsValue(e.getWhoClicked())) {
            List<Player> staffPlayers = new ArrayList<>();
            for (Map.Entry<Player, Player> entry : examinMap.entrySet()) {
                if (entry.getValue() == e.getWhoClicked()) {
                    staffPlayers.add(entry.getKey());
                }
            }
            for (Player staff : staffPlayers) {
                if (staff.getOpenInventory() != null && staff.getOpenInventory().getTopInventory() != null && staff.getOpenInventory().getTopInventory().getName().startsWith(ConfigurationService.GOLD + "Examine " + ConfigurationService.YELLOW)) {
                    Inventory inventory = staff.getOpenInventory().getTopInventory();
                    Player target = (Player) e.getWhoClicked();
                    new BukkitRunnable() {
                        public void run() {
                            ItemStack[] content = inventory.getContents();
                            System.arraycopy(target.getInventory().getArmorContents(), 0, content, 2, target.getInventory().getArmorContents().length);
                            System.arraycopy(target.getInventory().getContents(), 0, content, 9, target.getInventory().getContents().length);
                            inventory.setContents(content);
                        }
                    }.runTask(hcf);
                }
            }
        }
    }

    public void disableStaff(Player player, boolean silent) {
        Location location = player.getLocation();
        player.setCanPickupItems(true);
        if (staffMode.remove(player)) {
            PlayerCache playerCache = playerCacheMap.remove(player);
            if (playerCache != null) {
                playerCache.location = player.getLocation();
                playerCache.apply(player);
            }
        }
        if(!player.hasPermission("base.command.gamemode")) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        player.spigot().setViewDistance(Bukkit.getViewDistance());
        player.spigot().setAffectsSpawning(true);
        player.spigot().setCollidesWithEntities(true);
        if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().getName().startsWith(ConfigurationService.GOLD + "Examine " + ConfigurationService.YELLOW)) {
            player.closeInventory();
        }
        BaseUser baseUser = basePlugin.getUserManager().getUser(player.getUniqueId());
        baseUser.setVanished(false);
        player.teleport(location);
        if (!silent) {
            player.sendMessage(ConfigurationService.YELLOW + "Your " + name + ConfigurationService.YELLOW + " has been " + ChatColor.RED + "disabled");
        }
    }

    public void enableStaff(Player player, boolean silent) {
        staffMode.add(player);
        playerCacheMap.put(player, new PlayerCache(player));
        BaseUser baseUser = basePlugin.getUserManager().getUser(player.getUniqueId());
        baseUser.setVanished(true);
        player.setGameMode(GameMode.CREATIVE);
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
        player.getInventory().clear();
        player.spigot().setAffectsSpawning(false);
        player.spigot().setCollidesWithEntities(false);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.getInventory().setItem(0, RANDOM_TP);
        player.getInventory().setItem(1, MINER_TP);
        player.getInventory().setItem(2, FREEZE);
        player.getInventory().setItem(4, LIST);
        player.getInventory().setItem(7, THRU);
        player.getInventory().setItem(8, EXAMIN);
        player.updateInventory();
        if (!silent) {
            player.sendMessage(ConfigurationService.YELLOW + "Your " + name + ConfigurationService.YELLOW + " has been " + ChatColor.GREEN + "enabled");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (isStaff(player)) {
            disableStaff(player, true);
            e.getDrops().clear();
            if (!e.getKeepInventory()) {
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    e.getDrops().add(itemStack);
                }
                for (ItemStack itemStack : player.getInventory().getArmorContents()) {
                    e.getDrops().add(itemStack);
                }
            }
        }
    }

    public void toggleStaff(Player player) {
        if (staffMode.contains(player)) {
            disableStaff(player, false);
        } else {
            enableStaff(player, false);
        }
    }

    public boolean isStaff(Player player) {
        return staffMode.contains(player);
    }

    public void closeInventory(Player player) {
        new BukkitRunnable() {
            public void run() {
                player.closeInventory();
            }
        }.runTask(hcf);
    }

    public String getName() {
        return name;
    }

    public StaffListMenu getStaffModePlayers() {
        return staffModePlayers;
    }

    public Set<Player> getStaffMode() {
        return staffMode;
    }

    public Map<Player, PlayerCache> getPlayerCacheMap() {
        return playerCacheMap;
    }

    public Map<Player, Player> getExaminMap() {
        return examinMap;
    }
}
