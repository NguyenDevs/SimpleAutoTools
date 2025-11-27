package org.nguyendevs.simpleautotools.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.nguyendevs.simpleautotools.SimpleAutoTools;
import org.nguyendevs.simpleautotools.utils.ToolType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PriorityManager {

    private final SimpleAutoTools plugin;
    private File priorityFile;
    private FileConfiguration priorityConfig;
    private Map<ToolType, List<Enchantment>> enchantmentPriorities;

    public PriorityManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
        this.enchantmentPriorities = new EnumMap<>(ToolType.class);
    }

    public void loadPriority() {
        priorityFile = new File(plugin.getDataFolder(), "priority.yml");

        if (!priorityFile.exists()) {
            plugin.saveResource("priority.yml", false);
        }

        priorityConfig = YamlConfiguration.loadConfiguration(priorityFile);

        // Load defaults
        InputStream defConfigStream = plugin.getResource("priority.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            priorityConfig.setDefaults(defConfig);
        }

        // Parse enchantment priorities for each tool type
        for (ToolType toolType : ToolType.values()) {
            if (toolType == ToolType.NONE) continue;

            String path = toolType.name();
            List<String> enchantNames = priorityConfig.getStringList(path);
            List<Enchantment> enchants = new ArrayList<>();

            for (String enchantName : enchantNames) {
                Enchantment enchant = getEnchantmentByName(enchantName);
                if (enchant != null) {
                    enchants.add(enchant);
                } else {
                    plugin.getLogger().warning("Unknown enchantment in priority.yml: " + enchantName + " for " + toolType);
                }
            }

            enchantmentPriorities.put(toolType, enchants);
        }
    }

    public void savePriority() {
        try {
            priorityConfig.save(priorityFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save priority.yml!");
            e.printStackTrace();
        }
    }

    public List<Enchantment> getEnchantmentPriority(ToolType toolType) {
        return enchantmentPriorities.getOrDefault(toolType, Collections.emptyList());
    }

    private Enchantment getEnchantmentByName(String name) {
        name = name.toUpperCase().replace("-", "_").replace(" ", "_");

        try {
            return Enchantment.getByName(name);
        } catch (Exception e) {
            // Try common aliases
            switch (name) {
                case "EFFICIENCY":
                case "DIG_SPEED":
                    return Enchantment.DIG_SPEED;
                case "FORTUNE":
                case "LOOT_BONUS_BLOCKS":
                    return Enchantment.LOOT_BONUS_BLOCKS;
                case "SILK_TOUCH":
                case "SILKTOUCH":
                    return Enchantment.SILK_TOUCH;
                case "UNBREAKING":
                case "DURABILITY":
                    return Enchantment.DURABILITY;
                case "MENDING":
                    return Enchantment.MENDING;
                case "SHARPNESS":
                case "DAMAGE_ALL":
                    return Enchantment.DAMAGE_ALL;
                case "FIRE_ASPECT":
                case "FIRE":
                    return Enchantment.FIRE_ASPECT;
                case "LOOTING":
                case "LOOT_BONUS_MOBS":
                    return Enchantment.LOOT_BONUS_MOBS;
                case "SWEEPING":
                case "SWEEPING_EDGE":
                    return Enchantment.SWEEPING_EDGE;
                case "KNOCKBACK":
                    return Enchantment.KNOCKBACK;
                default:
                    return null;
            }
        }
    }
}