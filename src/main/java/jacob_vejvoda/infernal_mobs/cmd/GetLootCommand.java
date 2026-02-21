package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetLootCommand extends BaseCommand {

    public GetLootCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            int min = plugin.getConfig().getInt("minPowers");
            int max = plugin.getConfig().getInt("maxPowers");
            int powers = plugin.rand(min, max);
            ItemStack gottenLoot = plugin.getRandomLoot(player, plugin.getRandomMob(), powers);
            if (gottenLoot != null) {
                player.getInventory().addItem(gottenLoot);
            }
            sender.sendMessage("§eGave you some random loot!");
        } else if (args.length == 2) {
            try {
                int index = Integer.parseInt(args[1]);
                ItemStack i = plugin.getLoot(player, index);
                if (i != null) {
                    player.getInventory().addItem(i);
                    sender.sendMessage("§eGave you the loot at index §9" + index);
                } else {
                    sender.sendMessage(
                            "§eExecuted commands for loot at index §9"
                                    + index
                                    + " §e(no item to give)");
                }
                return true;
            } catch (Exception ignored) {
            }
            sender.sendMessage("§cUnable to get that loot!");
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
        return "getloot";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.getloot.usage");
    }
}
