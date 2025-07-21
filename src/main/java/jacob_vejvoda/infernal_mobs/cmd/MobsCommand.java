package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class MobsCommand extends BaseCommand {

    public MobsCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(localeManager.getMessage("commands.mobs.header"));
        for (EntityType e : EntityType.values()) {
            if (e != null) {
                sender.sendMessage(e.toString());
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "mobs";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.mobs.usage");
    }
}
