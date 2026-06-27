package com.selectiveadventure.visual;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VisualManager implements Listener {

    private static final int PER_PLAYER_CAP = 4000;
    private static final int INTERIOR_BUDGET = 1500;

    private final SelectiveAdventurePlugin plugin;
    private final Set<UUID> active = ConcurrentHashMap.newKeySet();
    private BukkitTask task;

    public VisualManager(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stopTask();
        if (!plugin.isVisualsEnabled()) {
            return;
        }
        int period = Math.max(1, plugin.getVisualRefreshTicks());
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 0L, period);
    }

    public void reload() {
        active.clear();
        start();
    }

    public void shutdown() {
        stopTask();
        active.clear();
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (active.remove(id)) {
            return false;
        }
        active.add(id);
        return true;
    }

    public boolean isActive(UUID id) {
        return active.contains(id);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        active.remove(event.getPlayer().getUniqueId());
    }

    private void tick() {
        if (active.isEmpty()) {
            return;
        }
        for (UUID id : active) {
            Player p = plugin.getServer().getPlayer(id);
            if (p != null && p.isOnline()) {
                Region r = resolveRegion(p);
                if (r != null) {
                    render(p, r);
                }
            }
        }
    }

    public Region resolveRegion(Player player) {
        UUID id = player.getUniqueId();
        if (plugin.getSelectionManager().hasBoth(id)) {
            Location a = plugin.getSelectionManager().getPos1(id);
            Location b = plugin.getSelectionManager().getPos2(id);
            if (a.getWorld() != null && a.getWorld().equals(b.getWorld())) {
                return new Region("__selection__", a.getWorld().getName(),
                        a.getBlockX(), a.getBlockY(), a.getBlockZ(),
                        b.getBlockX(), b.getBlockY(), b.getBlockZ());
            }
        }
        List<Region> here = plugin.getRegionManager().regionsAt(player.getLocation());
        return here.isEmpty() ? null : here.get(0);
    }

    private void render(Player p, Region r) {
        World w = p.getWorld();
        if (!w.getName().equalsIgnoreCase(r.getWorld())) {
            return;
        }
        Particle.DustOptions edge = new Particle.DustOptions(plugin.getVisualColor(), 1.6f);
        Particle.DustOptions fill = new Particle.DustOptions(plugin.getVisualColor(), 0.9f);

        double maxR = plugin.getVisualMaxRadius();
        double maxR2 = maxR * maxR;
        Location eye = p.getLocation();
        int[] count = {0};

        int minX = r.getMinX(), minY = r.getMinY(), minZ = r.getMinZ();
        int maxX = r.getMaxX(), maxY = r.getMaxY(), maxZ = r.getMaxZ();
        double x0 = minX, y0 = minY, z0 = minZ;
        double x1 = maxX + 1.0, y1 = maxY + 1.0, z1 = maxZ + 1.0;

        long lenX = maxX - minX + 1L, lenY = maxY - minY + 1L, lenZ = maxZ - minZ + 1L;
        double edgeStep = Math.max(1.0, Math.max(lenX, Math.max(lenY, lenZ)) / 200.0);

        for (double x = x0; x <= x1; x += edgeStep) {
            spawn(p, w, x, y0, z0, edge, eye, maxR2, count);
            spawn(p, w, x, y0, z1, edge, eye, maxR2, count);
            spawn(p, w, x, y1, z0, edge, eye, maxR2, count);
            spawn(p, w, x, y1, z1, edge, eye, maxR2, count);
        }
        for (double y = y0; y <= y1; y += edgeStep) {
            spawn(p, w, x0, y, z0, edge, eye, maxR2, count);
            spawn(p, w, x0, y, z1, edge, eye, maxR2, count);
            spawn(p, w, x1, y, z0, edge, eye, maxR2, count);
            spawn(p, w, x1, y, z1, edge, eye, maxR2, count);
        }
        for (double z = z0; z <= z1; z += edgeStep) {
            spawn(p, w, x0, y0, z, edge, eye, maxR2, count);
            spawn(p, w, x0, y1, z, edge, eye, maxR2, count);
            spawn(p, w, x1, y0, z, edge, eye, maxR2, count);
            spawn(p, w, x1, y1, z, edge, eye, maxR2, count);
        }

        long volume = lenX * lenY * lenZ;
        if (volume <= plugin.getVisualMaxVolume()) {
            int step = Math.max(1, (int) Math.cbrt((double) volume / INTERIOR_BUDGET));
            for (int ix = minX; ix <= maxX; ix += step) {
                for (int iy = minY; iy <= maxY; iy += step) {
                    for (int iz = minZ; iz <= maxZ; iz += step) {
                        spawn(p, w, ix + 0.5, iy + 0.5, iz + 0.5, fill, eye, maxR2, count);
                    }
                }
            }
        }
    }

    private void spawn(Player p, World w, double x, double y, double z,
                       Particle.DustOptions opts, Location eye, double maxR2, int[] count) {
        if (count[0] >= PER_PLAYER_CAP) {
            return;
        }
        double dx = x - eye.getX(), dy = y - eye.getY(), dz = z - eye.getZ();
        if (dx * dx + dy * dy + dz * dz > maxR2) {
            return;
        }
        p.spawnParticle(Particle.DUST, new Location(w, x, y, z), 1, 0, 0, 0, 0, opts);
        count[0]++;
    }
}
