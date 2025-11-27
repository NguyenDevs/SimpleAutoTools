package org.nguyendevs.simpleautotools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nguyendevs.simpleautotools.SimpleAutoTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoToolCommand implements CommandExecutor, TabCompleter {

    private final SimpleAutoTools plugin;

    public AutoToolCommand(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + "§7Use: §e/sat toggle");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);

            case "toggle":
                return handleToggle(sender, args);

            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + "§7Use: §e/sat toggle");
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("simpleautotools.reload")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.no-permission"));
            return true;
        }

        try {
            plugin.reload();
            sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.reload.success"));
        } catch (Exception e) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.reload.error"));
            e.printStackTrace();
        }

        return true;
    }

    private boolean handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("simpleautotools.toggle")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.no-permission"));
            return true;
        }

        boolean currentStatus = plugin.getDataManager().isPlayerEnabled(player.getUniqueId());
        boolean newStatus = !currentStatus;

        plugin.getDataManager().setPlayerEnabled(player.getUniqueId(), newStatus);

        if (newStatus) {
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.toggle.enabled"));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("prefix") + " " + plugin.getLanguageManager().getMessage("command.toggle.disabled"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = Arrays.asList("toggle", "reload");

            for (String cmd : commands) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        }

        return completions;
    }
}