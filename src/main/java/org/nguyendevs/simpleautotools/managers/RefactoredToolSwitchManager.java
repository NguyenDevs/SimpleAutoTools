package org.nguyendevs.simpleautotools.managers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.nguyendevs.simpleautotools.SimpleAutoTools;
import org.nguyendevs.simpleautotools.utils.PriorityType;
import org.nguyendevs.simpleautotools.utils.TagBasedToolUtils;
import org.nguyendevs.simpleautotools.utils.ToolType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Refactored ToolSwitchManager using Tag-based system
 * No longer requires tool-blocks.yml pattern matching
 */
public class RefactoredToolSwitchManager {

    private final SimpleAutoTools plugin;

    public RefactoredToolSwitchManager(SimpleAutoTools plugin) {
        this.plugin = plugin;
    }

    /**
     * Switch to the best tool for mining a block
     * Now uses Tag-based detection instead of pattern matching
     */
    public void switchToolForBlock(Player player, Block block) {
        if (!plugin.getDataManager().isPlayerEnabled(player.getUniqueId())) {
            return;
        }

        if (!plugin.getConfigManager().isAutoSwitchForBlocksEnabled()) {
            return;
        }

        Material blockType = block.getType();

        // Use Tag-based detection instead of tool-blocks.yml
        ToolType requiredTool = TagBasedToolUtils.getRequiredToolType(blockType);

        if (requiredTool == ToolType.NONE) {
            return;
        }

        ItemStack bestTool = findBestTool(player, requiredTool, blockType);

        if (bestTool != null) {
            switchToTool(player, bestTool);
        }
    }

    /**
     * Switch to the best weapon for attacking entities
     */
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

    /**
     * Find the best tool for a specific block type
     * Uses Tag-based harvest checking
     */
    private ItemStack findBestTool(Player player, ToolType toolType, Material blockType) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> availableTools = new ArrayList<>();

        boolean searchHotbar = plugin.getConfigManager().searchInHotbar();
        boolean searchInv = plugin.getConfigManager().searchInInventory();
        boolean checkHarvest = plugin.getConfigManager().isCheckHarvestLevelEnabled();

        // Search hotbar (slots 0-8)
        if (searchHotbar) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) continue;

                if (TagBasedToolUtils.isToolType(item.getType(), toolType)) {
                    // Check if tool can actually harvest this block
                    if (!checkHarvest || TagBasedToolUtils.canHarvest(item.getType(), blockType)) {
                        availableTools.add(item);
                    }
                }
            }
        }

        // Search main inventory (slots 9-35)
        if (searchInv) {
            for (int i = 9; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) continue;

                if (TagBasedToolUtils.isToolType(item.getType(), toolType)) {
                    if (!checkHarvest || TagBasedToolUtils.canHarvest(item.getType(), blockType)) {
                        availableTools.add(item);
                    }
                }
            }
        }

        if (availableTools.isEmpty()) {
            return null;
        }

        // Sort by priority and return the best one
        availableTools.sort(new ToolComparator(toolType, blockType));
        return availableTools.get(0);
    }

    /**
     * Find the best weapon for combat
     */
    private ItemStack findBestWeapon(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> weapons = new ArrayList<>();

        boolean searchHotbar = plugin.getConfigManager().searchInHotbar();
        boolean searchInv = plugin.getConfigManager().searchInInventory();

        // Search hotbar
        if (searchHotbar) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && TagBasedToolUtils.isWeapon(item.getType())) {
                    weapons.add(item);
                }
            }
        }

        // Search main inventory
        if (searchInv) {
            for (int i = 9; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && TagBasedToolUtils.isWeapon(item.getType())) {
                    weapons.add(item);
                }
            }
        }

        if (weapons.isEmpty()) {
            return null;
        }

        // Determine weapon type for priority
        ToolType weaponType = weapons.get(0).getType().name().endsWith("_SWORD")
                ? ToolType.SWORD
                : ToolType.AXE;

        weapons.sort(new ToolComparator(weaponType, null));
        return weapons.get(0);
    }

    /**
     * Switch player's held item to the specified tool
     */
    private void switchToTool(Player player, ItemStack tool) {
        PlayerInventory inventory = player.getInventory();
        ItemStack currentItem = inventory.getItemInMainHand();

        // Already holding the best tool
        if (currentItem != null && currentItem.equals(tool)) {
            return;
        }

        // Find the slot containing the tool
        int toolSlot = -1;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.equals(tool)) {
                toolSlot = i;
                break;
            }
        }

        if (toolSlot == -1) return;

        // If tool is in hotbar, just change held slot
        if (toolSlot < 9) {
            player.getInventory().setHeldItemSlot(toolSlot);
        }
        // If tool is in main inventory, swap with current item
        else {
            inventory.setItem(toolSlot, currentItem);
            inventory.setItemInMainHand(tool);
        }
    }

    /**
     * Comparator for sorting tools by priority
     * Now includes block-aware enchantment prioritization
     */
    private class ToolComparator implements Comparator<ItemStack> {
        private final ToolType toolType;
        private final Material blockType; // Can be null for weapons

        public ToolComparator(ToolType toolType, Material blockType) {
            this.toolType = toolType;
            this.blockType = blockType;
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

                // If this priority level makes a difference, return it
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        /**
         * Compare enchantments with optional smart logic
         * Smart logic can be disabled in config.yml
         */
        private int compareEnchantments(ItemStack t1, ItemStack t2) {
            // Get user-defined enchantment priority from priority.yml
            List<Enchantment> enchList = plugin.getPriorityManager().getEnchantmentPriority(toolType);

            // SMART LOGIC (Optional) - Only applies if enabled in config
            // This helps BETWEEN tools with same priority enchants
            if (blockType != null && plugin.getConfigManager().isSmartEnchantmentEnabled()) {
                // Example scenario: Both tools have same enchants at same levels
                // Smart logic acts as a tiebreaker

                // Check if both tools are equal on user's priority list first
                boolean toolsAreEqual = true;
                for (Enchantment ench : enchList) {
                    int lvl1 = t1.getEnchantmentLevel(ench);
                    int lvl2 = t2.getEnchantmentLevel(ench);
                    if (lvl1 != lvl2) {
                        toolsAreEqual = false;
                        break;
                    }
                }

                // Only use smart logic as tiebreaker
                if (toolsAreEqual) {
                    // For glass/ice: prefer Silk Touch as tiebreaker
                    if (TagBasedToolUtils.prefersSilkTouch(blockType)) {
                        boolean t1HasSilk = t1.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
                        boolean t2HasSilk = t2.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;

                        if (t1HasSilk && !t2HasSilk) return -1;
                        if (!t1HasSilk && t2HasSilk) return 1;
                    }

                    // For ores: prefer Fortune as tiebreaker
                    if (TagBasedToolUtils.benefitsFromFortune(blockType)) {
                        int t1Fortune = t1.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                        int t2Fortune = t2.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

                        if (t1Fortune != t2Fortune) {
                            return Integer.compare(t2Fortune, t1Fortune);
                        }
                    }
                }
            }

            // PRIMARY LOGIC: User's enchantment priority from priority.yml
            // This ALWAYS takes precedence over smart logic
            for (Enchantment ench : enchList) {
                int lvl1 = t1.getEnchantmentLevel(ench);
                int lvl2 = t2.getEnchantmentLevel(ench);

                if (lvl1 != lvl2) {
                    return Integer.compare(lvl2, lvl1); // Higher level wins
                }
            }

            return 0;
        }

        /**
         * Compare material tiers (Netherite > Diamond > Iron > etc.)
         */
        private int compareMaterial(ItemStack t1, ItemStack t2) {
            if (!plugin.getConfigManager().isMaterialPriorityEnabled()) {
                return 0;
            }

            int tier1 = TagBasedToolUtils.getMaterialTier(t1.getType());
            int tier2 = TagBasedToolUtils.getMaterialTier(t2.getType());

            if (tier1 != tier2) {
                return Integer.compare(tier2, tier1); // Higher tier wins
            }
            return 0;
        }

        /**
         * Compare durability (HIGH = use high durability first, LOW = opposite)
         */
        private int compareDurability(ItemStack t1, ItemStack t2) {
            String durPriority = plugin.getConfigManager().getDurabilityPriority();

            if (durPriority.equalsIgnoreCase("NONE")) {
                return 0;
            }

            int d1 = TagBasedToolUtils.getRemainingDurability(t1);
            int d2 = TagBasedToolUtils.getRemainingDurability(t2);

            if (durPriority.equalsIgnoreCase("HIGH")) {
                return Integer.compare(d2, d1); // Higher durability wins
            } else if (durPriority.equalsIgnoreCase("LOW")) {
                return Integer.compare(d1, d2); // Lower durability wins
            }
            return 0;
        }
    }
}