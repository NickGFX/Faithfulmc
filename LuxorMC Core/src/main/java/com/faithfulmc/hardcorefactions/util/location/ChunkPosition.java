package com.faithfulmc.hardcorefactions.util.location;

public class ChunkPosition {
    private final byte x, z;

    public ChunkPosition(byte x, byte z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChunkPosition that = (ChunkPosition) o;

        if (x != that.x) {
            return false;
        }
        return z == that.z;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + z;
        return result;
    }
}
