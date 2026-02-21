package jacob_vejvoda.infernal_mobs.loot;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles enchantment application to loot items
 */
public class EnchantmentHandler {
    private final InfernalMobs plugin;
    private final FileConfiguration lootFile;
    private final LootUtils lootUtils;

    public EnchantmentHandler(InfernalMobs plugin, FileConfiguration lootFile) {
        this.plugin = plugin;
        this.lootFile = lootFile;
        this.lootUtils = new LootUtils(plugin, lootFile);
    }

    /**
     * Applies enchantments to an item based on loot configuration
     */
    public void applyEnchantments(ItemStack stack, int loot) {
        if (lootFile.isConfigurationSection("loot." + loot + ".enchantments")) {
            ConfigurationSection enchantmentsSection =
                    lootFile.getConfigurationSection("loot." + loot + ".enchantments");
            if (enchantmentsSection != null) {
                for (String key : enchantmentsSection.getKeys(false)) {
                    applySingleEnchantment(stack, loot, key);
                }
            }
        }
    }

    /**
     * Applies a single enchantment to an item
     */
    private void applySingleEnchantment(ItemStack stack, int loot, String key) {
        String enchantmentName =
                lootFile.getString("loot." + loot + ".enchantments." + key + ".enchantment");
        if (enchantmentName != null) {
            try {
                Enchantment enchant =
                        Enchantment.getByKey(
                                NamespacedKey.minecraft(enchantmentName.toLowerCase()));
                if (enchant != null) {
                    int level = getEnchantmentLevel(loot, key);
                    int chance = getEnchantmentChance(loot, key);

                    if (chance >= 100 || (new Random().nextInt(100) < chance)) {
                        applyEnchantmentToItem(stack, enchant, level);
                    }
                } else {
                    plugin.getLogger().warning("Unknown enchantment: " + enchantmentName);
                }
            } catch (Exception e) {
                plugin.getLogger()
                        .warning(
                                "Error applying enchantment "
                                        + enchantmentName
                                        + ": "
                                        + e.getMessage());
            }
        }
    }

    /**
     * Gets the enchantment level from configuration
     */
    private int getEnchantmentLevel(int loot, String key) {
        int level = 1;
        String levelStr = lootFile.getString("loot." + loot + ".enchantments." + key + ".level");
        if (levelStr != null) {
            level = lootUtils.getIntFromString(levelStr);
        }
        return level;
    }

    /**
     * Gets the enchantment chance from configuration
     */
    private int getEnchantmentChance(int loot, String key) {
        int chance = 100;
        if (lootFile.contains("loot." + loot + ".enchantments." + key + ".chance")) {
            chance = lootFile.getInt("loot." + loot + ".enchantments." + key + ".chance");
        }
        return chance;
    }

    /**
     * Applies the enchantment to the item with proper formatting
     */
    private void applyEnchantmentToItem(ItemStack stack, Enchantment enchant, int level) {
        try {
            int maxAllowedLevel = getMaxAllowedEnchantmentLevel(enchant);

            if (level > maxAllowedLevel) {
                level = maxAllowedLevel;
            }

            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.addEnchant(enchant, 1, true);

                addEnchantmentToLore(meta, enchant, level);

                if (level > enchant.getMaxLevel()) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                stack.setItemMeta(meta);

                stack.addUnsafeEnchantment(enchant, level);
            } else {
                stack.addUnsafeEnchantment(enchant, level);
            }
        } catch (Exception e) {
            stack.addUnsafeEnchantment(enchant, level);
            plugin.getLogger()
                    .warning("Couldn't apply custom enchantment format: " + e.getMessage());
        }
    }

    /**
     * Adds enchantment information to item lore
     */
    private void addEnchantmentToLore(ItemMeta meta, Enchantment enchant, int level) {
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        String enchantmentDisplayName = enchant.getKey().getKey();
        enchantmentDisplayName =
                enchantmentDisplayName.substring(0, 1).toUpperCase()
                        + enchantmentDisplayName.substring(1).replace('_', ' ');

        String formattedEnchant =
                ChatColor.GRAY + enchantmentDisplayName + " " + lootUtils.toRomanNumeral(level);

        lore.add(formattedEnchant);
        meta.setLore(lore);
    }

    /**
     * Gets the maximum allowed level for an enchantment
     */
    public static int getMaxAllowedEnchantmentLevel(Enchantment enchant) {
        String enchantKey = enchant.getKey().getKey().toLowerCase();
        if (enchantKey.equals("mending")
                || enchantKey.equals("silk_touch")
                || enchantKey.equals("infinity")
                || enchantKey.equals("channeling")
                || enchantKey.equals("aqua_affinity")) {
            return 1;
        }
        return 255;
    }
}
