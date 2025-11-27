package org.nguyendevs.simpleautotools.managers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.nguyendevs.simpleautotools.SimpleAutoTools;
import org.nguyendevs.simpleautotools.utils.PriorityType;
import org.nguyendevs.simpleautotools.utils.ToolType;
import org.nguyendevs.simpleautotools.utils.ToolUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ToolSwitchManager {

    private final SimpleAutoTools plugin;

    public ToolSwitchManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    public void switchToolForBlock(Player player, Block block) {
        if (!plugin.getDataManager().isPlayerEnabled(player.getUniqueId())) {
            return;
        }

        if (!plugin.getConfigManager().isAutoSwitchForBlocksEnabled()) {
            return;
        }

        Material blockType = block.getType();
        ToolType requiredTool = plugin.getToolBlockManager().getRequiredToolType(blockType);

        if (requiredTool == ToolType.NONE) {
            return;
        }

        ItemStack bestTool = findBestTool(player, requiredTool, blockType);

        if (bestTool != null) {
            switchToTool(player, bestTool);
        }
    }

    public void switchWeaponForEntity(Player player) {
        if (!plugin.getDataManager().isPlayerEnabled(player.getUniqueId())) {
            return;
        }

        if (!plugin.getConfigManager().isAutoSwitchForEntitiesEnabled()) {
            return;
        }

        ItemStack bestWeapon = findBestWeapon(player);

        if (bestWeapon != null) {
            switchToTool(player, bestWeapon);
        }
    }

    private ItemStack findBestTool(Player player, ToolType toolType, Material blockType) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> availableTools = new ArrayList<>();

        if (plugin.getConfigManager().searchInHotbar()) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && ToolUtils.isToolType(item.getType(), toolType)) {
                    // Check if tool can harvest this block
                    if (plugin.getConfigManager().isCheckHarvestLevelEnabled()) {
                        if (ToolUtils.canHarvest(item.getType(), blockType)) {
                            availableTools.add(item);
                        }
                    } else {
                        availableTools.add(item);
                    }
                }
            }
        }

        if (plugin.getConfigManager().searchInInventory()) {
            for (int i = 9; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && ToolUtils.isToolType(item.getType(), toolType)) {
                    // Check if tool can harvest this block
                    if (plugin.getConfigManager().isCheckHarvestLevelEnabled()) {
                        if (ToolUtils.canHarvest(item.getType(), blockType)) {
                            availableTools.add(item);
                        }
                    } else {
                        availableTools.add(item);
                    }
                }
            }
        }

        if (availableTools.isEmpty()) {
            return null;
        }

        availableTools.sort(new ToolComparator(toolType));

        return availableTools.get(0);
    }

    private ItemStack findBestWeapon(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> availableWeapons = new ArrayList<>();

        if (plugin.getConfigManager().searchInHotbar()) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && ToolUtils.isWeapon(item.getType())) {
                    availableWeapons.add(item);
                }
            }
        }

        if (plugin.getConfigManager().searchInInventory()) {
            for (int i = 9; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && ToolUtils.isWeapon(item.getType())) {
                    availableWeapons.add(item);
                }
            }
        }

        if (availableWeapons.isEmpty()) {
            return null;
        }

        ToolType weaponType = availableWeapons.get(0).getType().name().endsWith("_SWORD") ? ToolType.SWORD : ToolType.AXE;
        availableWeapons.sort(new ToolComparator(weaponType));

        return availableWeapons.get(0);
    }

    private void switchToTool(Player player, ItemStack tool) {
        PlayerInventory inventory = player.getInventory();
        ItemStack currentItem = inventory.getItemInMainHand();

        // Don't switch if already holding the correct tool
        if (currentItem != null && currentItem.equals(tool)) {
            return;
        }

        int toolSlot = -1;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.equals(tool)) {
                toolSlot = i;
                break;
            }
        }

        if (toolSlot == -1) {
            return;
        }

        if (toolSlot < 9) {
            // Tool is in hotbar, just switch to it
            player.getInventory().setHeldItemSlot(toolSlot);
        } else {
            // Tool is in main inventory, swap with current item
            inventory.setItem(toolSlot, currentItem);
            inventory.setItemInMainHand(tool);
        }
    }

    private class ToolComparator implements Comparator<ItemStack> {
        private final ToolType toolType;

        public ToolComparator(ToolType toolType) {
            this.toolType = toolType;
        }

        @Override
        public int compare(ItemStack tool1, ItemStack tool2) {
            List<PriorityType> priorityOrder = plugin.getConfigManager().getPriorityOrder();

            for (PriorityType priorityType : priorityOrder) {
                int result = 0;

                switch (priorityType) {
                    case ENCHANTMENT:
                        result = compareEnchantments(tool1, tool2);
                        break;
                    case MATERIAL:
                        result = compareMaterial(tool1, tool2);
                        break;
                    case DURABILITY:
                        result = compareDurability(tool1, tool2);
                        break;
                }

                // If this priority level found a difference, return it
                if (result != 0) {
                    return result;
                }
            }

            return 0;
        }

        private int compareEnchantments(ItemStack tool1, ItemStack tool2) {
            List<Enchantment> enchantPriority = plugin.getPriorityManager().getEnchantmentPriority(toolType);

            for (Enchantment enchant : enchantPriority) {
                int level1 = tool1.getEnchantmentLevel(enchant);
                int level2 = tool2.getEnchantmentLevel(enchant);

                // If one has the enchantment and the other doesn't
                if ((level1 > 0 && level2 == 0) || (level1 == 0 && level2 > 0)) {
                    return Integer.compare(level2, level1);
                }

                // If both have the enchantment, compare levels
                if (level1 > 0 && level2 > 0 && level1 != level2) {
                    return Integer.compare(level2, level1);
                }
            }

            return 0;
        }

        private int compareMaterial(ItemStack tool1, ItemStack tool2) {
            if (!plugin.getConfigManager().isMaterialPriorityEnabled()) {
                return 0;
            }

            int tier1 = ToolUtils.getMaterialTier(tool1.getType());
            int tier2 = ToolUtils.getMaterialTier(tool2.getType());

            if (tier1 != tier2) {
                return Integer.compare(tier2, tier1);
            }

            return 0;
        }

        private int compareDurability(ItemStack tool1, ItemStack tool2) {
            String durabilityPriority = plugin.getConfigManager().getDurabilityPriority();

            if (durabilityPriority.equalsIgnoreCase("NONE")) {
                return 0;
            }

            int dur1 = ToolUtils.getRemainingDurability(tool1);
            int dur2 = ToolUtils.getRemainingDurability(tool2);

            if (durabilityPriority.equalsIgnoreCase("HIGH")) {
                return Integer.compare(dur2, dur1);
            } else if (durabilityPriority.equalsIgnoreCase("LOW")) {
                return Integer.compare(dur1, dur2);
            }

            return 0;
        }
    }
}