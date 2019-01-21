package com.faithfulmc.hardcorefactions.events.faction;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventType;
import com.faithfulmc.hardcorefactions.events.tracker.CitadelTracker;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.listener.SpawnerTradeListener;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.ItemBuilder;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@Entity(value = "faction")
public class CitadelFaction extends KothFaction implements ConfigurationSerializable {
    private Long lastChestReset;
    @Embedded
    private CitadelCapture citadelCapture;

    public CitadelFaction(){
    }


    public CitadelFaction(String name) {
        super(name);
    }


    public CitadelFaction(Map<String, Object> map) {
        super(map);
        if(map.containsKey("citadelCapture")){
            citadelCapture = (CitadelCapture) map.get("citadelCapture");
        }
        if(lastChestReset != null) {
            lastChestReset = (Long) map.get("lastChestReset");
        }
    }


    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        if(citadelCapture != null) {
            map.put("citadelCapture", citadelCapture);
        }
        if(lastChestReset != null){
            map.put("lastChestReset", lastChestReset);
        }
        return map;
    }

    @Override
    public EventType getEventType() {
        return EventType.CITADEL;
    }

    public Long getLastChestReset() {
        return lastChestReset;
    }

    public boolean canChestReset(long now){
        Long nextChestReset = getNextChestReset(now);
        if(nextChestReset != null){
            if(nextChestReset < now - 500 && (lastChestReset == null || now - lastChestReset > TimeUnit.HOURS.toMillis(20))){
                return true;
            }
        }
        return false;
    }

    public void setLastChestReset(Long lastChestReset) {
        this.lastChestReset = lastChestReset;
    }

    public Long getNextChestReset(){
        return getNextChestReset(System.currentTimeMillis());
    }

    public Long getNextChestReset(long now){
        if(citadelCapture != null && citadelCapture.hasControl(now)) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar(ConfigurationService.SERVER_TIME_ZONE);
            gregorianCalendar.setTimeInMillis(now);
            int dayOfYear = gregorianCalendar.get(Calendar.DAY_OF_YEAR);
            gregorianCalendar.set(Calendar.HOUR_OF_DAY, ConfigurationService.CITADEL_RESET_TIME);
            gregorianCalendar.set(Calendar.SECOND, 0);
            gregorianCalendar.set(Calendar.MILLISECOND, 0);
            GregorianCalendar lastCaptureCalendar = new GregorianCalendar(ConfigurationService.SERVER_TIME_ZONE);
            lastCaptureCalendar.setTimeInMillis(citadelCapture.getLastCapture());
            if(lastCaptureCalendar.get(Calendar.DAY_OF_YEAR) == dayOfYear || gregorianCalendar.getTimeInMillis() < now){
                gregorianCalendar.set(Calendar.DAY_OF_YEAR, dayOfYear + 1);
            }
            int dayOfWeek = gregorianCalendar.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY){
                return gregorianCalendar.getTimeInMillis();
            }
        }
        return null;
    }

    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));
        for (Claim claim : this.claims) {
            Location location = claim.getCenter();
            sender.sendMessage(ConfigurationService.YELLOW + "  Location: " + ConfigurationService.RED + '(' + ClaimableFaction.ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ() + ')');
        }
        long now = System.currentTimeMillis();
        if(citadelCapture != null && HCF.getInstance().getTimerManager().eventTimer.getEventFaction() != this) {
            Faction faction = citadelCapture.getFaction();
            if (faction != null) {
                boolean control = citadelCapture.hasControl(now);
                if (control) {
                    sender.sendMessage(ConfigurationService.YELLOW + "  Currently Controlled By:");
                } else {
                    sender.sendMessage(ConfigurationService.YELLOW + "  Previously Controlled By:");
                }
                sender.sendMessage(ConfigurationService.YELLOW + "   Faction: " + ConfigurationService.RED + faction.getName());
                sender.sendMessage(ConfigurationService.YELLOW + "   Time: " + ConfigurationService.RED + DurationFormatUtils.formatDurationWords((long)Math.ceil((now - citadelCapture.getLastCapture()) / (1000.0 * 60)) * (1000 * 60), true, true) + " ago");

                if (control) {
                    sender.sendMessage(ConfigurationService.YELLOW + "  Next Chest Reset: " + ConfigurationService.RED + DurationFormatUtils.formatDurationWords((long) Math.ceil((getNextChestReset(now) - now) / (1000.0 * 60)) * (1000 * 60), true, true));
                }
            }
        }
        if (this.captureZone != null) {
            long remainingCaptureMillis = this.captureZone.getRemainingCaptureMillis(now);
            long defaultCaptureMillis = this.captureZone.getDefaultCaptureMillis();
            if ((remainingCaptureMillis > 0L) && (remainingCaptureMillis != defaultCaptureMillis)) {
                sender.sendMessage(ConfigurationService.YELLOW + "  Remaining Time: " + ConfigurationService.RED + DurationFormatUtils.formatDurationWords(remainingCaptureMillis, true, true));
            }
            sender.sendMessage(ConfigurationService.YELLOW + "  Capture Delay: " + ConfigurationService.RED + this.captureZone.getDefaultCaptureWords());
            if ((this.captureZone.getCappingPlayer() != null) && (sender.hasPermission("hcf.citadel.checkcapper"))) {
                Player capping = this.captureZone.getCappingPlayer();
                PlayerFaction playerFaction = HCF.getInstance().getFactionManager().getPlayerFaction(capping);
                sender.sendMessage(ConfigurationService.YELLOW + "  Current Capper: " + ConfigurationService.GOLD + (capping == null ? "None" : capping.getName() + (playerFaction != null ? ConfigurationService.GRAY + " [" + playerFaction.getName() + "]" : "")));
            }
        }
        sender.sendMessage(ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public void fillChests(){
        fillChests(false);
    }

    public void fillChests(boolean announce){
        if(announce) {
            Bukkit.broadcastMessage(getEventType().getPrefix() + ChatColor.LIGHT_PURPLE + getName() + ConfigurationService.YELLOW + " has now regenerated.");
        }
        for(Claim claim: getClaims()){
            for(Chunk chunk: claim.getChunks()){
                for(BlockState blockState: chunk.getTileEntities()){
                    if(blockState instanceof Chest){
                        Chest chest = (Chest) blockState;
                        ItemStack[] array = new ItemStack[chest.getBlockInventory().getSize()];
                        array[9 + 2] = generateCitadel();
                        array[9 + 4] = generateCitadel();
                        array[9 + 6] = generateCitadel();
                        chest.getBlockInventory().setContents(array);
                    }
                }
            }
        }
    }

    private static final String ITEM_PREFIX = ChatColor.DARK_AQUA + "[Citadel] " + ChatColor.WHITE;

    private static final Material[] ARMOR = {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS};

    private static final ItemStack[] ITEMS = {
            new ItemStack(Material.WEB, 16),
            new ItemStack(Material.GOLD_BLOCK, 16),
            new ItemStack(Material.DIAMOND_BLOCK, 16),
            new ItemStack(Material.EMERALD_BLOCK, 16),
            new ItemStack(Material.IRON_BLOCK, 16),
            new ItemStack(Material.ENDER_PEARL, 32),
            new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1),
            new ItemStack(Material.GOLDEN_APPLE, 16),
            new ItemStack(Material.BEACON, 1)
    };

    private ItemStack generateCitadel(){
        Random random = new Random();
        ItemStack itemStack;
        if(random.nextInt(100) < 35) {
            String id = null;
            Material material;
            Enchantment enchantment;
            int item = random.nextInt(9);
            if (item <= 1) {
                material = Material.DIAMOND_SWORD;
                enchantment = Enchantment.DAMAGE_ALL;
            }
            else if(item == 2){
                material = Material.DIAMOND_SWORD;
                enchantment = Enchantment.LOOT_BONUS_MOBS;
                id = "Looting";
            }
            else if(item == 3){
                material = Material.BOW;
                enchantment = null;
            }
            else{
                material = ARMOR[ThreadLocalRandom.current().nextInt(ARMOR.length)];
                enchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
            }
            String name = id != null ? id : SpawnerTradeListener.capitalizeString(material.name().replace("_", "").replace("DIAMOND", "").toLowerCase());
            itemStack = new ItemBuilder(material).displayName(ITEM_PREFIX + name).build();
            if(enchantment != null) {
                itemStack.addEnchantment(enchantment, ConfigurationService.ENCHANTMENT_LIMITS.getOrDefault(enchantment, 1) + 2);
            }
            itemStack.addEnchantment(Enchantment.DURABILITY, 3);
            if (material == Material.DIAMOND_BOOTS) {
                itemStack.addEnchantment(Enchantment.PROTECTION_FALL, 4);
            }
            else if(material == Material.BOW){
                itemStack.addEnchantment(Enchantment.ARROW_DAMAGE, ConfigurationService.ENCHANTMENT_LIMITS.get(Enchantment.ARROW_DAMAGE) + 1);
                itemStack.addEnchantment(Enchantment.ARROW_FIRE, 1);
                itemStack.addEnchantment(Enchantment.ARROW_INFINITE, 1);
            }
        }
        else{
            int number = random.nextInt(ITEMS.length);
            itemStack = ITEMS[number].clone();
        }
        return itemStack;
    }

    public CitadelCapture getCitadelCapture() {
        return citadelCapture;
    }

    public void setCitadelCapture(CitadelCapture citadelCapture) {
        this.citadelCapture = citadelCapture;
    }

    @Override
    public String getDisplayName(Faction faction) {
        if(citadelCapture != null && citadelCapture.hasControl() && citadelCapture.getFaction() != null){
            return super.getDisplayName(faction) + ConfigurationService.YELLOW + " (" + citadelCapture.getFaction().getDisplayName(faction) + ConfigurationService.YELLOW + ")";
        }
        return super.getDisplayName(faction);
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        if(citadelCapture != null && citadelCapture.hasControl() && citadelCapture.getFaction() != null){
            return super.getDisplayName(sender) + ConfigurationService.YELLOW + " (" + citadelCapture.getFaction().getDisplayName(sender) + ConfigurationService.YELLOW + ")";
        }
        return super.getDisplayName(sender);
    }

    @Override
    public String getPrefix(){
        return CitadelTracker.PREFIX;
    }
}