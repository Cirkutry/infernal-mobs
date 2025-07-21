package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ShowAbilitiesCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public ShowAbilitiesCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (plugin.getTarget(player) != null) {
            Entity targeted = plugin.getTarget(player);
            UUID mobId = targeted.getUniqueId();
            if (plugin.idSearch(mobId) != -1) {
                List<String> oldMobAbilityList = plugin.findMobAbilities(mobId);
                if (!targeted.isDead()) {
                    sender.sendMessage(
                            localeManager.getMessage(
                                    "commands.showabilities.list", oldMobAbilityList.toString()));
                }
            } else {
                sender.sendMessage(
                        localeManager.getMessage(
                                "commands.showabilities.not_infernal",
                                targeted.getType().getName()));
            }
        } else {
            sender.sendMessage(localeManager.getMessage("commands.showabilities.mob_not_found"));
        }
        return true;
    }

    @Override
    public String getName() {
        return "showAbilities";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.showabilities.usage");
    }
}
