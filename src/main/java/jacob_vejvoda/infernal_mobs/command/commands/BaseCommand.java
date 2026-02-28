package jacob_vejvoda.infernal_mobs.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import jacob_vejvoda.infernal_mobs.config.LocaleManager;

public abstract class BaseCommand {
	protected final InfernalMobs plugin;
	protected final LocaleManager localeManager;

	public BaseCommand(InfernalMobs plugin, LocaleManager localeManager) {
		this.plugin = plugin;
		this.localeManager = localeManager;
	}

	public abstract boolean execute(CommandSender sender, String[] args);

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}

	public abstract String getName();

	public String getPermission() {
		return "infernalmobs.commands";
	}

	public abstract String getUsage();
}
