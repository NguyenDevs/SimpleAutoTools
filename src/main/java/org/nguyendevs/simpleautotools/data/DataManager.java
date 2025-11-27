package org.nguyendevs.simpleautotools.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nguyendevs.simpleautotools.SimpleAutoTools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final SimpleAutoTools plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private Map<UUID, PlayerData> playerDataMap;

    public DataManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all player data
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean enabled = dataConfig.getBoolean(key + ".enabled", true);
                playerDataMap.put(uuid, new PlayerData(uuid, enabled));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + key);
            }
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            String path = entry.getKey().toString();
            dataConfig.set(path + ".enabled", entry.getValue().isEnabled());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(uuid, true));
    }

    public void setPlayerEnabled(UUID uuid, boolean enabled) {
        PlayerData data = getPlayerData(uuid);
        data.setEnabled(enabled);
        saveData();
    }

    public boolean isPlayerEnabled(UUID uuid) {
        return getPlayerData(uuid).isEnabled();
    }
}