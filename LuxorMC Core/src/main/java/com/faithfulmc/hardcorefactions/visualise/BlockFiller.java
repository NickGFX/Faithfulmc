package com.faithfulmc.hardcorefactions.visualise;

import com.luxormc.block.BlockPosition;
import com.google.common.collect.Iterables;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class BlockFiller {
    @Deprecated
    VisualBlockData generate(Player player, int x, int y, int z) {
        return generate(player, new BlockPosition(x, y, z));
    }

    abstract VisualBlockData generate(Player paramPlayer, BlockPosition paramLocation);

    List<VisualBlockData> bulkGenerate(Player player, Iterable<BlockPosition> locations) {
        List<VisualBlockData> data = new ArrayList<>(Iterables.size(locations));
        Iterator<BlockPosition> var4 = locations.iterator();
        while (var4.hasNext()) {
            BlockPosition location =  var4.next();
            data.add(generate(player, location));
        }
        return data;
    }
}
