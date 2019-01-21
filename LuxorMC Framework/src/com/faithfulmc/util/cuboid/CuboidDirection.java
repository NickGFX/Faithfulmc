package com.faithfulmc.util.cuboid;

import org.bukkit.block.BlockFace;

public enum CuboidDirection {
    NORTH, EAST, SOUTH, WEST, UP, DOWN, HORIZONTAL, VERTICAL, BOTH, UNKNOWN;

    public CuboidDirection opposite() {
        switch (this.ordinal()) {
            case 1: {
                return CuboidDirection.SOUTH;
            }
            case 2: {
                return CuboidDirection.WEST;
            }
            case 3: {
                return CuboidDirection.NORTH;
            }
            case 4: {
                return CuboidDirection.EAST;
            }
            case 5: {
                return CuboidDirection.VERTICAL;
            }
            case 6: {
                return CuboidDirection.HORIZONTAL;
            }
            case 7: {
                return CuboidDirection.DOWN;
            }
            case 8: {
                return CuboidDirection.UP;
            }
            case 9: {
                return CuboidDirection.BOTH;
            }
            default: {
                return CuboidDirection.UNKNOWN;
            }
        }
    }

    public BlockFace toBukkitDirection() {
        switch (this.ordinal()) {
            case 1: {
                return BlockFace.NORTH;
            }
            case 2: {
                return BlockFace.EAST;
            }
            case 3: {
                return BlockFace.SOUTH;
            }
            case 4: {
                return BlockFace.WEST;
            }
            case 5: {
                return null;
            }
            case 6: {
                return null;
            }
            case 7: {
                return BlockFace.UP;
            }
            case 8: {
                return BlockFace.DOWN;
            }
            case 9: {
                return null;
            }
            default: {
                return null;
            }
        }
    }
}
