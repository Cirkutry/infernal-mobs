package jacob_vejvoda.InfernalMobs.cmd;

import jacob_vejvoda.InfernalMobs.InfernalMob;
import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnCommand extends BaseCommand {

    public SpawnCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("commands.player-only"));
            return true;
        }

        Player player = (Player) sender;

        try {
            if (EntityType.valueOf(args[1].toUpperCase()) != null) {
                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                Entity ent =
                        player.getWorld()
                                .spawnEntity(
                                        farSpawnLoc, EntityType.valueOf(args[1].toUpperCase()));

                List<String> abList;
                if (args.length == 2) {
                    // No specific abilities, get random ones
                    abList = plugin.getAbilitiesAmount(ent);
                } else {
                    // Specific abilities provided
                    ArrayList<String> specificAbList = new ArrayList<>();
                    for (int i = 2; i < args.length; i++) {
                        if (plugin.getConfig().getString(args[i]) != null) {
                            specificAbList.add(args[i]);
                        } else {
                            sender.sendMessage(
                                    localeManager.getMessage(
                                            "commands.spawn.invalid-ability", args[i]));
                            ent.remove();
                            return true;
                        }
                    }
                    abList = specificAbList;
                }

                InfernalMob newMob;
                UUID id = ent.getUniqueId();
                if (abList.contains("1up")) {
                    newMob = new InfernalMob(ent, id, true, abList, 2, plugin.getEffect());
                } else {
                    newMob = new InfernalMob(ent, id, true, abList, 1, plugin.getEffect());
                }

                if (abList.contains("flying")) {
                    plugin.makeFly(ent);
                }
                plugin.getInfernalList().add(newMob);
                plugin.getGui().setName(ent);
                plugin.giveMobGear(ent, false);
                plugin.addHealth(ent, abList);

                if (args.length > 2) {
                    sender.sendMessage(
                            localeManager.getMessage(
                                    "commands.spawn.success-with-abilities", args[1]));
                    sender.sendMessage(abList.toString());
                } else {
                    sender.sendMessage(
                            localeManager.getMessage("commands.spawn.success-simple", args[1]));
                }
            } else {
                sender.sendMessage(localeManager.getMessage("commands.spawn.failed", args[1]));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(localeManager.getMessage("commands.spawn.failed", args[1]));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();

        if (args.length == 2) {
            if (args[1].isEmpty()) {
                newTab.addAll(CommandUtils.getSpawnableEntities());
            } else {
                newTab.addAll(
                        CommandUtils.filterStartsWith(
                                CommandUtils.getSpawnableEntities(), args[1]));
            }
        } else if (args.length >= 3) {
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
        return "spawn";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.spawn.usage");
    }
}
