package com.faithfulmc.hardcorefactions.deathban;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.util.PersistableLocation;
import com.google.common.collect.Maps;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class DeathData{

    @SuppressWarnings("unchecked")
    public static ItemStack[] deserializeItems(Object object){
        return ((List<Document>) object).stream().map(document -> {
            if(document == null || document.isEmpty()){
                return null;
            }
            int amount = document.getInteger("amount");
            int type = document.getInteger("type");
            int data = document.getInteger("data");
            String displayName = document.getString("displayName");
            List<String> lore = document.get("lore", List.class);
            List<Document> enchantments = document.get("enchantments", List.class);
            ItemStack itemStack = new ItemStack(type, amount, (short)data);
            if(displayName != null || lore != null || enchantments != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    if (displayName != null) {
                        itemMeta.setDisplayName(displayName);
                    }
                    if(lore != null){
                        itemMeta.setLore(lore);
                    }
                    if(enchantments != null){
                        enchantments.forEach(enchant -> itemMeta.addEnchant(Enchantment.getById(enchant.getInteger("enchantment")), enchant.getInteger("level"), true));
                    }
                }
                itemStack.setItemMeta(itemMeta);
            }
            return itemStack;
        }).toArray(ItemStack[]::new);
    }

    public static List<Document> serializeItems(ItemStack[] itemStacks){
        return Arrays.stream(itemStacks).map(itemStack -> {
            Document document = new Document();
            if(itemStack != null){
                document.put("amount", itemStack.getAmount());
                document.put("type", itemStack.getTypeId());
                document.put("data", (int) itemStack.getDurability());
                if(itemStack.hasItemMeta()){
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if(itemMeta.hasDisplayName()){
                        document.put("displayName", itemMeta.getDisplayName());
                    }
                    if(itemMeta.hasLore()){
                        document.put("lore", itemMeta.getLore());
                    }
                    if(itemMeta.hasEnchants()){
                        document.put("enchants", itemMeta.getEnchants().entrySet().stream().map(
                                entry -> {
                                    Document enchant = new Document();
                                    enchant.put("enchantment", entry.getKey().getId());
                                    enchant.put("level", entry.getValue());
                                    return enchant;
                                }
                        ).collect(Collectors.toList()));
                    }
                }
            }
            return document;
        }).collect(Collectors.toList());
    }

    private ObjectId objectId;
    private final long timeStamp;
    private final UUID player;
    private final String playerName;
    private final UUID killer;
    private final String killerName;
    private final PersistableLocation location;
    private final ItemStack[] inventory;
    private final ItemStack[] armorContents;

    public DeathData(Map map){
        if(ConfigurationService.MONGO){
            objectId = (ObjectId) map.get("_id");
        }
        timeStamp = (Long) map.get("timeStamp");
        player = (UUID) map.get("player");
        playerName = (String) map.get("playerName");
        killer = (UUID) map.get("killer");
        killerName = (String) map.get("killerName");
        location = ConfigurationService.MONGO ? new PersistableLocation((Map) map.get("location")) : (PersistableLocation) map.get("location");
        inventory = ConfigurationService.MONGO ? deserializeItems(map.get("inventory")) : (ItemStack[]) map.get("inventory");
        armorContents = ConfigurationService.MONGO ? deserializeItems(map.get("armorContents")) : (ItemStack[]) map.get("armorContents");
    }

    public DeathData(long timeStamp, UUID player, String playerName, UUID killer, String killerName, PersistableLocation location, ItemStack[] inventory, ItemStack[] armorContents) {
        if(ConfigurationService.MONGO) {
            this.objectId = new ObjectId(new Date(timeStamp));
        }
        this.timeStamp = timeStamp;
        this.player = player;
        this.playerName = playerName;
        this.killer = killer;
        this.killerName = killerName;
        this.location = location;
        this.inventory = inventory;
        this.armorContents = armorContents;
    }

    @SuppressWarnings("unchecked")
    public Map serialize(){
        Map map = Maps.newHashMap();
        if(ConfigurationService.MONGO) {
            map.put("objectId", objectId);
        }
        map.put("timeStamp", timeStamp);
        map.put("player", player);
        map.put("playerName", playerName);
        if(killer != null){
            map.put("killer", killer);
        }
        if(killerName != null){
            map.put("killerName", killerName);
        }
        map.put("location", ConfigurationService.MONGO ? new Document(location.serialize()) : location);
        map.put("inventory", ConfigurationService.MONGO ? serializeItems(inventory) : inventory);
        map.put("armorContents", ConfigurationService.MONGO ? serializeItems(armorContents) : armorContents);
        return map;
    }
}
