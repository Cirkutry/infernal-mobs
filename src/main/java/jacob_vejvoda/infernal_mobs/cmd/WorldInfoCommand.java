package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldInfoCommand extends BaseCommand {

    public WorldInfoCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        List<String> enWorldList = plugin.getConfig().getStringList("mobWorlds");
        World world = player.getWorld();
        String enabled = "is not";
        if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
            enabled = "is";
        }
        sender.sendMessage(
                localeManager.getMessage(
                        "commands.worldinfo.current-world", world.getName(), enabled));
        sender.sendMessage(
                localeManager.getMessage(
                        "commands.worldinfo.enabled-worlds", enWorldList.toString()));
        return true;
    }

    @Override
    public String getName() {
        return "worldInfo";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.worldinfo.usage");
    }
}
