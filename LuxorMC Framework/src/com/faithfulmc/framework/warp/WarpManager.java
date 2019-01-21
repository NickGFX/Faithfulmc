package com.faithfulmc.framework.warp;

import java.util.Collection;

public interface WarpManager {
    Collection<String> getWarpNames();

    Collection<Warp> getWarps();

    Warp getWarp(final String p0);

    boolean containsWarp(final Warp p0);

    void createWarp(final Warp p0);

    void removeWarp(final Warp p0);

    String getWarpDelayWords();

    long getWarpDelayMillis();

    long getWarpDelayTicks();

    void reloadWarpData();

    void saveWarpData();
}
