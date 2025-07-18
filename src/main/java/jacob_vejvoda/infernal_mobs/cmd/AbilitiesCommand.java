package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.command.CommandSender;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import jacob_vejvoda.InfernalMobs.InfernalMobs;

import java.util.ArrayList;
import java.util.List;

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
