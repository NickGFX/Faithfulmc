package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.util.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SettingsCommand extends BaseCommand implements InventoryHolder, Listener{
    private final BasePlugin plugin;

    public SettingsCommand(BasePlugin plugin) {
        super("settings", "Manage your framework settings");
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ServerParticipator serverParticipator = plugin.getUserManager().getParticipator(sender);
        if(serverParticipator != null) {
            if (serverParticipator instanceof BaseUser) {
                Player player = (Player) sender;
                BaseUser baseUser = (BaseUser) serverParticipator;
                ItemStack[] contents = getContents(player, baseUser);
                Inventory inventory = Bukkit.createInventory(this, contents.length, ChatColor.YELLOW + "Settings");
                inventory.setContents(contents);
                player.openInventory(inventory);
            }
            else{
                sender.sendMessage(ChatColor.RED + "You need to be a player to do this");
            }
        }
        else{
            sender.sendMessage(BaseConstants.YELLOW + "Please wait for your data to load");
        }
        return false;
    }

    public ItemStack[] getContents(Player player, BaseUser baseUser){
        boolean staff = player.hasPermission("base.command.staffchat");
        ItemStack[] itemStacks = new ItemStack[3 * 9];
        itemStacks[(9) + (staff ? 0 : 1)] = new ItemBuilder(Material.INK_SACK, 1, (byte) (baseUser.isMessagesVisible() ? 10 : 8))
                .displayName(ChatColor.YELLOW + "Private Chat Messages")
                .lore(baseUser.isMessagesVisible() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                .build();
        itemStacks[(9) + (staff ? 2 : 4)] = new ItemBuilder(Material.INK_SACK, 1, (byte) (baseUser.isMessagingSounds() ? 10 : 8))
                .displayName(ChatColor.YELLOW + "Private Chat Sounds")
                .lore(baseUser.isMessagingSounds() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                .build();
        if(staff) {
            itemStacks[(9) + (4)] = new ItemBuilder(Material.INK_SACK, 1, (byte) (baseUser.isStaffChatVisible() ? 10 : 8))
                    .displayName(ChatColor.YELLOW + "Staff Chat Messages")
                    .lore(baseUser.isStaffChatVisible() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                    .build();
            itemStacks[(9) + (6)] = new ItemBuilder(Material.INK_SACK, 1, (byte) (baseUser.isInStaffChat() ? 10 : 8))
                    .displayName(ChatColor.YELLOW + "Staff Chat")
                    .lore(baseUser.isInStaffChat() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                    .build();

        }
        itemStacks[(9) + (staff ? 8 : 7)] = new ItemBuilder(Material.INK_SACK, 1, (byte) (baseUser.isGlobalChatVisible() ? 10 : 8))
                .displayName(ChatColor.YELLOW + "Global Chat Messages")
                .lore(baseUser.isGlobalChatVisible() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                .build();
        return itemStacks;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof SettingsCommand){
            event.setCancelled(true);
            if(event.getWhoClicked() instanceof Player && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof SettingsCommand){
                Player player = (Player) event.getWhoClicked();
                BaseUser baseUser = plugin.getUserManager().getUser(player.getUniqueId());
                if(baseUser != null) {
                    int slot = event.getSlot();
                    boolean staff = player.hasPermission("base.command.staffchat");
                    if (slot == (9) + (staff ? 0 : 1)) {
                        Bukkit.dispatchCommand(player, "togglepm");
                    }
                    else if(slot == (9) + (staff ? 2 : 4)){
                        Bukkit.dispatchCommand(player, "sounds");
                    }
                    else if(slot == (9) + (staff ? 8 : 7)){
                        Bukkit.dispatchCommand(player, "toggleglobalchat");
                    }
                    else if(staff){
                        if(slot == (9) + (4)){
                            Bukkit.dispatchCommand(player, "togglestaffchat");
                        }
                        else if(slot == (9) + (6)){
                            baseUser.setInStaffChat(!baseUser.isInStaffChat());
                            player.sendMessage(ChatColor.YELLOW + "Staff chat is now " + (baseUser.isInStaffChat() ? ChatColor.GREEN + "on" : ChatColor.RED + "off"));
                        }
                        else{
                            return;
                        }
                    }
                    else{
                        return;
                    }
                    Inventory inventory = event.getClickedInventory();
                    inventory.setContents(getContents(player, baseUser));
                }
            }
        }
    }

    public Inventory getInventory(){
        return null;
    }
}
