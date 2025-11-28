package org.nguyendevs.simpleautotools.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashSet;
import java.util.Set;

/**
 * Modern Tag-based tool utilities for Minecraft 1.19+
 * Uses Bukkit's Tag API for better compatibility and maintainability
 */
public class TagBasedToolUtils {

    // Custom tag sets for materials not covered by vanilla tags
    private static final Set<Material> PICKAXE_MATERIALS = new HashSet<>();
    private static final Set<Material> AXE_MATERIALS = new HashSet<>();
    private static final Set<Material> SHOVEL_MATERIALS = new HashSet<>();
    private static final Set<Material> HOE_MATERIALS = new HashSet<>();

    static {
        initializeCustomTags();
    }

    private static void initializeCustomTags() {
        // Additional pickaxe materials
        PICKAXE_MATERIALS.add(Material.SPAWNER);
        PICKAXE_MATERIALS.add(Material.BEACON);
        PICKAXE_MATERIALS.add(Material.CONDUIT);
        PICKAXE_MATERIALS.add(Material.ENCHANTING_TABLE);
        PICKAXE_MATERIALS.add(Material.ENDER_CHEST);
        PICKAXE_MATERIALS.add(Material.RESPAWN_ANCHOR);
        PICKAXE_MATERIALS.add(Material.LODESTONE);
        PICKAXE_MATERIALS.add(Material.ANCIENT_DEBRIS);
        PICKAXE_MATERIALS.add(Material.CRYING_OBSIDIAN);
        PICKAXE_MATERIALS.add(Material.REINFORCED_DEEPSLATE);

        // Additional axe materials
        AXE_MATERIALS.add(Material.LADDER);
        AXE_MATERIALS.add(Material.CRAFTING_TABLE);
        AXE_MATERIALS.add(Material.BARREL);
        AXE_MATERIALS.add(Material.LOOM);
        AXE_MATERIALS.add(Material.COMPOSTER);
        AXE_MATERIALS.add(Material.LECTERN);
        AXE_MATERIALS.add(Material.NOTE_BLOCK);
        AXE_MATERIALS.add(Material.JUKEBOX);
        AXE_MATERIALS.add(Material.BEEHIVE);
        AXE_MATERIALS.add(Material.BEE_NEST);
        AXE_MATERIALS.add(Material.MELON);
        AXE_MATERIALS.add(Material.PUMPKIN);
        AXE_MATERIALS.add(Material.CARVED_PUMPKIN);
        AXE_MATERIALS.add(Material.JACK_O_LANTERN);

        // Additional shovel materials
        SHOVEL_MATERIALS.add(Material.GRASS_BLOCK);
        SHOVEL_MATERIALS.add(Material.PODZOL);
        SHOVEL_MATERIALS.add(Material.MYCELIUM);
        SHOVEL_MATERIALS.add(Material.FARMLAND);
        SHOVEL_MATERIALS.add(Material.DIRT_PATH);
        SHOVEL_MATERIALS.add(Material.ROOTED_DIRT);
        SHOVEL_MATERIALS.add(Material.SOUL_SAND);
        SHOVEL_MATERIALS.add(Material.SOUL_SOIL);
        SHOVEL_MATERIALS.add(Material.CLAY);

        // Additional hoe materials
        HOE_MATERIALS.add(Material.HAY_BLOCK);
        HOE_MATERIALS.add(Material.DRIED_KELP_BLOCK);
        HOE_MATERIALS.add(Material.TARGET);
        HOE_MATERIALS.add(Material.SPONGE);
        HOE_MATERIALS.add(Material.WET_SPONGE);
        HOE_MATERIALS.add(Material.SHROOMLIGHT);
        HOE_MATERIALS.add(Material.NETHER_WART_BLOCK);
        HOE_MATERIALS.add(Material.WARPED_WART_BLOCK);
    }

    /**
     * Check if a material is a tool of specific type
     */
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

    /**
     * Check if a material is a weapon (sword or axe)
     */
    public static boolean isWeapon(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE");
    }

    /**
     * Get material tier for comparison (higher is better)
     * Netherite(6) > Diamond(5) > Iron(4) > Golden(2) > Stone(1) > Wood(0)
     */
    public static int getMaterialTier(Material material) {
        String name = material.name();

        if (name.startsWith("NETHERITE_")) return 6;
        if (name.startsWith("DIAMOND_")) return 5;
        if (name.startsWith("IRON_")) return 4;
        if (name.startsWith("GOLDEN_") || name.startsWith("GOLD_")) return 2;
        if (name.startsWith("STONE_")) return 1;
        if (name.startsWith("WOODEN_") || name.startsWith("WOOD_")) return 0;
        if (material == Material.SHEARS) return 4; // Treat shears as iron tier

        return -1;
    }

    /**
     * Get remaining durability of a tool
     */
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
     * Check if a tool can harvest a specific block (Tag-based)
     * This replaces the old pattern matching system
     */
    public static boolean canHarvest(Material toolMaterial, Material blockMaterial) {
        int toolTier = getMaterialTier(toolMaterial);
        int requiredTier = getRequiredHarvestTier(blockMaterial);

        // If block doesn't require specific tier, any tool works
        if (requiredTier == -1) return true;

        return toolTier >= requiredTier;
    }

    /**
     * Get required tool tier to harvest a block using Tags
     */
    private static int getRequiredHarvestTier(Material blockMaterial) {
        // Diamond pickaxe required (tier 5)
        if (Tag.NEEDS_DIAMOND_TOOL.isTagged(blockMaterial)) {
            return 5;
        }

        // Iron pickaxe required (tier 4)
        if (Tag.NEEDS_IRON_TOOL.isTagged(blockMaterial)) {
            return 4;
        }

        // Stone pickaxe required (tier 1)
        if (Tag.NEEDS_STONE_TOOL.isTagged(blockMaterial)) {
            return 1;
        }

        // Special cases
        if (blockMaterial == Material.REINFORCED_DEEPSLATE) {
            return Integer.MAX_VALUE; // Cannot be mined
        }

        // No specific requirement
        return -1;
    }

    /**
     * Determine which tool type is best for a block using Tags
     * This is the core method that replaces tool-blocks.yml pattern matching
     */
    public static ToolType getRequiredToolType(Material blockMaterial) {
        // PICKAXE - Check vanilla tags first
        if (Tag.MINEABLE_PICKAXE.isTagged(blockMaterial)) {
            return ToolType.PICKAXE;
        }
        if (PICKAXE_MATERIALS.contains(blockMaterial)) {
            return ToolType.PICKAXE;
        }

        // AXE - Check vanilla tags
        if (Tag.MINEABLE_AXE.isTagged(blockMaterial)) {
            return ToolType.AXE;
        }
        if (AXE_MATERIALS.contains(blockMaterial)) {
            return ToolType.AXE;
        }

        // SHOVEL - Check vanilla tags
        if (Tag.MINEABLE_SHOVEL.isTagged(blockMaterial)) {
            return ToolType.SHOVEL;
        }
        if (SHOVEL_MATERIALS.contains(blockMaterial)) {
            return ToolType.SHOVEL;
        }

        // HOE - Check vanilla tags
        if (Tag.MINEABLE_HOE.isTagged(blockMaterial)) {
            return ToolType.HOE;
        }
        if (HOE_MATERIALS.contains(blockMaterial)) {
            return ToolType.HOE;
        }

        // SHEARS - Check specific tags
        if (Tag.LEAVES.isTagged(blockMaterial) ||
                Tag.WOOL.isTagged(blockMaterial) ||
                Tag.WOOL_CARPETS.isTagged(blockMaterial)) {
            return ToolType.SHEARS;
        }

        // Additional shears materials
        String name = blockMaterial.name();
        if (name.contains("VINE") ||
                blockMaterial == Material.COBWEB ||
                blockMaterial == Material.GLOW_LICHEN ||
                blockMaterial == Material.SEAGRASS ||
                blockMaterial == Material.TALL_SEAGRASS) {
            return ToolType.SHEARS;
        }

        return ToolType.NONE;
    }

    /**
     * Check if a block is better mined with Silk Touch
     * Useful for enchantment priority
     */
    public static boolean prefersSilkTouch(Material blockMaterial) {
        // Glass and ice
        if (blockMaterial.name().contains("GLASS") ||
                blockMaterial.name().contains("ICE")) {
            return true;
        }

        // Specific blocks
        return blockMaterial == Material.GRASS_BLOCK ||
                blockMaterial == Material.MYCELIUM ||
                blockMaterial == Material.PODZOL ||
                blockMaterial == Material.SEA_PICKLE ||
                blockMaterial == Material.GLOWSTONE ||
                blockMaterial == Material.MELON;
    }

    /**
     * Check if a block benefits from Fortune enchantment
     */
    public static boolean benefitsFromFortune(Material blockMaterial) {
        // Ores (excluding ancient debris and some special blocks)
        if (blockMaterial.name().contains("_ORE") &&
                blockMaterial != Material.ANCIENT_DEBRIS) {
            return true;
        }

        // Other fortune-affected blocks
        return blockMaterial == Material.GRAVEL ||
                blockMaterial == Material.MELON ||
                blockMaterial == Material.CLAY ||
                blockMaterial == Material.GLOWSTONE ||
                blockMaterial == Material.SEA_LANTERN ||
                Tag.LEAVES.isTagged(blockMaterial);
    }

    /**
     * Get the best tool material tier name for display
     */
    public static String getTierName(int tier) {
        switch (tier) {
            case 6: return "Netherite";
            case 5: return "Diamond";
            case 4: return "Iron";
            case 2: return "Golden";
            case 1: return "Stone";
            case 0: return "Wooden";
            default: return "Unknown";
        }
    }
}