package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.listener.NameVerifyListener;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.mongo.MongoUserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NicknameCommand extends BaseCommand{
    private final BasePlugin plugin;

    public NicknameCommand(BasePlugin plugin) {
        super("nickname", "Sets a players nickname");
        setAliases(new String[]{"nick"});
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            BaseUser baseUser = plugin.getUserManager().getUser(player.getUniqueId());
            if(args.length == 0){
                if(baseUser.getNickName() == null){
                    player.sendMessage(ChatColor.YELLOW + "You do not currently have a nickname, to set a nickname use " + ChatColor.GRAY + "/" + command.getName() + " <name/none>");
                }
                else{
                    player.sendMessage(ChatColor.YELLOW + "Your nickname is currently " + ChatColor.WHITE + baseUser.getNickName() + ChatColor.YELLOW + ", to reset your nickname use " + ChatColor.GRAY + "/" + command.getName() + " none");
                }
            }
            else{
                String nickName = args[0];
                if(nickName.equalsIgnoreCase("none")){
                    baseUser.setNickName(null);
                    player.setDisplayName(player.getName());
                    player.sendMessage(ChatColor.YELLOW + "Your nickname has been reset");
                }
                else if(!nickName.equalsIgnoreCase(player.getName()) && (nickName.isEmpty() || nickName.length() > 16 || !NameVerifyListener.NAME_PATTERN.matcher(nickName).matches())){
                    player.sendMessage(ChatColor.YELLOW + "Invalid nickname");
                }
                else{
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        if(!nickName.equalsIgnoreCase(player.getName()) && plugin.getUserManager() instanceof MongoUserManager && ((MongoUserManager) plugin.getUserManager()).exists(nickName)){
                            player.sendMessage(ChatColor.YELLOW + "A player already exists with that name");
                        }
                        else {
                            player.sendMessage(ChatColor.YELLOW + "Your nickname is now " + ChatColor.WHITE + nickName);
                            baseUser.setNickName(nickName);
                            player.setDisplayName(nickName);
                        }
                    });
                }
            }
        }
        return true;
    }
}
