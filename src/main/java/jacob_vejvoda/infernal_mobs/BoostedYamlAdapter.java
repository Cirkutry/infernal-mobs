package jacob_vejvoda.infernal_mobs;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoostedYamlAdapter {
    private static final Logger logger = Logger.getLogger(BoostedYamlAdapter.class.getName());
    
    public static Map<String, Object> getConfigSection(YamlDocument document, String path) {
        if (document.contains(path)) {
            if (document.isSection(path)) {
                Map<String, Object> result = new HashMap<>();
                Map<?, Object> rawValues = document.getSection(path).getRouteMappedValues(false);
                for (Map.Entry<?, Object> entry : rawValues.entrySet()) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
                return result;
            }
        }
        return null;
    }
    
    public static Set<String> getKeys(YamlDocument document, String path) {
        if (document.contains(path)) {
            if (document.isSection(path)) {
                return document.getSection(path).getRoutesAsStrings(false);
            }
        }
        return Collections.emptySet();
    }
    
    public static List<Integer> getIntegerList(YamlDocument document, String path) {
        if (document.contains(path)) {
            return document.getIntList(path);
        }
        return new ArrayList<>();
    }
    
    public static YamlConfiguration convertToFileConfiguration(YamlDocument document) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            File tempFile = File.createTempFile("temp_config", ".yml");
            document.save(tempFile);
            config.load(tempFile);
            tempFile.delete();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error converting YamlDocument to FileConfiguration", e);
        }
        return config;
    }
    
    public static void updateFromFileConfiguration(YamlDocument document, ConfigurationSection configuration) {
        for (String key : configuration.getKeys(false)) {
            if (configuration.isConfigurationSection(key)) {
                if (document.contains(key) && document.isSection(key)) {
                    updateFromFileConfiguration(document, configuration.getConfigurationSection(key));
                } else {
                    document.createSection(key);
                    updateFromFileConfiguration(document, configuration.getConfigurationSection(key));
                }
            } else {
                document.set(key, configuration.get(key));
            }
        }
        try {
            document.save();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving document after update", e);
        }
    }
    

    public static ConfigurationSection getConfigurationSection(YamlDocument document, String path) {
        if (document.contains(path) && document.isSection(path)) {
            YamlConfiguration config = new YamlConfiguration();
            Map<String, Object> values = getConfigSection(document, path);
            if (values != null) {
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    config.set(entry.getKey(), entry.getValue());
                }
                return config.getConfigurationSection("");
            }
        }
        return null;
    }
} 