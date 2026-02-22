package jacob_vejvoda.infernal_mobs.cmd;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetLootCommand extends BaseCommand {

    private final LocaleManager localeManager;

    public SetLootCommand(InfernalMobs plugin, LocaleManager localeManager) {
        super(plugin, localeManager);
        this.localeManager = localeManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(localeManager.getMessage("commands.player-only"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }

        Player player = (Player) sender;
        try {
            int nextIndex = getNextLootIndex();

            ItemStack item = player.getInventory().getItemInMainHand();
            String lootPath = "loot." + nextIndex;

            byte[] bytes = item.serializeAsBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            plugin.getLootFile().set(lootPath + ".b64", base64);

            plugin.getLootFile().save(plugin.getLootYML());
            sender.sendMessage(
                    localeManager.getMessage(
                            "commands.setloot.success", String.valueOf(nextIndex)));
        } catch (IOException e) {
            sender.sendMessage(localeManager.getMessage("commands.setloot.error", e.getMessage()));
            e.printStackTrace();
        }
        return true;
    }

    private int getNextLootIndex() {
        ConfigurationSection lootSection = plugin.getLootFile().getConfigurationSection("loot");
        if (lootSection == null) {
            return 1;
        }

        int maxIndex = 0;
        for (String key : lootSection.getKeys(false)) {
            try {
                int index = Integer.parseInt(key);
                if (index > maxIndex) {
                    maxIndex = index;
                }
            } catch (NumberFormatException e) {
            }
        }

        return maxIndex + 1;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "setloot";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.setloot.usage");
    }
}
