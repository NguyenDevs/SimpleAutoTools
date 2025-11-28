package org.nguyendevs.simpleautotools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.nguyendevs.simpleautotools.commands.AutoToolCommand;
import org.nguyendevs.simpleautotools.config.ConfigManager;
import org.nguyendevs.simpleautotools.config.LanguageManager;
import org.nguyendevs.simpleautotools.managers.PriorityManager;
import org.nguyendevs.simpleautotools.data.DataManager;
import org.nguyendevs.simpleautotools.listeners.EntityDamageListener;
import org.nguyendevs.simpleautotools.listeners.PlayerInteractListener;
import org.nguyendevs.simpleautotools.managers.RefactoredToolSwitchManager;

/**
 * SimpleAutoTools - Refactored version using Tag API
 *
 * CHANGES FROM ORIGINAL:
 * - Removed ToolBlockManager (no longer needed, uses Tag API)
 * - Removed tool-blocks.yml (replaced by Minecraft's built-in tags)
 * - Uses RefactoredToolSwitchManager with Tag-based detection
 * - More maintainable and automatically compatible with new blocks
 */
public final class SimpleAutoTools extends JavaPlugin {

    private static SimpleAutoTools instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DataManager dataManager;
    private RefactoredToolSwitchManager toolSwitchManager;
    private PriorityManager priorityManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers (ToolBlockManager removed)
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.priorityManager = new PriorityManager(this);
        this.dataManager = new DataManager(this);
        this.toolSwitchManager = new RefactoredToolSwitchManager(this);

        // Load configurations (tool-blocks.yml no longer needed)
        configManager.loadConfig();
        languageManager.loadLanguage();
        priorityManager.loadPriority();
        dataManager.loadData();

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        printLogo();
        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        "&3[&bSAT&3] &aSimpleAutoTools (Tag-Based) has been enabled!")
        );
        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        "&3[&bSAT&3] &7Using Minecraft Tag API for block detection")
        );
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }

        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        "&3[&bSAT&3] &cSimpleAutoTools has been disabled!")
        );
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
    }

    private void registerCommands() {
        getCommand("autotool").setExecutor(new AutoToolCommand(this));
    }

    public void reload() {
        configManager.loadConfig();
        languageManager.loadLanguage();
        priorityManager.loadPriority();
        dataManager.loadData();

        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        "&3[&bSAT&3] &aConfiguration reloaded!")
        );
    }

    // Getters
    public static SimpleAutoTools getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public RefactoredToolSwitchManager getToolSwitchManager() {
        return toolSwitchManager;
    }

    public PriorityManager getPriorityManager() {
        return priorityManager;
    }

    /**
     * Check if smart enchantment selection is enabled
     * This is used as a tiebreaker when tools have equal priority
     */
    public boolean isSmartEnchantmentEnabled() {
        return configManager.getConfig().getBoolean("smart-enchantment-selection", true);
    }

    // Logo display
    public void printLogo() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ███████╗ █████╗ ████████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ██╔════╝██╔══██╗╚══██╔══╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ███████╗███████║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ╚════██║██╔══██║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ███████║██║  ██║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ╚══════╝╚═╝  ╚═╝   ╚═╝   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3         Simple Auto Tools &7(Tag-Based)"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6         Version " + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b         Development by NguyenDevs"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    }
}