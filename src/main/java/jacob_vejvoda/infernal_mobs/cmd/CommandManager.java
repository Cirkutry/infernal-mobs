package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class CommandManager implements TabExecutor {
    private final InfernalMobs plugin;
    private final Map<String, BaseCommand> commands;
    private final LocaleManager localeManager;

    public CommandManager(InfernalMobs plugin) throws Exception {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.localeManager = new LocaleManager(plugin);
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new ReloadCommand(plugin, localeManager));
        registerCommand(new InfoCommand(plugin, localeManager));
        registerCommand(new GiveLootCommand(plugin, localeManager));
        registerCommand(new SetLootCommand(plugin, localeManager));
        registerCommand(new SetSpawnerCommand(plugin, localeManager));
        registerCommand(new SpawnCommand(plugin, localeManager));
        registerCommand(new HelpCommand(plugin, localeManager));
    }

    private void registerCommand(BaseCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("infernalmobs"))
                || (cmd.getName().equalsIgnoreCase("im"))) {
            try {
                if (!sender.hasPermission("infernalmobs.commands")) {
                    sender.sendMessage(localeManager.getMessage("commands.no-permission"));
                    return true;
                }

                if (args.length == 0) {
                    commands.get("help").execute(sender, args);
                    return true;
                }

                BaseCommand command = commands.get(args[0].toLowerCase());
                if (command != null) {
                    if (!sender.hasPermission(command.getPermission())) {
                        sender.sendMessage(localeManager.getMessage("commands.no-permission"));
                        return true;
                    }
                    return command.execute(sender, args);
                } else {
                    commands.get("help").execute(sender, args);
                }
            } catch (Exception e) {
                commands.get("help").execute(sender, args);
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("infernalmobs"))
                || (cmd.getName().equalsIgnoreCase("im"))) {
            if (!sender.hasPermission("infernalmobs.commands")) {
                return new ArrayList<>();
            }

            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                String partial = args[0].toLowerCase();

                for (String commandName : commands.keySet()) {
                    BaseCommand command = commands.get(commandName);
                    if (commandName.startsWith(partial)
                            && sender.hasPermission(command.getPermission())) {
                        completions.add(commandName);
                    }
                }
                return completions;
            } else if (args.length > 1) {
                BaseCommand command = commands.get(args[0].toLowerCase());
                if (command != null && sender.hasPermission(command.getPermission())) {
                    return command.tabComplete(sender, args);
                }
            }
        }
        return new ArrayList<>();
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }
}
