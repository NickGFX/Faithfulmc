package com.faithfulmc.hardcorefactions.kit;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.ItemBuilder;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GodKit {
    private final String id;
    private final String display;
    private final int slot;
    private final Material type;
    private final short data;

    public GodKit(String id, String display, int slot, Material type, short data) {
        this.id = id;
        this.display = display;
        this.slot = slot;
        this.type = type;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public int getSlot() {
        return slot;
    }

    public Material getType() {
        return type;
    }

    public short getData() {
        return data;
    }

    public Kit getKit(){
        return HCF.getInstance().getKitManager().getKit(id);
    }

    public ItemStack createItem(Player player, FactionUser factionUser, List<String> loreLines){
        Kit kit = getKit();
        if(kit != null && kit.isEnabled()) {
            boolean hasPermission = player.hasPermission(kit.getPermissionNode());
            String kitDescription = kit.getDescription();
            long cooldown = factionUser.getRemainingKitCooldown(kit);
            String status = hasPermission ? cooldown < 0 ? ChatColor.GREEN + "Click to use this kit" : ChatColor.WHITE + DurationFormatUtils.formatDurationWords(cooldown, true, true) : ChatColor.RED + "You do not have access to this god kit";
            ItemBuilder itemBuilder = new ItemBuilder(type).data(data).displayName(display);
            itemBuilder.lore(loreLines.stream().map(line -> line
                    .replace("%description%", kitDescription == null ? "" : kitDescription)
                    .replace("%status%", status)
            ).toArray(String[]::new));
            return itemBuilder.build();
        }
        return null;
    }
}
