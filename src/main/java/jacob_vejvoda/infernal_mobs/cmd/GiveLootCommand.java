package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import jacob_vejvoda.InfernalMobs.InfernalMobs;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;

import java.util.ArrayList;
import java.util.List;

public class GiveLootCommand extends BaseCommand {
    
    private final LocaleManager localeManager;
    
    public GiveLootCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }
        
        try {
            Player p = Bukkit.getServer().getPlayer(args[1]);
            if (p != null) {
                int index = Integer.parseInt(args[2]);
                ItemStack i = plugin.getLoot(p, index);
                if (i != null) {
                    p.getInventory().addItem(i);
                    sender.sendMessage(localeManager.getMessage("commands.giveloot.success", String.valueOf(index)));
                } else {
                    sender.sendMessage("§eExecuted commands for loot at index §9" + index + " §efor player §b" + p.getName() + " §e(no item to give)");
                }
                return true;
            } else {
                sender.sendMessage(localeManager.getMessage("commands.player.not_found"));
                return true;
            }
        } catch (Exception ignored) {
        }
        sender.sendMessage(localeManager.getMessage("commands.giveloot.error"));
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();
        if (args.length == 2) {
            newTab.addAll(CommandUtils.getOnlinePlayerNames());
        }
        if (args.length == 3) {
            newTab.add("1");
        }
        return newTab;
    }
    
    @Override
    public String getName() {
        return "giveloot";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.giveloot.usage");
    }
}
