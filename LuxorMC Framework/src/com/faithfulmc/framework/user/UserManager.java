package com.faithfulmc.framework.user;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.event.UserLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class UserManager{
    protected final ConsoleUser console;
    protected final BasePlugin plugin;
    protected final ConcurrentMap<UUID, ServerParticipator> onlinePlayers = new ConcurrentHashMap<>();

    public UserManager(final BasePlugin plugin) {
        this.plugin = plugin;
        this.onlinePlayers.put(ConsoleUser.CONSOLE_UUID, this.console = new ConsoleUser());
    }

    public ConcurrentMap<UUID, ServerParticipator> getOnlinePlayers() {
        return onlinePlayers;
    }

    public ConsoleUser getConsole() {
        return this.console;
    }

    public ServerParticipator getParticipator(UUID uuid){
        ServerParticipator serverParticipator = onlinePlayers.get(uuid);
        if(serverParticipator == null){
            if(Thread.currentThread() == BasePlugin.getMainThread()){
                throw new NullPointerException("Cannot establish connection from main thread"); //Stops us from fetching users on the main thread
            }
            serverParticipator = load(uuid);
        }
        return serverParticipator;
    }

    public ServerParticipator getParticipator(CommandSender sender){
        return sender instanceof ConsoleCommandSender ? console : getParticipator(((Player)sender).getUniqueId());
    }

    public BaseUser getUser(UUID uuid){
        ServerParticipator serverParticipator;
        return (serverParticipator = getParticipator(uuid)) == null || !(serverParticipator instanceof BaseUser) ? null : (BaseUser) serverParticipator;
    }

    public void joinTask(Player player){
        UUID uuid = player.getUniqueId();
        onlinePlayers.put(uuid, new BaseUser(player.getUniqueId(), false));
        BasePlugin.mongoService.submit(() -> {
            BaseUser baseUser = load(uuid);
            if(baseUser == null){
                baseUser = new BaseUser(uuid, player.getName());
                save(baseUser);
            }
            if(player.isOnline() && onlinePlayers.containsKey(uuid)) {
                BaseUser old = (BaseUser) onlinePlayers.put(uuid, baseUser);
                if(old != null) {
                    old.merge(baseUser);
                    if(baseUser.isOnline()){
                        baseUser.setLastSeen(System.currentTimeMillis());
                    }
                }
                Bukkit.getPluginManager().callEvent(new UserLoadEvent(baseUser, player));
            }
        }, 5);
    }

    public void quitTask(Player player){
        UUID uuid = player.getUniqueId();
        ServerParticipator serverParticipator = onlinePlayers.remove(uuid);
        if(serverParticipator != null && serverParticipator instanceof BaseUser) {
            BasePlugin.mongoService.submit(() -> {
                save((BaseUser) serverParticipator);
            });
        }
    }

    public abstract void save(BaseUser baseUser);
    public abstract BaseUser load(UUID uuid);
}
