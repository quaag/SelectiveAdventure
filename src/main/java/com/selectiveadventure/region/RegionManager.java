package com.selectiveadventure.region;

import com.selectiveadventure.SelectiveAdventurePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Loads, stores and persists all regions in regions.yml.
 */
public class RegionManager {

    private final SelectiveAdventurePlugin plugin;
    /** key = lowercase region name. */
    private final Map<String, Region> regions = new TreeMap<>();
    private File file;

    public RegionManager(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    private File resolveFile() {
        String name = plugin.getConfig().getString("save-file", "regions.yml");
        if (name == null || name.isBlank()) {
            name = "regions.yml";
        }
        return new File(plugin.getDataFolder(), name);
    }

    public void load() {
        regions.clear();
        this.file = resolveFile();

        if (!file.exists()) {
            plugin.debug("No regions file found at " + file.getName() + " - starting empty.");
            return;
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yml.getConfigurationSection("regions");
        if (root == null) {
            return;
        }

        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) {
                continue;
            }
            try {
                Region region = readRegion(key, sec);
                regions.put(region.getName().toLowerCase(Locale.ROOT), region);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to load region '" + key + "': " + ex.getMessage(), ex);
            }
        }

        plugin.getLogger().info("Loaded " + regions.size() + " region(s).");
    }

    private Region readRegion(String key, ConfigurationSection sec) {
        String name = sec.getString("name", key);
        String world = sec.getString("world", "world");
        int minX = sec.getInt("minX");
        int minY = sec.getInt("minY");
        int minZ = sec.getInt("minZ");
        int maxX = sec.getInt("maxX");
        int maxY = sec.getInt("maxY");
        int maxZ = sec.getInt("maxZ");

        Region region = new Region(name, world, minX, minY, minZ, maxX, maxY, maxZ);
        region.setEnabled(sec.getBoolean("enabled", true));
        region.setDefaultDeny(sec.getBoolean("default-deny", true));
        region.setOneRowHearts(sec.getBoolean("one-row-hearts", false));
        region.setDisableNaturalMobSpawning(sec.getBoolean("disable-natural-mob-spawning", false));
        region.setDisablePearls(sec.getBoolean("disable-pearls", false));
        region.setDisableExplosions(sec.getBoolean("disable-explosions", false));
        region.setHideNametags(sec.getBoolean("hide-nametags", false));

        // players
        Map<UUID, String> players = new HashMap<>();
        ConfigurationSection psec = sec.getConfigurationSection("players");
        if (psec != null) {
            for (String uuidStr : psec.getKeys(false)) {
                try {
                    players.put(UUID.fromString(uuidStr), psec.getString(uuidStr, uuidStr));
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Invalid UUID in region '" + name + "': " + uuidStr);
                }
            }
        }
        region.rawAllowedPlayers(players);

        region.rawAllowedBreak(readMaterials(sec.getStringList("allowed-break")));
        region.rawAllowedPlace(readMaterials(sec.getStringList("allowed-place")));
        return region;
    }

    private Set<Material> readMaterials(List<String> list) {
        Set<Material> set = EnumSet.noneOf(Material.class);
        for (String s : list) {
            Material m = Material.matchMaterial(s);
            if (m != null && m.isBlock()) {
                set.add(m);
            } else {
                plugin.getLogger().warning("Ignoring unknown/non-block material in regions file: " + s);
            }
        }
        return set;
    }

    public void save() {
        if (file == null) {
            file = resolveFile();
        }
        YamlConfiguration yml = new YamlConfiguration();
        for (Region r : regions.values()) {
            String path = "regions." + r.getName();
            yml.set(path + ".name", r.getName());
            yml.set(path + ".world", r.getWorld());
            yml.set(path + ".minX", r.getMinX());
            yml.set(path + ".minY", r.getMinY());
            yml.set(path + ".minZ", r.getMinZ());
            yml.set(path + ".maxX", r.getMaxX());
            yml.set(path + ".maxY", r.getMaxY());
            yml.set(path + ".maxZ", r.getMaxZ());
            yml.set(path + ".enabled", r.isEnabled());
            yml.set(path + ".default-deny", r.isDefaultDeny());
            yml.set(path + ".one-row-hearts", r.isOneRowHearts());
            yml.set(path + ".disable-natural-mob-spawning", r.isDisableNaturalMobSpawning());
            yml.set(path + ".disable-pearls", r.isDisablePearls());
            yml.set(path + ".disable-explosions", r.isDisableExplosions());
            yml.set(path + ".hide-nametags", r.isHideNametags());

            for (Map.Entry<UUID, String> e : r.getAllowedPlayers().entrySet()) {
                yml.set(path + ".players." + e.getKey(), e.getValue());
            }

            yml.set(path + ".allowed-break", toNames(r.getAllowedBreak()));
            yml.set(path + ".allowed-place", toNames(r.getAllowedPlace()));
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yml.save(file);
            plugin.debug("Saved " + regions.size() + " region(s) to " + file.getName());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save regions to " + file, ex);
        }
    }

    private List<String> toNames(Set<Material> mats) {
        List<String> out = new ArrayList<>(mats.size());
        for (Material m : mats) {
            out.add(m.name());
        }
        out.sort(String::compareTo);
        return out;
    }

    // --- CRUD ---

    public Region create(String name, String world, Location p1, Location p2) {
        Region region = new Region(name, world,
                p1.getBlockX(), p1.getBlockY(), p1.getBlockZ(),
                p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
        regions.put(name.toLowerCase(Locale.ROOT), region);
        save();
        return region;
    }

    public boolean delete(String name) {
        boolean removed = regions.remove(name.toLowerCase(Locale.ROOT)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Region get(String name) {
        return regions.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String name) {
        return regions.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public List<Region> all() {
        return new ArrayList<>(regions.values());
    }

    public List<String> names() {
        List<String> out = new ArrayList<>();
        for (Region r : regions.values()) {
            out.add(r.getName());
        }
        return out;
    }

    /** All regions (enabled or not) containing the given location. */
    public List<Region> regionsAt(Location loc) {
        List<Region> out = new ArrayList<>();
        for (Region r : regions.values()) {
            if (r.contains(loc)) {
                out.add(r);
            }
        }
        return out;
    }

    /** Only enabled regions containing the given location. */
    public List<Region> enabledRegionsAt(Location loc) {
        List<Region> out = new ArrayList<>();
        for (Region r : regions.values()) {
            if (r.isEnabled() && r.contains(loc)) {
                out.add(r);
            }
        }
        return out;
    }
}
