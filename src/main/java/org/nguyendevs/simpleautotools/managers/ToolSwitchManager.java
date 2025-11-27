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

    // ============================================================
    // Find Best TOOL for Block (Now uses Tag-based canHarvest)
    // ============================================================
    private ItemStack findBestTool(Player player, ToolType toolType, Material blockType) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> availableTools = new ArrayList<>();

        boolean searchHotbar = plugin.getConfigManager().searchInHotbar();
        boolean searchInv = plugin.getConfigManager().searchInInventory();
        boolean checkHarvest = plugin.getConfigManager().isCheckHarvestLevelEnabled();

        // -------- HOTBAR --------
        if (searchHotbar) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) continue;

                if (ToolUtils.isToolType(item.getType(), toolType)) {
                    if (!checkHarvest || ToolUtils.canHarvest(item.getType(), blockType)) {
                        availableTools.add(item);
                    }
                }
            }
        }

        // -------- MAIN INVENTORY --------
        if (searchInv) {
            for (int i = 9; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) continue;

                if (ToolUtils.isToolType(item.getType(), toolType)) {
                    if (!checkHarvest || ToolUtils.canHarvest(item.getType(), blockType)) {
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

    // ============================================================
    // Find Best WEAPON
    // ============================================================
    private ItemStack findBestWeapon(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> weapons = new ArrayList<>();

        boolean searchHotbar = plugin.getConfigManager().searchInHotbar();
        boolean searchInv = plugin.getConfigManager().searchInInventory();

        // --- HOTBAR ---
        if (searchHotbar) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && ToolUtils.isWeapon(item.getType())) {
                    weapons.add(item);
                }
            }
        }

        // --- INVENTORY ---
        if (searchInv) {
            for (int i = 9; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && ToolUtils.isWeapon(item.getType())) {
                    weapons.add(item);
                }
            }
        }

        if (weapons.isEmpty()) {
            return null;
        }

        ToolType weaponType =
                weapons.get(0).getType().name().endsWith("_SWORD")
                        ? ToolType.SWORD
                        : ToolType.AXE;

        weapons.sort(new ToolComparator(weaponType));
        return weapons.get(0);
    }

    // ============================================================
    // Switch Tool
    // ============================================================
    private void switchToTool(Player player, ItemStack tool) {
        PlayerInventory inventory = player.getInventory();
        ItemStack currentItem = inventory.getItemInMainHand();

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

        if (toolSlot == -1) return;

        if (toolSlot < 9) {
            player.getInventory().setHeldItemSlot(toolSlot);
        } else {
            inventory.setItem(toolSlot, currentItem);
            inventory.setItemInMainHand(tool);
        }
    }

    // ============================================================
    // Tool Comparator
    // ============================================================
    private class ToolComparator implements Comparator<ItemStack> {
        private final ToolType toolType;

        public ToolComparator(ToolType toolType) {
            this.toolType = toolType;
        }

        @Override
        public int compare(ItemStack t1, ItemStack t2) {
            List<PriorityType> order = plugin.getConfigManager().getPriorityOrder();

            for (PriorityType type : order) {
                int result = 0;

                switch (type) {
                    case ENCHANTMENT:
                        result = compareEnchantments(t1, t2);
                        break;

                    case MATERIAL:
                        result = compareMaterial(t1, t2);
                        break;

                    case DURABILITY:
                        result = compareDurability(t1, t2);
                        break;
                }

                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        private int compareEnchantments(ItemStack t1, ItemStack t2) {
            List<Enchantment> enchList = plugin.getPriorityManager().getEnchantmentPriority(toolType);

            for (Enchantment ench : enchList) {
                int lvl1 = t1.getEnchantmentLevel(ench);
                int lvl2 = t2.getEnchantmentLevel(ench);

                if (lvl1 != lvl2) {
                    return Integer.compare(lvl2, lvl1);
                }
            }
            return 0;
        }

        private int compareMaterial(ItemStack t1, ItemStack t2) {
            if (!plugin.getConfigManager().isMaterialPriorityEnabled()) return 0;

            int tier1 = ToolUtils.getMaterialTier(t1.getType());
            int tier2 = ToolUtils.getMaterialTier(t2.getType());

            if (tier1 != tier2) {
                return Integer.compare(tier2, tier1);
            }
            return 0;
        }

        private int compareDurability(ItemStack t1, ItemStack t2) {
            String dur = plugin.getConfigManager().getDurabilityPriority();

            if (dur.equalsIgnoreCase("NONE")) return 0;

            int d1 = ToolUtils.getRemainingDurability(t1);
            int d2 = ToolUtils.getRemainingDurability(t2);

            if (dur.equalsIgnoreCase("HIGH")) {
                return Integer.compare(d2, d1);
            } else if (dur.equalsIgnoreCase("LOW")) {
                return Integer.compare(d1, d2);
            }
            return 0;
        }
    }
}
