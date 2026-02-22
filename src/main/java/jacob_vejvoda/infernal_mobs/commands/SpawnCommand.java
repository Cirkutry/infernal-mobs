package jacob_vejvoda.infernal_mobs.commands;

import jacob_vejvoda.infernal_mobs.InfernalMob;
import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class SpawnCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public SpawnCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 6) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }

        if (Bukkit.getServer().getWorld(args[2]) == null) {
            sender.sendMessage(localeManager.getMessage("commands.spawn.world-not-exist", args[2]));
            return true;
        }

        World world = Bukkit.getServer().getWorld(args[2]);
        try {
            Location spoint =
                    new Location(
                            world,
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]),
                            Integer.parseInt(args[5]));
            ArrayList<String> abList = new ArrayList<>(Arrays.asList(args).subList(6, args.length));

            if (spawn(sender, args[1], spoint, abList)) {
                sender.sendMessage(
                        localeManager.getMessage(
                                "commands.spawn.success",
                                args[1],
                                args[2],
                                args[3],
                                args[4],
                                args[5]));
                sender.sendMessage(abList.toString());
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(localeManager.getMessage("commands.spawn.invalid-coordinates"));
        }
        return true;
    }

    private boolean spawn(CommandSender sender, String mob, Location l, ArrayList<String> abList) {
        try {
            EntityType entityType = EntityType.valueOf(mob.toUpperCase());
            Entity ent = l.getWorld().spawnEntity(l, entityType);
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
            return true;
        } catch (IllegalArgumentException e) {
            sender.sendMessage(localeManager.getMessage("commands.spawn.failed", mob));
            return false;
        }
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
        } else if (args.length == 3) {
            if (args[args.length - 1].isEmpty()) {
                newTab.addAll(CommandUtils.getWorldNames());
            } else {
                newTab.addAll(
                        CommandUtils.filterStartsWith(
                                CommandUtils.getWorldNames(), args[args.length - 1]));
            }
        } else if (args.length > 3 && args.length < 7) {
            newTab.add("~");
        } else if (args.length >= 7) {
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
