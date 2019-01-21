package com.faithfulmc.util.player;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PlayerList implements Iterable {
    private final List<UUID> playerUniqueIds;
    private final List<Player> playerList;

    public PlayerList() {
        this.playerList = new ArrayList<>();
        this.playerUniqueIds = new ArrayList<>();
    }

    public PlayerList(final Iterable<UUID> iterable) {
        this.playerList = new ArrayList<>();
        this.playerUniqueIds = Lists.newArrayList(iterable);
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return !PlayerList.this.playerUniqueIds.isEmpty() && this.index < PlayerList.this.playerUniqueIds.size();
            }

            @Override
            public Player next() {
                ++this.index;
                return PlayerList.this.getPlayers().get(this.index - 1);
            }

            @Override
            public void remove() {
            }
        };
    }

    public int size() {
        return this.playerUniqueIds.size();
    }

    public List<Player> getPlayers() {
        this.playerList.clear();
        for (final UUID uuid : this.playerUniqueIds) {
            this.playerList.add(Bukkit.getPlayer(uuid));
        }
        return this.playerList;
    }

    public boolean contains(final Player player) {
        return player != null && this.playerUniqueIds.contains(player.getUniqueId());
    }

    public boolean add(final Player player) {
        return !this.playerUniqueIds.contains(player.getUniqueId()) && this.playerUniqueIds.add(player.getUniqueId());
    }

    public boolean remove(final Player player) {
        return this.playerUniqueIds.remove(player.getUniqueId());
    }

    public void remove(final UUID playerUUID) {
        this.playerUniqueIds.remove(playerUUID);
    }

    public void clear() {
        this.playerUniqueIds.clear();
    }
}
