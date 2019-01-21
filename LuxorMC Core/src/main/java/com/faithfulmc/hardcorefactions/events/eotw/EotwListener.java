package com.faithfulmc.hardcorefactions.events.eotw;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionClaimChangeEvent;
import com.faithfulmc.hardcorefactions.faction.event.FactionCreateEvent;
import com.faithfulmc.hardcorefactions.faction.event.cause.ClaimChangeCause;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.kit.event.KitApplyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;


public class EotwListener implements org.bukkit.event.Listener {
    private final HCF plugin;


    public EotwListener(HCF plugin) {
        this.plugin = plugin;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLandClaim(KitApplyEvent event) {
        if ((!event.isForce()) && (this.plugin.getEotwHandler().isEndOfTheWorld())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ConfigurationService.RED + "Kits cannot be applied during EOTW.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionCreate(FactionCreateEvent event){
        if(event.getFaction() instanceof PlayerFaction && this.plugin.getEotwHandler().isEndOfTheWorld()){
            ((PlayerFaction) event.getFaction()).setDeathsUntilRaidable(-9999999);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionClaimChange(FactionClaimChangeEvent event) {
        if ((this.plugin.getEotwHandler().isEndOfTheWorld()) && (event.getCause() == ClaimChangeCause.CLAIM)) {
            Faction faction = event.getClaimableFaction();
            if ((faction instanceof PlayerFaction)) {
                event.getSender().sendMessage(ConfigurationService.RED + "Player based faction land cannot be claimed during EOTW.");
                event.setCancelled(true);
            }
        }

    }

}