package org.nguyendevs.simpleautotools.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nguyendevs.simpleautotools.SimpleAutoTools;

public class EntityDamageListener implements Listener {

    private final SimpleAutoTools plugin;

    public EntityDamageListener(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        // Check if player has auto-tool enabled
        if (!plugin.getDataManager().isPlayerEnabled(player.getUniqueId())) {
            return;
        }

        // Check if world is enabled
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        plugin.getToolSwitchManager().switchWeaponForEntity(player);
    }
}