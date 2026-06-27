package com.selectiveadventure.nametag;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagManager implements Listener {

    private static final String TEAM = "sa_hidden";

    private final SelectiveAdventurePlugin plugin;

    public NametagManager(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    private Team team() {
        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Team t = board.getTeam(TEAM);
        if (t == null) {
            t = board.registerNewTeam(TEAM);
        }
        t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        return t;
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
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    public void evaluate(Player player, Location loc) {
        boolean hide = false;
        for (Region r : plugin.getRegionManager().enabledRegionsAt(loc)) {
            if (r.isHideNametags()) {
                hide = true;
                break;
            }
        }
        if (hide) {
            add(player);
        } else {
            remove(player);
        }
    }

    private void add(Player player) {
        Team t = team();
        if (!t.hasEntry(player.getName())) {
            t.addEntry(player.getName());
        }
    }

    private void remove(Player player) {
        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Team t = board.getTeam(TEAM);
        if (t != null) {
            t.removeEntry(player.getName());
        }
    }

    public void recheckAll() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            evaluate(p, p.getLocation());
        }
    }

    public void shutdown() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            remove(p);
        }
    }
}
