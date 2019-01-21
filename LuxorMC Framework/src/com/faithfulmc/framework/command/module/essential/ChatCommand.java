package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.util.ItemBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

public class ChatCommand extends BaseCommand implements Listener, InventoryHolder{
    private final BasePlugin plugin;
    private final Inventory inventory;
    private final DyeColor[] COLORS = new DyeColor[]{DyeColor.BLUE, DyeColor.CYAN, DyeColor.SILVER, DyeColor.GREEN, DyeColor.WHITE, DyeColor.PINK, DyeColor.RED};
    private final ChatColor[] CHAT_COLORS = new ChatColor[]{ChatColor.BLUE, ChatColor.AQUA, ChatColor.GRAY, ChatColor.GREEN, ChatColor.WHITE, ChatColor.LIGHT_PURPLE, ChatColor.RED};


    public ChatCommand(BasePlugin plugin) {
        super("chat", "Changes your chat color");
        this.plugin = plugin;
        setAliases(new String[]{"c", "chatcolor", "color", "chatcolour", "colour", "chatsettings", "chatsetting"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        inventory = Bukkit.createInventory(this, 9, ChatColor.YELLOW + "Chat Setting");
        for(int i = 0; i < COLORS.length; i++){
            inventory.setItem(i + 1, new ItemBuilder(Material.STAINED_GLASS_PANE).data((short) COLORS[i].getData()).displayName(CHAT_COLORS[i] + ChatColor.BOLD.toString() + WordUtils.capitalize(COLORS[i].name().toLowerCase())).build());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof HumanEntity){
            ((HumanEntity) sender).openInventory(inventory);
        }
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        HumanEntity humanEntity = event.getWhoClicked();
        if(humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            Inventory inventory = event.getClickedInventory();
            InventoryView inventoryView = event.getView();
            if (inventoryView != null && inventoryView.getTopInventory() != null && inventoryView.getTopInventory().getHolder() instanceof ChatCommand) {
                event.setCancelled(true);
                if (inventory != null && inventory.getHolder() instanceof ChatCommand) {
                    int slot = event.getSlot();
                    int i = slot - 1;
                    if (i >= 0 && i < CHAT_COLORS.length) {
                        BaseUser baseUser = plugin.getUserManager().getUser(player.getUniqueId());
                        baseUser.setChatColor(CHAT_COLORS[i]);
                        player.sendMessage(ChatColor.YELLOW + "Your chat color is now " + CHAT_COLORS[i] + ChatColor.BOLD.toString() + WordUtils.capitalize(COLORS[i].name().toLowerCase()));
                    }
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
