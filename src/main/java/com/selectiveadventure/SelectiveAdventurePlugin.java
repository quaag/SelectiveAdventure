package com.selectiveadventure;

import com.selectiveadventure.command.SACommand;
import com.selectiveadventure.listener.BlockProtectionListener;
import com.selectiveadventure.listener.ForceAdventureListener;
import com.selectiveadventure.listener.WandListener;
import com.selectiveadventure.region.RegionManager;
import com.selectiveadventure.util.SelectionManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SelectiveAdventurePlugin extends JavaPlugin {

    private RegionManager regionManager;
    private SelectionManager selectionManager;
    private NamespacedKey wandKey;

    // cached config values
    private boolean globalAdventureProtection;
    private boolean forceAdventureInRegions;
    private Material selectionTool;
    private boolean showActionbarDenyMessage;
    private String denyMessage;
    private boolean debug;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.wandKey = new NamespacedKey(this, "wand");

        loadConfigValues();

        this.selectionManager = new SelectionManager();
        this.regionManager = new RegionManager(this);
        this.regionManager.load();

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

        getLogger().info("SelectiveAdventure v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
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
