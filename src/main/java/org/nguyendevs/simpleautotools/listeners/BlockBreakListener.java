package org.nguyendevs.simpleautotools.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nguyendevs.simpleautotools.SimpleAutoTools;

public class BlockBreakListener implements Listener {

    private final SimpleAutoTools plugin;

    public BlockBreakListener(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        // This listener is kept for compatibility and additional checks if needed
        // Main switching logic is now in PlayerInteractListener
    }
}