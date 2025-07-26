package jacob_vejvoda.InfernalMobs.loot;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility methods for loot processing and manipulation
 */
public class LootUtils {
    private final InfernalMobs plugin;
    private final FileConfiguration lootFile;

    public LootUtils(InfernalMobs plugin, FileConfiguration lootFile) {
        this.plugin = plugin;
        this.lootFile = lootFile;
    }

    /**
     * Parses integer values from string ranges (e.g., "1-5" or "3")
     */
    public int getIntFromString(String range) {
        if (range == null || range.isEmpty()) {
            return 1;
        }

        if (range.contains("-")) {
            String[] parts = range.split("-");
            if (parts.length == 2) {
                try {
                    int min = Integer.parseInt(parts[0]);
                    int max = Integer.parseInt(parts[1]);
                    return min + (int) (Math.random() * ((max - min) + 1));
                } catch (NumberFormatException e) {
                    return 1;
                }
            }
        }

        try {
            return Integer.parseInt(range);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Processes loot names, handling special references to configuration lists
     */
    public String processLootName(String lootName, ItemStack stack) {
        if (lootName == null) {
            return null;
        }

        if (!lootName.startsWith("[")) {
            return lootName;
        }

        try {
            List<String> names = lootFile.getStringList(lootName);
            if (names != null && !names.isEmpty()) {
                String name = names.get(new Random().nextInt(names.size()));
                return name;
            }
        } catch (Exception e) {
            // Silently handle exception
        }

        return lootName;
    }

    /**
     * Converts raw pattern objects to Pattern list for banners
     */
    public List<Pattern> convertToPatterns(List<?> rawPatterns) {
        List<Pattern> patternList = new ArrayList<>();

        if (rawPatterns == null || rawPatterns.isEmpty()) {
            return patternList;
        }

        for (Object obj : rawPatterns) {
            if (obj instanceof Pattern) {
                patternList.add((Pattern) obj);
            } else if (obj instanceof Map) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    DyeColor color = DyeColor.valueOf(String.valueOf(map.get("color")));
                    PatternType type =
                            PatternType.getByIdentifier(String.valueOf(map.get("pattern")));
                    if (type == null) {
                        try {
                            type = PatternType.valueOf(String.valueOf(map.get("pattern")));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger()
                                    .warning("Unknown pattern type: " + map.get("pattern"));
                            continue;
                        }
                    }
                    patternList.add(new Pattern(color, type));
                } catch (Exception e) {
                    plugin.getLogger()
                            .warning(
                                    "Failed to parse banner pattern: "
                                            + obj
                                            + " - "
                                            + e.getMessage());
                }
            }
        }

        return patternList;
    }

    /**
     * Converts integer to Roman numeral representation
     */
    public String toRomanNumeral(int num) {
        if (num <= 0) {
            return String.valueOf(num);
        }

        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return hundreds[num / 100] + tens[(num % 100) / 10] + ones[num % 10];
    }

    /**
     * Sets item amount from loot configuration
     */
    public void setItem(ItemStack stack, String loot, FileConfiguration lootFile) {
        if (stack == null || loot == null || lootFile == null) {
            return;
        }

        if (lootFile.getString(loot + ".amount") != null) {
            String amountStr = lootFile.getString(loot + ".amount");
            int amount = getIntFromString(amountStr);
            stack.setAmount(amount);
        }
    }

    /**
     * Creates an ItemStack from configuration section
     */
    public ItemStack createItemFromConfig(String configPath, FileConfiguration config) {
        if (config == null || !config.isConfigurationSection(configPath)) {
            return null;
        }

        String materialStr = config.getString(configPath + ".material", "STICK");
        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger()
                    .warning("Invalid material '" + materialStr + "' in config at " + configPath);
            material = Material.STICK;
        }

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        String name = config.getString(configPath + ".name");
        if (name != null) {
            name = ConsumeEffectHandler.hex(name);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        List<String> loreConfig = config.getStringList(configPath + ".lore");
        if (loreConfig != null && !loreConfig.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : loreConfig) {
                line = ConsumeEffectHandler.hex(line);
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);
        }

        if (config.isInt(configPath + ".customModelData")) {
            int customModelData = config.getInt(configPath + ".customModelData");
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gets particle type from configuration
     */
    public String getParticleFromConfig(String configPath, FileConfiguration config) {
        return config.getString(configPath + ".particle", "DRIP_LAVA");
    }

    /**
     * Checks if item should be enchanted based on configuration
     */
    public boolean isEnchantedFromConfig(String configPath, FileConfiguration config) {
        return config.getBoolean(configPath + ".enchanted", false);
    }
}
