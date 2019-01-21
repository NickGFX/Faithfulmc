package com.faithfulmc.hardcorefactions.scoreboard;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerBoard {
    public static boolean NAMES_ENABLED = true;
    public static boolean INVISIBILITYFIX = true;

    public final BufferedObjective bufferedObjective;
    private final Team members;
    private final Team archers;
    private final Team neutrals;
    private final Team allies;
    private final Team focused;
    private final Scoreboard scoreboard;
    private final Player player;
    private final HCF plugin;
    private boolean sidebarVisible;
    private boolean removed;
    private SidebarProvider defaultProvider;
    private ScoreboardThread scoreboardThread;

    public PlayerBoard(HCF plugin, Player player) {
        this.sidebarVisible = false;
        this.removed = false;
        this.plugin = plugin;
        this.player = player;
        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        this.bufferedObjective = new BufferedObjective(this.scoreboard);
        (this.members = this.scoreboard.registerNewTeam("members")).setPrefix(ConfigurationService.TEAMMATE_COLOUR.toString());
        this.members.setCanSeeFriendlyInvisibles(true);
        (this.archers = this.scoreboard.registerNewTeam("archers")).setPrefix(ConfigurationService.ARCHER_COLOUR.toString());
        (this.neutrals = this.scoreboard.registerNewTeam("neutrals")).setPrefix(ConfigurationService.ENEMY_COLOUR.toString());
        (this.allies = this.scoreboard.registerNewTeam("allies")).setPrefix(ConfigurationService.ALLY_COLOUR.toString());
        (this.focused = this.scoreboard.registerNewTeam("focused")).setPrefix(ConfigurationService.FOCUS_COLOUR.toString());
        player.setScoreboard(this.scoreboard);
    }

    public ScoreboardThread getScoreboardThread() {
        return scoreboardThread;
    }

    public void setScoreboardThread(ScoreboardThread scoreboardThread) {
        this.scoreboardThread = scoreboardThread;
        if(scoreboardThread != null) {
            scoreboardThread.addBoard(this);
        }
    }

    public void remove() {
        if(scoreboardThread != null){
            scoreboardThread.removeBoard(this);
        }
        this.removed = true;
        if (this.scoreboard != null) {
            synchronized (this.scoreboard) {
                for (final Team team : this.scoreboard.getTeams()) {
                    team.unregister();
                }
                for (final Objective objective : this.scoreboard.getObjectives()) {
                    objective.unregister();
                }
            }
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public boolean isSidebarVisible() {
        return this.sidebarVisible;
    }

    public void setSidebarVisible(final boolean visible) {
        this.sidebarVisible = visible;
        this.bufferedObjective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    public void setDefaultSidebar(final SidebarProvider provider) {
        if (provider != null && provider.equals(this.defaultProvider)) {
            return;
        }
        this.defaultProvider = provider;
        if (provider == null) {
            synchronized (this.scoreboard) {
                this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            }
        }
    }

    protected void updateObjective(long now) {
        synchronized (this.scoreboard) {
            final SidebarProvider provider = this.defaultProvider;
            if (provider == null) {
                this.bufferedObjective.setVisible(false);
            } else {
                this.bufferedObjective.setTitle(provider.getTitle());
                this.bufferedObjective.setAllLines(provider.getLines(this.player, now));
                this.bufferedObjective.flip();
            }
        }
    }

    public boolean isRemoved() {
        return removed;
    }

    public SidebarProvider getDefaultProvider() {
        return defaultProvider;
    }

    public void setArcherTagged(Collection<Player> players){
        if(!NAMES_ENABLED || this.isRemoved()){
            return;
        }
        Collection<String> entries = players.stream().filter(player -> player.isOnline() && !checkInvis(player)).map(Player::getName).collect(Collectors.toList());
        synchronized (this.scoreboard) {
            archers.addEntries(entries);
        }
    }

    public void setMembers(Collection<Player> players) {
        if(!NAMES_ENABLED || this.isRemoved()){
            return;
        }
        Collection<String> entries = players.stream().filter(Player::isOnline).map(Player::getName).collect(Collectors.toList());
        synchronized (this.scoreboard) {
            members.addEntries(entries);
        }
    }

    public void setAllies(Collection<Player> players){
        if(!NAMES_ENABLED || this.isRemoved()){
            return;
        }
        Collection<String> entries = players.stream().filter(player -> player.isOnline() && !checkInvis(player)).map(Player::getName).collect(Collectors.toList());
        synchronized (this.scoreboard) {
            allies.addEntries(entries);
        }
    }

    public void setNeutrals(Collection<Player> players){
        if(!NAMES_ENABLED || this.isRemoved()){
            return;
        }
        Collection<String> entries = players.stream().filter(player -> player.isOnline() && !checkInvis(player)).map(Player::getName).collect(Collectors.toList());
        synchronized (this.scoreboard) {
            neutrals.addEntries(entries);
        }
    }

    public boolean checkInvis(Player player){
        return INVISIBILITYFIX && player.hasPotionEffect(PotionEffectType.INVISIBILITY) && HCF.getInstance().getScoreboardHandler().getInvisibilityFixed().contains(player.getUniqueId());
    }

    public void setFocused(Collection<Player> players){
        if(!NAMES_ENABLED || this.isRemoved()){
            return;
        }
        Collection<String> entries = players.stream().filter(player -> player.isOnline() && !checkInvis(player)).map(Player::getName).collect(Collectors.toList());
        synchronized (this.scoreboard) {
            focused.addEntries(entries);
        }
    }

    public void removeAll(Player player){
        synchronized (this.scoreboard){
            this.neutrals.removePlayer(player);
            this.allies.removePlayer(player);
            this.archers.removePlayer(player);
            this.focused.removePlayer(player);
        }
    }

    public void wipe(String entry){
        synchronized (this.scoreboard){
            neutrals.removeEntry(entry);
            members.removeEntry(entry);
            focused.removeEntry(entry);
            archers.removeEntry(entry);
            allies.removeEntry(entry);
        }
    }

    public void init(Player player){
        init(Collections.singleton(player));
    }

    public void init(Collection<? extends Player> players){
        if(!NAMES_ENABLED || this.isRemoved()){
            return;
        }
        boolean foundFaction = false;
        PlayerFaction playerFaction = null;
        List<Player> neutrals = new ArrayList<>();
        List<Player> members = new ArrayList<>();
        List<Player> archerTagged = new ArrayList<>();
        List<Player> toRemove = new ArrayList<>();
        List<Player> allies = new ArrayList<>();
        List<Player> focused = new ArrayList<>();
        for(Player player: players){
            if(!player.isOnline()) continue;

            boolean invis = checkInvis(player);
            if(player == this.player){
                members.add(player);
            }
            else {
                if(!foundFaction){
                    playerFaction = plugin.getFactionManager().getPlayerFaction(PlayerBoard.this.player);
                    foundFaction = true;
                }
                if (playerFaction != null) {
                    if (playerFaction.getMembers().keySet().contains(player.getUniqueId())) {
                        members.add(player);
                    }
                    else if(invis){
                        toRemove.add(player);
                    } else if (playerFaction.getRelation(player) == Relation.ALLY) {
                        allies.add(player);
                    } else if (playerFaction.getFocus() == player.getUniqueId()) {
                        focused.add(player);
                    } else if(ArcherClass.TAGGED.containsKey(player.getUniqueId())){
                        archerTagged.add(player);
                    } else {
                        neutrals.add(player);
                    }
                }
                else if(invis){
                    toRemove.add(player);
                }
                else if(ArcherClass.TAGGED.containsKey(player.getUniqueId())){
                    archerTagged.add(player);
                }
                else {
                    neutrals.add(player);
                }
            }
        }
        if(!neutrals.isEmpty()) setNeutrals(neutrals);
        if(!members.isEmpty()) setMembers(members);
        if(!archerTagged.isEmpty()) setArcherTagged(archerTagged);
        for(Player player: toRemove){
            removeAll(player);
        }
        if(!allies.isEmpty()) setAllies(allies);
        if(!focused.isEmpty()) setFocused(focused);
    }
}