package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.command.CommandSender;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import jacob_vejvoda.InfernalMobs.InfernalMobs;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class KillCommand extends BaseCommand {
    
    private final LocaleManager localeManager;
    
    public KillCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("commands.player-only"));
            return true;
        }
    
        if (args.length != 2) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }
    
        Player player = (Player) sender;
        try {
            int size = Integer.parseInt(args[1]);
            for (Entity e : player.getNearbyEntities(size, size, size)) {
                int id = plugin.idSearch(e.getUniqueId());
                if (id != -1) {
                    try {
                        plugin.removeMob(id);
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to remove mob ID " + id, ex);
                        sender.sendMessage(localeManager.getMessage("commands.kill.failed"));
                        continue;
                    }
                    e.remove();
                    plugin.getLogger().log(Level.INFO, "Entity removed due to /kill");
                }
            }
            sender.sendMessage(localeManager.getMessage("commands.kill.success"));
        } catch (NumberFormatException e) {
            sender.sendMessage(localeManager.getMessage("commands.kill.invalid-radius"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();
        if (args.length == 2) {
            newTab.add("1");
        }
        return newTab;
    }
    
    @Override
    public String getName() {
        return "kill";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.kill.usage");
    }
}
