package jacob_vejvoda.infernal_mobs.cmd;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class LocaleManager {
    private final Plugin plugin;
    private FileConfiguration langConfig;
    private String locale;

    public LocaleManager(Plugin plugin) throws Exception {
        this.plugin = plugin;
        this.locale = plugin.getConfig().getString("locale", "en_US");
        loadLanguageFile();
    }

    private void loadLanguageFile() throws Exception {
        String fileName = locale + ".yml";
        File langFile = new File(plugin.getDataFolder() + File.separator + "lang", fileName);

        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        if (!langFile.exists()) {
            try {
                plugin.saveResource("lang/" + fileName, false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Could not save language file: " + fileName);

                if (!locale.equals("en_US")) {
                    this.locale = "en_US";
                    fileName = "en_US.yml";
                    langFile = new File(plugin.getDataFolder() + File.separator + "lang", fileName);
                    try {
                        plugin.saveResource("lang/" + fileName, false);
                    } catch (Exception e2) {
                        plugin.getLogger()
                                .log(Level.SEVERE, "Could not save default language file!");
                        throw e2;
                    }
                }
            }
        }

        YamlConfiguration newLangConfig = new YamlConfiguration();
        newLangConfig.load(langFile);

        InputStream defConfigStream = plugin.getResource("lang/" + fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig =
                    YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            newLangConfig.setDefaults(defConfig);
        }

        this.langConfig = newLangConfig;
    }

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

        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }

        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    public void reload() throws Exception {
        this.locale = plugin.getConfig().getString("locale", "en_US");
        loadLanguageFile();
    }

    public String getLocale() {
        return locale;
    }
}
