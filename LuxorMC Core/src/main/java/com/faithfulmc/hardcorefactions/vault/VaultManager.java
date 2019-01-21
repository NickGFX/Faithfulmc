package com.faithfulmc.hardcorefactions.vault;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class VaultManager implements InventoryHolder{
    private final HCF plugin;
    public static int ROWS = 0;
    private Config config;
    private ConcurrentMap<UUID, ItemStack[]> storageMap;

    public VaultManager(HCF plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig(){
        storageMap = new ConcurrentHashMap<>();
        config = new Config(plugin, "vaults");
        for(String key: config.getKeys(false)){
            UUID uuid = UUID.fromString(key);
            Object vaultObject = config.get(key);
            ItemStack[] stacks;
            if(vaultObject instanceof List){
                List<ItemStack> itemList = (List<ItemStack>) vaultObject;
                stacks = itemList.toArray(new ItemStack[itemList.size()]);
            }
            else if(vaultObject instanceof ItemStack[]){
                stacks = (ItemStack[]) vaultObject;
            }
            else{
                continue;
            }
            storageMap.put(uuid, stacks);
        }
    }

    public void saveVault(UUID uuid, Inventory inventory){
        storageMap.put(uuid, inventory.getContents());
    }

    public ItemStack[] getVault(UUID uuid, int size){
        ItemStack[] storage = storageMap.get(uuid);
        if(storage == null){
            storage = new ItemStack[size];
        }
        else if(storage.length > size){
            List<ItemStack> itemStacks = new ArrayList<>();
            for(ItemStack stack: storage){
                if(stack != null && stack.getType() != Material.AIR){
                    itemStacks.add(stack);
                }
            }
            if(itemStacks.size() > size){
                itemStacks = itemStacks.subList(0, size);
            }
            storage = itemStacks.toArray(new ItemStack[size]);
        }
        else if(storage.length < size){
            storage = Arrays.asList(storage).toArray(new ItemStack[size]);
        }
        return storage;
    }

    public int getRows(Permissible permissible){
        int current = ROWS;
        for(Map.Entry<String, Integer> entry: ConfigurationService.VAULT_ROWS.entrySet()){
            if(permissible.hasPermission("hcf.playervaults." + entry.getKey()) && entry.getValue() > current){
                current = entry.getValue();
            }
        }
        return current;
    }

    public Inventory createVault(Player player, int rows){
        Inventory inventory = Bukkit.createInventory(this, rows * 9, ConfigurationService.GOLD + ConfigurationService.DOUBLEARROW + ConfigurationService.YELLOW + " Vault");
        inventory.setContents(getVault(player.getUniqueId(), rows * 9));
        return inventory;
    }

    public void saveConfig(){
        for(Map.Entry<UUID, ItemStack[]> entry: storageMap.entrySet()){
            config.set(entry.getKey().toString(), new LinkedList<>(Arrays.asList(entry.getValue())));
        }
        config.save();
    }



    public Inventory getInventory() {
        return null;
    }
}
