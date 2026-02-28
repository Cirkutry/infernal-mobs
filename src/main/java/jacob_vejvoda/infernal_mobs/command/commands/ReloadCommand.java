package jacob_vejvoda.infernal_mobs.command.commands;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import jacob_vejvoda.infernal_mobs.config.LocaleManager;
import jacob_vejvoda.infernal_mobs.loot.ConsumeEffectHandler;
import jacob_vejvoda.infernal_mobs.loot.LootManager;
import jacob_vejvoda.infernal_mobs.loot.PotionEffectHandler;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand(InfernalMobs plugin, LocaleManager localeManager) {
		super(plugin, localeManager);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		try {
			plugin.reloadConfig();
			plugin.getLogger().info("config.yml reloaded successfully.");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to reload config.yml!", e);
			sender.sendMessage(localeManager.getMessage("commands.reload.failed"));
			return true;
		}

		try {
			File lootYML = plugin.getLootYML();
			if (!lootYML.exists()) {
				plugin.saveResource("loot.yml", false);
			}

			YamlConfiguration lootFile = new YamlConfiguration();
			lootFile.load(lootYML);
			plugin.setLootFile(lootFile);

			LootManager lootManager = new LootManager(plugin, lootFile);
			plugin.setLootManager(lootManager);

			ConsumeEffectHandler consumeEffectHandler = new ConsumeEffectHandler(plugin);
			plugin.setConsumeEffectHandler(consumeEffectHandler);

			PotionEffectHandler potionEffectHandler = new PotionEffectHandler(plugin);
			plugin.setPotionEffectHandler(potionEffectHandler);

			plugin.getLogger().info("loot.yml reloaded successfully.");

			if (plugin.getDiviningStaffManager() != null) {
				plugin.getDiviningStaffManager().removeRecipe();
				plugin.getDiviningStaffManager().addRecipe();
			}
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to reload loot.yml!", e);
			sender.sendMessage(localeManager.getMessage("commands.reload.failed"));
			return true;
		}

		try {
			File saveYML = plugin.getSaveYML();
			YamlConfiguration saveFile = new YamlConfiguration();
			saveFile.load(saveYML);
			plugin.setSaveFile(saveFile);

			plugin.getLogger().info("save.yml reloaded successfully.");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to reload save.yml!", e);
			sender.sendMessage(localeManager.getMessage("commands.reload.failed"));
			return true;
		}

		try {
			localeManager.reload();
			plugin.getLogger().info("Language configuration reloaded successfully.");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to reload language file!", e);
			sender.sendMessage(localeManager.getMessage("commands.reload.failed"));
			return true;
		}

		sender.sendMessage(localeManager.getMessage("commands.reload.success"));
		return true;
	}

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getUsage() {
		return localeManager.getMessage("commands.reload.usage");
	}
}
