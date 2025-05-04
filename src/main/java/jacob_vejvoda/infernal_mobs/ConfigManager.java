package jacob_vejvoda.infernal_mobs;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final infernal_mobs plugin;
    
    public ConfigManager(infernal_mobs plugin) {
        this.plugin = plugin;
    }
    
    public String getString(String path) {
        return plugin.getBoostedConfig().getString(path);
    }
    
    public String getString(String path, String defaultValue) {
        return plugin.getBoostedConfig().getString(path, defaultValue);
    }
    
    public boolean getBoolean(String path) {
        return plugin.getBoostedConfig().getBoolean(path);
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        return plugin.getBoostedConfig().getBoolean(path, defaultValue);
    }
    
    public int getInt(String path) {
        return plugin.getBoostedConfig().getInt(path);
    }
    
    public int getInt(String path, int defaultValue) {
        return plugin.getBoostedConfig().getInt(path, defaultValue);
    }
    
    public List<String> getStringList(String path) {
        return plugin.getBoostedConfig().getStringList(path);
    }
    
    public List<?> getList(String path) {
        return plugin.getBoostedConfig().getList(path, new ArrayList<>());
    }
    
    public List<?> getList(String path, List<?> defaultValue) {
        return plugin.getBoostedConfig().getList(path, defaultValue);
    }
    
    public List<Integer> getIntList(String path) {
        return plugin.getBoostedConfig().getIntList(path);
    }
    
    public boolean contains(String path) {
        return plugin.getBoostedConfig().contains(path);
    }
} 