package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.GodKit;
import com.faithfulmc.hardcorefactions.kit.Kit;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.Config;
import com.faithfulmc.util.GenericUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class GodKitsCommand implements CommandExecutor, InventoryHolder, Listener{
    private final HCF plugin;
    private List<GodKit> godKitList = new ArrayList<>();
    private Map<Integer, GodKit> godKitsBySlot = new HashMap<>();
    private int inventorySize;
    private String inventoryTitle;
    private List<String> loreLines;

    public GodKitsCommand(HCF plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Config config = new Config(plugin, "godkits");
        inventoryTitle = HCF.c(config.getString("inventoryTitle", ChatColor.RED + "God Kits"));
        loreLines = GenericUtils.createList(config.get("loreLines", Collections.emptyList()), String.class).stream().map(HCF::c).collect(Collectors.toList());
        inventorySize = config.getInt("inventorySize", 5 * 9);
        if(config.contains("kits")) {
            for (String key : config.getConfigurationSection("kits").getKeys(false)) {
                String display = HCF.c(config.getString("kits." + key + ".display"));
                int slot = config.getInt("kits." + key + ".slot");
                Material type = Material.getMaterial(config.getString("kits." + key + ".type").toUpperCase());
                short data = (short) config.getInt("kits." + key + ".data");
                GodKit godKit = new GodKit(key, display, slot, type, data);
                godKitList.add(godKit);
                godKitsBySlot.put(slot, godKit);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            Inventory inventory = createInventory(player);
            player.openInventory(inventory);
        }
        else{
            sender.sendMessage(ChatColor.RED + "You need to be a player to do this");
        }
        return true;
    }

    public Inventory getInventory(){
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            InventoryView inventoryView = event.getView();
            if (inventoryView != null) {
                if (inventoryView.getTopInventory() != null && inventoryView.getTopInventory().getHolder() instanceof GodKitsCommand) {
                    event.setCancelled(true);
                }
                if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GodKitsCommand) {
                    int slot = event.getSlot();
                    GodKit godKit = godKitsBySlot.get(slot);
                    if (godKit != null) {
                        Kit kit = godKit.getKit();
                        if(kit != null && kit.isEnabled()){
                            if(player.hasPermission(kit.getPermissionNode())){
                                FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                                long cooldown = factionUser.getRemainingKitCooldown(kit);
                                if(cooldown > 0){
                                    player.sendMessage(ChatColor.YELLOW + "You may not use this kit for " + ChatColor.WHITE + DurationFormatUtils.formatDurationWords(cooldown, true, true));
                                }
                                else{
                                    if(kit.applyTo(player, false, false)){
                                        player.sendMessage(ChatColor.YELLOW + "Successfully redeemed the god kit " + ChatColor.GOLD + ChatColor.stripColor(godKit.getDisplay()));
                                        event.setCurrentItem(null);
                                        event.getClickedInventory().setContents(getContents(player, factionUser));
                                    }
                                }
                            }
                            else{
                                player.sendMessage(ChatColor.RED + "You do not have permission for this kit");
                            }
                        }
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    public ItemStack[] getContents(Player player, FactionUser factionUser){
        ItemStack[] contents = new ItemStack[inventorySize];
        for(GodKit godKit: godKitList){
            contents[godKit.getSlot()] =  godKit.createItem(player, factionUser, loreLines);
        }
        return contents;
    }

    public Inventory createInventory(Player player){
        Inventory inventory = Bukkit.createInventory(this,  inventorySize, inventoryTitle);
        inventory.setContents(getContents(player, plugin.getUserManager().getUser(player.getUniqueId())));
        return inventory;
    }
}
