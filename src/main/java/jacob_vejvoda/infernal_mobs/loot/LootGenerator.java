package jacob_vejvoda.infernal_mobs.loot;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jacob_vejvoda.infernal_mobs.InfernalMobs;

/**
 * Handles the generation and customization of loot items
 */
public class LootGenerator {
    private final InfernalMobs plugin;
    private final FileConfiguration lootFile;

    public LootGenerator(InfernalMobs plugin, FileConfiguration lootFile) {
        this.plugin = plugin;
        this.lootFile = lootFile;
    }

    /**
     * Creates an ItemStack from loot configuration
     */
    public ItemStack getItem(int loot) {
        try {
            // Check for base64 encoded ItemStack first
            String base64 = this.lootFile.getString("loot." + loot + ".b64");
            ItemStack stack;
            
            if (base64 != null) {
                byte[] bytes = Base64.getDecoder().decode(base64);
                stack = ItemStack.deserializeBytes(bytes);
                applyOverrides(stack, loot);
            } else {
                String itemType = this.lootFile.getString("loot." + loot + ".item");
                if (itemType == null) {
                    return null;
                }

                stack = new ItemStack(getMaterial(itemType));

                applyNameAndLore(stack, loot);
            }

            return stack;
        } catch (Exception x) {
            plugin.getLogger().log(Level.WARNING, "Error getting item with ID: " + loot, x);
            return null;
        }
    }

    /**
     * Gets Material from string
     */
    private Material getMaterial(String s) {
        return Material.valueOf(s.toUpperCase());
    }

    /**
     * Applies name and lore to the item
     */
    private void applyNameAndLore(ItemStack stack, int loot) {
        String name = getName(loot, stack);
        ArrayList<String> loreList = getLore(loot, stack);

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(name);
            }
            if (!loreList.isEmpty()) {
                meta.setLore(loreList);
            }
            stack.setItemMeta(meta);
        }
    }

    /**
     * Applies name and lore overrides for base64 items
     */
    private void applyOverrides(ItemStack stack, int loot) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        String name = getName(loot, stack);
        if (name != null) {
            meta.setDisplayName(name);
        }

        ArrayList<String> loreList = getLore(loot, stack);
        if (!loreList.isEmpty()) {
            meta.setLore(loreList);
        }

        stack.setItemMeta(meta);
    }

    /**
     * Gets the display name for the item
     */
    private String getName(int loot, ItemStack stack) {
        String name = lootFile.getString("loot." + loot + ".name");
        if (name != null) {
            name = LootUtils.hex(name);
        }
        return name;
    }

    /**
     * Gets the lore for the item
     */
    private ArrayList<String> getLore(int loot, ItemStack stack) {
        ArrayList<String> loreList = new ArrayList<>();

        // Lore as string or list
        if (lootFile.isString("loot." + loot + ".lore")) {
            String lore = lootFile.getString("loot." + loot + ".lore");
            if (lore != null) {
                lore = ChatColor.translateAlternateColorCodes('&', lore);
                loreList.add(lore);
            }
        } else if (lootFile.isList("loot." + loot + ".lore")) {
            List<String> loreLines = lootFile.getStringList("loot." + loot + ".lore");
            for (String lore : loreLines) {
                lore = ChatColor.translateAlternateColorCodes('&', lore);
                loreList.add(lore);
            }
        }

        return loreList;
    }
}
