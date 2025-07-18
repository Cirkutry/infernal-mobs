package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import jacob_vejvoda.InfernalMobs.InfernalMobs;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSpawnCommand extends BaseCommand {
    
    private final LocaleManager localeManager;
    
    public CSpawnCommand(InfernalMobs plugin, LocaleManager localeManager) {
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
            sender.sendMessage(localeManager.getMessage("commands.cspawn.world-not-exist", args[2]));
            return true;
        }
        
        World world = Bukkit.getServer().getWorld(args[2]);
        try {
            Location spoint = new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
            ArrayList<String> abList = new ArrayList<>(Arrays.asList(args).subList(6, args.length));
            
            if (plugin.cSpawn(sender, args[1], spoint, abList)) {
                sender.sendMessage(localeManager.getMessage("commands.cspawn.success", args[1], args[2], args[3], args[4], args[5]));
                sender.sendMessage(abList.toString());
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(localeManager.getMessage("commands.coordinates.invalid"));
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
                newTab.addAll(CommandUtils.filterStartsWith(CommandUtils.getSpawnableEntities(), args[1]));
            }
        } else if (args.length == 3) {
            if (args[args.length - 1].isEmpty()) {
                newTab.addAll(CommandUtils.getWorldNames());
            } else {
                newTab.addAll(CommandUtils.filterStartsWith(CommandUtils.getWorldNames(), args[args.length - 1]));
            }
        } else if (args.length > 3 && args.length < 7) {
            newTab.add("~");
        } else if (args.length >= 7) {
            if (args[args.length-1].isEmpty()) {
                newTab.addAll(CommandUtils.ALL_ABILITIES);
            } else {
                newTab.addAll(CommandUtils.filterStartsWith(CommandUtils.ALL_ABILITIES, args[args.length-1]));
            }
        }
        return newTab;
    }
    
    @Override
    public String getName() {
        return "cspawn";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.cspawn.usage");
    }
}
