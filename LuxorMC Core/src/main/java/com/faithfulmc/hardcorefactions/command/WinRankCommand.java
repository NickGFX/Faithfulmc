package com.faithfulmc.hardcorefactions.command;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.faithfulmc.framework.buycraft.BuycraftFramework;
import com.faithfulmc.framework.buycraft.api.BuycraftException;
import com.faithfulmc.framework.buycraft.api.from.BuycraftCategory;
import com.faithfulmc.framework.buycraft.api.from.BuycraftPackage;
import com.faithfulmc.framework.buycraft.api.to.BuycraftPostResponse;
import com.faithfulmc.framework.buycraft.api.to.ManualPayment;
import com.faithfulmc.framework.buycraft.api.to.PaymentPackage;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.craftbukkit.v1_7_R4.conversations.ConversationTracker;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class WinRankCommand implements CommandExecutor{
    private final HCF plugin;

    public WinRankCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(!(commandSender instanceof ConsoleCommandSender)){
            commandSender.sendMessage(ConfigurationService.RED + "(This command should be executed from console)");
        }
        if(args.length != 2){
            commandSender.sendMessage(ConfigurationService.RED + "(Invalid usage)");
        }
        else {
            String playerName = args[0];
            String newGroup = args[1].toLowerCase();
            Player player = Bukkit.getPlayerExact(playerName);
            boolean groupFound = false;
            if (player == null) {
                commandSender.sendMessage(ConfigurationService.RED + "(Player " + playerName + " not found)");
            } else {
                String[] groups = HCF.getPermission().getPlayerGroups(player);
                for(String group: groups){
                    if(group.equals(newGroup)){
                        groupFound = true;
                    }
                }
                boolean hasGroup = groupFound;
                String group = HCF.getPermission().getPrimaryGroup(player);
                int groupWeight = HCF.getChat().getGroupInfoInteger(player.getWorld(), group, "weight", 0);
                int newGroupWeight = HCF.getChat().getGroupInfoInteger(player.getWorld(), newGroup, "weight", 0);
                player.beginConversation(
                        new ConversationFactory(plugin)
                                .withLocalEcho(false)
                                .withFirstPrompt(new StringPrompt() {
                                    public String getPromptText(ConversationContext conversationContext) {
                                        return ConfigurationService.YELLOW + "Are you sure you wish to continue" + (hasGroup || groupWeight > newGroupWeight ? ", you already have " + ChatColor.GOLD + WordUtils.capitalize(group) + " Rank" + ConfigurationService.YELLOW : "") + "? Type " + ConfigurationService.GREEN + "yes" + ConfigurationService.YELLOW + " to continue or " + ConfigurationService.RED + "no" + ConfigurationService.YELLOW + "to cancel.";
                                    }

                                    public Prompt acceptInput(ConversationContext conversationContext, String input) {
                                        if(input.toLowerCase().contains("y")){
                                            player.sendMessage(ConfigurationService.RED + ChatColor.BOLD.toString() + "Warning: " + ChatColor.GRAY + "Do not logout whilst your rank is processed");
                                            player.sendMessage(ConfigurationService.YELLOW + "Processing your rank, this will automatically register across all servers in a few moments...");
                                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                                BuycraftPackage buycraftPackage = null;
                                                try {
                                                    List<BuycraftCategory> buycraftCategoryList = BuycraftFramework.getCachedCategories();
                                                    for(BuycraftCategory buycraftCategory: buycraftCategoryList){
                                                        if(buycraftCategory.getName().equals("Global Ranks")){
                                                            for(BuycraftCategory subCategory: buycraftCategory.getSubcategories()){
                                                                if(subCategory.getName().equals("Lifetime Ranks")){
                                                                    for(BuycraftPackage otherBuycraftPackage: subCategory.getPackages()){
                                                                        if(otherBuycraftPackage.getName().contains(WordUtils.capitalize(newGroup))){
                                                                            buycraftPackage = otherBuycraftPackage;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (BuycraftException e) {
                                                    e.printStackTrace();
                                                    refund(player, newGroup);
                                                    return;
                                                }
                                                if(buycraftPackage == null){
                                                    refund(player, newGroup);}
                                                else{
                                                    ManualPayment manualPayment = new ManualPayment(playerName, 0.0, Collections.singletonList(new PaymentPackage(buycraftPackage.getId(), Collections.emptyMap())));
                                                    BuycraftPostResponse postResponse;
                                                    try {
                                                        postResponse = BuycraftFramework.addManualPayment(manualPayment);
                                                    } catch (BuycraftException e) {
                                                        e.printStackTrace();
                                                        refund(player, newGroup);
                                                        return;
                                                    }

                                                    if(postResponse.isSuccess()){
                                                        new BukkitRunnable(){
                                                            public void run() {
                                                                Bukkit.broadcastMessage("");
                                                                Bukkit.broadcastMessage(
                                                                        ConfigurationService.GRAY + "[" + ConfigurationService.YELLOW + "*" + ConfigurationService.GRAY + "] " +
                                                                                ConfigurationService.GOLD + ChatColor.BOLD.toString() + playerName + ConfigurationService.YELLOW +
                                                                                " just redeemed the " + ChatColor.AQUA + ChatColor.BOLD.toString() + WordUtils.capitalize(newGroup) + " Rank " +
                                                                                ConfigurationService.YELLOW + "from a " + ConfigurationService.GOLD + ChatColor.BOLD.toString() + "crate" + ConfigurationService.YELLOW + "."
                                                                );
                                                                Bukkit.broadcastMessage(
                                                                        ConfigurationService.GRAY + "[" + ConfigurationService.YELLOW + "*" + ConfigurationService.GRAY + "] " +
                                                                               ConfigurationService.YELLOW + "You can buy crates at " + ConfigurationService.WHITE + ChatColor.BOLD.toString() + ConfigurationService.STORE + ConfigurationService.YELLOW + "."
                                                                );
                                                                Bukkit.broadcastMessage("");
                                                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "perm player " + playerName + " purge");
                                                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setrank " + playerName + " " + newGroup + " donor");
                                                            }
                                                        }.runTask(plugin);
                                                    }
                                                    else{
                                                        refund(player, newGroup);
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            refund(player, newGroup);
                                        }
                                        return Prompt.END_OF_CONVERSATION;
                                    }
                                })
                        .buildConversation(player)
                );
            }
        }
        return true;
    }

    private FieldAccessor conversationTracker = Accessors.getFieldAccessor(MinecraftReflection.getCraftPlayerClass(), "conversationTracker", true);

    public void refund(Player player, String newGroup) {
        if (player.isOnline()) {
            boolean wasOp = player.isOp();
            player.setOp(true);
            Bukkit.dispatchCommand(player, "voucher " + player.getName() + " " + WordUtils.capitalize(newGroup));
            player.setOp(wasOp);
            player.sendMessage(new String[50]);
            player.sendMessage(ChatColor.YELLOW + "Failed you apply your " + ChatColor.GOLD + WordUtils.capitalize(newGroup) + " Rank" + ChatColor.YELLOW + " you have been refunded");
            ((ConversationTracker)conversationTracker.get(player)).abandonAllConversations();
        }
    }
}
