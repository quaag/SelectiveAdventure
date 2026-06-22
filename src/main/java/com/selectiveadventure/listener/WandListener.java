package com.selectiveadventure.listener;

import com.selectiveadventure.SelectiveAdventurePlugin;
import com.selectiveadventure.util.Msg;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles wand selections: left-click sets pos1, right-click sets pos2.
 */
public class WandListener implements Listener {

    private final SelectiveAdventurePlugin plugin;

    public WandListener(SelectiveAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !isWand(item)) {
            return;
        }
        if (!player.hasPermission("selectiveadventure.wand")
                && !player.hasPermission("selectiveadventure.admin")) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Action action = event.getAction();
        Location loc = block.getLocation();

        if (action == Action.LEFT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos1(player.getUniqueId(), loc);
            event.setCancelled(true);
            Msg.send(player, "&aPos1 &7set to &f"
                    + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos2(player.getUniqueId(), loc);
            event.setCancelled(true);
            Msg.send(player, "&aPos2 &7set to &f"
                    + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        }
    }

    private boolean isWand(ItemStack item) {
        if (item.getType() != plugin.getSelectionTool()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        Byte flag = meta.getPersistentDataContainer()
                .get(plugin.getWandKey(), PersistentDataType.BYTE);
        return flag != null && flag == 1;
    }
}
