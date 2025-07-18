package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.logging.Level;

public class LocaleManager {
    private final Plugin plugin;
    private FileConfiguration langConfig;
    private String locale;
    
    public LocaleManager(Plugin plugin) {
        this.plugin = plugin;
        this.locale = plugin.getConfig().getString("locale", "en_US");
        loadLanguageFile();
    }
    
    private void loadLanguageFile() {
        String fileName = locale + ".yml";
        File langFile = new File(plugin.getDataFolder() + File.separator + "lang", fileName);
        
        // Create lang directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }
        
        // Copy default language file if it doesn't exist
        if (!langFile.exists()) {
            try {
                plugin.saveResource("lang/" + fileName, false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Could not save language file: " + fileName);
                // Fall back to en_US if the requested locale doesn't exist
                if (!locale.equals("en_US")) {
                    this.locale = "en_US";
                    fileName = "en_US.yml";
                    langFile = new File(plugin.getDataFolder() + File.separator + "lang", fileName);
                    try {
                        plugin.saveResource("lang/" + fileName, false);
                    } catch (Exception e2) {
                        plugin.getLogger().log(Level.SEVERE, "Could not save default language file!");
                        return;
                    }
                }
            }
        }
        
        // Load the language file
        try {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
            
            // Load defaults from jar if available
            InputStream defConfigStream = plugin.getResource("lang/" + fileName);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
                langConfig.setDefaults(defConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load language file: " + fileName, e);
        }
    }
    
    /**
     * Get a localized message with optional placeholders
     * @param key The message key (e.g., "commands.spawn.success")
     * @param args Arguments to replace {0}, {1}, etc. placeholders
     * @return The formatted message with color codes translated
     */
    public String getMessage(String key, Object... args) {
        if (langConfig == null) {
            return "Missing language config: " + key;
        }
        
        String message = langConfig.getString(key);
        if (message == null) {
            plugin.getLogger().log(Level.WARNING, "Missing translation key: " + key);
        }
        
        // Replace {p} placeholder with prefix
        String prefix = langConfig.getString("prefix", "");
        if (prefix != null && !prefix.isEmpty()) {
            message = message.replace("{p}", prefix);
        }
        
        // Replace placeholders if arguments are provided
        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }
        
        // Translate color codes
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    /**
     * Reload the language configuration
     */
    public void reload() {
        this.locale = plugin.getConfig().getString("locale", "en_US");
        loadLanguageFile();
    }
    
    /**
     * Get the current locale
     */
    public String getLocale() {
        return locale;
    }
}
