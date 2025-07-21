package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class KillAllCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public KillAllCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        World w = null;
        if (args.length == 1 && sender instanceof Player) {
            w = ((Player) sender).getWorld();
        } else if (args.length == 2) {
            w = Bukkit.getServer().getWorld(args[1]);
        }

        if (w != null) {
            for (Entity e : w.getEntities()) {
                int id = plugin.idSearch(e.getUniqueId());
                if (id != -1) {
                    try {
                        plugin.removeMob(id);
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to remove mob ID " + id, ex);
                        sender.sendMessage(localeManager.getMessage("commands.killall.failed"));
                        continue;
                    }

                    if (e instanceof LivingEntity) {
                        ((LivingEntity) e).setCustomName(null);
                    }

                    plugin.getLogger().log(Level.INFO, "Entity removed due to /killall");
                    e.remove();
                }
            }
            sender.sendMessage(localeManager.getMessage("commands.killall.success"));
        } else {
            sender.sendMessage(localeManager.getMessage("commands.world.not_found"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();
        if (args.length == 2) {
            if (args[args.length - 1].isEmpty()) {
                newTab.addAll(CommandUtils.getWorldNames());
            } else {
                newTab.addAll(
                        CommandUtils.filterStartsWith(
                                CommandUtils.getWorldNames(), args[args.length - 1]));
            }
        }
        return newTab;
    }

    @Override
    public String getName() {
        return "killall";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.killall.usage");
    }
}
