package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveLootCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public GiveLootCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // /im giveloot - give self random loot
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(localeManager.getMessage("commands.player-only"));
                return true;
            }

            Player player = (Player) sender;
            int min = plugin.getConfig().getInt("minPowers");
            int max = plugin.getConfig().getInt("maxPowers");
            int powers = plugin.rand(min, max);
            ItemStack gottenLoot = plugin.getRandomLoot(player, plugin.getRandomMob(), powers);
            if (gottenLoot != null) {
                player.getInventory().addItem(gottenLoot);
            }
            sender.sendMessage(localeManager.getMessage("commands.giveloot.success-random"));
            return true;
        }

        // /im giveloot <index> - give self specific loot by index
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(localeManager.getMessage("commands.player-only"));
                return true;
            }

            Player player = (Player) sender;
            try {
                int index = Integer.parseInt(args[1]);
                ItemStack i = plugin.getLoot(player, index);
                if (i != null) {
                    player.getInventory().addItem(i);
                    sender.sendMessage(
                            localeManager.getMessage(
                                    "commands.giveloot.success-index", String.valueOf(index)));
                } else {
                    sender.sendMessage(
                            localeManager.getMessage(
                                    "commands.giveloot.success-commands", String.valueOf(index)));
                }
                return true;
            } catch (NumberFormatException e) {
                // Not a number, treat as player name for random loot
                Player targetPlayer = Bukkit.getServer().getPlayer(args[1]);
                if (targetPlayer != null) {
                    int min = plugin.getConfig().getInt("minPowers");
                    int max = plugin.getConfig().getInt("maxPowers");
                    int powers = plugin.rand(min, max);
                    ItemStack gottenLoot =
                            plugin.getRandomLoot(
                                    targetPlayer, plugin.getRandomMob(), powers);
                    if (gottenLoot != null) {
                        targetPlayer.getInventory().addItem(gottenLoot);
                    }
                    sender.sendMessage(
                            localeManager.getMessage(
                                    "commands.giveloot.success-random-player",
                                    targetPlayer.getName()));
                    return true;
                } else {
                    sender.sendMessage(localeManager.getMessage("commands.player-not-found"));
                    return true;
                }
            }
        }

        // /im giveloot <player> <index> - give player specific loot by index
        if (args.length == 3) {
            try {
                Player p = Bukkit.getServer().getPlayer(args[1]);
                if (p != null) {
                    int index = Integer.parseInt(args[2]);
                    ItemStack i = plugin.getLoot(p, index);
                    if (i != null) {
                        p.getInventory().addItem(i);
                        sender.sendMessage(
                                localeManager.getMessage(
                                        "commands.giveloot.success-player",
                                        p.getName(),
                                        String.valueOf(index)));
                    } else {
                        sender.sendMessage(
                                localeManager.getMessage(
                                        "commands.giveloot.success-commands-player",
                                        String.valueOf(index),
                                        p.getName()));
                    }
                    return true;
                } else {
                    sender.sendMessage(localeManager.getMessage("commands.player-not-found"));
                    return true;
                }
            } catch (Exception ignored) {
            }
            sender.sendMessage(localeManager.getMessage("commands.giveloot.error"));
            return true;
        }

        sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> newTab = new ArrayList<>();
        if (args.length == 2) {
            // Could be index or player name
            newTab.add("<index>");
            newTab.addAll(CommandUtils.getOnlinePlayerNames());
        }
        if (args.length == 3) {
            newTab.add("<index>");
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
