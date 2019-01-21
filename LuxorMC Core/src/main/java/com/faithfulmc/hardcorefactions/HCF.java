package com.faithfulmc.hardcorefactions;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.faithfulmc.framework.command.SimpleCommandManager;
import com.faithfulmc.hardcorefactions.command.*;
import com.faithfulmc.hardcorefactions.command.lives.LivesExecutor;
import com.faithfulmc.hardcorefactions.command.revive.ReviveManager;
import com.faithfulmc.hardcorefactions.command.spawncredit.SpawnCreditExecutor;
import com.faithfulmc.hardcorefactions.deathban.BasicDeathbanManager;
import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.deathban.DeathbanListener;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.EventCapture;
import com.faithfulmc.hardcorefactions.events.EventExecutor;
import com.faithfulmc.hardcorefactions.events.conquest.ConquestExecutor;
import com.faithfulmc.hardcorefactions.events.eotw.EotwCommand;
import com.faithfulmc.hardcorefactions.events.eotw.EotwHandler;
import com.faithfulmc.hardcorefactions.events.eotw.EotwListener;
import com.faithfulmc.hardcorefactions.events.faction.*;
import com.faithfulmc.hardcorefactions.faction.*;
import com.faithfulmc.hardcorefactions.faction.argument.FactionCreateArgument;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.claim.ClaimHandler;
import com.faithfulmc.hardcorefactions.faction.claim.ClaimWandListener;
import com.faithfulmc.hardcorefactions.faction.claim.Subclaim;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.*;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClassManager;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerLevel;
import com.faithfulmc.hardcorefactions.kit.*;
import com.faithfulmc.hardcorefactions.listener.*;
import com.faithfulmc.hardcorefactions.listener.fixes.*;
import com.faithfulmc.hardcorefactions.logger.CombatLogListener;
import com.faithfulmc.hardcorefactions.logger.CustomEntityRegistration;
import com.faithfulmc.hardcorefactions.mountain.*;
import com.faithfulmc.hardcorefactions.scoreboard.ScoreboardHandler;
import com.faithfulmc.hardcorefactions.staffmode.StaffModeListener;
import com.faithfulmc.hardcorefactions.timer.TimerExecutor;
import com.faithfulmc.hardcorefactions.timer.TimerManager;
import com.faithfulmc.hardcorefactions.user.AbstractUserManager;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.user.FlatfileUserManager;
import com.faithfulmc.hardcorefactions.user.MongoUserManager;
import com.faithfulmc.hardcorefactions.util.Cooldowns;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.faithfulmc.hardcorefactions.util.location.BlockLocation;
import com.faithfulmc.hardcorefactions.vault.VaultManager;
import com.faithfulmc.hardcorefactions.visualise.AsyncWallBorderListener;
import com.faithfulmc.hardcorefactions.visualise.VisualiseHandler;
import com.faithfulmc.util.Config;
import com.faithfulmc.util.PersistableLocation;
import com.faithfulmc.util.cuboid.Cuboid;
import com.faithfulmc.util.morphia.CustomAllowMapper;
import com.faithfulmc.util.morphia.CustomObjectFactory;
import com.google.common.base.Joiner;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class HCF extends JavaPlugin {
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1L);
    private static final long HOUR = TimeUnit.HOURS.toMillis(1L);
    private static final long MULTI_HOUR = TimeUnit.HOURS.toMillis(10);

    @Getter public static Permission permission = null;
    @Getter public static Economy economy = null;
    @Getter public static Chat chat = null;

    public static String c(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String getRemaining(long millis, boolean milliseconds) {
        return getRemaining(millis, milliseconds, true);
    }

    public static String getRemaining(long duration, boolean milliseconds, boolean trail) {
        if ((milliseconds) && (duration < MINUTE)) {
            return ( (trail ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get()).format(duration * 0.001D) + 's';
        }
        return DurationFormatUtils.formatDuration(duration, (duration >= HOUR ? (duration >= MULTI_HOUR ? "H" : "") + "H:" : "") + "mm:ss");
    }

    public static boolean isMongo() {
        return ConfigurationService.MONGO;
    }

    @Getter private static HCF instance;

    private final Object saveLock = new Object();

    @Getter private Thread mainThread;

    private PortalListener portalListener;
    @Getter private WorldEditPlugin worldEdit;
    private FoundDiamondsListener foundDiamondsListener;
    @Getter private ClaimHandler claimHandler;
    @Getter private BasicDeathbanManager deathbanManager;
    @Getter private EotwHandler eotwHandler;
    @Getter private FactionManager factionManager;
    @Getter private HCFClassManager hcfClassManager;
    @Getter private ScoreboardHandler scoreboardHandler;
    @Getter private TimerManager timerManager;
    @Getter private AbstractUserManager userManager;
    @Getter private VisualiseHandler visualiseHandler;
    @Getter private StaffModeListener staffModeListener;
    @Getter private VaultManager vaultManager;
    private CobwebbFixListener cobwebbFixListener;
    @Getter private GlowstoneMountainManager glowstoneMountainManager;
    @Getter private OreMountainManager oreMountainManager;
    private ReviveManager reviveManager;
    private DeathListener deathListener;
    private BackCommand backCommand;
    @Getter private KitManager kitManager;
    @Getter private MongoClient mongoClient;
    @Getter private ServerAddress mongoAddress;
    @Getter private MongoCredential credential;
    @Getter private Morphia morphia;
    @Getter private Datastore morphiastore;
    private Config locations;
    private PersistableLocation endSpawn;
    private PersistableLocation endExit;
    private String dataStore;

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public void onLoad() {

    }

    private void connectToMongo(){
        getLogger().info("Connecting to MongoDB");
        try {
            mongoClient = credential == null ? new MongoClient(mongoAddress) : new MongoClient(mongoAddress, Collections.singletonList(credential));
            morphia = new Morphia();
            morphia.getMapper().getOptions().setObjectFactory(new CustomObjectFactory(getClassLoader()));
            morphia.getMapper().getOptions().setReferenceMapper(new CustomAllowMapper(morphia.getMapper().getOptions().getReferenceMapper(), getLogger()));
            morphia.getMapper().getOptions().setEmbeddedMapper(new CustomAllowMapper(morphia.getMapper().getOptions().getEmbeddedMapper(), getLogger()));
            morphia.getMapper().getOptions().setDefaultMapper(new CustomAllowMapper(morphia.getMapper().getOptions().getDefaultMapper(), getLogger()));
            morphia.getMapper().getOptions().setStoreEmpties(true);
            mapMorphia();
            morphiastore = morphia.createDatastore(mongoClient, dataStore);
            morphiastore.ensureIndexes();//
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to connect to Mongo ", e);
            Bukkit.shutdown();
            throw new RuntimeException("Failed to connect to Mongo");
        }
    }

    private void mapMorphia(){
        morphia.map(FactionUser.class);
        morphia.map(EventCapture.class);
        morphia.mapPackage("com.faithfulmc.hardcorefactions.faction.miner");
        morphia.map(Deathban.class);
        morphia.mapPackage("com.faithfulmc.base.util.cuboid");
        morphia.map(Claim.class, Subclaim.class);
        morphia.map(Relation.class);
        morphia.map(PersistableLocation.class);
        morphia.mapPackage("com.faithfulmc.hardcorefactions.faction.claim");
        morphia.map(MinerLevel.class);
        morphia.mapPackage("com.faithfulmc.hardcorefactions.util.mongo");
        morphia.mapPackage("com.faithfulmc.hardcorefactions.events.faction");
    }

    public void onEnable() {
        instance = this;
        mainThread = Thread.currentThread();

        reloadConfig();
        if (getConfig().getBoolean("mongo.enabled", false)) {
            getLogger().info("MongoDB detected");
            ConfigurationService.MONGO = true;
            String host = getConfig().getString("mongo.host");
            int port = getConfig().getInt("mongo.port");
            boolean auth = getConfig().getBoolean("mongo.auth", true);
            if(auth) {
                String user = getConfig().getString("mongo.user");
                String psw = getConfig().getString("mongo.password");
                credential = MongoCredential.createCredential(user, dataStore, psw.toCharArray());
            }
            dataStore = getConfig().getString("mongo.database");
            mongoAddress = new ServerAddress(host, port);
        }
        ConfigurationService.setupMap(getConfig(), getLogger());

        if (isMongo()) {
            executeMonititoredTask("MongoDB Connection", this::connectToMongo);
        }

        executeMonititoredTask("Configuration Serializable Registration", this::registerConfiguration);

        locations = new Config(this, "locations");
        executeMonititoredTask("Custom Entity Registration", CustomEntityRegistration::registerCustomEntities);
        Plugin wep = Bukkit.getPluginManager().getPlugin("WorldEdit");
        this.worldEdit = (((wep instanceof WorldEditPlugin)) && (wep.isEnabled()) ? (WorldEditPlugin) wep : null);
        executeMonititoredTask("Manager Registration", this::registerManagers);
        executeMonititoredTask("Command Registration", this::registerCommands);
        executeMonititoredTask("Listener Registration", this::registerListeners);
        executeMonititoredTask("Cooldown Registration", this::registerCooldowns);
        staffModeListener.onEnable();
        endSpawn = (PersistableLocation) locations.get("end.spawn", new PersistableLocation(portalListener.getEndSpawn()));
        endExit = (PersistableLocation) locations.get("end.exit", new PersistableLocation(portalListener.getEndExit()));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        new BukkitRunnable() {
            public void run() {
                executeMonititoredTask("HCF Plugin Data Saving", HCF.this::saveData);
            }
        }.runTaskTimerAsynchronously(instance, 20 * 30 * 5, 20 * 60 * 5);
        new BukkitRunnable() {
            public void run() {
                executeMonititoredTask("Startup Commands", HCF.this::startupCommands);
                setupBroadcast();
                setupChat();
                setupEconomy();
                setupPermissions();
                for(World world: Bukkit.getWorlds()){
                    world.setAutoSave(true);
                    world.setGameRuleValue("doDaylightCycle", "false");
                    world.setTime(0);
                }
            }
        }.runTask(this);
        addRecipe();

        for(Player player: Bukkit.getOnlinePlayers()){
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, null));
        }
    }

    public Location getEndSpawn() {
        return endSpawn.getLocation();
    }

    public void setEndSpawn(Location location) {
        endSpawn = new PersistableLocation(location);
        locations.set("end.spawn", endSpawn);
        locations.save();
    }

    public Location getEndExit() {
        return endExit.getLocation();
    }

    public void setEndExit(Location location) {
        endExit = new PersistableLocation(location);
        locations.set("end.exit", endExit);
        locations.save();
    }

    private void registerCooldowns() {
        Cooldowns.createCooldown("Faction_cooldown", FactionCreateArgument.FACTION_COOLDON);
        Cooldowns.createCooldown("Archer_item_cooldown", ArcherClass.ARCHER_SPEED_COOLDOWN_DELAY);
    }

    private void startupCommands() {
        File file = new File(getDataFolder(), "startupCommands.txt");
        if (file.exists()) {
            List<String> commands = new ArrayList<>();
            try(InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), "UTF8");
                BufferedReader bufferedReader = new BufferedReader(fileReader);
            ){
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    commands.add(line);
                }
            }
            catch (IOException exception){
                getLogger().log(Level.SEVERE, "An exception was thrown whilst fetching the startup commands ", exception);
            }
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    private void executeMonititoredTask(String name, Runnable runnable){
        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();
        long diff = end - start;
        if(diff > 100000000){
            double millis = diff / 1000000D;
            String output = DateTimeFormats.REMAINING_SECONDS.get().format(millis);
            getLogger().info("Running [" + name + "] took " + output + "ms");
        }
    }

    private void saveData() {
        synchronized (saveLock) {
            getLogger().info("Saving all HCF data");
            if (!ConfigurationService.KIT_MAP) {
                if(!ConfigurationService.ORIGINS) {
                    executeMonititoredTask("Mountain Data Save", () -> {
                        glowstoneMountainManager.save();
                        oreMountainManager.save();
                    });
                }
                executeMonititoredTask("Death Data Save", deathListener::saveData);
            }
            executeMonititoredTask("Faction Data Save", factionManager::saveFactionData);
            executeMonititoredTask("Timer Data Save", timerManager::saveTimerData);
            executeMonititoredTask("User Data Save", userManager::saveUserData);
            executeMonititoredTask("Kit Data Save", kitManager::saveKitData);
            executeMonititoredTask("Vault Data Save", vaultManager::saveConfig);
        }
    }

    public void onDisable() {
        if (!ConfigurationService.KIT_MAP) {
            executeMonititoredTask("FD Save", foundDiamondsListener::saveConfig);
        }
        executeMonititoredTask("Web Cleanup", cobwebbFixListener::cleanUp);
        executeMonititoredTask("Staff Mode Disable", staffModeListener::onDisable);
        executeMonititoredTask("Unregistering Custom Entities", CustomEntityRegistration::unregisterCustomEntities);
        executeMonititoredTask("Removing Combat Loggers", CombatLogListener::removeCombatLoggers);
        executeMonititoredTask("Disabline HCF Classes", hcfClassManager::onDisable);
        executeMonititoredTask("Clearing Scoreboards", scoreboardHandler::disable);
        executeMonititoredTask("Closing HCF Plugin Data Save", this::saveData);
        if (isMongo()) {
            executeMonititoredTask("Closing MongoDB Connection", mongoClient::close);
        }
        //executeMonititoredTask("Unregistering configuration classes", this::unregisterConfiguration);
        mainThread = null;
        instance = null;
    }

    private void registerConfiguration() {
        ConfigurationSerialization.registerClass(CaptureZone.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(Claim.class);
        ConfigurationSerialization.registerClass(Subclaim.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(FactionUser.class);
        ConfigurationSerialization.registerClass(ClaimableFaction.class);
        ConfigurationSerialization.registerClass(ConquestFaction.class);
        ConfigurationSerialization.registerClass(CitadelFaction.class);
        ConfigurationSerialization.registerClass(CitadelCapture.class);
        ConfigurationSerialization.registerClass(CapturableFaction.class);
        ConfigurationSerialization.registerClass(KothFaction.class);
        ConfigurationSerialization.registerClass(EndPortalFaction.class);
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(FactionMember.class);
        ConfigurationSerialization.registerClass(PlayerFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.class);
        ConfigurationSerialization.registerClass(SpawnFaction.class);
        ConfigurationSerialization.registerClass(NorthRoadFaction.class);
        ConfigurationSerialization.registerClass(EastRoadFaction.class);
        ConfigurationSerialization.registerClass(SouthRoadFaction.class);
        ConfigurationSerialization.registerClass(WestRoadFaction.class);
        ConfigurationSerialization.registerClass(MountainFaction.class);
        ConfigurationSerialization.registerClass(GlowstoneFaction.class);
        ConfigurationSerialization.registerClass(OreFaction.class);
        ConfigurationSerialization.registerClass(BlockLocation.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(PersistableLocation.class);
        ConfigurationSerialization.registerClass(Cuboid.class);
    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();
        if (!ConfigurationService.KIT_MAP) {
            manager.registerEvents(new CutCleanListener(), this);
            manager.registerEvents(new CrowbarListener(this), this);
            manager.registerEvents(new EotwListener(this), this);
            manager.registerEvents(new ExpMultiplierListener(), this);
            manager.registerEvents(new ExpListener(), this);
            manager.registerEvents(new SpawnerTradeListener(this), this);
            manager.registerEvents(new DeathSignListener(this), this);
            manager.registerEvents(new MobFixes(), this);
            manager.registerEvents(new AntiPrime(), this);
            manager.registerEvents(new OreListener(this), this);
            manager.registerEvents(this.foundDiamondsListener = new FoundDiamondsListener(this), this);
            manager.registerEvents(new PotionLimitListener(), this);
            manager.registerEvents(new SkullListener(), this);
            if(!ConfigurationService.ORIGINS){
                manager.registerEvents(reviveManager, this);
                manager.registerEvents(new MobFarmListener(), this);
            }
        } else {
            manager.registerEvents(new KillStreakListener(this), this);
            manager.registerEvents(new KitMapListener(this), this);
            manager.registerEvents(new BoatCraftFix(), this);
        }
        manager.registerEvents(new SignSubclaimListener(this), this);
        manager.registerEvents(new PhaseListener(), this);
        manager.registerEvents(new DeathbanListener(this), this);
        manager.registerEvents(new OreMultiplierListener(), this);
        manager.registerEvents(new TabListener(this), this);
        manager.registerEvents(new PickupListener(this), this);
        manager.registerEvents(new BoatGlitchFixListener(), this);
        manager.registerEvents(timerManager.sotw, this);
        manager.registerEvents(new ElevatorListener(this), this);
        manager.registerEvents(new ClaimWandListener(this), this);
        manager.registerEvents(new EntityLimitListener(), this);
        manager.registerEvents(new EnchantLimitListener(), this);
        manager.registerEvents(deathListener = new DeathListener(this), this);
        manager.registerEvents(backCommand, this);
        manager.registerEvents(new FirstJoinMessage(), this);
        manager.registerEvents(new PearlLandListener(), this);
        manager.registerEvents(new ArmorFixListener(), this);
        manager.registerEvents(cobwebbFixListener = new CobwebbFixListener(this), this);
        manager.registerEvents(new BorderListener(), this);
        manager.registerEvents(new ArcherClass(this), this);
        manager.registerEvents(new BlockJumpGlitchFixListener(), this);
        manager.registerEvents(new BookDeenchantListener(), this);
        manager.registerEvents(new ArmorFixListener(), this);
        manager.registerEvents(new PlayerVaultListener(this), this);
        manager.registerEvents(new ChatListener(this), this);
        manager.registerEvents(new CombatLogListener(this), this);
        manager.registerEvents(new CoreListener(this), this);
        manager.registerEvents(new EnderChestRemovalListener(), this);
        manager.registerEvents(factionManager, this);
        manager.registerEvents(new EventSignListener(), this);
        manager.registerEvents(new FactionListener(this), this);
        manager.registerEvents(new InfinityArrowFixListener(), this);
        manager.registerEvents(new KitListener(this), this);
        manager.registerEvents(new HungerFixListener(this), this);
        manager.registerEvents(new PearlGlitchListener(this), this);
        manager.registerEvents(new ShopSignListener(this), this);
        manager.registerEvents(portalListener = new PortalListener(this), this);
        manager.registerEvents(new ProtectionListener(this), this);
        manager.registerEvents(new BeaconStreanthFixListener(), this);
        manager.registerEvents(new AsyncWallBorderListener(this), this);
        manager.registerEvents(new WorldListener(this), this);
        manager.registerEvents(new HCFClassPotionFixListener(this), this);
        manager.registerEvents(new EnderpearlRefundListener(this), this);
        manager.registerEvents(new AnvilRenameFix(this), this);
        manager.registerEvents(new OtherCommandListener(), this);
        if(ConfigurationService.ORIGINS) {
            manager.registerEvents(new EventPearlFix(this), this);
            manager.registerEvents(new OriginsListener(this), this);
        }
        manager.registerEvents(new ColonCommandFix(this), this);
        manager.registerEvents(staffModeListener = new StaffModeListener(this), this);
        manager.registerEvents(new WeatherFixListener(), this);
        if(!ConfigurationService.ORIGINS) {
            manager.registerEvents(new PotFixListener(this), this);
        }
        manager.registerEvents(new PhaseGlitchListener(), this);
        manager.registerEvents(new PickFixListener(), this);
        manager.registerEvents(new FenceGateGlitchListener(), this);
        if(!ConfigurationService.ORIGINS) {
            manager.registerEvents(new FlyListener(this), this);
        }
    }

    public void addRecipe() {
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON, 1));
        shapelessRecipe.addIngredient(1, Material.GOLD_NUGGET);
        shapelessRecipe.addIngredient(1, Material.MELON);
        Bukkit.addRecipe(shapelessRecipe);
    }

    public void setupBroadcast() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String primary = getPermission().getPrimaryGroup(player);
                if (primary != null && primary.equalsIgnoreCase(ConfigurationService.TOP_RANK)) {
                    players.add(c(getChat().getGroupPrefix(player.getWorld(), primary).replace("&l", "")) + player.getName());
                }
            }
            String message = ConfigurationService.GOLD +  "Online " + ConfigurationService.TOP_RANK + " Donators " +  ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.GRAY + Joiner.on(ConfigurationService.GRAY + ", ").join(players);
            if (!players.isEmpty()) {
                Bukkit.getScheduler().runTask(HCF.this, () -> {
                    Bukkit.broadcastMessage(message);
                    Bukkit.broadcastMessage(ConfigurationService.YELLOW + "You can buy this rank at " + ConfigurationService.WHITE + ConfigurationService.STORE);

                });
            }
        }, (20 * 60 * 5) + 45, 20 * 60 * 5);
    }

    private void registerCommands() {
        if (!ConfigurationService.KIT_MAP) {
            getCommand("crowbar").setExecutor(new CrowbarCommand());
            getCommand("pvptimer").setExecutor(new PvpTimerCommand(this));
            getCommand("spawner").setExecutor(new SpawnerCommand(this));
            getCommand("diamonds").setExecutor(new DiamondsCommand(this));
            if(!ConfigurationService.ORIGINS) {
                getCommand("bottle").setExecutor(new BottleCommand(this));
            }
            getCommand("lives").setExecutor(new LivesExecutor(this));
            getCommand("refund").setExecutor(new RefundCommand());
            if(!ConfigurationService.ORIGINS) {
                getCommand("glowstone").setExecutor(new GlowstoneMountainCommand(this));
                getCommand("oremountain").setExecutor(new OreMountainCommand(this));
            }
            getCommand("miner").setExecutor(new MinerRewardsCommand());
            getCommand("ores").setExecutor(new OreAmountCommand(this));
            if(!ConfigurationService.ORIGINS) {
                getCommand("spawncredit").setExecutor(new SpawnCreditExecutor(this));
                getCommand("oreinventory").setExecutor(new OreInventoryCommand(this));
                getCommand("godkits").setExecutor(new GodKitsCommand(this));
            }
        }
        if(!ConfigurationService.ORIGINS) {
            getCommand("reclaim").setExecutor(new ReclaimCommand(this));
        }
        LFFCommand lffCommand;
        getCommand("lff").setExecutor(lffCommand = new LFFCommand(this));
        getCommand("lffalerts").setExecutor(lffCommand);
        BroadcastLocationCommand broadcastLocationCommand;
        getCommand("broadcastlocation").setExecutor(broadcastLocationCommand = new BroadcastLocationCommand(this));
        getCommand("locationalerts").setExecutor(broadcastLocationCommand);
        getCommand("conquest").setExecutor(new ConquestExecutor(this));
        getCommand("cobble").setExecutor(new CobbleCommand(this));
        getCommand ("logout").setExecutor(new LogoutCommand(this));
        getCommand("sotw").setExecutor(new TimerCommand(getTimerManager().sotw));
        getCommand("keysale").setExecutor(new TimerCommand(getTimerManager().keySale));
        getCommand("sale").setExecutor(new TimerCommand(getTimerManager().sale));
        getCommand("eotw").setExecutor(new EotwCommand(this));
        if(!ConfigurationService.ORIGINS) {
            getCommand("panic").setExecutor(new PanicCommand(this));
        }
        getCommand("repairable").setExecutor(new RepairableCommand(this));
        getCommand("winrank").setExecutor(new WinRankCommand(this));
        getCommand("playtime").setExecutor(new PlayTimeCommand(this));
        getCommand("setworldregion").setExecutor(new SetWorldRegion(this));
        getCommand("addworldregion").setExecutor(new AddWorldRegion(this));
        if(!ConfigurationService.ORIGINS) {
            getCommand("near").setExecutor(new NearCommand(this));
            getCommand("ptime").setExecutor(new PTimeCommand(this));
            getCommand("setreclaimed").setExecutor(new SetReclaimedCommand(this));
        }
        getCommand("stack").setExecutor(new StackCommand(this));
        getCommand("back").setExecutor(backCommand = new BackCommand(this));
        getCommand("seen").setExecutor(new SeenCommand(this));
        getCommand("teamspeak").setExecutor(new TeamspeakCommand());
        getCommand("hub").setExecutor(new HubCommand());
        getCommand("random").setExecutor(new RandomCommand(this));
        getCommand("regen").setExecutor(new RegenCommand(this));
        getCommand("endportal").setExecutor(new EndPortalCommand(this));
        getCommand("angle").setExecutor(new AngleCommand());
        getCommand("economy").setExecutor(new EconomyCommand(this));
        getCommand("event").setExecutor(new EventExecutor(this));
        getCommand("website").setExecutor(new WebsiteCommand());
        getCommand("help").setExecutor(new HelpCommand());
        getCommand("faction").setExecutor(new FactionExecutor(this));
        getCommand("gopple").setExecutor(new GoppleCommand(this));
        getCommand("location").setExecutor(new LocationCommand(this));
        getCommand("mapkit").setExecutor(new MapKitCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("servertime").setExecutor(new ServerTimeCommand());
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("timer").setExecutor(new TimerExecutor(this));
        getCommand("togglecapzone").setExecutor(new ToggleCapzoneCommand(this));
        getCommand("togglelightning").setExecutor(new ToggleLightningCommand(this));
        getCommand("togglesidebar").setExecutor(new ToggleSidebarCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("statsreset").setExecutor(new StatsResetCommand(this));
        getCommand("buy").setExecutor(new BuyCommand(this));
        if(!ConfigurationService.ORIGINS) {
            getCommand("condense").setExecutor(new CondenseCommand(this));
        }
        getCommand("coords").setExecutor(new CoordsCommand(this));
        getCommand("focus").setExecutor(new FocusCommand(this));
        getCommand("setendspawn").setExecutor(new SetEndSpawn(this));
        getCommand("setendexit").setExecutor(new SetEndExit(this));
        getCommand("kit").setExecutor(new KitExecutor(this));
        getCommand("mobdrops").setExecutor(new MobDropsCommand(this));
        getCommand("staffserver").setExecutor(new StaffServerCommand(this));
        getCommand("famous").setExecutor(new FamousCommand(this));
        getCommand("youtube").setExecutor(new YoutubeCommand(this));
        getCommand("mod").setExecutor(new StaffModeCommand(this));
        getCommand("playervault").setExecutor(new PlayerVaultCommand(this));
        getCommand("setborder").setExecutor(new SetBorderCommand());
        getCommand("toggledeathmessages").setExecutor(new ToggleDeathMessagesCommand(this));
        getCommand("disguise").setExecutor(new DisguiseCommand(this));

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            if (!Arrays.asList("reclaim", "mod", "glowstone", "oremountain").contains(command.getName())) {
                command.setPermission("hcf.command." + entry.getKey());
            }
            command.setPermissionMessage(SimpleCommandManager.PERMISSION_MESSAGE);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> executeMonititoredTask("Command Message Changes", () -> {
                FieldAccessor fieldAccessor = Accessors.getFieldAccessor(SimplePluginManager.class, SimpleCommandMap.class, true);
                SimpleCommandMap commandMap = (SimpleCommandMap) fieldAccessor.get(Bukkit.getPluginManager());
                for(Command command: commandMap.getCommands()){
                    command.setPermissionMessage(SimpleCommandManager.PERMISSION_MESSAGE);
                }
        }), 10);
    }

    private void registerManagers() {
        executeMonititoredTask("Deathban Manager Loading", () -> deathbanManager = new BasicDeathbanManager(this));
        executeMonititoredTask("EOTW Handler Loading", () -> eotwHandler = new EotwHandler(this));
        executeMonititoredTask("Claim Handler Loading", () -> claimHandler = new ClaimHandler(this));
        executeMonititoredTask("User Data Loading", () -> userManager = isMongo() ? new MongoUserManager(this) : new FlatfileUserManager(this));
        executeMonititoredTask("Faction Data Loading", () -> factionManager = isMongo() ? new MongoFactionManager(this) : new FlatFileFactionManager(this));
        executeMonititoredTask("HCFClass Manager Loading", () -> hcfClassManager = new HCFClassManager(this));
        executeMonititoredTask("Timer Data Loading", () -> {
            timerManager = new TimerManager(this);
            timerManager.reloadTimerData();
        });
        executeMonititoredTask("Scoreboard Handler Loading", () -> scoreboardHandler = new ScoreboardHandler(this));
        executeMonititoredTask("Visualize Handler Loading", () -> visualiseHandler = new VisualiseHandler());
        executeMonititoredTask("Kit Data Loading", () -> kitManager = new FlatFileKitManager(this));
        if (!ConfigurationService.KIT_MAP && !ConfigurationService.ORIGINS) {
            executeMonititoredTask("Revive Manager Loading", () -> reviveManager = new ReviveManager(this));
            executeMonititoredTask("Mountain Managers Loading", () -> {
                glowstoneMountainManager = new GlowstoneMountainManager(this);
                oreMountainManager = new OreMountainManager(this);
            });
        }
        executeMonititoredTask("Vault Data Loading", () -> vaultManager = new VaultManager(this));
    }
}
