package com.faithfulmc.hardcorefactions.visualise;

import com.luxormc.block.BlockPosition;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public enum VisualType {
    SPAWN_BORDER {
        private final BlockFiller blockFiller;

        {
            this.blockFiller = new BlockFiller() {
                @Override
                VisualBlockData generate(final Player player, final BlockPosition location) {
                    return new VisualBlockData(Material.STAINED_GLASS, DyeColor.RED.getData());
                }
            };
        }

        @Override
        BlockFiller blockFiller() {
            return this.blockFiller;
        }
    }, CLAIM_BORDER {
        private final BlockFiller blockFiller;

        {
            this.blockFiller = new BlockFiller() {
                @Override
                VisualBlockData generate(final Player player, final BlockPosition location) {
                    return new VisualBlockData(Material.STAINED_GLASS, DyeColor.LIGHT_BLUE.getData());
                }
            };
        }

        @Override
        BlockFiller blockFiller() {
            return this.blockFiller;
        }
    }, SUBCLAIM_MAP {
        private final BlockFiller blockFiller;

        {
            this.blockFiller = new BlockFiller() {
                @Override
                VisualBlockData generate(final Player player, final BlockPosition location) {
                    return new VisualBlockData(Material.LOG, (byte) 1);
                }
            };
        }

        @Override
        BlockFiller blockFiller() {
            return this.blockFiller;
        }
    }, CLAIM_MAP {
        private final BlockFiller blockFiller;

        {
            this.blockFiller = new BlockFiller() {
                private final Material[] types = {Material.SNOW_BLOCK, Material.SANDSTONE, Material.NETHERRACK, Material.GLOWSTONE, Material.LAPIS_BLOCK, Material.NETHER_BRICK, Material.DIAMOND_ORE, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE};
                private int materialCounter = 0;

                @Override
                VisualBlockData generate(final Player player, final BlockPosition location) {
                    final int y = location.getY();
                    if (y != 0 && y % 3 != 0) {
                        final Faction faction = HCF.getInstance().getFactionManager().getFactionAt(new Location(player.getWorld(), location.getX(), location.getY(), location.getZ()));
                        return new VisualBlockData(Material.STAINED_GLASS, ((faction != null) ? faction.getRelation(player) : Relation.ENEMY).toDyeColour().getData());
                    }
                    return new VisualBlockData(this.types[this.materialCounter]);
                }

                @Override
                List<VisualBlockData> bulkGenerate(final Player player, final Iterable<BlockPosition> locations) {
                    List<VisualBlockData> result = super.bulkGenerate(player, locations);
                    if (++this.materialCounter == this.types.length) {
                        this.materialCounter = 0;
                    }
                    return result;
                }
            };
        }

        @Override
        BlockFiller blockFiller() {
            return this.blockFiller;
        }
    }, CREATE_CLAIM_SELECTION {
        private final BlockFiller blockFiller;

        {
            this.blockFiller = new BlockFiller() {
                @Override
                VisualBlockData generate(final Player player, final BlockPosition location) {
                    return new VisualBlockData((location.getY() % 3 != 0) ? Material.GLASS : Material.GOLD_BLOCK);
                }
            };
        }

        @Override
        BlockFiller blockFiller() {
            return this.blockFiller;
        }
    }, WORLD_BORDER {
        private final BlockFiller blockFiller;

        {
            this.blockFiller = new BlockFiller() {
                @Override
                VisualBlockData generate(final Player player, final BlockPosition location) {
                    return new VisualBlockData(Material.STAINED_GLASS, DyeColor.BLUE.getData());
                }
            };
        }

        @Override
        BlockFiller blockFiller() {
            return this.blockFiller;
        }
    };

    private VisualType(final Object x2) {
        this();
    }

    private VisualType() {
    }

    abstract BlockFiller blockFiller();
}