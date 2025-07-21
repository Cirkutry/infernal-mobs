package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import org.bukkit.command.CommandSender;

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
        sender.sendMessage(
                localeManager.getMessage(
                        "commands.help.header", plugin.getDescription().getVersion()));
        sender.sendMessage(localeManager.getMessage("commands.reload.usage"));
        sender.sendMessage(localeManager.getMessage("commands.worldinfo.usage"));
        sender.sendMessage(localeManager.getMessage("commands.error.usage"));
        sender.sendMessage(localeManager.getMessage("commands.getloot.usage"));
        sender.sendMessage(localeManager.getMessage("commands.setloot.usage"));
        sender.sendMessage(localeManager.getMessage("commands.giveloot.usage"));
        sender.sendMessage(localeManager.getMessage("commands.abilities.usage"));
        sender.sendMessage(localeManager.getMessage("commands.showabilities.usage"));
        sender.sendMessage(localeManager.getMessage("commands.setinfernal.usage"));
        sender.sendMessage(localeManager.getMessage("commands.spawn.usage"));
        sender.sendMessage(localeManager.getMessage("commands.cspawn.usage"));
        sender.sendMessage(localeManager.getMessage("commands.pspawn.usage"));
        sender.sendMessage(localeManager.getMessage("commands.kill.usage"));
        sender.sendMessage(localeManager.getMessage("commands.killall.usage"));
    }
}
