package com.faithfulmc.util.itemdb;

import com.faithfulmc.framework.BasePlugin;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Ints;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleItemDb implements ItemDb {
    private static final Comparator<String> STRING_LENGTH_COMPARATOR;
    private static final Pattern PARTS_PATTERN;

    static {
        STRING_LENGTH_COMPARATOR = ((o1, o2) -> o1.length() - o2.length());
        PARTS_PATTERN = Pattern.compile("[^a-z0-9]");
    }

    private final TObjectIntMap<String> items;
    private final TreeMultimap<ItemData, String> names;
    private final Map<ItemData, String> primaryName;
    private final TObjectShortMap<String> durabilities;
    private final ManagedFile file;
    private final Pattern splitPattern;

    public SimpleItemDb(final JavaPlugin plugin) {
        this.items =new TObjectIntHashMap<>();
        this.names = TreeMultimap.create(Ordering.allEqual(), SimpleItemDb.STRING_LENGTH_COMPARATOR);
        this.primaryName = new HashMap<ItemData, String>();
        this.durabilities = new TObjectShortHashMap<>();
        this.splitPattern = Pattern.compile("((.*)[:+',;.](\\d+))");
        this.file = new ManagedFile("items.csv", plugin);
        this.reloadItemDatabase();
    }

    @Override
    public void reloadItemDatabase() {
        if (this.file.getFile() != null) {
            final List<String> lines = this.file.getLines();
            if (!lines.isEmpty()) {
                this.durabilities.clear();
                this.items.clear();
                this.names.clear();
                this.primaryName.clear();
                for (String line : lines) {
                    line = line.trim().toLowerCase(Locale.ENGLISH);
                    if (line.length() <= 0 || line.charAt(0) != '#') {
                        final String[] parts = SimpleItemDb.PARTS_PATTERN.split(line);
                        if (parts.length < 2) {
                            continue;
                        }
                        Material material;
                        try {
                            final int data = Integer.parseInt(parts[1]);
                            material = Material.getMaterial(data);
                        } catch (IllegalArgumentException var3) {
                            material = Material.getMaterial(parts[1]);
                        }
                        final short data2 = ((parts.length > 2 && !parts[2].equals("0")) ? Short.parseShort(parts[2]) : 0);
                        final String itemName = parts[0].toLowerCase(Locale.ENGLISH);
                        this.durabilities.put(itemName, data2);
                        this.items.put(itemName, material.getId());
                        final ItemData itemData = new ItemData(material, data2);
                        if (this.names.containsKey((Object) itemData)) {
                            this.names.get(itemData).add(itemName);
                        } else {
                            this.names.put(itemData, itemName);
                            this.primaryName.put(itemData, itemName);
                        }
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getPotion(final String id) {
        return this.getPotion(id, 1);
    }

    @Override
    public ItemStack getPotion(String id, final int quantity) {
        int length = id.length();
        if (length <= 1) {
            return null;
        }
        boolean splash = false;
        if (length > 1 && id.endsWith("s")) {
            --length;
            id = id.substring(0, length);
            splash = true;
            if (length <= 1) {
                return null;
            }
        }
        boolean extended = false;
        if (id.endsWith("e")) {
            --length;
            id = id.substring(0, length);
            extended = true;
            if (length <= 1) {
                return null;
            }
        }
        final Integer level = Ints.tryParse(id.substring(length - 1, length));
        --length;
        id = id.substring(0, length);
        final String lowerCase = id.toLowerCase(Locale.ENGLISH);
        PotionType type = null;
        byte result = -1;
        switch (lowerCase.hashCode()) {
            case 3212: {
                if (lowerCase.equals("dp")) {
                    result = 2;
                    break;
                }
                break;
            }
            case 3336: {
                if (lowerCase.equals("hp")) {
                    result = 0;
                    break;
                }
                break;
            }
            case 3584: {
                if (lowerCase.equals("pp")) {
                    result = 7;
                    break;
                }
                break;
            }
            case 3646: {
                if (lowerCase.equals("rp")) {
                    result = 1;
                    break;
                }
                break;
            }
            case 3801: {
                if (lowerCase.equals("wp")) {
                    result = 6;
                    break;
                }
                break;
            }
            case 101668: {
                if (lowerCase.equals("frp")) {
                    result = 8;
                    break;
                }
                break;
            }
            case 109480: {
                if (lowerCase.equals("nvp")) {
                    result = 10;
                    break;
                }
                break;
            }
            case 113975: {
                if (lowerCase.equals("slp")) {
                    result = 4;
                    break;
                }
                break;
            }
            case 114316: {
                if (lowerCase.equals("swp")) {
                    result = 3;
                    break;
                }
                break;
            }
            case 3237535: {
                if (lowerCase.equals("invp")) {
                    result = 9;
                    break;
                }
                break;
            }
            case 3541087: {
                if (lowerCase.equals("strp")) {
                    result = 5;
                    break;
                }
                break;
            }
        }
        switch (result) {
            case 0: {
                type = PotionType.FIRE_RESISTANCE;
                break;
            }
            case 1: {
                type = PotionType.REGEN;
                break;
            }
            case 2: {
                type = PotionType.INSTANT_DAMAGE;
                break;
            }
            case 3: {
                type = PotionType.SPEED;
                break;
            }
            case 4: {
                type = PotionType.SLOWNESS;
                break;
            }
            case 5: {
                type = PotionType.STRENGTH;
                break;
            }
            case 6: {
                type = PotionType.WEAKNESS;
                break;
            }
            case 7: {
                type = PotionType.POISON;
                break;
            }
            case 8: {
                type = PotionType.FIRE_RESISTANCE;
                break;
            }
            case 9: {
                type = PotionType.INVISIBILITY;
                break;
            }
            case 10: {
                type = PotionType.NIGHT_VISION;
                break;
            }
            default: {
                return null;
            }
        }
        if (level != null && level <= type.getMaxLevel()) {
            final Potion potion = new Potion(type);
            potion.setLevel((int) level);
            potion.setSplash(splash);
            potion.setHasExtendedDuration(extended);
            final ItemStack var11 = potion.toItemStack(quantity);
            var11.setDurability((short) (var11.getDurability() + 8192));
            return var11;
        }
        return null;
    }

    @Override
    public ItemStack getItem(final String id) {
        final ItemStack result = this.getItem(id, 1);
        if (result == null) {
            return null;
        }
        result.setAmount(result.getMaxStackSize());
        return result;
    }

    @Override
    public ItemStack getItem(final String id, final int quantity) {
        ItemStack result = this.getPotion(id, quantity);
        if (result != null) {
            return result;
        }
        int itemId = 0;
        short metaData = 0;
        final Matcher parts = this.splitPattern.matcher(id);
        String itemName;
        if (parts.matches()) {
            itemName = parts.group(2);
            metaData = Short.parseShort(parts.group(3));
        } else {
            itemName = id;
        }
        Integer last;
        if ((last = Ints.tryParse(itemName)) != null) {
            itemId = last;
        } else if ((last = Ints.tryParse(id)) != null) {
            itemId = last;
        } else {
            itemName = itemName.toLowerCase(Locale.ENGLISH);
        }
        if (itemId < 1) {
            if (this.items.containsKey((Object) itemName)) {
                itemId = this.items.get((Object) itemName);
                if (this.durabilities.containsKey((Object) itemName) && metaData == 0) {
                    metaData = this.durabilities.get((Object) itemName);
                }
            } else if (Material.getMaterial(itemName.toUpperCase(Locale.ENGLISH)) != null) {
                final Material mat = Material.getMaterial(itemName.toUpperCase(Locale.ENGLISH));
                itemId = mat.getId();
            } else {
                try {
                    final Material mat = Bukkit.getUnsafe().getMaterialFromInternalName(itemName.toLowerCase(Locale.ENGLISH));
                    itemId = mat.getId();
                } catch (Exception var10) {
                    return null;
                }
            }
        }
        if (itemId < 1) {
            return null;
        }
        final Material mat = Material.getMaterial(itemId);
        if (mat == null) {
            return null;
        }
        result = new ItemStack(mat);
        result.setAmount(quantity);
        result.setDurability(metaData);
        return result;
    }

    @Override
    public List<?> getMatching(final Player player, final String[] args) {
        final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        final PlayerInventory inventory = player.getInventory();
        if (args.length >= 1 && !args[0].equalsIgnoreCase("hand")) {
            if (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("invent") || args[0].equalsIgnoreCase("all")) {
                for (final ItemStack stack : inventory.getContents()) {
                    if (stack != null && stack.getType() != Material.AIR) {
                        items.add(stack);
                    }
                }
            } else if (args[0].equalsIgnoreCase("blocks")) {
                for (final ItemStack stack : inventory.getContents()) {
                    if (stack != null && stack.getType() != Material.AIR && stack.getType().isBlock()) {
                        items.add(stack);
                    }
                }
            } else {
                items.add(this.getItem(args[0]));
            }
        } else {
            items.add(player.getItemInHand());
        }
        return (!items.isEmpty() && items.get(0).getType() != Material.AIR) ? items : null;
    }

    @Override
    public String getName(final ItemStack item) {
        return BasePlugin.getPlugin().getNmsProvider().getName(item);
    }

    @Deprecated
    @Override
    public String getPrimaryName(final ItemStack item) {
        ItemData itemData = new ItemData(item.getType(), item.getDurability());
        String name = this.primaryName.get(itemData);
        if (name == null) {
            itemData = new ItemData(item.getType(), (short) 0);
            name = this.primaryName.get(itemData);
            if (name == null) {
                return null;
            }
        }
        return name;
    }

    @Override
    public String getNames(final ItemStack item) {
        ItemData itemData = new ItemData(item.getType(), item.getDurability());
        NavigableSet<String> nameList = (NavigableSet<String>) this.names.get(itemData);
        if (nameList == null) {
            itemData = new ItemData(item.getType(), (short) 0);
            nameList = (NavigableSet<String>) this.names.get(itemData);
            if (nameList == null) {
                return null;
            }
        }
        Object list = new ArrayList(nameList);
        if (nameList.size() > 15) {
            list = ((List) list).subList(0, 14);
        }
        return StringUtils.join((Iterable) list, ", ");
    }
}
