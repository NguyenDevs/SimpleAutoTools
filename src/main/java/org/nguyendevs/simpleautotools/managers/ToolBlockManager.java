package org.nguyendevs.simpleautotools.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nguyendevs.simpleautotools.SimpleAutoTools;
import org.nguyendevs.simpleautotools.utils.ToolType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ToolBlockManager {

    private final SimpleAutoTools plugin;
    private File toolBlockFile;
    private FileConfiguration toolBlockConfig;
    private Map<ToolType, List<String>> toolBlockPatterns;
    private Map<Material, ToolType> materialCache;

    public ToolBlockManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
        this.toolBlockPatterns = new EnumMap<>(ToolType.class);
        this.materialCache = new EnumMap<>(Material.class);
    }

    public void loadToolBlocks() {
        toolBlockFile = new File(plugin.getDataFolder(), "tool-blocks.yml");

        if (!toolBlockFile.exists()) {
            plugin.saveResource("tool-blocks.yml", false);
        }

        toolBlockConfig = YamlConfiguration.loadConfiguration(toolBlockFile);

        // Load defaults
        InputStream defConfigStream = plugin.getResource("tool-blocks.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            toolBlockConfig.setDefaults(defConfig);
        }

        // Clear cache
        materialCache.clear();

        // Load patterns for each tool type
        for (ToolType toolType : ToolType.values()) {
            if (toolType == ToolType.NONE) continue;

            String path = toolType.name();
            List<String> patterns = toolBlockConfig.getStringList(path);
            toolBlockPatterns.put(toolType, patterns);
        }
    }

    public void saveToolBlocks() {
        try {
            toolBlockConfig.save(toolBlockFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save tool-blocks.yml!");
            e.printStackTrace();
        }
    }

    public ToolType getRequiredToolType(Material blockType) {
        // Check cache first
        if (materialCache.containsKey(blockType)) {
            return materialCache.get(blockType);
        }

        String name = blockType.name();
        ToolType result = ToolType.NONE;

        // Check each tool type's patterns
        for (Map.Entry<ToolType, List<String>> entry : toolBlockPatterns.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (matchesPattern(name, pattern)) {
                    result = entry.getKey();
                    materialCache.put(blockType, result);
                    return result;
                }
            }
        }

        // Cache the result (even if NONE)
        materialCache.put(blockType, result);
        return result;
    }

    private boolean matchesPattern(String materialName, String pattern) {
        pattern = pattern.toUpperCase().trim();

        // Exact match
        if (materialName.equals(pattern)) {
            return true;
        }

        // Contains check (indicated by *)
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            String substring = pattern.substring(1, pattern.length() - 1);
            return materialName.contains(substring);
        }

        // Starts with check
        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return materialName.startsWith(prefix);
        }

        // Ends with check
        if (pattern.startsWith("*")) {
            String suffix = pattern.substring(1);
            return materialName.endsWith(suffix);
        }

        // Default: contains check
        return materialName.contains(pattern);
    }

    public List<String> getPatternsForToolType(ToolType toolType) {
        return toolBlockPatterns.getOrDefault(toolType, Collections.emptyList());
    }
}