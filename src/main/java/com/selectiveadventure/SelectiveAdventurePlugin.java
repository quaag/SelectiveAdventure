package com.selectiveadventure;

import com.selectiveadventure.command.SACommand;
import com.selectiveadventure.listener.BlockProtectionListener;
import com.selectiveadventure.listener.ForceAdventureListener;
import com.selectiveadventure.listener.WandListener;
import com.selectiveadventure.region.RegionManager;
import com.selectiveadventure.util.SelectionManager;
import com.selectiveadventure.visual.VisualManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class SelectiveAdventurePlugin extends JavaPlugin {

    private RegionManager regionManager;
    private SelectionManager selectionManager;
    private VisualManager visualManager;
    private NamespacedKey wandKey;

    // cached config values
    private boolean globalAdventureProtection;
    private boolean forceAdventureInRegions;
    private Material selectionTool;
    private boolean showActionbarDenyMessage;
    private String denyMessage;
    private boolean debug;

    private boolean visualsEnabled;
    private Color visualColor;
    private int visualRefreshTicks;
    private int visualMaxRadius;
    private long visualMaxVolume;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.wandKey = new NamespacedKey(this, "wand");

        loadConfigValues();

        this.selectionManager = new SelectionManager();
        this.regionManager = new RegionManager(this);
        this.regionManager.load();
        this.visualManager = new VisualManager(this);

        // command + tab completion
        SACommand command = new SACommand(this);
        PluginCommand sa = getCommand("sa");
        if (sa != null) {
            sa.setExecutor(command);
            sa.setTabCompleter(command);
        } else {
            getLogger().severe("Command 'sa' is not defined in plugin.yml!");
        }

        // listeners
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new ForceAdventureListener(this), this);
        getServer().getPluginManager().registerEvents(visualManager, this);
        visualManager.start();

        getLogger().info("SelectiveAdventure v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (visualManager != null) {
            visualManager.shutdown();
        }
        if (regionManager != null) {
            regionManager.save();
        }
        getLogger().info("SelectiveAdventure disabled.");
    }

    /** Reloads config.yml and regions.yml. */
    public void reloadAll() {
        reloadConfig();
        loadConfigValues();
        regionManager.load();
        visualManager.reload();
    }

    private void loadConfigValues() {
        this.globalAdventureProtection = getConfig().getBoolean("global-adventure-protection", false);
        this.forceAdventureInRegions = getConfig().getBoolean("force-adventure-in-regions", false);
        this.showActionbarDenyMessage = getConfig().getBoolean("show-actionbar-deny-message", true);
        this.denyMessage = getConfig().getString("deny-message", "&cYou cannot do that here.");
        this.debug = getConfig().getBoolean("debug", false);

        String toolName = getConfig().getString("selection-tool", "GOLDEN_AXE");
        Material tool = Material.matchMaterial(toolName == null ? "" : toolName);
        if (tool == null) {
            getLogger().warning("Invalid selection-tool '" + toolName + "', defaulting to GOLDEN_AXE.");
            tool = Material.GOLDEN_AXE;
        }
        this.selectionTool = tool;

        this.visualsEnabled = getConfig().getBoolean("visuals.enabled", true);
        this.visualColor = parseColor(getConfig().getString("visuals.color", "aqua"));
        this.visualRefreshTicks = Math.max(1, getConfig().getInt("visuals.refresh-ticks", 20));
        this.visualMaxRadius = Math.max(1, getConfig().getInt("visuals.max-radius", 128));
        this.visualMaxVolume = Math.max(0L, getConfig().getLong("visuals.max-volume", 250000L));
    }

    private Color parseColor(String name) {
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT).replace(' ', '_');
        return switch (n) {
            case "light_blue", "lightblue" -> Color.fromRGB(102, 204, 255);
            case "blue" -> Color.fromRGB(0, 128, 255);
            case "cyan", "aqua" -> Color.AQUA;
            default -> Color.AQUA;
        };
    }

    public void debug(String message) {
        if (debug) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    // --- accessors ---

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public VisualManager getVisualManager() {
        return visualManager;
    }

    public boolean isVisualsEnabled() {
        return visualsEnabled;
    }

    public Color getVisualColor() {
        return visualColor;
    }

    public int getVisualRefreshTicks() {
        return visualRefreshTicks;
    }

    public int getVisualMaxRadius() {
        return visualMaxRadius;
    }

    public long getVisualMaxVolume() {
        return visualMaxVolume;
    }

    public NamespacedKey getWandKey() {
        return wandKey;
    }

    public boolean isGlobalAdventureProtection() {
        return globalAdventureProtection;
    }

    public boolean isForceAdventureInRegions() {
        return forceAdventureInRegions;
    }

    public Material getSelectionTool() {
        return selectionTool;
    }

    public boolean isShowActionbarDenyMessage() {
        return showActionbarDenyMessage;
    }

    public String getDenyMessage() {
        return denyMessage;
    }
}
