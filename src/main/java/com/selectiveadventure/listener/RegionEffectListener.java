package com.selectiveadventure.listener;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import com.selectiveadventure.util.Msg;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class RegionEffectListener implements Listener {

    private final SelectiveAdventurePlugin plugin;

    public RegionEffectListener(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }
        if (!(pearl.getShooter() instanceof Player player)) {
            return;
        }
        if (isInRegionWith(player.getLocation(), Flag.PEARLS)) {
            event.setCancelled(true);
            Msg.actionbar(player, "&cYou cannot use ender pearls here.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPearlLand(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Location to = event.getTo();
        if (to != null && isInRegionWith(to, Flag.PEARLS)) {
            event.setCancelled(true);
            Msg.actionbar(event.getPlayer(), "&cEnder pearls cannot land here.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(b -> isInRegionWith(b.getLocation(), Flag.EXPLOSIONS));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(b -> isInRegionWith(b.getLocation(), Flag.EXPLOSIONS));
    }

    private enum Flag {
        PEARLS, EXPLOSIONS
    }

    private boolean isInRegionWith(Location loc, Flag flag) {
        for (Region r : plugin.getRegionManager().enabledRegionsAt(loc)) {
            boolean on = flag == Flag.PEARLS ? r.isDisablePearls() : r.isDisableExplosions();
            if (on) {
                return true;
            }
        }
        return false;
    }
}
