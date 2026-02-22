package jacob_vejvoda.infernal_mobs.commands;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnerCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public SetSpawnerCommand(InfernalMobs plugin, LocaleManager localeManager) {
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
        if (player.getTargetBlock(null, 25).getType().equals(Material.SPAWNER)) {
            int delaySeconds = Integer.parseInt(args[1]);
            int delayTicks = delaySeconds * 20;

            String name = plugin.getLocationName(player.getTargetBlock(null, 25).getLocation());

            plugin.getSaveFile().set("infernalSpawners." + name, delayTicks);
            try {
                plugin.getSaveFile().save(plugin.getSaveYML());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save spawner data!", e);
            }
            sender.sendMessage(
                    localeManager.getMessage(
                            "commands.setspawner.success", String.valueOf(delaySeconds)));
        } else {
            sender.sendMessage(localeManager.getMessage("commands.setspawner.not_spawner"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();
        if (args.length == 2) {
            newTab.add("<cooldown>");
        }
        return newTab;
    }

    @Override
    public String getName() {
        return "setSpawner";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.setspawner.usage");
    }
}
