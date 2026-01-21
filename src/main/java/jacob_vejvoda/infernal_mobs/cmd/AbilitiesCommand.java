package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.command.CommandSender;

public class AbilitiesCommand extends BaseCommand {

    public AbilitiesCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(localeManager.getMessage("commands.abilities.list"));
        return true;
    }

    @Override
    public String getName() {
        return "abilities";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.abilities.usage");
    }
}
