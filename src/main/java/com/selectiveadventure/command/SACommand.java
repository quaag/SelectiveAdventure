package com.selectiveadventure.command;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.region.Region;
import com.selectiveadventure.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SACommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "wand", "pos1", "pos2", "create", "delete", "list", "info",
            "enable", "disable", "addplayer", "removeplayer", "listplayers",
            "allowbreak", "denybreak", "allowplace", "denyplace", "listblocks",
            "togglebreak", "toggleplace", "reload", "version", "here",
            "visualize", "togglevisual", "visual", "hearts", "naturalmobs",
            "allowbreakhand", "allowplacehand", "help");

    private final SelectiveAdventurePlugin plugin;

    public SACommand(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean has(CommandSender s, String perm) {
        return s.hasPermission("selectiveadventure.admin") || s.hasPermission(perm);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return help(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "version":
                return version(sender);
            case "help":
                return help(sender);
            case "reload":
                return reload(sender);
            case "wand":
                return wand(sender);
            case "pos1":
                return pos(sender, 1);
            case "pos2":
                return pos(sender, 2);
            case "create":
                return create(sender, args);
            case "delete":
                return delete(sender, args);
            case "list":
                return list(sender);
            case "info":
                return info(sender, args);
            case "enable":
                return setEnabled(sender, args, true);
            case "disable":
                return setEnabled(sender, args, false);
            case "addplayer":
                return addPlayer(sender, args);
            case "removeplayer":
                return removePlayer(sender, args);
            case "listplayers":
                return listPlayers(sender, args);
            case "allowbreak":
                return changeBlock(sender, args, BlockOp.ALLOW_BREAK);
            case "denybreak":
                return changeBlock(sender, args, BlockOp.DENY_BREAK);
            case "allowplace":
                return changeBlock(sender, args, BlockOp.ALLOW_PLACE);
            case "denyplace":
                return changeBlock(sender, args, BlockOp.DENY_PLACE);
            case "togglebreak":
                return changeBlock(sender, args, BlockOp.TOGGLE_BREAK);
            case "toggleplace":
                return changeBlock(sender, args, BlockOp.TOGGLE_PLACE);
            case "listblocks":
                return listBlocks(sender, args);
            case "here":
                return here(sender);
            case "visualize":
                return visualize(sender, args);
            case "togglevisual":
            case "visual":
                return toggleVisual(sender);
            case "hearts":
                return hearts(sender, args);
            case "naturalmobs":
                return naturalMobs(sender, args);
            case "allowbreakhand":
                return hand(sender, args, true);
            case "allowplacehand":
                return hand(sender, args, false);
            default:
                Msg.send(sender, "&cUnknown subcommand. Use &f/sa help");
                return true;
        }
    }

    // ------------------------------------------------------------------
    //  Subcommand handlers
    // ------------------------------------------------------------------

    private boolean version(CommandSender sender) {
        Msg.send(sender, "&aSelectiveAdventure &7v&f" + plugin.getPluginMeta().getVersion());
        return true;
    }

    private boolean help(CommandSender sender) {
        Msg.raw(sender, "&8&m                    &r &aSelectiveAdventure &8&m                    ");
        Msg.raw(sender, "&7/sa wand &8- &fget the selection wand");
        Msg.raw(sender, "&7/sa pos1 | pos2 &8- &fset corners at your location");
        Msg.raw(sender, "&7/sa create <name> &8- &fcreate region from selection");
        Msg.raw(sender, "&7/sa delete <name> &8- &fdelete a region");
        Msg.raw(sender, "&7/sa list | info <name> | here &8- &finspect regions");
        Msg.raw(sender, "&7/sa enable | disable <name>");
        Msg.raw(sender, "&7/sa addplayer | removeplayer <name> <player>");
        Msg.raw(sender, "&7/sa listplayers <name>");
        Msg.raw(sender, "&7/sa allowbreak | denybreak <name> <block>");
        Msg.raw(sender, "&7/sa allowplace | denyplace <name> <block>");
        Msg.raw(sender, "&7/sa togglebreak | toggleplace <name> <block>");
        Msg.raw(sender, "&7/sa allowbreakhand | allowplacehand <name>");
        Msg.raw(sender, "&7/sa listblocks <name> | visualize <name>");
        Msg.raw(sender, "&7/sa togglevisual &8- &flive region preview overlay");
        Msg.raw(sender, "&7/sa hearts <name> <on|off> &8- &f1-row hearts in region");
        Msg.raw(sender, "&7/sa naturalmobs <name> <on|off> &8- &fnatural mob spawning");
        Msg.raw(sender, "&7/sa reload | version");
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!has(sender, "selectiveadventure.reload")) {
            return noPerm(sender);
        }
        plugin.reloadAll();
        Msg.send(sender, "&aConfig and regions reloaded.");
        return true;
    }

    private boolean wand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        if (!has(sender, "selectiveadventure.wand")) {
            return noPerm(sender);
        }
        ItemStack wand = new ItemStack(plugin.getSelectionTool());
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(Msg.color("&aSelectiveAdventure Wand"));
        meta.lore(List.of(
                Msg.color("&7Left-click &8- &fset pos1"),
                Msg.color("&7Right-click &8- &fset pos2")));
        meta.getPersistentDataContainer()
                .set(plugin.getWandKey(), PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        Msg.send(player, "&aGiven the selection wand (&f"
                + plugin.getSelectionTool().name() + "&a).");
        return true;
    }

    private boolean pos(CommandSender sender, int which) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        if (!has(sender, "selectiveadventure.wand")) {
            return noPerm(sender);
        }
        Location loc = player.getLocation();
        if (which == 1) {
            plugin.getSelectionManager().setPos1(player.getUniqueId(), loc);
        } else {
            plugin.getSelectionManager().setPos2(player.getUniqueId(), loc);
        }
        Msg.send(player, "&aPos" + which + " &7set to &f"
                + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        return true;
    }

    private boolean create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        if (!has(sender, "selectiveadventure.region.create")) {
            return noPerm(sender);
        }
        if (args.length < 2) {
            Msg.send(sender, "&cUsage: /sa create <regionName>");
            return true;
        }
        String name = args[1];
        if (plugin.getRegionManager().exists(name)) {
            Msg.send(sender, "&cA region named &f" + name + " &calready exists.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (!plugin.getSelectionManager().hasBoth(uuid)) {
            Msg.send(sender, "&cSet both positions first (&f/sa wand&c, then left/right-click, "
                    + "or &f/sa pos1 &cand &f/sa pos2&c).");
            return true;
        }
        Location p1 = plugin.getSelectionManager().getPos1(uuid);
        Location p2 = plugin.getSelectionManager().getPos2(uuid);
        if (p1.getWorld() == null || !p1.getWorld().equals(p2.getWorld())) {
            Msg.send(sender, "&cBoth positions must be in the same world.");
            return true;
        }
        Region region = plugin.getRegionManager()
                .create(name, p1.getWorld().getName(), p1, p2);
        Msg.send(sender, "&aCreated region &f" + region.getName() + " &ain world &f"
                + region.getWorld() + "&a.");
        Msg.send(sender, "&7From &f" + region.getMinX() + "," + region.getMinY() + "," + region.getMinZ()
                + " &7to &f" + region.getMaxX() + "," + region.getMaxY() + "," + region.getMaxZ());
        return true;
    }

    private boolean delete(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        if (args.length < 2) {
            Msg.send(sender, "&cUsage: /sa delete <regionName>");
            return true;
        }
        if (plugin.getRegionManager().delete(args[1])) {
            plugin.getHeartsManager().recheckAll();
            Msg.send(sender, "&aDeleted region &f" + args[1] + "&a.");
        } else {
            Msg.send(sender, "&cNo region named &f" + args[1] + "&c.");
        }
        return true;
    }

    private boolean list(CommandSender sender) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        List<Region> regions = plugin.getRegionManager().all();
        if (regions.isEmpty()) {
            Msg.send(sender, "&7No regions defined.");
            return true;
        }
        Msg.send(sender, "&aRegions (&f" + regions.size() + "&a):");
        for (Region r : regions) {
            Msg.raw(sender, " &8- &f" + r.getName() + " &7(" + r.getWorld() + ") "
                    + (r.isEnabled() ? "&aenabled" : "&cdisabled"));
        }
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        Msg.send(sender, "&aRegion &f" + r.getName());
        Msg.raw(sender, " &7World: &f" + r.getWorld());
        Msg.raw(sender, " &7Min: &f" + r.getMinX() + ", " + r.getMinY() + ", " + r.getMinZ());
        Msg.raw(sender, " &7Max: &f" + r.getMaxX() + ", " + r.getMaxY() + ", " + r.getMaxZ());
        Msg.raw(sender, " &7Enabled: " + (r.isEnabled() ? "&atrue" : "&cfalse"));
        Msg.raw(sender, " &7Default-deny: &f" + r.isDefaultDeny());
        Msg.raw(sender, " &7One-row hearts: " + (r.isOneRowHearts() ? "&atrue" : "&cfalse"));
        Msg.raw(sender, " &7Natural mob spawning: "
                + (r.isDisableNaturalMobSpawning() ? "&coff" : "&aon"));
        Msg.raw(sender, " &7Players: &f" + r.getAllowedPlayers().size());
        Msg.raw(sender, " &7Allowed break: &f" + namesOf(r.getAllowedBreak()));
        Msg.raw(sender, " &7Allowed place: &f" + namesOf(r.getAllowedPlace()));
        return true;
    }

    private boolean setEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        r.setEnabled(enabled);
        plugin.getRegionManager().save();
        plugin.getHeartsManager().recheckAll();
        Msg.send(sender, "&aRegion &f" + r.getName() + " &ais now "
                + (enabled ? "&aenabled" : "&cdisabled") + "&a.");
        return true;
    }

    private boolean addPlayer(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        if (args.length < 3) {
            Msg.send(sender, "&cUsage: /sa addplayer <regionName> <player>");
            return true;
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        String target = args[2];
        OfflinePlayer op = resolvePlayer(target);
        if (op == null || op.getUniqueId() == null) {
            Msg.send(sender, "&cCould not resolve player &f" + target + "&c.");
            return true;
        }
        String name = op.getName() != null ? op.getName() : target;
        r.addPlayer(op.getUniqueId(), name);
        plugin.getRegionManager().save();
        Msg.send(sender, "&aAdded &f" + name + " &ato region &f" + r.getName() + "&a.");
        return true;
    }

    private boolean removePlayer(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        if (args.length < 3) {
            Msg.send(sender, "&cUsage: /sa removeplayer <regionName> <player>");
            return true;
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        String target = args[2];
        // try by online/cached UUID first, then by stored name
        OfflinePlayer op = resolvePlayer(target);
        boolean removed = false;
        if (op != null && op.getUniqueId() != null) {
            removed = r.removePlayer(op.getUniqueId());
        }
        if (!removed) {
            removed = r.removePlayerByName(target) != null;
        }
        if (removed) {
            plugin.getRegionManager().save();
            Msg.send(sender, "&aRemoved &f" + target + " &afrom region &f" + r.getName() + "&a.");
        } else {
            Msg.send(sender, "&c" + target + " is not in region &f" + r.getName() + "&c.");
        }
        return true;
    }

    private boolean listPlayers(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        Map<UUID, String> players = r.orderedPlayers();
        if (players.isEmpty()) {
            Msg.send(sender, "&7No players allowed in &f" + r.getName() + "&7.");
            return true;
        }
        Msg.send(sender, "&aPlayers in &f" + r.getName() + "&a:");
        for (Map.Entry<UUID, String> e : players.entrySet()) {
            Msg.raw(sender, " &8- &f" + e.getValue() + " &7(" + e.getKey() + ")");
        }
        return true;
    }

    private enum BlockOp {
        ALLOW_BREAK, DENY_BREAK, ALLOW_PLACE, DENY_PLACE, TOGGLE_BREAK, TOGGLE_PLACE
    }

    private boolean changeBlock(CommandSender sender, String[] args, BlockOp op) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        if (args.length < 3) {
            Msg.send(sender, "&cUsage: /sa " + args[0] + " <regionName> <block>");
            return true;
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        Material mat = parseBlock(sender, args[2]);
        if (mat == null) {
            return true;
        }
        applyBlockOp(sender, r, mat, op);
        return true;
    }

    private void applyBlockOp(CommandSender sender, Region r, Material mat, BlockOp op) {
        switch (op) {
            case ALLOW_BREAK -> {
                r.allowBreak(mat);
                Msg.send(sender, "&aAllowed breaking &f" + mat.name() + " &ain &f" + r.getName() + "&a.");
            }
            case DENY_BREAK -> {
                r.denyBreak(mat);
                Msg.send(sender, "&aDenied breaking &f" + mat.name() + " &ain &f" + r.getName() + "&a.");
            }
            case ALLOW_PLACE -> {
                r.allowPlace(mat);
                Msg.send(sender, "&aAllowed placing &f" + mat.name() + " &ain &f" + r.getName() + "&a.");
            }
            case DENY_PLACE -> {
                r.denyPlace(mat);
                Msg.send(sender, "&aDenied placing &f" + mat.name() + " &ain &f" + r.getName() + "&a.");
            }
            case TOGGLE_BREAK -> {
                boolean now = r.toggleBreak(mat);
                Msg.send(sender, "&fBreak &f" + mat.name() + " &7in &f" + r.getName() + "&7: "
                        + (now ? "&aALLOWED" : "&cDENIED"));
            }
            case TOGGLE_PLACE -> {
                boolean now = r.togglePlace(mat);
                Msg.send(sender, "&fPlace &f" + mat.name() + " &7in &f" + r.getName() + "&7: "
                        + (now ? "&aALLOWED" : "&cDENIED"));
            }
        }
        plugin.getRegionManager().save();
    }

    private boolean listBlocks(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        Msg.send(sender, "&aBlocks for &f" + r.getName() + "&a:");
        Msg.raw(sender, " &7Break: &f" + namesOf(r.getAllowedBreak()));
        Msg.raw(sender, " &7Place: &f" + namesOf(r.getAllowedPlace()));
        return true;
    }

    private boolean here(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        List<Region> regions = plugin.getRegionManager().regionsAt(player.getLocation());
        if (regions.isEmpty()) {
            Msg.send(sender, "&7You are not standing in any region.");
            return true;
        }
        Msg.send(sender, "&aYou are standing in:");
        for (Region r : regions) {
            Msg.raw(sender, " &8- &f" + r.getName() + " "
                    + (r.isEnabled() ? "&aenabled" : "&cdisabled"));
        }
        return true;
    }

    private boolean toggleVisual(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        if (!plugin.isVisualsEnabled()) {
            Msg.send(sender, "&cVisuals are disabled in the config.");
            return true;
        }
        boolean on = plugin.getVisualManager().toggle(player);
        if (!on) {
            Msg.send(sender, "&7Region visuals turned &foff&7.");
            return true;
        }
        Msg.send(sender, "&aRegion visuals turned &fon&a.");
        if (plugin.getVisualManager().resolveRegion(player) == null) {
            Msg.send(sender, "&7No region to show yet. Make a wand selection or stand in a region.");
        }
        return true;
    }

    private boolean hearts(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region region;
        Boolean value;
        if (args.length >= 3) {
            region = plugin.getRegionManager().get(args[1]);
            if (region == null) {
                Msg.send(sender, "&cNo region named &f" + args[1] + "&c.");
                return true;
            }
            value = parseOnOff(args[2]);
        } else if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "&cUsage: /sa hearts <regionName> <on|off>");
                return true;
            }
            value = parseOnOff(args[1]);
            List<Region> here = plugin.getRegionManager().regionsAt(player.getLocation());
            if (here.isEmpty()) {
                Msg.send(sender, "&cYou are not standing in a region. Use /sa hearts <regionName> <on|off>");
                return true;
            }
            region = here.get(0);
        } else {
            Msg.send(sender, "&cUsage: /sa hearts <regionName> <on|off>");
            return true;
        }
        if (value == null) {
            Msg.send(sender, "&cUse &fon &cor &foff&c.");
            return true;
        }
        region.setOneRowHearts(value);
        plugin.getRegionManager().save();
        plugin.getHeartsManager().recheckAll();
        Msg.send(sender, "&aOne-row hearts for &f" + region.getName() + "&a: "
                + (value ? "&aon" : "&coff"));
        return true;
    }

    private boolean naturalMobs(CommandSender sender, String[] args) {
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region region;
        Boolean value;
        if (args.length >= 3) {
            region = plugin.getRegionManager().get(args[1]);
            if (region == null) {
                Msg.send(sender, "&cNo region named &f" + args[1] + "&c.");
                return true;
            }
            value = parseOnOff(args[2]);
        } else if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "&cUsage: /sa naturalmobs <regionName> <on|off>");
                return true;
            }
            value = parseOnOff(args[1]);
            List<Region> here = plugin.getRegionManager().regionsAt(player.getLocation());
            if (here.isEmpty()) {
                Msg.send(sender, "&cYou are not standing in a region. Use /sa naturalmobs <regionName> <on|off>");
                return true;
            }
            region = here.get(0);
        } else {
            Msg.send(sender, "&cUsage: /sa naturalmobs <regionName> <on|off>");
            return true;
        }
        if (value == null) {
            Msg.send(sender, "&cUse &fon &cor &foff&c.");
            return true;
        }
        region.setDisableNaturalMobSpawning(!value);
        plugin.getRegionManager().save();
        Msg.send(sender, "&aNatural mob spawning for &f" + region.getName() + "&a: "
                + (value ? "&aon" : "&coff"));
        return true;
    }

    private Boolean parseOnOff(String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "on", "true", "enable", "yes" -> Boolean.TRUE;
            case "off", "false", "disable", "no" -> Boolean.FALSE;
            default -> null;
        };
    }

    private boolean visualize(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        World world = Bukkit.getWorld(r.getWorld());
        if (world == null) {
            Msg.send(sender, "&cWorld &f" + r.getWorld() + " &cis not loaded.");
            return true;
        }
        Msg.send(sender, "&aShowing outline of &f" + r.getName() + " &afor ~10s.");
        showOutline(player, world, r);
        return true;
    }

    private void showOutline(Player player, World world, Region r) {
        final List<Location> points = edgePoints(world, r);
        final Particle.DustOptions dust =
                new Particle.DustOptions(Color.fromRGB(0, 255, 120), 1.5f);
        // 20 ticks * 10 = ~10 seconds
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ >= 10 || !player.isOnline()) {
                    cancel();
                    return;
                }
                for (Location loc : points) {
                    player.spawnParticle(Particle.DUST, loc, 1, dust);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private List<Location> edgePoints(World world, Region r) {
        List<Location> pts = new ArrayList<>();
        double minX = r.getMinX(), minY = r.getMinY(), minZ = r.getMinZ();
        double maxX = r.getMaxX() + 1, maxY = r.getMaxY() + 1, maxZ = r.getMaxZ() + 1;
        double step = 1.0;
        // 12 edges of the cuboid
        for (double x = minX; x <= maxX; x += step) {
            pts.add(new Location(world, x, minY, minZ));
            pts.add(new Location(world, x, minY, maxZ));
            pts.add(new Location(world, x, maxY, minZ));
            pts.add(new Location(world, x, maxY, maxZ));
        }
        for (double y = minY; y <= maxY; y += step) {
            pts.add(new Location(world, minX, y, minZ));
            pts.add(new Location(world, minX, y, maxZ));
            pts.add(new Location(world, maxX, y, minZ));
            pts.add(new Location(world, maxX, y, maxZ));
        }
        for (double z = minZ; z <= maxZ; z += step) {
            pts.add(new Location(world, minX, minY, z));
            pts.add(new Location(world, minX, maxY, z));
            pts.add(new Location(world, maxX, minY, z));
            pts.add(new Location(world, maxX, maxY, z));
        }
        return pts;
    }

    private boolean hand(CommandSender sender, String[] args, boolean breakOp) {
        if (!(sender instanceof Player player)) {
            return playerOnly(sender);
        }
        if (!has(sender, "selectiveadventure.region.manage")) {
            return noPerm(sender);
        }
        Region r = require(sender, args);
        if (r == null) {
            return true;
        }
        Material mat = null;
        // for break: prefer the block being looked at
        if (breakOp) {
            Block target = player.getTargetBlockExact(6);
            if (target != null && target.getType().isBlock()
                    && target.getType() != Material.AIR) {
                mat = target.getType();
            }
        }
        // fall back to (or for place, use) the item in hand
        if (mat == null) {
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand != null && inHand.getType().isBlock()
                    && inHand.getType() != Material.AIR) {
                mat = inHand.getType();
            }
        }
        if (mat == null) {
            Msg.send(sender, "&cLook at a block or hold a block item first.");
            return true;
        }
        applyBlockOp(sender, r, mat, breakOp ? BlockOp.ALLOW_BREAK : BlockOp.ALLOW_PLACE);
        return true;
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------

    private Region require(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Msg.send(sender, "&cUsage: /sa " + args[0] + " <regionName>");
            return null;
        }
        Region r = plugin.getRegionManager().get(args[1]);
        if (r == null) {
            Msg.send(sender, "&cNo region named &f" + args[1] + "&c.");
        }
        return r;
    }

    private Material parseBlock(CommandSender sender, String input) {
        Material m = Material.matchMaterial(input);
        if (m == null) {
            Msg.send(sender, "&cUnknown material: &f" + input);
            return null;
        }
        if (!m.isBlock()) {
            Msg.send(sender, "&f" + m.name() + " &cis not a placeable/breakable block.");
            return null;
        }
        return m;
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null) {
            return cached;
        }
        // last resort: name-based lookup (may create a stub UUID for unknown names)
        return Bukkit.getOfflinePlayer(name);
    }

    private String namesOf(java.util.Collection<Material> mats) {
        if (mats.isEmpty()) {
            return "(none)";
        }
        return mats.stream().map(Material::name).sorted().collect(Collectors.joining(", "));
    }

    private boolean noPerm(CommandSender sender) {
        Msg.send(sender, "&cYou don't have permission to do that.");
        return true;
    }

    private boolean playerOnly(CommandSender sender) {
        Msg.send(sender, "&cThis command can only be used by a player.");
        return true;
    }

    // ------------------------------------------------------------------
    //  Tab completion
    // ------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("hearts") || sub.equals("naturalmobs")) {
            if (args.length == 2) {
                List<String> opts = new ArrayList<>(plugin.getRegionManager().names());
                opts.add("on");
                opts.add("off");
                return filter(opts, args[1]);
            }
            if (args.length == 3) {
                return filter(Arrays.asList("on", "off"), args[2]);
            }
            return List.of();
        }

        // arg 2 = region name for most subcommands
        if (args.length == 2 && needsRegionArg(sub)) {
            return filter(plugin.getRegionManager().names(), args[1]);
        }

        // arg 3
        if (args.length == 3) {
            if (sub.equals("addplayer")) {
                return filter(onlineNames(), args[2]);
            }
            if (sub.equals("removeplayer")) {
                Region r = plugin.getRegionManager().get(args[1]);
                if (r != null) {
                    return filter(new ArrayList<>(r.orderedPlayers().values()), args[2]);
                }
                return List.of();
            }
            if (isBlockSub(sub)) {
                return filter(blockNames(), args[2]);
            }
        }

        return List.of();
    }

    private boolean needsRegionArg(String sub) {
        return switch (sub) {
            case "delete", "info", "enable", "disable", "addplayer", "removeplayer",
                 "listplayers", "allowbreak", "denybreak", "allowplace", "denyplace",
                 "listblocks", "togglebreak", "toggleplace", "visualize",
                 "allowbreakhand", "allowplacehand" -> true;
            default -> false;
        };
    }

    private boolean isBlockSub(String sub) {
        return switch (sub) {
            case "allowbreak", "denybreak", "allowplace", "denyplace",
                 "togglebreak", "toggleplace" -> true;
            default -> false;
        };
    }

    private List<String> onlineNames() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return names;
    }

    private List<String> blockNames() {
        List<String> names = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m.isBlock() && !m.isLegacy()) {
                names.add(m.name().toLowerCase(Locale.ROOT));
            }
        }
        return names;
    }

    private List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(o);
            }
        }
        return out;
    }
}
