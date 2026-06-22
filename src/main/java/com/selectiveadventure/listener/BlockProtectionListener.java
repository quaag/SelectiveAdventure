package com.selectiveadventure.listener;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.UUID;

/**
 * Core protection: cancels break/place events according to region rules.
 */
public class BlockProtectionListener implements Listener {

    private final SelectiveAdventurePlugin plugin;

    public BlockProtectionListener(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        Location loc = event.getBlock().getLocation();

        if (!isAllowed(player, loc, material, true)) {
            event.setCancelled(true);
            deny(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlockPlaced().getType();
        Location loc = event.getBlockPlaced().getLocation();

        if (!isAllowed(player, loc, material, false)) {
            event.setCancelled(true);
            deny(player);
        }
    }

    /**
     * Returns true if the action should be permitted.
     *
     * @param isBreak true for break, false for place.
     */
    private boolean isAllowed(Player player, Location loc, Material material, boolean isBreak) {
        // 1. bypass
        if (player.hasPermission("selectiveadventure.bypass")
                || player.hasPermission("selectiveadventure.admin")) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        List<Region> enabled = plugin.getRegionManager().enabledRegionsAt(loc);

        // 2. no enabled region here -> fall back to global config behaviour
        if (enabled.isEmpty()) {
            boolean allow = !plugin.isGlobalAdventureProtection();
            plugin.debug((isBreak ? "BREAK" : "PLACE") + " by " + player.getName()
                    + " outside regions -> " + (allow ? "ALLOW" : "DENY"));
            return allow;
        }

        // 3. inside one or more enabled regions: allow only if at least one
        //    region explicitly permits this player + block type.
        for (Region region : enabled) {
            if (!region.isPlayerAllowed(uuid)) {
                continue;
            }
            boolean ok = isBreak ? region.canBreak(material) : region.canPlace(material);
            if (ok) {
                plugin.debug((isBreak ? "BREAK" : "PLACE") + " by " + player.getName()
                        + " in region '" + region.getName() + "' -> ALLOW");
                return true;
            }
        }

        plugin.debug((isBreak ? "BREAK" : "PLACE") + " by " + player.getName()
                + " in protected region(s) -> DENY (" + material + ")");
        return false;
    }

    private void deny(Player player) {
        String msg = plugin.getDenyMessage();
        if (msg == null || msg.isBlank()) {
            return;
        }
        if (plugin.isShowActionbarDenyMessage()) {
            com.selectiveadventure.util.Msg.actionbar(player, msg);
        } else {
            com.selectiveadventure.util.Msg.raw(player, msg);
        }
    }
}
