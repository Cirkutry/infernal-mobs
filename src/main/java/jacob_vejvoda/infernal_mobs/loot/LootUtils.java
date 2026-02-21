package jacob_vejvoda.infernal_mobs.loot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

/**
 * Utility methods for loot processing and manipulation
 */
public class LootUtils {

    /**
     * Parses integer values from string ranges (e.g., "1-5" or "3")
     */
    public static int getIntFromString(String range) {
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
     * Gets particle type from configuration
     */
    public static String getParticleFromConfig(String configPath, FileConfiguration config) {
        return config.getString(configPath + ".particle", "DRIP_LAVA");
    }

    /**
     * Applies potion effects to an entity based on configuration
     */
    public static PotionEffectType getPotionEffectType(String name) {
        PotionEffectType effectType = PotionEffectType.getByName(name);

        if (effectType == null) {
            effectType =
                    Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(name.toLowerCase()));
        }

        return effectType;
    }

    /**
     * Checks if item should be enchanted based on configuration
     */
    public static boolean isEnchantedFromConfig(String configPath, FileConfiguration config) {
        return config.getBoolean(configPath + ".enchanted", false);
    }

    /**
     * Translates color codes and hex codes in a string
     */
    public static String hex(String msg) {
        Matcher matcher = Pattern.compile("&?#([A-Fa-f0-9]{6})").matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String h = matcher.group(1);
            StringBuilder r = new StringBuilder("&x");
            for (char c : h.toCharArray()) r.append("&").append(c);
            matcher.appendReplacement(sb, r.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
