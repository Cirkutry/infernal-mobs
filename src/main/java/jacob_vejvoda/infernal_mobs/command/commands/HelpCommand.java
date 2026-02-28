package jacob_vejvoda.infernal_mobs.command.commands;

import org.bukkit.command.CommandSender;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import jacob_vejvoda.infernal_mobs.config.LocaleManager;

public class HelpCommand extends BaseCommand {

	public HelpCommand(InfernalMobs plugin, LocaleManager localeManager) {
		super(plugin, localeManager);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		throwError(sender);
		return true;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getUsage() {
		return localeManager.getMessage("commands.help.usage");
	}

	private void throwError(CommandSender sender) {
		sender.sendMessage(localeManager.getMessage("commands.help.header", plugin.getPluginMeta().getVersion()));
		sender.sendMessage(localeManager.getMessage("commands.reload.usage"));
		sender.sendMessage(localeManager.getMessage("commands.info.usage"));
		sender.sendMessage(localeManager.getMessage("commands.giveloot.usage"));
		sender.sendMessage(localeManager.getMessage("commands.setloot.usage"));
		sender.sendMessage(localeManager.getMessage("commands.setspawner.usage"));
		sender.sendMessage(localeManager.getMessage("commands.spawn.usage"));
	}
}
