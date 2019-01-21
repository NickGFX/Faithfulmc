package com.faithfulmc.util.chat;

import com.faithfulmc.util.MoreObjects;
import net.minecraft.server.v1_7_R4.Item;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R4.potion.CraftPotionEffectType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang {
    private static final Pattern PAT;

    public static void initialize(String lang) throws IOException {
        Lang.translations = new HashMap<>();
        if (lang == null) {
            lang = "en_US";
        }
        if (!lang.equals(Lang.language)) {
            InputStream stream = null;
            BufferedReader reader = null;
            try {
                Lang.language = lang;
                final String resourcePath = "/assets/minecraft/lang/" + Lang.language + ".lang";
                stream = Item.class.getResourceAsStream(resourcePath);
                reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.contains("=")) {
                        final Matcher matcher = Lang.PAT.matcher(line);
                        if (!matcher.matches()) {
                            continue;
                        }
                        Lang.translations.put(matcher.group(1), matcher.group(2));
                    }
                }
            } finally {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }

    public static String getLanguage() {
        return Lang.language;
    }

    public static String translatableFromStack(final ItemStack stack) {
        final net.minecraft.server.v1_7_R4.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        final Item item = nms.getItem();
        return item.a(nms) + ".name";
    }

    public static String fromStack(final ItemStack stack) {
        final String node = translatableFromStack(stack);
        return (String) MoreObjects.firstNonNull(Lang.translations.get(node), (Object) node);
    }

    public static String translatableFromEnchantment(final Enchantment ench) {
        final net.minecraft.server.v1_7_R4.Enchantment nms = net.minecraft.server.v1_7_R4.Enchantment.byId[ench.getId()];
        return (nms == null) ? ench.getName() : nms.a();
    }

    public static String fromEnchantment(final Enchantment ench) {
        final String node = translatableFromEnchantment(ench);
        return (String) MoreObjects.firstNonNull(Lang.translations.get(node), (Object) node);
    }

    public static String translatableFromPotionEffectType(final PotionEffectType effectType) {
        final CraftPotionEffectType craftType = (CraftPotionEffectType) PotionEffectType.getById(effectType.getId());
        return craftType.getHandle().a();
    }

    public static String fromPotionEffectType(final PotionEffectType effectType) {
        final String node = translatableFromPotionEffectType(effectType);
        final String val = Lang.translations.get(node);
        return (val == null) ? node : val;
    }

    public static String translate(final String key, final Object... args) {
        return String.format(Lang.translations.get(key), args);
    }
    private static Map<String, String> translations;
    private static String language;

    static {
        PAT = Pattern.compile("^\\s*([\\w\\d\\.]+)\\s*=\\s*(.*)\\s*$");
        Lang.language = null;
    }
}
