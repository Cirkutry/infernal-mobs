package jacob_vejvoda.infernal_mobs.command.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import jacob_vejvoda.infernal_mobs.config.LocaleManager;

public class InfoCommand extends BaseCommand {

	private static final ArrayList<Player> clickStream = new ArrayList<>();

	public InfoCommand(InfernalMobs plugin, LocaleManager localeManager) {
		super(plugin, localeManager);
	}

	public static ArrayList<Player> getClickStream() {
		return clickStream;
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(localeManager.getMessage("commands.player-only"));
			return true;
		}

		Player player = (Player) sender;
		clickStream.add(player);
		sender.sendMessage(localeManager.getMessage("commands.info.instructions"));
		return true;
	}

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getUsage() {
		return localeManager.getMessage("commands.info.usage");
	}
}
