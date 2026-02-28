package jacob_vejvoda.infernal_mobs.config;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class FileManager {
	private final Plugin plugin;
	private File lootYML;
	private File saveYML;
	private FileConfiguration lootConfig;
	private FileConfiguration saveConfig;

	public FileManager(Plugin plugin) {
		this.plugin = plugin;
		this.lootYML = new File(plugin.getDataFolder(), "loot.yml");
		this.saveYML = new File(plugin.getDataFolder(), "save.yml");
	}

	public void initializeFiles() throws Exception {
		File dir = new File(plugin.getDataFolder().getParentFile().getPath(), plugin.getName());
		if (!dir.exists()) {
			dir.mkdir();
		}
		loadConfigYML();
		loadLootYML();
		loadSaveYML();
	}

	public void loadConfigYML() throws Exception {
		try {
			plugin.saveDefaultConfig();
			plugin.getLogger().info("config.yml loaded successfully.");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to load config.yml!", e);
			throw e;
		}
	}

	public void loadLootYML() throws Exception {
		try {
			if (!lootYML.exists()) {
				plugin.saveResource("loot.yml", false);
			}

			lootConfig = new YamlConfiguration();
			lootConfig.load(lootYML);
			plugin.getLogger().info("loot.yml loaded successfully.");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to load loot.yml!", e);
			throw e;
		}
	}

	public void loadSaveYML() throws Exception {
		try {
			if (!saveYML.exists()) {
				saveYML.createNewFile();
			}

			saveConfig = new YamlConfiguration();
			saveConfig.load(saveYML);
			plugin.getLogger().info("save.yml loaded successfully.");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to load save.yml!", e);
			throw e;
		}
	}

	public File loadLanguageFile(String locale) throws Exception {
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

				// Fall back to en_US if the requested locale isn't available
				if (!locale.equals("en_US")) {
					fileName = "en_US.yml";
					langFile = new File(plugin.getDataFolder() + File.separator + "lang", fileName);
					try {
						plugin.saveResource("lang/" + fileName, false);
					} catch (Exception e2) {
						plugin.getLogger().log(Level.SEVERE, "Could not save default language file!");
						throw e2;
					}
				} else {
					throw e;
				}
			}
		}

		return langFile;
	}

	public void reloadAll() throws Exception {
		plugin.reloadConfig();
		loadLootYML();
		loadSaveYML();
	}

	public File getLootYML() {
		return lootYML;
	}

	public File getSaveYML() {
		return saveYML;
	}

	public FileConfiguration getLootConfig() {
		return lootConfig;
	}

	public FileConfiguration getSaveConfig() {
		return saveConfig;
	}

	public void setLootConfig(FileConfiguration lootConfig) {
		this.lootConfig = lootConfig;
	}

	public void setSaveConfig(FileConfiguration saveConfig) {
		this.saveConfig = saveConfig;
	}
}
