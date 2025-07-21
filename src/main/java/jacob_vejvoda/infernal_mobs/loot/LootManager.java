package jacob_vejvoda.InfernalMobs.loot;

import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Manages loot configuration, selection, and generation for infernal mobs
 */
public class LootManager {
    private final InfernalMobs plugin;
    private final FileConfiguration lootFile;
    private final ArrayList<UUID> droppedLootList;
    private final LootGenerator lootGenerator;

    public LootManager(InfernalMobs plugin, FileConfiguration lootFile) {
        this.plugin = plugin;
        this.lootFile = lootFile;
        this.droppedLootList = new ArrayList<>();
        this.lootGenerator = new LootGenerator(plugin, lootFile);
    }

    /**
     * Gets random loot for a player based on mob type and powers
     */
    public ItemStack getRandomLoot(Player player, String mob, int powers) {
        ArrayList<Integer> lootList = new ArrayList<>();
        ConfigurationSection lootSection = lootFile.getConfigurationSection("loot");

        if (lootSection != null) {
            for (String i : lootSection.getKeys(false)) {
                // Check if loot entry has either an item or commands defined
                boolean hasItem = lootFile.getString("loot." + i + ".item") != null;
                boolean hasCommands = !lootFile.getStringList("loot." + i + ".commands").isEmpty();

                if ((hasItem || hasCommands)
                        && ((lootFile.getList("loot." + i + ".mobs") == null)
                                || (lootFile.getList("loot." + i + ".mobs", new ArrayList<>())
                                        .contains(mob)))
                        && (lootFile.getString("loot." + i + ".chancePercentage") == null
                                || plugin.rand(1, 100)
                                        <= lootFile.getInt("loot." + i + ".chancePercentage"))) {
                    if (mobPowerLevelFine(Integer.parseInt(i), powers)) {
                        lootList.add(Integer.valueOf(i));
                    }
                }
            }
        }

        try {
            if (plugin.getConfig().getBoolean("debug"))
                plugin.getLogger().log(Level.INFO, "Loot List " + lootList.toString());
            if (!lootList.isEmpty()) {
                return getLoot(player, lootList.get(plugin.rand(1, lootList.size()) - 1));
            } else return null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error in get random loot ", e);
            plugin.getLogger().warning("Error: No valid drops found!");
        }
        return null;
    }

    /**
     * Gets specific loot item by ID for a player (public method for commands)
     */
    public ItemStack getLoot(Player player, int loot) {
        ItemStack i = null;
        try {
            // Execute commands first if they exist
            if (!this.lootFile.getStringList("loot." + loot + ".commands").isEmpty()) {
                List<String> commandList =
                        this.lootFile.getStringList("loot." + loot + ".commands");
                for (String command : commandList) {
                    command = org.bukkit.ChatColor.translateAlternateColorCodes('&', command);
                    command = replaceCommandVariables(command, player);
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }

            // Try to get the item (may be null if only commands are defined)
            i = lootGenerator.getItem(loot);
        } catch (Exception x) {
            plugin.getServer()
                    .getLogger()
                    .log(Level.WARNING, "Error processing loot with ID: " + loot, x);
        }
        return i;
    }

    /**
     * Gets item by loot ID without executing commands (for PotionEffectHandler)
     */
    public ItemStack getItem(int loot) {
        return lootGenerator.getItem(loot);
    }

    /**
     * Checks if mob power level is within loot requirements
     */
    private boolean mobPowerLevelFine(int lootId, int mobPowers) {
        int min = 0;
        int max = 99;
        if (lootFile.getString("loot." + lootId + ".powersMin") != null) {
            min = lootFile.getInt("loot." + lootId + ".powersMin");
        }
        if (lootFile.getString("loot." + lootId + ".powersMax") != null)
            max = lootFile.getInt("loot." + lootId + ".powersMax");
        if (plugin.getConfig().getBoolean("debug"))
            plugin.getLogger()
                    .log(Level.INFO, "Loot " + lootId + " min = " + min + " and max = " + max);
        return (mobPowers >= min) && (mobPowers <= max);
    }

    /**
     * Replaces variables in command strings with actual values
     * Available variables:
     * - player: Player's name
     * - worldName: Name of the player's current world
     * - playerX: Player's X coordinate (rounded)
     * - playerY: Player's Y coordinate (rounded)
     * - playerZ: Player's Z coordinate (rounded)
     */
    private String replaceCommandVariables(String command, org.bukkit.entity.Player player) {
        // Basic player info
        command = command.replace("{player}", player.getName());

        // Location info
        command = command.replace("{worldName}", player.getWorld().getName());
        command =
                command.replace(
                        "{playerX}", String.valueOf((int) Math.round(player.getLocation().getX())));
        command =
                command.replace(
                        "{playerY}", String.valueOf((int) Math.round(player.getLocation().getY())));
        command =
                command.replace(
                        "{playerZ}", String.valueOf((int) Math.round(player.getLocation().getZ())));

        return command;
    }

    /**
     * Keeps dropped items alive for a set duration
     */
    public void keepAlive(Item item) {
        final UUID id = item.getUniqueId();
        this.droppedLootList.add(id);
        Bukkit.getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(
                        plugin,
                        new Runnable() {
                            public void run() {
                                droppedLootList.remove(id);
                            }
                        },
                        300L);
    }

    /**
     * Gets the loot file configuration
     */
    public FileConfiguration getLootFile() {
        return this.lootFile;
    }

    /**
     * Gets the dropped loot list
     */
    public ArrayList<UUID> getDroppedLootList() {
        return this.droppedLootList;
    }
}
