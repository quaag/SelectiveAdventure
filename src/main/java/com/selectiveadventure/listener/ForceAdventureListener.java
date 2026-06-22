package com.selectiveadventure.listener;

import com.selectiveadventure.SelectiveAdventurePlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Optional feature: when force-adventure-in-regions is enabled, players without
 * bypass are put into ADVENTURE mode inside enabled regions and restored to
 * their previous game mode when they leave.
 */
public class ForceAdventureListener implements Listener {

    private final SelectiveAdventurePlugin plugin;
    /** players currently forced to adventure -> their previous game mode. */
    private final Map<UUID, GameMode> previous = new HashMap<>();

    public ForceAdventureListener(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.isForceAdventureInRegions()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        // only care when the block position changes
        if (to == null
                || (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        evaluate(event.getPlayer(), to);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.isForceAdventureInRegions()) {
            return;
        }
        evaluate(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // restore on quit so the player keeps their real gamemode next login
        restore(event.getPlayer());
    }

    private void evaluate(Player player, Location loc) {
        if (player.hasPermission("selectiveadventure.bypass")
                || player.hasPermission("selectiveadventure.admin")) {
            restore(player);
            return;
        }

        boolean inProtected = !plugin.getRegionManager().enabledRegionsAt(loc).isEmpty();
        UUID uuid = player.getUniqueId();

        if (inProtected) {
            if (!previous.containsKey(uuid) && player.getGameMode() != GameMode.ADVENTURE) {
                previous.put(uuid, player.getGameMode());
                player.setGameMode(GameMode.ADVENTURE);
            }
        } else {
            restore(player);
        }
    }

    private void restore(Player player) {
        GameMode prev = previous.remove(player.getUniqueId());
        if (prev != null) {
            player.setGameMode(prev);
        }
    }
}
