package com.selectiveadventure.listener;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.EnumSet;
import java.util.Set;

public class MobSpawnListener implements Listener {

    private static final Set<SpawnReason> NATURAL = EnumSet.of(
            SpawnReason.NATURAL,
            SpawnReason.PATROL,
            SpawnReason.RAID,
            SpawnReason.REINFORCEMENTS);

    private final SelectiveAdventurePlugin plugin;

    public MobSpawnListener(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!NATURAL.contains(event.getSpawnReason())) {
            return;
        }
        Location loc = event.getLocation();
        for (Region r : plugin.getRegionManager().enabledRegionsAt(loc)) {
            if (r.isDisableNaturalMobSpawning()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
