package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class BaseCommand {
    protected final InfernalMobs plugin;
    protected final LocaleManager localeManager;

    public BaseCommand(InfernalMobs plugin, LocaleManager localeManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Override this method only if your command needs tab completion
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return List of tab completions
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    public abstract String getName();

    public String getPermission() {
        return "infernalmobs.commands";
    }

    public abstract String getUsage();
}
