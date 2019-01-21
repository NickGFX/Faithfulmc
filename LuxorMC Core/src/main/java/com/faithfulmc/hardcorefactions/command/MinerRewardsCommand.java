package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerLevel;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MinerRewardsCommand implements CommandExecutor{
    public static final Map<PotionEffectType, String> NICKS = new ImmutableMap.Builder<PotionEffectType, String>()
            .put(PotionEffectType.SPEED, "Speed")
            .put(PotionEffectType.FAST_DIGGING, "Haste")
            .put(PotionEffectType.SATURATION, "Saturation")
            .put(PotionEffectType.DAMAGE_RESISTANCE, "Resistance")
            .build();

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 50));
        commandSender.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "Miner Upgrades");
        commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 50));
        for(MinerLevel minerLevel: MinerLevel.values()){
            if(minerLevel != MinerLevel.DEFAULT){
                commandSender.sendMessage(ChatColor.AQUA + " " + minerLevel.getNick());
                commandSender.sendMessage(ConfigurationService.GRAY + "   Diamonds " + ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + ConfigurationService.WHITE + " " + minerLevel.getAmount());
                commandSender.sendMessage(ConfigurationService.GRAY + "   Effects " +  ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + ConfigurationService.WHITE + " " + formatEffects(minerLevel.getGive()));
            }
        }
        commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 50));
        return true;
    }

    public String getNick(PotionEffect effect){
        String nick = NICKS.get(effect.getType());
        if(nick != null){
            return nick + " " + (effect.getAmplifier() + 1);
        }
        return null;
    }

    public String formatEffects(List<PotionEffect> potionEffectTypeList){
        return potionEffectTypeList.stream().map(this::getNick).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
}
