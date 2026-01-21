package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class MobListCommand extends BaseCommand {

    public MobListCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(localeManager.getMessage("commands.moblist.header"));
        for (EntityType et : EntityType.values()) {
            if (et != null && et.getName() != null) {
                sender.sendMessage(
                        localeManager.getMessage("commands.moblist.mob-entry", et.getName()));
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "mobList";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.moblist.usage");
    }
}
