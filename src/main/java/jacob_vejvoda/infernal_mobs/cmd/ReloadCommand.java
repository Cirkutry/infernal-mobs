package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        plugin.refreshLoot();
        plugin.reloadMobSave();

        // Also reload the locale manager
        localeManager.reload();

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
