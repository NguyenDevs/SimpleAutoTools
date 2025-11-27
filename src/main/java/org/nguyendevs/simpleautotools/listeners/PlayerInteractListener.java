package org.nguyendevs.simpleautotools.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nguyendevs.simpleautotools.SimpleAutoTools;

public class PlayerInteractListener implements Listener {

    private final SimpleAutoTools plugin;

    public PlayerInteractListener(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Ignore creative and spectator mode
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Check if player has auto-tool enabled
        if (!plugin.getDataManager().isPlayerEnabled(player.getUniqueId())) {
            return;
        }

        // Check if world is enabled
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        // Switch tool immediately when player starts mining
        plugin.getToolSwitchManager().switchToolForBlock(player, block);
    }
}