package com.faithfulmc.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Set;

public final class InventoryUtils {
    public static final int DEFAULT_INVENTORY_WIDTH = 9;
    public static final int MINIMUM_INVENTORY_HEIGHT = 1;
    public static final int MINIMUM_INVENTORY_SIZE = 9;
    public static final int MAXIMUM_INVENTORY_HEIGHT = 6;
    public static final int MAXIMUM_INVENTORY_SIZE = 54;
    public static final int MAXIMUM_SINGLE_CHEST_SIZE = 27;
    public static final int MAXIMUM_DOUBLE_CHEST_SIZE = 54;

    public static ItemStack[] deepClone(final ItemStack[] origin) {
        Preconditions.checkNotNull((Object) origin, (Object) "Origin cannot be null");
        final ItemStack[] cloned = new ItemStack[origin.length];
        for (int i = 0; i < origin.length; ++i) {
            final ItemStack next = origin[i];
            cloned[i] = ((next == null) ? null : next.clone());
        }
        return cloned;
    }

    public static int getSafestInventorySize(final int initialSize) {
        return (initialSize + 8) / 9 * 9;
    }

    public static void removeItem(final Inventory inventory, final Material type, final short data, final int quantity) {
        final ItemStack[] contents = inventory.getContents();
        final boolean compareDamage = type.getMaxDurability() == 0;
        for (int i = quantity; i > 0; --i) {
            final ItemStack[] var7 = contents;
            final int var8 = contents.length;
            int var9 = 0;
            while (var9 < var8) {
                final ItemStack content = var7[var9];
                if (content != null && content.getType() == type && (!compareDamage || content.getData().getData() == data)) {
                    if (content.getAmount() <= 1) {
                        inventory.removeItem(new ItemStack[]{content});
                        break;
                    }
                    content.setAmount(content.getAmount() - 1);
                    break;
                } else {
                    ++var9;
                }
            }
        }
    }

    public static int countAmount(final Inventory inventory, final Material type, final short data) {
        final ItemStack[] contents = inventory.getContents();
        final boolean compareDamage = type.getMaxDurability() == 0;
        int counter = 0;
        final ItemStack[] var6 = contents;
        for (int var7 = contents.length, var8 = 0; var8 < var7; ++var8) {
            final ItemStack item = var6[var8];
            if (item != null && item.getType() == type && (!compareDamage || item.getData().getData() == data)) {
                counter += item.getAmount();
            }
        }
        return counter;
    }

    public static boolean isEmpty(final Inventory inventory) {
        return isEmpty(inventory, true);
    }

    public static boolean isEmpty(final Inventory inventory, final boolean checkArmour) {
        boolean result = true;
        ItemStack[] armorContents;
        final ItemStack[] contents2 = armorContents = inventory.getContents();
        for (int var6 = contents2.length, var7 = 0; var7 < var6; ++var7) {
            final ItemStack content = armorContents[var7];
            if (content != null && content.getType() != Material.AIR) {
                result = false;
                break;
            }
        }
        if (!result) {
            return false;
        }
        if (checkArmour && inventory instanceof PlayerInventory) {
            final ItemStack[] var8;
            armorContents = (var8 = ((PlayerInventory) inventory).getArmorContents());
            for (int var7 = armorContents.length, var9 = 0; var9 < var7; ++var9) {
                final ItemStack content2 = var8[var9];
                if (content2 != null && content2.getType() != Material.AIR) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean clickedTopInventory(final InventoryDragEvent event) {
        final InventoryView view = event.getView();
        final Inventory topInventory = view.getTopInventory();
        if (topInventory == null) {
            return false;
        }
        boolean result = false;
        final Set<Map.Entry<Integer, ItemStack>> entrySet = event.getNewItems().entrySet();
        final int size = topInventory.getSize();
        for (final Map.Entry<Integer, ItemStack> entry : entrySet) {
            if (entry.getKey() < size) {
                result = true;
                break;
            }
        }
        return result;
    }
}
