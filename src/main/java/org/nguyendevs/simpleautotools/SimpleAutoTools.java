package org.nguyendevs.simpleautotools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.nguyendevs.simpleautotools.commands.AutoToolCommand;
import org.nguyendevs.simpleautotools.config.ConfigManager;
import org.nguyendevs.simpleautotools.config.LanguageManager;
import org.nguyendevs.simpleautotools.config.PriorityManager;
import org.nguyendevs.simpleautotools.config.ToolBlockManager;
import org.nguyendevs.simpleautotools.data.DataManager;
import org.nguyendevs.simpleautotools.listeners.BlockBreakListener;
import org.nguyendevs.simpleautotools.listeners.EntityDamageListener;
import org.nguyendevs.simpleautotools.listeners.PlayerInteractListener;
import org.nguyendevs.simpleautotools.managers.ToolSwitchManager;

public final class SimpleAutoTools extends JavaPlugin {

    private static SimpleAutoTools instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DataManager dataManager;
    private ToolSwitchManager toolSwitchManager;
    private PriorityManager priorityManager;
    private ToolBlockManager toolBlockManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.priorityManager = new PriorityManager(this);
        this.toolBlockManager = new ToolBlockManager(this);
        this.dataManager = new DataManager(this);
        this.toolSwitchManager = new ToolSwitchManager(this);

        // Load configurations
        configManager.loadConfig();
        languageManager.loadLanguage();
        priorityManager.loadPriority();
        toolBlockManager.loadToolBlocks();
        dataManager.loadData();

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        printLogo();
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[&bSAT&3] &aSimpleAutoTools has been enabled!"));
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3[&bSAT&3] &cSimpleAutoTools has been disabled!"));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
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
        toolBlockManager.loadToolBlocks();
        dataManager.loadData();
    }

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

    public ToolSwitchManager getToolSwitchManager() {
        return toolSwitchManager;
    }

    public PriorityManager getPriorityManager() {
        return priorityManager;
    }

    public ToolBlockManager getToolBlockManager() {
        return toolBlockManager;
    }

    public void printLogo() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ███████╗ █████╗ ████████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ██╔════╝██╔══██╗╚══██╔══╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ███████╗███████║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ╚════██║██╔══██║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ███████║██║  ██║   ██║   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b   ╚══════╝╚═╝  ╚═╝   ╚═╝   "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3         Simple Auto Tools"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6         Version " + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b         Development by NguyenDevs"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    }
}