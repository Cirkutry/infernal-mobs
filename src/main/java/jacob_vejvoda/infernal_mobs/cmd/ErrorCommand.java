package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.command.CommandSender;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import org.bukkit.entity.Player;
import jacob_vejvoda.InfernalMobs.InfernalMobs;

import java.util.ArrayList;
import java.util.List;

public class ErrorCommand extends BaseCommand {
    
    public ErrorCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("commands.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getErrorList().add(player);
        sender.sendMessage(localeManager.getMessage("commands.error.instructions"));
        return true;
    }
    
    @Override
    public String getName() {
        return "error";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.error.usage");
    }
}
