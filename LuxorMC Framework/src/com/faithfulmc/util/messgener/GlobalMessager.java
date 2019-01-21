package com.faithfulmc.util.messgener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class GlobalMessager implements PluginMessageListener, Listener {
    private final Plugin plugin;
    private String id = null;

    public GlobalMessager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "FaithfulMessage", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startup();
    }

    public void broadcastToOtherServers(Player sender, BaseComponent[] messages, String permission){
        if(sender == null){
            return;
        }
        /*
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("broadcast");
        dataOutput.writeUTF(id == null ? "None" : id);
        dataOutput.writeUTF(permission);
        dataOutput.writeUTF(ComponentSerializer.toString(messages));
        dataOutput.writeLong(System.currentTimeMillis());
        sender.sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());
        */
    }

    public void broadcastToOtherServers(Player sender, String message, String permission){
        broadcastToOtherServers(sender, TextComponent.fromLegacyText(message), permission);
    }

    public void forceCommandOnOtherServer(UUID uuid, String server, String command){
        /*
        Player sender = getRandomPlayer();
        if(sender != null) {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF("command");
            dataOutput.writeUTF(server);
            dataOutput.writeUTF(uuid.toString());
            dataOutput.writeUTF(command);
            sender.sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());
        }
        */
    }

    public void startup(){
        plugin.getLogger().log(Level.INFO, "Global messager is starting up");
        Player randomPlayer = getRandomPlayer();
        if(randomPlayer != null){
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF("getid");
            dataOutput.writeUTF(randomPlayer.getUniqueId().toString());
            randomPlayer.sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());
            plugin.getLogger().log(Level.INFO, "Sending ID request to BungeeCord");
        }
        else{
            plugin.getLogger().log(Level.INFO, "Failed to send instant API request");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event){
        if(id == null){
            new BukkitRunnable(){
                public void run() {
                    Player player = event.getPlayer();
                    if(player.isOnline()) {
                        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
                        dataOutput.writeUTF("getid");
                        dataOutput.writeUTF(player.getUniqueId().toString());
                        player.sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());
                        plugin.getLogger().log(Level.INFO, "Sending needed ID request to BungeeCord");
                    }
                }
            }.runTaskLater(plugin, 5);
        }
    }

    public static Player getRandomPlayer(){
        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
        if(playerList.isEmpty()){
            return null;
        }
        if(playerList.size() == 1){
            return playerList.get(0);
        }
        return playerList.get(ThreadLocalRandom.current().nextInt(playerList.size()));
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if(channel.equals("FaithfulMessage")){
            ByteArrayDataInput dataInputStream = ByteStreams.newDataInput(bytes);
            String action = dataInputStream.readUTF();
            switch (action){
                case "broadcast":
                    String serverFrom = dataInputStream.readUTF();
                    if(!Objects.equals(serverFrom, id)) {
                        String permission = dataInputStream.readUTF();
                        String message = dataInputStream.readUTF();
                        BaseComponent[] components = ComponentSerializer.parse(message);
                        long time = dataInputStream.readLong();
                        if(time + 1000 >= System.currentTimeMillis()) {
                            for (Player other : Bukkit.getOnlinePlayers()) {
                                if (other.hasPermission(permission)) {
                                    other.spigot().sendMessage(components);
                                }
                            }
                        }
                    }
                    break;
                case "command":
                    dataInputStream.readUTF(); //server
                    String uuidString = dataInputStream.readUTF();
                    Player p;
                    if(uuidString.equals("%RANDOM")){
                        p = getRandomPlayer();
                    }
                    else{
                        UUID uuid = UUID.fromString(uuidString);
                        p = Bukkit.getPlayer(uuid);
                    }
                    if(p != null){
                        String command = dataInputStream.readUTF();
                        Bukkit.dispatchCommand(p, command);
                    }
                    break;
                case "id_return":
                    id = dataInputStream.readUTF();
                    plugin.getLogger().log(Level.INFO, "Id found " + id);
                    Bukkit.getPluginManager().callEvent(new ServerAssignedEvent(id));
                    break;
            }
        }
    }

    public String getId() {
        if(id == null){
            plugin.getLogger().log(Level.INFO, "Failed to obtain Id");
            return "None";
        }
        return id;
    }
}
