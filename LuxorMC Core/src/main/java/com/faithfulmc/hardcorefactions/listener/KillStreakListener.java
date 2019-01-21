package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.UUID;

public class KillStreakListener implements Listener {
    private final HCF plugin;
    private final KillStreakReward[] rewards = {new KillStreakReward("3x Crapples") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if (ks == 3) {
                for (ItemStack itemStack : player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 3)).values()) {
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                }
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Double Debuffs") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if (ks == 5) {
                Potion poison = new Potion(PotionType.POISON, Potion.Tier.ONE, true, false);
                Potion slowness = new Potion(PotionType.SLOWNESS, Potion.Tier.ONE, true, false);
                for (ItemStack itemStack : player.getInventory().addItem(poison.toItemStack(1), slowness.toItemStack(1)).values()) {
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                }
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Invisibility") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if (ks == 10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 60 * 5, 0));
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Gopple") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if (ks == 20) {
                for (ItemStack itemStack : player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1)).values()) {
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                }
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Strength 2") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if (ks == 30) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, 1));
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Mega Money") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if(ks == 50){
                factionUser.setBalance(factionUser.getBalance() + 5000);
                player.sendMessage(ConfigurationService.YELLOW + "You earned " + ConfigurationService.GOLD + ChatColor.BOLD + "$5000" + ConfigurationService.YELLOW + " from your killstreak.");
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Big Bucks") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if(ks == 75){
                factionUser.setBalance(factionUser.getBalance() + 10000);
                player.sendMessage(ConfigurationService.YELLOW + "You earned " + ConfigurationService.GOLD + ChatColor.BOLD + "$10000" + ConfigurationService.YELLOW + " from your killstreak.");
                return true;
            }
            return false;
        }
    }, new KillStreakReward("Cash King") {
        public boolean execute(Player player, int ks, FactionUser factionUser) {
            if(ks == 100){
                factionUser.setBalance(factionUser.getBalance() + 15000);
                player.sendMessage(ConfigurationService.YELLOW + "You earned " + ConfigurationService.GOLD + ChatColor.BOLD + "$15000" + ConfigurationService.YELLOW + " from your killstreak.");
                return true;
            }
            return false;
        }
    }
    };

    public KillStreakListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player died = event.getEntity();
        if(died.hasMetadata(DeathListener.DEATH_META)) {
            MetadataValue metadataValue = BukkitUtils.getMetaData(died, DeathListener.DEATH_META, plugin);
            Player killer = (Player) metadataValue.value();
            Player player = event.getEntity();
            if(killer != null && player != null && killer != player) {
                boolean boosting = false;
                boolean ignoreKillstreak = false;
                if(killer.hasMetadata(LAST_KILL_META)){
                    UUID lastKilled = (UUID) BukkitUtils.getMetaData(killer, LAST_KILL_META, plugin).value();
                    if(lastKilled == player.getUniqueId()){
                        int boost = killer.hasMetadata(KILL_BOOST_META) ? BukkitUtils.getMetaData(killer, KILL_BOOST_META, plugin).asInt() + 1: 1;
                        killer.removeMetadata(KILL_BOOST_META, plugin);
                        killer.setMetadata(KILL_BOOST_META, new FixedMetadataValue(plugin, boost));
                        if(boost >= 5){
                            Bukkit.broadcast(ChatColor.RED + ChatColor.BOLD.toString() + killer.getName() + ChatColor.GRAY + " has a kill boost of " + ChatColor.RED + ChatColor.BOLD.toString() + boost, "base.command.staffchat");
                            ignoreKillstreak = true;
                            if(boost >= 20) {
                                boosting = true;
                            }
                        }
                    }
                    else{
                        killer.removeMetadata(KILL_BOOST_META, plugin);
                    }
                    killer.removeMetadata(LAST_KILL_META, plugin);
                }
                killer.setMetadata(LAST_KILL_META, new FixedMetadataValue(plugin, player.getUniqueId()));
                if (player.isOnline()) {
                    int money = getMoney(killer);
                    FactionUser factionUser = plugin.getUserManager().getUser(killer.getUniqueId());
                    if(boosting){
                        factionUser.setKillStreak(Math.max(0, factionUser.getKillStreak() - 20));
                        factionUser.setKills(Math.max(0, factionUser.getKills() - 20));
                    }
                    else if(!ignoreKillstreak){
                        factionUser.setBalance(money + factionUser.getBalance());
                        killer.sendMessage(ConfigurationService.YELLOW + "You received " + ConfigurationService.GOLD + ChatColor.BOLD + "$" + money + ConfigurationService.YELLOW + " for killing " + ConfigurationService.GOLD + event.getEntity().getName());
                        int ks = factionUser.getKillStreak();
                        for (KillStreakReward killStreakReward : rewards) {
                            if (killStreakReward.execute(killer, ks, factionUser)) {
                                Bukkit.broadcastMessage(ConfigurationService.GRAY + "[" + ConfigurationService.YELLOW + "*" + ConfigurationService.GRAY + "] " + ConfigurationService.RED + ChatColor.BOLD + killer.getName() + ConfigurationService.YELLOW + " received " + ConfigurationService.GOLD + ChatColor.BOLD + killStreakReward.getDisplay() + ConfigurationService.YELLOW + " from their killstreak of " + ConfigurationService.RED + ChatColor.BOLD + ks);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static final String MONEY_PERMISSION = "hcf.kitmap.money.extra.";
    public static final String LAST_KILL_META = "LAST_KILL";
    public static final String KILL_BOOST_META = "KILL_BOOST";

    public int getMoney(Permissible permissible){
        int money = 100;
        for(int i = 10; i <= 80; i+=10){
            int total = 100 + i;
            if(total > money && permissible.hasPermission(MONEY_PERMISSION + (i))){
                money = total;
            }
        }
        return money;
    }


    public abstract class KillStreakReward {
        private final String display;

        public KillStreakReward(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        public abstract boolean execute(Player player, int ks, FactionUser factionUser);
    }
}
