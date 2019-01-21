package com.faithfulmc.framework;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.faithfulmc.framework.announcement.Announcement;
import com.faithfulmc.framework.announcement.AnnouncementManager;
import com.faithfulmc.framework.announcement.MongoAnnouncementManager;
import com.faithfulmc.framework.command.CommandManager;
import com.faithfulmc.framework.command.SimpleCommandManager;
import com.faithfulmc.framework.command.module.ChatModule;
import com.faithfulmc.framework.command.module.EssentialModule;
import com.faithfulmc.framework.command.module.InventoryModule;
import com.faithfulmc.framework.command.module.TeleportModule;
import com.faithfulmc.framework.hideplayers.PlayerHiddenManager;
import com.faithfulmc.framework.listener.*;
import com.faithfulmc.framework.server.FaithfulServer;
import com.faithfulmc.framework.server.ServerSettings;
import com.faithfulmc.framework.user.*;
import com.faithfulmc.framework.user.mongo.CursorThread;
import com.faithfulmc.framework.user.mongo.MongoUserManager;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ConsoleUser;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.framework.user.util.NameHistory;
import com.faithfulmc.framework.user.yaml.YamlUserManager;
import com.faithfulmc.framework.warp.FlatFileWarpManager;
import com.faithfulmc.framework.warp.Warp;
import com.faithfulmc.framework.warp.WarpManager;
import com.faithfulmc.util.PersistableLocation;
import com.faithfulmc.util.RandomUtils;
import com.faithfulmc.util.SignHandler;
import com.faithfulmc.util.buycraft.BuycraftAPI;
import com.faithfulmc.util.chat.Lang;
import com.faithfulmc.util.cuboid.Cuboid;
import com.faithfulmc.util.cuboid.NamedCuboid;
import com.faithfulmc.util.itemdb.ItemDb;
import com.faithfulmc.util.itemdb.SimpleItemDb;
import com.faithfulmc.util.messgener.GlobalMessager;
import com.faithfulmc.util.morphia.CustomObjectFactory;
import com.faithfulmc.util.morphia.CustomAllowMapper;
import com.faithfulmc.util.nms.NMSProvider;
import com.faithfulmc.util.nms.NMSVersionProvider;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BasePlugin extends JavaPlugin {
    public static BasePlugin plugin;
    public static boolean MONGO;
    public static boolean PRACTICE;
    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chat = null;

    private static Thread MAIN_THREAD;

    public static BasePlugin getPlugin() {
        return BasePlugin.plugin;
    }

    public static boolean isMongo() {
        return MONGO;
    }

    public static Chat getChat() {
        return chat;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Thread getMainThread() {
        return MAIN_THREAD;
    }

    public static Permission getPermission() {
        return permission;
    }
    public BukkitRunnable clearEntityHandler;
    private FaithfulServer faithfulServer;
    private AnnouncementManager announcementManager;
    private ItemDb itemDb;
    private Random random;
    private RandomUtils randomUtils;
    private CommandManager commandManager;
    private ServerHandler serverHandler;
    private SignHandler signHandler;
    private WarpManager warpManager;
    private CursorThread cursorThread;
    private ServerAddress serverAddress;
    private GlobalMessager globalMessager;
    private MongoCredential mongoCredential;
    private String database;
    private MongoClient mongoClient;
    private Morphia morphia;
    private Datastore datastore;
    private NMSProvider nmsProvider;
    private BuycraftAPI buycraftAPI;
    private PlayerHiddenManager playerHiddenManager;
    public UserManager userManager;
    public static ExecutorService mongoService;

    public BasePlugin() {
        this.random = new Random();
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public void onEnable() {
        BasePlugin.plugin = this;
        MAIN_THREAD = Thread.currentThread();
        mongoService = Executors.newFixedThreadPool(4);
        ConfigurationSerialization.registerClass(Warp.class);
        ConfigurationSerialization.registerClass(ServerParticipator.class);
        ConfigurationSerialization.registerClass(BaseUser.class);
        ConfigurationSerialization.registerClass(ConsoleUser.class);
        ConfigurationSerialization.registerClass(NameHistory.class);
        ConfigurationSerialization.registerClass(PersistableLocation.class);
        ConfigurationSerialization.registerClass(Cuboid.class);
        ConfigurationSerialization.registerClass(NamedCuboid.class);
        ConfigurationSerialization.registerClass(Announcement.class);
        setupMongo();
        if(isMongo() && ServerSettings.HASNAME){
            faithfulServer = new FaithfulServer(this);
        }
        this.registerManagers();
        this.registerCommands();
        this.registerListeners();
        /*
        final Plugin plugin = this.getServer().getPluginManager().getPlugin("ProtocolLib");
        if (plugin != null && plugin.isEnabled()) {
            try {
                ProtocolHook.hook(this);
            } catch (Exception var3) {
                this.getLogger().severe("Error hooking into ProtocolLib from Base.");
                var3.printStackTrace();
            }
        }
        */
        new BukkitRunnable() {
            public void run() {
                setupPermissions();
                setupChat();
                setupEconomy();
            }
        }.runTask(plugin);
        new BukkitRunnable() {
            public void run() {
                saveData();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 20, 20 * 60 * 5);
        /*Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            long now = System.currentTimeMillis();
            try {
                BuycraftPostResponse response = BuycraftFramework.addManualPayment(new ManualPayment(
                        "KillaMC",
                        150.00,
                        Collections.singletonList(
                                new PaymentPackage(
                                        2469318,
                                        Maps.newHashMap()
                                )
                        )

                ));
                System.out.println(response.getError_code() + " : " + response.getError_message());
            }
            catch (BuycraftException exception){
                exception.printStackTrace();
            }
        });*/

    }

    public void onLoad() {
        File extraConfig = new File(getDataFolder(), "server.yml");
        if(extraConfig.exists()){
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.load(extraConfig);
            } catch (IOException|InvalidConfigurationException e) {
                e.printStackTrace();
            }
            BaseConstants.load(yamlConfiguration);
        }
        MONGO = getConfig().getBoolean("mongo.enabled", false);
        if (MONGO) {
            String host = getConfig().getString("mongo.host");
            int port = getConfig().getInt("mongo.port");
            database = getConfig().getString("mongo.database");
            boolean auth = getConfig().getBoolean("mongo.auth", false);
            if(auth) {
                String user = getConfig().getString("mongo.user");
                String passwd = getConfig().getString("mongo.passwd");
                mongoCredential = MongoCredential.createCredential(user, database, passwd.toCharArray());
            }
            serverAddress = new ServerAddress(host, port);
            getLogger().info("Using MongoDB");
        }
        PRACTICE = getConfig().getBoolean("practice", false);
        if(PRACTICE){
            getLogger().info("Using Practice Core");
        }
    }

    public void setupMongo() {
        if (isMongo()) {
            try {
                mongoClient = mongoCredential == null ? new MongoClient(serverAddress) : new MongoClient(serverAddress, Collections.singletonList(mongoCredential));
                morphia = new Morphia();
                morphia.getMapper().getOptions().setObjectFactory(new CustomObjectFactory(getClassLoader()));
                morphia.getMapper().getOptions().setReferenceMapper(new CustomAllowMapper(morphia.getMapper().getOptions().getReferenceMapper(), getLogger()));
                morphia.getMapper().getOptions().setEmbeddedMapper(new CustomAllowMapper(morphia.getMapper().getOptions().getEmbeddedMapper(), getLogger()));
                morphia.getMapper().getOptions().setDefaultMapper(new CustomAllowMapper(morphia.getMapper().getOptions().getDefaultMapper(), getLogger()));
                morphia.getMapper().getOptions().setStoreEmpties(true);
                morphia.map(ServerParticipator.class);
                morphia.map(BaseUser.class);
                morphia.map(ConsoleUser.class);
                morphia.map(NameHistory.class);
                datastore = morphia.createDatastore(mongoClient, database);
                datastore.ensureIndexes();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to connect to mongoDB ", e);
                Bukkit.shutdown();
                throw new RuntimeException("Failed to connect to mongoDB ", e);
            }
        }
    }

    public void onDisable() {
        super.onDisable();
        if (isMongo()) {
            if(faithfulServer != null){
                faithfulServer.close();
            }
            mongoClient.close();
        }
        mongoService.shutdown();
        if(playerHiddenManager != null){
            playerHiddenManager.save();
        }
        this.serverHandler.saveServerData();
        this.signHandler.cancelTasks(null);
        this.warpManager.saveWarpData();
        BasePlugin.plugin = null;
    }

    public void saveData() {
        this.serverHandler.saveServerData();
        this.warpManager.saveWarpData();
    }

    private void registerManagers() {
        if(Bukkit.getPluginManager().isPluginEnabled("BuycraftX")) {
            BuycraftPlugin buycraftPlugin = (BuycraftPlugin) Bukkit.getPluginManager().getPlugin("BuycraftX");
            buycraftPlugin.getPlatform().executeAsync(
                    new Runnable() {
                        public void run() {
                            buycraftPlugin.getDuePlayerFetcher().run(false);
                            buycraftPlugin.getPlatform().executeAsyncLater(this, 10, TimeUnit.SECONDS);
                        }
                    }
        );
        }
        nmsProvider = NMSVersionProvider.getProvider();
        buycraftAPI = new BuycraftAPI(getConfig().getString("buycraft-secret", ""));
        this.globalMessager = new GlobalMessager(this);
        //BossBarManager.hook();
        this.randomUtils = new RandomUtils();
        this.serverHandler = new ServerHandler(this);
        this.signHandler = new SignHandler(this);
        this.userManager = isMongo() ? new MongoUserManager(this) : new YamlUserManager(this);
        if (isMongo()) {
            cursorThread = new CursorThread(this);
            announcementManager = new MongoAnnouncementManager(this);
        }
        if(!PRACTICE){
            playerHiddenManager = new PlayerHiddenManager();
        }
        this.warpManager = new FlatFileWarpManager(this);
        this.itemDb = new SimpleItemDb(this);try {
            Lang.initialize("en_US");
        } catch (Throwable var2) {
            var2.printStackTrace();
        }
    }

    public PlayerHiddenManager getPlayerHiddenManager() {
        return playerHiddenManager;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

    public MongoCredential getMongoCredential() {
        return mongoCredential;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public Morphia getMorphia() {
        return morphia;
    }

    private void registerCommands() {
        (this.commandManager = new SimpleCommandManager(this)).registerAll(new ChatModule(this));
        this.commandManager.registerAll(new EssentialModule(this));
        this.commandManager.registerAll(new InventoryModule(this));
        this.commandManager.registerAll(new TeleportModule(this));
        new BukkitRunnable(){
            public void run() {
                FieldAccessor fieldAccessor = Accessors.getFieldAccessor(SimplePluginManager.class, SimpleCommandMap.class, true);
                SimpleCommandMap commandMap = (SimpleCommandMap) fieldAccessor.get(Bukkit.getPluginManager());
                for(Command command: commandMap.getCommands()){
                    command.setPermissionMessage(SimpleCommandManager.PERMISSION_MESSAGE);
                }
            }
        }.runTaskLater(this, 10);
    }

    private void registerListeners() {
        final PluginManager manager = this.getServer().getPluginManager();
        if(!PRACTICE) {
            manager.registerEvents(new VanishListener(this), this);
            manager.registerEvents(new MobstackListener(this), this);
        }
        manager.registerEvents(new ChatListener(this), this);
        manager.registerEvents(new ColouredSignListener(), this);
        manager.registerEvents(new DecreasedLagListener(this), this);
        manager.registerEvents(new JoinListener(this), this);
        manager.registerEvents(new MoveByBlockEvent(), this);
        manager.registerEvents(new NameVerifyListener(this), this);
        manager.registerEvents(new PlayerLimitListener(this), this);
        manager.registerEvents(new ServerSecurityListener(), this);
        manager.registerEvents(new QuitListener(this), this);
        manager.registerEvents(new EventListener(this), this);
    }

    public RandomUtils getRandomUtils() {
        return this.randomUtils;
    }

    public Random getRandom() {
        return this.random;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ItemDb getItemDb() {
        return this.itemDb;
    }

    public ServerHandler getServerHandler() {
        return this.serverHandler;
    }

    public SignHandler getSignHandler() {
        return this.signHandler;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public WarpManager getWarpManager() {
        return this.warpManager;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public CursorThread getCursorThread() {
        return cursorThread;
    }

    public GlobalMessager getGlobalMessager() {
        return globalMessager;
    }

    public NMSProvider getNmsProvider() {
        return nmsProvider;
    }

    public String getDatabaseName(){
        return database;
    }

    public FaithfulServer getFaithfulServer() {
        return faithfulServer;
    }

    public void setFaithfulServer(FaithfulServer faithfulServer) {
        this.faithfulServer = faithfulServer;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }

    public BuycraftAPI getBuycraftAPI() {
        return buycraftAPI;
    }
}
