package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ServerParticipator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RealNameCommand extends BaseCommand{
    private final BasePlugin plugin;

    public RealNameCommand(BasePlugin plugin) {
        super("realname", "Shows the real name of a nicknamed player");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0){
            sender.sendMessage(ChatColor.YELLOW + "Invalid usage, " + ChatColor.GRAY + "/" + command.getName() + " <nickname>");
        }
        else{
            List<BaseUser> userList = new ArrayList<>();
            String nickName = args[0];
            for(ServerParticipator serverParticipator: plugin.getUserManager().getOnlinePlayers().values()){
                if(serverParticipator instanceof BaseUser){
                    BaseUser baseUser = (BaseUser) serverParticipator;
                    if(baseUser.getNickName() != null && baseUser.getNickName().equalsIgnoreCase(nickName)){
                        userList.add(baseUser);
                    }
                }
            }
            if(userList.isEmpty()){
                sender.sendMessage(ChatColor.YELLOW + "There are no players currently online with that nickname");
            }
            else{
                sender.sendMessage(ChatColor.YELLOW + "There are " + userList.size() + " players online with that nickname: " + ChatColor.WHITE + userList.stream().map(BaseUser::getName).collect(Collectors.joining(", ")));
            }
        }
        return true;
    }
}
