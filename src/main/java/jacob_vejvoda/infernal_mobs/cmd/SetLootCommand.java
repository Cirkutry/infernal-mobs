package jacob_vejvoda.InfernalMobs.cmd;

import org.bukkit.command.CommandSender;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import jacob_vejvoda.InfernalMobs.InfernalMobs;
import jacob_vejvoda.InfernalMobs.cmd.LocaleManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        
        if (args.length != 2) {
            sender.sendMessage(localeManager.getMessage("commands.usage", getUsage()));
            return true;
        }
        
        Player player = (Player) sender;
        try {
            ItemStack item = player.getInventory().getItemInMainHand();
            String lootPath = "loot." + args[1];
            plugin.getLootFile().set(lootPath + ".item", item.getType().toString());
            plugin.getLootFile().set(lootPath + ".amount", item.getAmount());
            if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                plugin.getLootFile().set(lootPath + ".name", item.getItemMeta().getDisplayName());
            }
            if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
                plugin.getLootFile().set(lootPath + ".lore", item.getItemMeta().getLore());
            }
            if (item.getItemMeta() instanceof Damageable) {
                plugin.getLootFile().set(lootPath + ".durability", ((Damageable)item.getItemMeta()).getDamage());
            }
            
            plugin.getLootFile().save(plugin.getLootYML());
            sender.sendMessage(localeManager.getMessage("commands.setloot.success", args[1]));
        } catch (IOException e) {
            sender.sendMessage(localeManager.getMessage("commands.setloot.error", e.getMessage()));
            e.printStackTrace();
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
        return "setloot";
    }

    @Override
    public String getUsage() {
        return localeManager.getMessage("commands.setloot.usage");
    }
}
