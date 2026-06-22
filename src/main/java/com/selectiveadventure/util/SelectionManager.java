package com.selectiveadventure.util;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player wand selections (pos1 / pos2). In-memory only; selections
 * are not persisted across restarts (they are throwaway working state).
 */
public class SelectionManager {

    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public void setPos1(UUID player, Location loc) {
        pos1.put(player, loc.clone());
    }

    public void setPos2(UUID player, Location loc) {
        pos2.put(player, loc.clone());
    }

    public Location getPos1(UUID player) {
        return pos1.get(player);
    }

    public Location getPos2(UUID player) {
        return pos2.get(player);
    }

    public boolean hasBoth(UUID player) {
        return pos1.containsKey(player) && pos2.containsKey(player);
    }

    public void clear(UUID player) {
        pos1.remove(player);
        pos2.remove(player);
    }
}
