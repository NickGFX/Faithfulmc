package com.faithfulmc.hardcorefactions;

import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.tracker.KothTracker;
import com.faithfulmc.hardcorefactions.faction.claim.ClaimHandler;
import com.faithfulmc.hardcorefactions.listener.ExpMultiplierListener;
import com.faithfulmc.hardcorefactions.listener.OreMultiplierListener;
import com.faithfulmc.hardcorefactions.scoreboard.PlayerBoard;
import com.faithfulmc.hardcorefactions.vault.VaultManager;
import com.faithfulmc.util.GenericUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.faithfulmc.hardcorefactions.HCF.c;

public final class ConfigurationService {
    public static String DOUBLEARROW = "»";
    public static TimeZone SERVER_TIME_ZONE;
    public static int WARZONE_RADIUS;
    public static String TEAMSPEAK = "ts.faithfulmc.com";
    public static String STORE = "store.faithfulmc.com";
    public static String SITE = "www.faithfulmc.com";
    public static int SPAWN_BUFFER;
    public static int MAP_NUMBER;
    public static boolean KIT_MAP;
    public static List<String> DISALLOWED_FACTION_NAMES;
    public static Map<Enchantment, Integer> ENCHANTMENT_LIMITS;
    public static Map<PotionType, Integer> POTION_LIMITS;
    public static Map<PotionType, Boolean> POTION_LENGTH_LIMIT_SHORT;
    public static Map<PotionType, Boolean> POTION_LENGTH_LIMIT_LONG;
    public static List<Enchantment> DISABLED_ENCHANTS = ImmutableList.of(
            Enchantment.DAMAGE_ARTHROPODS,
            Enchantment.DAMAGE_UNDEAD,
            Enchantment.PROTECTION_EXPLOSIONS,
            Enchantment.PROTECTION_PROJECTILE,
            Enchantment.PROTECTION_FIRE,
            Enchantment.THORNS,
            Enchantment.WATER_WORKER,
            Enchantment.OXYGEN
    );
    public static Map<String, Integer> VAULT_ROWS;
    public static Map<World.Environment, Integer> BORDER_SIZES;
    public static Map<World.Environment, Double> SPAWN_RADIUS_MAP;
    public static int END_PORTAL_LOCATION = 1500;
    public static int FACTION_PLAYER_LIMIT;
    public static ChatColor TEAMMATE_COLOUR = ChatColor.GREEN;
    public static ChatColor ALLY_COLOUR = ChatColor.AQUA;
    public static ChatColor ENEMY_COLOUR = ChatColor.YELLOW;
    public static ChatColor FOCUS_COLOUR = ChatColor.LIGHT_PURPLE;
    public static ChatColor ARCHER_COLOUR = ChatColor.RED;
    public static ChatColor GOLD = ChatColor.GOLD;
    public static ChatColor YELLOW = ChatColor.YELLOW;
    public static ChatColor GRAY = ChatColor.GRAY;
    public static ChatColor SAFEZONE_COLOUR = ChatColor.AQUA;
    public static ChatColor ROAD_COLOUR = ChatColor.YELLOW;
    public static ChatColor WARZONE_COLOUR = ChatColor.DARK_RED;
    public static ChatColor WILDERNESS_COLOUR = ChatColor.DARK_GREEN;
    public static ChatColor SCOREBOARD_COLOR = ChatColor.GRAY;
    public static ChatColor LINE_COLOR = ChatColor.DARK_GRAY;
    public static ChatColor ARROW_COLOR = ChatColor.GOLD;
    public static ChatColor WHITE = ChatColor.WHITE;
    public static ChatColor GREEN = ChatColor.GREEN;
    public static ChatColor RED = ChatColor.RED;
    public static boolean STATTRACK = false;
    public static boolean LUXOR = false;
    public static int HUBS = 8;
    public static String SCOREBOARD_TITLE;
    public static int MAX_ALLIES_PER_FACTION;
    public static long DTR_FREEZE_TIME_MILLIS;
    public static long DTR_MILLIS_BETWEEN_UPDATES;
    public static double CITADEL_CAPTURE_DAYS = 5.5;
    public static int CITADEL_RESET_TIME = 17;
    public static String DTR_WORDS_BETWEEN_UPDATES;
    public static int CONQUEST_REQUIRED_WIN_POINTS;
    public static long DEFAULT_DEATHBAN_DURATION;
    public static boolean MONGO = false;
    public static boolean DIAMONDS_METADATA = false;
    public static String MAP_TITLE;
    public static int SPAWNER_PRICE;
    public static Map<String, Boolean> HCF_CLASSES;
    public static boolean CUT_CLEAN = false;
    public static String TOP_RANK = "Faithful";
    public static int PLAYTIME_RECLAIM_MINUTES = 120;
    public static double DEATHBAN_MULTIPLIER = 1;
    public static List<String> PLAYTIME_RECLAIM_COMMANDS = Collections.singletonList("/crate givekey %player% Bronze 1");
    public static int PVPTIMER_MINUTES = 60;
    public static int STARTING_MONEY = 500;
    public static boolean ORIGINS = false;
    public static int FIRST_KOTH_DAY = 0;
    public static int FIRST_KOTH_HOUR = 18;
    public static int CONQUEST_DAY = 1;
    public static int CONQUEST_HOUR = 13;

    public static String C(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void setupMap(MemorySection memorySection, Logger logger) {
        MAP_TITLE = c(memorySection.getString("title"));
        SERVER_TIME_ZONE = TimeZone.getTimeZone(memorySection.getString("timezone"));
        DISALLOWED_FACTION_NAMES = GenericUtils.createList(memorySection.get("disallowed-faction-names"), String.class);

        ENCHANTMENT_LIMITS = new HashMap<>();
        for(Map.Entry<String, Integer> entry: GenericUtils.castMap(memorySection.get("enchantment-limits"), String.class, Integer.class).entrySet()){
            Enchantment enchantment = Enchantment.getByName(entry.getKey().toUpperCase());
            if(enchantment != null){
                ENCHANTMENT_LIMITS.put(enchantment, entry.getValue());
                logger.info("Using enchantment limit " + enchantment.getName() + " " + entry.getValue());
            }
            else{
                logger.severe("Failed to find enchantment " + entry.getKey());
            }
        }

        if(memorySection.contains("disabled-enchants")){
            DISABLED_ENCHANTS = new ArrayList<>();
            for(String enchantName: GenericUtils.createList(memorySection.get("disabled-enchants"), String.class)){
                Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
                if(enchantment != null){
                    DISABLED_ENCHANTS.add(enchantment);
                    logger.info("Disabling natural enchantment of " + enchantment.getName());
                }

                else{
                    logger.severe("Failed to find enchantment " + enchantName);
                }
            }
        }

        POTION_LIMITS = new EnumMap<>(PotionType.class);
        for(Map.Entry<String, Integer> entry: GenericUtils.castMap(memorySection.get("potion-limits"), String.class, Integer.class).entrySet()){
            PotionType potion = getPotionByName(entry.getKey().toUpperCase());
            if(potion != null){
                POTION_LIMITS.put(potion, entry.getValue());
                logger.info("Using potion limit " + potion.name() + " " + entry.getValue());
            }
            else{
                logger.severe("Failed to find potion " + entry.getKey());
            }
        }

        POTION_LENGTH_LIMIT_SHORT = new EnumMap<>(PotionType.class);
        for(Map.Entry<String, Boolean> entry: GenericUtils.castMap(memorySection.get("potion-limits-short"), String.class, Boolean.class).entrySet()){
            PotionType potion = getPotionByName(entry.getKey().toUpperCase());
            if(potion != null){
                POTION_LENGTH_LIMIT_SHORT.put(potion, entry.getValue());
                logger.info("Using potion limit short " + potion.name() + " " + entry.getValue());
            }
            else{
                logger.severe("Failed to find potion " + entry.getKey());
            }
        }
        POTION_LENGTH_LIMIT_LONG = new EnumMap<>(PotionType.class);
        for(Map.Entry<String, Boolean> entry: GenericUtils.castMap(memorySection.get("potion-limits-long"), String.class, Boolean.class).entrySet()){
            PotionType potion = getPotionByName(entry.getKey().toUpperCase());
            if(potion != null){
                POTION_LENGTH_LIMIT_LONG.put(potion, entry.getValue());
                logger.info("Using potion limit long: " + potion.name() + " " + entry.getValue());
            }
            else{
                logger.severe("Failed to find potion " + entry.getKey());
            }
        }


        BORDER_SIZES = new EnumMap<>(World.Environment.class);
        for(Map.Entry<String, Integer> entry: GenericUtils.castMap(memorySection.get("border-sizes"), String.class, Integer.class).entrySet()){
            World.Environment environment = getEnvironmentByName(entry.getKey().toUpperCase());
            if(environment != null){
                BORDER_SIZES.put(environment, entry.getValue());
                logger.info("Using border size " + environment.name() + " " + entry.getValue());
            }
            else{
                logger.severe("Failed to find environment " + entry.getKey());
            }
        }

        SPAWN_RADIUS_MAP = new EnumMap<>(World.Environment.class);
        for(Map.Entry<String, Double> entry: GenericUtils.castMap(memorySection.get("spawn-radius"), String.class, Double.class).entrySet()){
            World.Environment environment = getEnvironmentByName(entry.getKey().toUpperCase());
            if(environment != null){
                SPAWN_RADIUS_MAP.put(environment, entry.getValue());
                logger.info("Using spawn radius " + environment.name() + " " + entry.getValue());
            }
            else{
                logger.severe("Failed to find environment " + entry.getKey());
            }
        }

        HCF_CLASSES = new HashMap<>();
        for(Map.Entry<String, Boolean> entry: GenericUtils.castMap(memorySection.get("hcf-classes"), String.class, Boolean.class).entrySet()){
            HCF_CLASSES.put(entry.getKey(), entry.getValue());

            if(entry.getValue()){
                logger.info("Enabling HCF Class " + entry.getValue());
            }
            else{
                logger.info("Disabling HCF Class " + entry.getValue());
            }
        }

        GOLD = getColourByName(memorySection.getString("main-color", ChatColor.GOLD.name()));
        YELLOW = getColourByName(memorySection.getString("side-color", ChatColor.YELLOW.name()));
        GRAY = getColourByName(memorySection.getString("extra-color", ChatColor.GRAY.name()));
        WHITE = getColourByName(memorySection.getString("blank-color", ConfigurationService.WHITE.name()));
        SCOREBOARD_COLOR = getColourByName(memorySection.getString("scoreboard-color", ChatColor.GRAY.name()));
        LINE_COLOR = getColourByName(memorySection.getString("lines-color", ChatColor.DARK_GRAY.name()));
        ARROW_COLOR = getColourByName(memorySection.getString("arrow-color", ChatColor.GOLD.name()));
        GREEN = getColourByName(memorySection.getString("outstanding-color", ChatColor.GREEN.name()));
        RED = getColourByName(memorySection.getString("addon-color", ChatColor.RED.name()));

        ENEMY_COLOUR = getColourByName(memorySection.getString("enemy-color", ChatColor.YELLOW.name()));
        TEAMMATE_COLOUR = getColourByName(memorySection.getString("teammate-color", ChatColor.GREEN.name()));
        ALLY_COLOUR = getColourByName(memorySection.getString("ally-color", ChatColor.AQUA.name()));
        ARCHER_COLOUR = getColourByName(memorySection.getString("archer-color", ChatColor.RED.name()));

        PlayerBoard.INVISIBILITYFIX = memorySection.getBoolean("invis-fix", false);
        logger.info("Invisibility fixed " + PlayerBoard.INVISIBILITYFIX);
        MAP_NUMBER = memorySection.getInt("map-number");
        logger.info("Map Number " + MAP_NUMBER);
        FACTION_PLAYER_LIMIT = memorySection.getInt("faction-size");
        logger.info("Faction Size " + FACTION_PLAYER_LIMIT);
        MAX_ALLIES_PER_FACTION = memorySection.getInt("allies");
        logger.info("Allies " + MAX_ALLIES_PER_FACTION);
        KothTracker.DEFAULT_CAP_MILLIS = TimeUnit.MINUTES.toMillis(memorySection.getInt("default-koth-time"));
        logger.info("Default KOTH cap time " + KothTracker.DEFAULT_CAP_MILLIS);
        DEFAULT_DEATHBAN_DURATION = TimeUnit.MINUTES.toMillis(memorySection.getInt("default-deathban-time"));
        CONQUEST_REQUIRED_WIN_POINTS = memorySection.getInt("conquest-win-points");
        logger.info("Conquest win points " + CONQUEST_REQUIRED_WIN_POINTS);
        CITADEL_CAPTURE_DAYS = memorySection.getDouble("citadel-capture-days", CITADEL_CAPTURE_DAYS);
        logger.info("Citadel capture days " + CITADEL_CAPTURE_DAYS);
        CITADEL_RESET_TIME = memorySection.getInt("citadel-reset-time", CITADEL_RESET_TIME);
        logger.info("Citadel capture time " + CITADEL_RESET_TIME);
        KIT_MAP = memorySection.getBoolean("kitmap");
        logger.info("Kitmap " + KIT_MAP);
        SPAWN_BUFFER = memorySection.getInt("spawnbuffer");
        logger.info("Spawn Buffer " + SPAWN_BUFFER);
        WARZONE_RADIUS = memorySection.getInt("warzone-radius");
        logger.info("Warzone Radius " + WARZONE_RADIUS);
        SCOREBOARD_TITLE = C(memorySection.getString("scoreboard-title").replace("%title%", MAP_TITLE).replace("%number%", String.valueOf(MAP_NUMBER)));
        logger.info("Scoreboard Title " + SCOREBOARD_TITLE);
        SPAWNER_PRICE = memorySection.getInt("spawner-price", 20000);
        logger.info("Spawner Price " + SPAWNER_PRICE);
        DOUBLEARROW = memorySection.getString("doublearrow", DOUBLEARROW);
        logger.info("Double Arrow " + DOUBLEARROW);
        ClaimHandler.NEARBY_CLAIM_RADIUS = memorySection.getInt("nearby-claim-radius", ClaimHandler.NEARBY_CLAIM_RADIUS);
        logger.info("Nearby claim radius " + ClaimHandler.NEARBY_CLAIM_RADIUS);
        ClaimHandler.CLAIM_PRICE_PER_BLOCK = memorySection.getDouble("claim-price-block", ClaimHandler.CLAIM_PRICE_PER_BLOCK);
        logger.info("Claim price per block " + ClaimHandler.CLAIM_PRICE_PER_BLOCK);
        VaultManager.ROWS = memorySection.getInt("default-vault-rows", VaultManager.ROWS);

        if(memorySection.contains("rogue-enabled")){
            HCF_CLASSES.put("Rogue", memorySection.getBoolean("rogue-enabled"));
        }
        if(memorySection.contains("bard-enabled")){
            HCF_CLASSES.put("Bard", memorySection.getBoolean("bard-enabled"));
        }

        LUXOR = memorySection.getBoolean("luxor", LUXOR);
        logger.info("Luxor " + LUXOR);
        HUBS = memorySection.getInt("hubs", HUBS);
        logger.info("Hubs " + HUBS);
        STATTRACK = memorySection.getBoolean("stattrack", STATTRACK);
        logger.info("Stat Tracking " + STATTRACK);
        if(VaultManager.ROWS > 0) {
            logger.info("Default Vault rows " + VaultManager.ROWS);
        }
        else{
            logger.info("Vault\'s are disabled by default");
        }
        if(memorySection.contains("vault-rows")) {
            VAULT_ROWS = GenericUtils.castMap(memorySection.get("vault-rows"), String.class, Integer.class);
        } else{
            VAULT_ROWS = Maps.newHashMap();
            logger.info("No vault-rows found");
        }
        logger.info("Using " + VAULT_ROWS.size() + " vault permissions");
        DIAMONDS_METADATA = memorySection.getBoolean("diamonds-metadata", DIAMONDS_METADATA);
        logger.info("Diamonds Metadata " +  DIAMONDS_METADATA);
        ExpMultiplierListener.DEFAULT_MULTIPLER = memorySection.getDouble("xp.default-multiplier", ExpMultiplierListener.DEFAULT_MULTIPLER);
        logger.info("Default XP Multiplier " + ExpMultiplierListener.DEFAULT_MULTIPLER);
        ExpMultiplierListener.LOOTING_MULTIPLIER = memorySection.getDouble("xp.looting-multiplier", ExpMultiplierListener.LOOTING_MULTIPLIER);
        logger.info("Looting XP Multiplier " + ExpMultiplierListener.LOOTING_MULTIPLIER);
        TEAMSPEAK = memorySection.getString("teamspeak", TEAMSPEAK);
        logger.info("Teamspeak " + TEAMSPEAK);
        CUT_CLEAN = memorySection.getBoolean("cut-clean", CUT_CLEAN);
        logger.info("Cut Clean " + CUT_CLEAN);
        STORE = memorySection.getString("store", STORE);
        logger.info("Store " + STORE);
        SITE = memorySection.getString("site", SITE);
        logger.info("Site " + SITE);
        TOP_RANK = memorySection.getString("top-rank", TOP_RANK);
        logger.info("Top Rank " + TOP_RANK);
        PLAYTIME_RECLAIM_MINUTES = memorySection.getInt("playtime-reclaim-minutes", PLAYTIME_RECLAIM_MINUTES);
        logger.info("Playtime Reclaim Minutes " + PLAYTIME_RECLAIM_MINUTES);
        PLAYTIME_RECLAIM_COMMANDS = GenericUtils.createList(memorySection.get("playtime-reclaim-commands", PLAYTIME_RECLAIM_COMMANDS), String.class);
        logger.info("Playtime Reclaim Commands " + Joiner.on(", ").join(PLAYTIME_RECLAIM_COMMANDS));
        EventTimer.EVENT_FREQUENCY = TimeUnit.MINUTES.toMillis(memorySection.getInt("event-frequency", 360));
        logger.info("Automatic Event Frequency " + DurationFormatUtils.formatDurationWords(EventTimer.EVENT_FREQUENCY, true, true));
        DTR_FREEZE_TIME_MILLIS = TimeUnit.MINUTES.toMillis(memorySection.getInt("dtr-freeze-time", 30));
        logger.info("DTR Freeze Time " + DurationFormatUtils.formatDurationWords(DTR_FREEZE_TIME_MILLIS, true, true));
        DTR_MILLIS_BETWEEN_UPDATES = TimeUnit.SECONDS.toMillis(memorySection.getInt("dtr-update-seconds", 40));
        DTR_WORDS_BETWEEN_UPDATES = DurationFormatUtils.formatDurationWords(DTR_MILLIS_BETWEEN_UPDATES, true, true);
        logger.info("DTR Update Rate " + DTR_WORDS_BETWEEN_UPDATES);
        OreMultiplierListener.MULTIPLIER = memorySection.getDouble("ore-multiplier", OreMultiplierListener.MULTIPLIER);
        logger.info("Ore Multiplier " + OreMultiplierListener.MULTIPLIER);
        DEATHBAN_MULTIPLIER = memorySection.getDouble("deathban-multiplier", DEATHBAN_MULTIPLIER);
        logger.info("Deathban Multiplier " + DEATHBAN_MULTIPLIER);
        PVPTIMER_MINUTES = memorySection.getInt("pvptimer-minutes", PVPTIMER_MINUTES);
        logger.info("PvPTimer Minutes " + PVPTIMER_MINUTES);
        STARTING_MONEY = memorySection.getInt("starting-money", STARTING_MONEY);
        logger.info("Starting Money " + STARTING_MONEY);
        ORIGINS = memorySection.getBoolean("origins", ORIGINS);
        logger.info("Origins " + ORIGINS);
        FIRST_KOTH_DAY = memorySection.getInt("first-koth.day", FIRST_KOTH_DAY);
        logger.info("First Koth Day " + FIRST_KOTH_DAY);
        FIRST_KOTH_HOUR = memorySection.getInt("first-koth.hour", FIRST_KOTH_HOUR);
        logger.info("First Koth Hour " + FIRST_KOTH_HOUR);
        CONQUEST_DAY = memorySection.getInt("conquest.day", CONQUEST_DAY);
        logger.info("Conquest Day " + CONQUEST_DAY);
        CONQUEST_HOUR = memorySection.getInt("conquest.hour", CONQUEST_HOUR);
        logger.info("Conquest Hour " + CONQUEST_HOUR);
    }

    public static final PotionType getPotionByName(String name){
        for(PotionType potionType: PotionType.values()){
            if(potionType.name().equals(name)){
                return potionType;
            }
        }
        return null;
    }

    public static final World.Environment getEnvironmentByName(String name){
        for(World.Environment environment: World.Environment.values()){
            if(environment.name().equals(name)){
                return environment;
            }
        }
        return null;
    }

    public static final ChatColor getColourByName(String name){
        for(ChatColor chatColor: ChatColor.values()){
            if(chatColor.name().equals(name)){
                return chatColor;
            }
        }
        return null;
    }
}
