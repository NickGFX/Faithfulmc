package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.event.PlayerFreezeEvent;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.chat.ClickAction;
import com.faithfulmc.util.chat.Text;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FreezeCommand extends BaseCommand implements Listener, Runnable, InventoryHolder {

    private static final String FREEZE_BYPASS = "base.freeze.bypass";
    private final TObjectLongMap<UUID> frozenPlayers = new TObjectLongHashMap<>();
    private final Set<UUID> inventoryUnlock = new HashSet<>();
    private long defaultFreezeDuration;
    private static Set<UUID> frozen = new HashSet<>();

    public static boolean isFrozen(UUID uuid){
        return frozen.contains(uuid);
    }

    public FreezeCommand(BasePlugin plugin) {
        super("freeze", "Freezes a player from moving");
        this.setUsage("/(command) (<all|playerName>)/(lock <player>)");
        this.setAliases(new String[]{"ss"});
        this.defaultFreezeDuration = TimeUnit.MINUTES.toMillis(60);
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) plugin);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1, 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getUsage(label));
        }
        else if(args[0].equalsIgnoreCase("lock") && args.length == 2) {
            Player target = Bukkit.getServer().getPlayer(args[1]);
            if (target == null || !BaseCommand.canSee(sender, target)) {
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                return true;
            }
            if (target.equals(sender) && target.hasPermission(FREEZE_BYPASS)) {
                sender.sendMessage(ChatColor.RED + "You cannot unlock yourself.");
                return true;
            }
            if(!frozen.contains(target.getUniqueId())){
                sender.sendMessage(ChatColor.RED + "Target is not frozen");
            }
            else{
                if(inventoryUnlock.add(target.getUniqueId())){
                    sender.sendMessage(BaseConstants.YELLOW + "Inventory lock toggled off for " + target.getName());
                    target.closeInventory();
                }
                else if(inventoryUnlock.remove(target.getUniqueId())){
                    sender.sendMessage(BaseConstants.YELLOW + "Inventory lock toggled on for " + target.getName());
                    target.openInventory(inventory);
                }
            }
        }
        else{
            Long freezeTicks = this.defaultFreezeDuration;
            long millis = System.currentTimeMillis();
            Player target = Bukkit.getServer().getPlayer(args[0]);
            if (target == null || !BaseCommand.canSee(sender, target)) {
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                return true;
            }
            if (target.equals(sender) && target.hasPermission(FREEZE_BYPASS)) {
                sender.sendMessage(ChatColor.RED + "You cannot freeze yourself.");
                return true;
            }
            UUID targetUUID = target.getUniqueId();
            boolean shouldFreeze = this.getRemainingPlayerFrozenMillis(targetUUID) > 0;
            PlayerFreezeEvent playerFreezeEvent = new PlayerFreezeEvent(target, shouldFreeze);
            Bukkit.getServer().getPluginManager().callEvent(playerFreezeEvent);
            if (playerFreezeEvent.isCancelled()) {
                sender.sendMessage(ChatColor.RED + "Unable to freeze " + target.getName() + '.');
                return false;
            }
            if (shouldFreeze) {
                this.frozen.remove(target.getUniqueId());
                this.frozenPlayers.remove(targetUUID);
                inventoryUnlock.remove(targetUUID);
                target.sendMessage(ChatColor.GREEN + "You have been un-frozen.");
                target.updateInventory();
                Command.broadcastCommandMessage(sender, (BaseConstants.YELLOW + target.getName() + " is no longer frozen"));
            } else {
                this.frozen.add(target.getUniqueId());
                this.frozenPlayers.put(targetUUID, millis + freezeTicks);
                String timeString = DurationFormatUtils.formatDurationWords(freezeTicks, true, true);
                Command.broadcastCommandMessage(sender, (BaseConstants.YELLOW + target.getName() + " is now frozen for " + timeString));
            }
        }
        return true;
    }

    private final Inventory inventory = Bukkit.createInventory(this, 9, BaseConstants.YELLOW + "You are frozen");
    private final ItemStack BOOK = new ItemStack(Material.BOOK);

    {
        ItemMeta meta = BOOK.getItemMeta();
        meta.setDisplayName(BaseConstants.GOLD + BaseConstants.NAME + "MC" + ChatColor.DARK_GRAY + " » " + BaseConstants.YELLOW + "You have been frozen");
        meta.setLore(Arrays.asList(
                ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 37),
                BaseConstants.YELLOW + "You have been frozen by a staff member",
                BaseConstants.YELLOW + "  If you disconnect you will be " + ChatColor.DARK_RED + ChatColor.BOLD + "BANNED",
                BaseConstants.YELLOW + "    Please connect to our teamspeak",
                BaseConstants.GRAY + "                   (" + BaseConstants.TEAMSPEAK + ")",
                BaseConstants.YELLOW + "            You have 3 minutes to join",
                ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 37))
        );
        BOOK.setItemMeta(meta);
        inventory.setItem(4, BOOK);
    }

    public Inventory getInventory(){
        return inventory;
    }

    private int i = 0;

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event){
        Player player = (Player)event.getWhoClicked();
        if(event.getWhoClicked() instanceof Player) {
            if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
                event.setCancelled(true);
                return;
            }
        }
        if(!inventoryUnlock.contains(player.getUniqueId())) {
            if (event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() == this) {
                event.setCancelled(true);
            } else if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() == this) {
                event.setCancelled(true);
            }
        }
    }

    public void run() {
        for(UUID uuid: frozen) {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                if (i % (10 * 20) == 0) {
                    player.sendMessage(BaseConstants.GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 37));
                    player.sendMessage(BaseConstants.YELLOW + " You have been frozen by a staff member");
                    player.sendMessage(BaseConstants.YELLOW + "   If you disconnect you will be " + ChatColor.DARK_RED + ChatColor.BOLD + "BANNED");
                    player.sendMessage(BaseConstants.YELLOW + "     Please connect to our teamspeak");
                    player.sendMessage(BaseConstants.GRAY + "                (" + BaseConstants.TEAMSPEAK + ")");
                    player.sendMessage(BaseConstants.YELLOW + "           You have 3 minutes to join");
                    player.sendMessage(BaseConstants.GRAY + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 37));
                }
                if(inventoryUnlock.contains(player.getUniqueId()) && player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null || player.getOpenInventory().getTopInventory().getHolder() != this){
                    player.openInventory(inventory);
                }
            }
        }
        i++;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 ? null : args.length == 2 && args[0].equalsIgnoreCase("lock") ? null : Collections.emptyList();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player attacker = BukkitUtils.getFinalAttacker(event, false);
            if (attacker == null) {
                return;
            }
            Player player = (Player) entity;
            if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
                if (!attacker.hasPermission(FREEZE_BYPASS)) {
                    attacker.sendMessage(ChatColor.RED + player.getName() + " is currently frozen, you may not attack.");
                    event.setCancelled(true);
                }
                return;
            }
            if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(attacker.getUniqueId()) <= 0 || attacker.hasPermission(FREEZE_BYPASS))) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "You may not attack players whilst frozen.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPreCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
            String message = event.getMessage().toLowerCase();
            if (message.startsWith("/reply") || message.startsWith("/msg") || message.startsWith("/r") || message.startsWith("/message") || message.startsWith("/helpop") || message.startsWith("/m")) {
                return;
            }
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You may not use commands whilst frozen.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
                return;
            }
            Player player = event.getPlayer();
            if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
                event.setTo(event.getFrom());
            }
        }
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
            player.sendMessage(ChatColor.RED + "You may not use blocks whilst frozen.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!(this.getRemainingServerFrozenMillis() <= 0 && this.getRemainingPlayerFrozenMillis(player.getUniqueId()) <= 0 || player.hasPermission(FREEZE_BYPASS))) {
            player.sendMessage(ChatColor.RED + "You may not use blocks whilst frozen.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BaseComponent[] components = new ComponentBuilder(player.getName()).color(BaseConstants.fromBukkit(BaseConstants.YELLOW))
                .append(" has ")
                .append("QUIT").color(BaseConstants.fromBukkit(ChatColor.DARK_RED))
                .append(" while frozen ").color(BaseConstants.fromBukkit(BaseConstants.YELLOW))
                .append("(BAN)").color(BaseConstants.fromBukkit(ChatColor.GRAY))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                        ChatColor.GRAY + "Click to ban " + ChatColor.WHITE + player.getName()
                )))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ban -s " + player.getName() + " Disconnected whilst frozen"))
                .create();
        if (frozen.contains(player.getUniqueId())) {
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (!online.hasPermission("base.command.freeze")) {
                    continue;
                }
                online.spigot().sendMessage(components);
                return;
            }
        }
    }

    public long getRemainingServerFrozenMillis() {
        return -1;
    }

    public long getRemainingPlayerFrozenMillis(UUID uuid) {
        long remaining = this.frozenPlayers.get(uuid);
        if (remaining == this.frozenPlayers.getNoEntryValue()) {
            return 0;
        }
        return remaining - System.currentTimeMillis();
    }
}
