package com.selectiveadventure.hearts;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeartsManager implements Listener {

    private static final double ONE_ROW = 20.0;

    private final SelectiveAdventurePlugin plugin;
    private final Map<UUID, Double> previousMax = new HashMap<>();

    public HeartsManager(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();
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
        evaluate(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        Location loc = event.getRespawnLocation();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (p.isOnline()) {
                evaluate(p, loc);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restore(event.getPlayer());
    }

    public void evaluate(Player player, Location loc) {
        boolean force = false;
        for (Region r : plugin.getRegionManager().enabledRegionsAt(loc)) {
            if (r.isOneRowHearts()) {
                force = true;
                break;
            }
        }
        if (force) {
            apply(player);
        } else {
            restore(player);
        }
    }

    private void apply(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) {
            return;
        }
        if (!previousMax.containsKey(player.getUniqueId())) {
            previousMax.put(player.getUniqueId(), attr.getBaseValue());
        }
        if (attr.getBaseValue() != ONE_ROW) {
            attr.setBaseValue(ONE_ROW);
        }
        if (player.getHealth() > ONE_ROW) {
            player.setHealth(ONE_ROW);
        }
    }

    private void restore(Player player) {
        Double prev = previousMax.remove(player.getUniqueId());
        if (prev == null) {
            return;
        }
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(prev);
        }
    }

    public void recheckAll() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            evaluate(p, p.getLocation());
        }
    }

    public void shutdown() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            restore(p);
        }
        previousMax.clear();
    }
}
