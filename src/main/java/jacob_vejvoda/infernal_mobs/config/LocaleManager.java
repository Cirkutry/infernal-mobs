package jacob_vejvoda.infernal_mobs.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class LocaleManager {
	private final Plugin plugin;
	private final FileManager fileManager;
	private FileConfiguration langConfig;
	private String locale;

	public LocaleManager(Plugin plugin, FileManager fileManager) throws Exception {
		this.plugin = plugin;
		this.fileManager = fileManager;
		this.locale = plugin.getConfig().getString("locale", "en_US");
		loadLanguageFile();
	}

	private void loadLanguageFile() throws Exception {
		String fileName = locale + ".yml";
		File langFile = fileManager.loadLanguageFile(locale);
		String actualFileName = langFile.getName();
		if (!actualFileName.equals(fileName)) {
			this.locale = actualFileName.replace(".yml", "");
		}

		YamlConfiguration newLangConfig = new YamlConfiguration();
		newLangConfig.load(langFile);

		InputStream defConfigStream = plugin.getResource("lang/" + fileName);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
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
