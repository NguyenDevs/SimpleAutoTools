package org.nguyendevs.simpleautotools.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ToolUtils {

    public static boolean isToolType(Material material, ToolType toolType) {
        String name = material.name();

        switch (toolType) {
            case PICKAXE:
                return name.endsWith("_PICKAXE");
            case AXE:
                return name.endsWith("_AXE");
            case SHOVEL:
                return name.endsWith("_SHOVEL");
            case HOE:
                return name.endsWith("_HOE");
            case SWORD:
                return name.endsWith("_SWORD");
            case SHEARS:
                return material == Material.SHEARS;
            default:
                return false;
        }
    }

    public static boolean isWeapon(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }

    public static int getMaterialTier(Material material) {
        String name = material.name();

        if (name.startsWith("NETHERITE_")) return 6;
        if (name.startsWith("DIAMOND_")) return 5;
        if (name.startsWith("IRON_")) return 4;
        if (name.startsWith("COPPER_")) return 3;
        if (name.startsWith("GOLDEN_") || name.startsWith("GOLD_")) return 2;
        if (name.startsWith("STONE_")) return 1;
        if (name.startsWith("WOODEN_") || name.startsWith("WOOD_")) return 0;
        if (material == Material.SHEARS) return 4;

        return -1;
    }

    public static int getRemainingDurability(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) item.getItemMeta();
            int maxDurability = item.getType().getMaxDurability();
            int damage = damageable.getDamage();
            return maxDurability - damage;
        }
        return 0;
    }

    /**
     * Check if a tool can harvest a specific block
     * For example: wooden pickaxe can't mine diamond ore
     */
    public static boolean canHarvest(Material toolMaterial, Material blockMaterial) {
        int toolTier = getMaterialTier(toolMaterial);
        int requiredTier = getRequiredHarvestTier(blockMaterial);

        return toolTier >= requiredTier;
    }

    /**
     * Get the minimum tool tier required to harvest a block
     * Returns -1 if no specific tier is required
     */
    private static int getRequiredHarvestTier(Material blockMaterial) {
        String blockName = blockMaterial.name();

        // Netherite/Ancient Debris - requires diamond (tier 5)
        if (blockName.equals("ANCIENT_DEBRIS") || blockName.equals("CRYING_OBSIDIAN")) {
            return 5;
        }

        // Obsidian - requires diamond (tier 5)
        if (blockName.contains("OBSIDIAN")) {
            return 5;
        }

        if (blockName.contains("DIAMOND") || blockName.contains("EMERALD") ||
                blockName.contains("GOLD_ORE") || blockName.contains("REDSTONE")) {
            return 4;
        }

        if (blockName.contains("LAPIS") || blockName.contains("IRON_ORE")) {
            return 1;
        }

        if (blockName.contains("_ORE")) {
            return 1;
        }

        if (blockName.contains("ANVIL") || blockName.equals("SPAWNER") ||
                blockName.equals("BEACON") || blockName.equals("ENCHANTING_TABLE") ||
                blockName.equals("ENDER_CHEST") || blockName.equals("REINFORCED_DEEPSLATE")) {
            return 4;
        }

        if (blockName.contains("STONE") || blockName.contains("COBBLE") ||
                blockName.contains("BRICK") || blockName.contains("CONCRETE")) {
            return 0;
        }

        return -1;
    }
}