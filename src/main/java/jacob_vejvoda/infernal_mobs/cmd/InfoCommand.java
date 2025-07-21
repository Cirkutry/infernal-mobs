package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import org.bukkit.command.CommandSender;

public class InfoCommand extends BaseCommand {

    public InfoCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(
                localeManager.getMessage("commands.info.mounts", plugin.getMountList().size()));
        sender.sendMessage(localeManager.getMessage("commands.info.loops", plugin.getLoops()));
        sender.sendMessage(
                localeManager.getMessage(
                        "commands.info.infernals", plugin.getInfernalList().size()));
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
