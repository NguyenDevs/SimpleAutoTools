package org.nguyendevs.simpleautotools.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nguyendevs.simpleautotools.SimpleAutoTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LanguageManager {

    private final SimpleAutoTools plugin;
    private File languageFile;
    private FileConfiguration languageConfig;

    public LanguageManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage() {
        languageFile = new File(plugin.getDataFolder(), "language.yml");

        if (!languageFile.exists()) {
            plugin.saveResource("language.yml", false);
        }

        languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Load defaults
        InputStream defConfigStream = plugin.getResource("language.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            languageConfig.setDefaults(defConfig);
        }
    }

    public void saveLanguage() {
        try {
            languageConfig.save(languageFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save language.yml!");
            e.printStackTrace();
        }
    }

    public String getMessage(String path) {
        String message = languageConfig.getString(path);
        if (message == null) {
            return ChatColor.RED + "Missing message: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}