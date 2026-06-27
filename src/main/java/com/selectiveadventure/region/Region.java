package com.selectiveadventure.region;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A cuboid region with its own set of allowed players and allowed break/place
 * block types.
 */
public class Region {

    private final String name;
    private String world;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    private boolean enabled = true;

    /**
     * When true (default), allowed players may only break/place block types that
     * are explicitly in the allowed lists. When false, allowed players may
     * break/place ANY block inside the region (the region only restricts WHO,
     * not WHAT).
     */
    private boolean defaultDeny = true;
    private boolean oneRowHearts = false;
    private boolean disableNaturalMobSpawning = false;
    private boolean disablePearls = false;
    private boolean disableExplosions = false;
    private boolean hideNametags = false;

    /** UUID -> last known name. */
    private final Map<UUID, String> allowedPlayers = new HashMap<>();
    private final Set<Material> allowedBreak = EnumSet.noneOf(Material.class);
    private final Set<Material> allowedPlace = EnumSet.noneOf(Material.class);

    public Region(String name, String world,
                  int x1, int y1, int z1,
                  int x2, int y2, int z2) {
        this.name = name;
        this.world = world;
        setCorners(x1, y1, z1, x2, y2, z2);
    }

    public void setCorners(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().getName().equalsIgnoreCase(world)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean isPlayerAllowed(UUID uuid) {
        return uuid != null && allowedPlayers.containsKey(uuid);
    }

    public boolean canBreak(Material material) {
        return !defaultDeny || allowedBreak.contains(material);
    }

    public boolean canPlace(Material material) {
        return !defaultDeny || allowedPlace.contains(material);
    }

    // --- player management ---

    public void addPlayer(UUID uuid, String name) {
        allowedPlayers.put(uuid, name);
    }

    public boolean removePlayer(UUID uuid) {
        return allowedPlayers.remove(uuid) != null;
    }

    /** Removes by last-known name (case-insensitive). Returns removed UUID or null. */
    public UUID removePlayerByName(String name) {
        for (Map.Entry<UUID, String> e : allowedPlayers.entrySet()) {
            if (e.getValue() != null && e.getValue().equalsIgnoreCase(name)) {
                allowedPlayers.remove(e.getKey());
                return e.getKey();
            }
        }
        return null;
    }

    public Map<UUID, String> getAllowedPlayers() {
        return Collections.unmodifiableMap(allowedPlayers);
    }

    // --- block management ---

    public boolean allowBreak(Material m) {
        return allowedBreak.add(m);
    }

    public boolean denyBreak(Material m) {
        return allowedBreak.remove(m);
    }

    public boolean allowPlace(Material m) {
        return allowedPlace.add(m);
    }

    public boolean denyPlace(Material m) {
        return allowedPlace.remove(m);
    }

    /** Toggles break permission for a material. Returns true if now allowed. */
    public boolean toggleBreak(Material m) {
        if (allowedBreak.contains(m)) {
            allowedBreak.remove(m);
            return false;
        }
        allowedBreak.add(m);
        return true;
    }

    /** Toggles place permission for a material. Returns true if now allowed. */
    public boolean togglePlace(Material m) {
        if (allowedPlace.contains(m)) {
            allowedPlace.remove(m);
            return false;
        }
        allowedPlace.add(m);
        return true;
    }

    public Set<Material> getAllowedBreak() {
        return Collections.unmodifiableSet(allowedBreak);
    }

    public Set<Material> getAllowedPlace() {
        return Collections.unmodifiableSet(allowedPlace);
    }

    // used by storage to re-populate without copying
    public void rawAllowedPlayers(Map<UUID, String> players) {
        allowedPlayers.clear();
        allowedPlayers.putAll(players);
    }

    public void rawAllowedBreak(Set<Material> mats) {
        allowedBreak.clear();
        allowedBreak.addAll(mats);
    }

    public void rawAllowedPlace(Set<Material> mats) {
        allowedPlace.clear();
        allowedPlace.addAll(mats);
    }

    // --- getters / setters ---

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefaultDeny() {
        return defaultDeny;
    }

    public void setDefaultDeny(boolean defaultDeny) {
        this.defaultDeny = defaultDeny;
    }

    public boolean isOneRowHearts() {
        return oneRowHearts;
    }

    public void setOneRowHearts(boolean oneRowHearts) {
        this.oneRowHearts = oneRowHearts;
    }

    public boolean isDisableNaturalMobSpawning() {
        return disableNaturalMobSpawning;
    }

    public void setDisableNaturalMobSpawning(boolean disableNaturalMobSpawning) {
        this.disableNaturalMobSpawning = disableNaturalMobSpawning;
    }

    public boolean isDisablePearls() {
        return disablePearls;
    }

    public void setDisablePearls(boolean disablePearls) {
        this.disablePearls = disablePearls;
    }

    public boolean isDisableExplosions() {
        return disableExplosions;
    }

    public void setDisableExplosions(boolean disableExplosions) {
        this.disableExplosions = disableExplosions;
    }

    public boolean isHideNametags() {
        return hideNametags;
    }

    public void setHideNametags(boolean hideNametags) {
        this.hideNametags = hideNametags;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    /** Returns a deterministic map of the player names for display. */
    public Map<UUID, String> orderedPlayers() {
        return new LinkedHashMap<>(allowedPlayers);
    }
}
