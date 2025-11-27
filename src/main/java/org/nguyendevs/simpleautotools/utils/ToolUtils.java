package org.nguyendevs.simpleautotools.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
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

    /**
     * Tiers:
     * 6 = Netherite
     * 5 = Diamond
     * 4 = Iron
     * 3 = Copper (nếu plugin thêm)
     * 2 = Gold
     * 1 = Stone
     * 0 = Wood
     */
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
     * Check xem tool có thể harvest block không bằng tags.
     */
    public static boolean canHarvest(Material toolMaterial, Material blockMaterial) {
        int toolTier = getMaterialTier(toolMaterial);

        // Nếu tool không có tier => không thể harvest
        if (toolTier < 0) return false;

        // 1. Block yêu cầu Diamond hoặc Netherite
        if (Tag.NEEDS_DIAMOND_TOOL.isTagged(blockMaterial)) {
            return toolTier >= 5;
        }

        // 2. Block yêu cầu Iron trở lên
        if (Tag.NEEDS_IRON_TOOL.isTagged(blockMaterial)) {
            return toolTier >= 4;
        }

        // 3. Block yêu cầu Stone trở lên
        if (Tag.NEEDS_STONE_TOOL.isTagged(blockMaterial)) {
            return toolTier >= 1;
        }

        // 4. Nếu block thuộc nhóm mineable, tức là có thể harvest với tool đúng loại
        if (isPickaxeMineable(blockMaterial) ||
                isAxeMineable(blockMaterial) ||
                isShovelMineable(blockMaterial) ||
                isHoeMineable(blockMaterial)) {

            // Không có yêu cầu tool tier => tool nào cũng harvest được
            return true;
        }

        // 5. Nếu block không thuộc mineable tags => không harvest được
        return false;
    }

    private static boolean isPickaxeMineable(Material block) {
        return Tag.MINEABLE_PICKAXE.isTagged(block);
    }

    private static boolean isAxeMineable(Material block) {
        return Tag.MINEABLE_AXE.isTagged(block);
    }

    private static boolean isShovelMineable(Material block) {
        return Tag.MINEABLE_SHOVEL.isTagged(block);
    }

    private static boolean isHoeMineable(Material block) {
        return Tag.MINEABLE_HOE.isTagged(block);
    }
}
