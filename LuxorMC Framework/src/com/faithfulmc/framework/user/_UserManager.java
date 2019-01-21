package com.faithfulmc.framework.user;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.mongodb.morphia.query.Query;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

@Deprecated
public abstract class _UserManager implements Listener {
    private final ConsoleUser console;
    private final BasePlugin plugin;
    private final ConcurrentMap<UUID, ServerParticipator> onlinePlayers = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ServerParticipator> participators = new ConcurrentHashMap<>();
    private Config userConfig;

    public _UserManager(final BasePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        ServerParticipator participator = this.participators.get(ConsoleUser.CONSOLE_UUID);
        if (participator != null) {
            this.console = (ConsoleUser) participator;
        } else {
            this.participators.put(ConsoleUser.CONSOLE_UUID, this.console = new ConsoleUser());
        }
        if (BasePlugin.isMongo()) {
            Query<ServerParticipator> serverParticipators = plugin.getDatastore().find(ServerParticipator.class);
            Iterator<ServerParticipator> iterator = serverParticipators.iterator();
            while (iterator.hasNext()){
                try{
                    ServerParticipator serverParticipator = iterator.next();
                    participators.put(serverParticipator.getUniqueId(), serverParticipator);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        } else {
            userConfig = new Config(plugin, "users");
            for (String uuid : userConfig.getKeys(false)) {
                UUID uid = UUID.fromString(uuid);
                participators.put(uid, (ServerParticipator) userConfig.get(uuid));
            }
        }
    }

    public ConsoleUser getConsole() {
        return this.console;
    }

    public Map<UUID, ServerParticipator> getParticipators() {
        return this.participators;
    }

    public ServerParticipator getParticipator(final CommandSender sender) {
        return (sender instanceof Player) ? getParticipator(((Player) sender).getUniqueId()) : ((sender instanceof ConsoleCommandSender) ? this.console : null);
    }

    public void joinPlayer(Player player) {
        BaseUser baseUser = getUser(player.getUniqueId());
        if(baseUser.getUniqueId() == null){
            baseUser.setUniqueId(player.getUniqueId());
        }
        onlinePlayers.put(baseUser.getUniqueId(), baseUser);
    }

    public void quitPlayer(Player player){
        onlinePlayers.remove(player.getUniqueId());
    }

    public ServerParticipator getParticipator(UUID uuid) {
        ServerParticipator serverParticipator = onlinePlayers.get(uuid);
        if(serverParticipator == null){
            serverParticipator = participators.get(uuid);
        }
        return serverParticipator == null ? insertAndReturn(uuid) : serverParticipator;
    }

    public BaseUser insertAndReturn(UUID uuid){
        plugin.getLogger().log(Level.INFO, "Created new user " + uuid.toString());
        BaseUser serverParticipator;
        participators.put(uuid, serverParticipator = new BaseUser());
        serverParticipator.update();
        return serverParticipator;
    }

    public BaseUser getUser(UUID uuid) {
        return (BaseUser) getParticipator(uuid);
    }

    public void mongoFetch(UUID uuid) {
        ServerParticipator serverParticipator = plugin.getDatastore().find(ServerParticipator.class).field("uniqueId").equal(uuid).get();
        if (serverParticipator != null) {
            ServerParticipator old;
            if((old = participators.put(uuid, serverParticipator)) != null){
                old.merge(serverParticipator);
            }
            if(onlinePlayers.remove(uuid) != null && Bukkit.getPlayer(uuid) != null){
                onlinePlayers.put(uuid, serverParticipator);
            }
        }
    }
}
