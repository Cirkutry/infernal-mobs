package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PSpawnCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public PSpawnCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }

        Player p = Bukkit.getServer().getPlayer(args[2]);
        if (p == null) {
            sender.sendMessage(localeManager.getMessage("commands.player.not_online", args[2]));
            return true;
        }

        ArrayList<String> abList = new ArrayList<>(Arrays.asList(args).subList(3, args.length));
        if (plugin.cSpawn(sender, args[1], p.getLocation(), abList)) {
            sender.sendMessage(
                    localeManager.getMessage("commands.pspawn.success", args[1], p.getName()));
            sender.sendMessage(abList.toString());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();

        if (args.length == 3) {
            if (args[args.length - 1].isEmpty()) {
                newTab.addAll(CommandUtils.getOnlinePlayerNames());
            } else {
                newTab.addAll(
                        CommandUtils.filterStartsWith(
                                CommandUtils.getOnlinePlayerNames(), args[args.length - 1]));
            }
        } else if (args.length > 3) {
            if (args[args.length - 1].isEmpty()) {
                newTab.addAll(CommandUtils.ALL_ABILITIES);
            } else {
                newTab.addAll(
                        CommandUtils.filterStartsWith(
                                CommandUtils.ALL_ABILITIES, args[args.length - 1]));
            }
        }
        return newTab;
    }

    @Override
    public String getName() {
        return "pspawn";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.pspawn.usage");
    }
}
