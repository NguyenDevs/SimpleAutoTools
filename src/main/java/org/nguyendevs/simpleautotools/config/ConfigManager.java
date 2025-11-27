package org.nguyendevs.simpleautotools.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.nguyendevs.simpleautotools.SimpleAutoTools;
import org.nguyendevs.simpleautotools.utils.PriorityType;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final SimpleAutoTools plugin;
    private FileConfiguration config;

    public ConfigManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // World settings
    public List<String> getEnabledWorlds() {
        return config.getStringList("enabled-worlds");
    }

    public boolean isWorldEnabled(String worldName) {
        List<String> enabledWorlds = getEnabledWorlds();
        return enabledWorlds.isEmpty() || enabledWorlds.contains(worldName);
    }

    // Tool priority settings
    public List<PriorityType> getPriorityOrder() {
        List<String> orderStrings = config.getStringList("priority.order");
        List<PriorityType> order = new ArrayList<>();

        for (String orderString : orderStrings) {
            try {
                PriorityType type = PriorityType.valueOf(orderString.toUpperCase());
                if (!order.contains(type)) {
                    order.add(type);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown priority type in config: " + orderString);
            }
        }

        // Default order if config is empty or invalid
        if (order.isEmpty()) {
            order.add(PriorityType.ENCHANTMENT);
            order.add(PriorityType.MATERIAL);
            order.add(PriorityType.DURABILITY);
        }

        return order;
    }

    public String getDurabilityPriority() {
        return config.getString("priority.durability", "HIGH");
    }

    public boolean isMaterialPriorityEnabled() {
        return config.getBoolean("priority.material.enabled", true);
    }

    public boolean isCheckHarvestLevelEnabled() {
        return config.getBoolean("priority.material.check-harvest-level", true);
    }

    // Search locations
    public boolean searchInHotbar() {
        return config.getBoolean("search-locations.hotbar", true);
    }

    public boolean searchInInventory() {
        return config.getBoolean("search-locations.inventory", true);
    }

    // Features
    public boolean isAutoSwitchForBlocksEnabled() {
        return config.getBoolean("features.auto-switch-blocks", true);
    }

    public boolean isAutoSwitchForEntitiesEnabled() {
        return config.getBoolean("features.auto-switch-entities", true);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}