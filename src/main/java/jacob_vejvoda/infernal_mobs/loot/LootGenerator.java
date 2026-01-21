package jacob_vejvoda.infernal_mobs.loot;

import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles the generation and customization of loot items
 */
public class LootGenerator {
    private final InfernalMobs plugin;
    private final FileConfiguration lootFile;
    private final EnchantmentHandler enchantmentHandler;
    private final LootUtils lootUtils;

    public LootGenerator(InfernalMobs plugin, FileConfiguration lootFile) {
        this.plugin = plugin;
        this.lootFile = lootFile;
        this.enchantmentHandler = new EnchantmentHandler(plugin, lootFile);
        this.lootUtils = new LootUtils(plugin, lootFile);
    }

    /**
     * Creates an ItemStack from loot configuration
     */
    public ItemStack getItem(int loot) {
        try {
            String itemType = this.lootFile.getString("loot." + loot + ".item");
            if (itemType == null) {
                return null;
            }

            int amount = getAmount(loot);
            ItemStack stack = new ItemStack(getMaterial(itemType), amount);

            // Apply basic properties
            applyNameAndLore(stack, loot);
            applyDurability(stack, loot);
            applyEnchantments(stack, loot);
            applyColor(stack, loot);

            // Apply specific item type properties
            applyBookProperties(stack, loot);
            applyBannerProperties(stack, loot);
            applyShieldProperties(stack, loot);
            applyPotionProperties(stack, loot);

            return stack;
        } catch (Exception x) {
            plugin.getLogger().log(Level.WARNING, "Error getting item with ID: " + loot, x);
            return null;
        }
    }

    /**
     * Gets the amount for the loot item
     */
    private int getAmount(int loot) {
        int amount = 1;
        if (lootFile.getString("loot." + loot + ".amount") != null) {
            String amountStr = lootFile.getString("loot." + loot + ".amount");
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                amount = lootUtils.getIntFromString(amountStr);
            }
        }
        return amount;
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
     * Gets the display name for the item
     */
    private String getName(int loot, ItemStack stack) {
        String name = null;
        if (lootFile.getString("loot." + loot + ".name") != null
                && lootFile.isString("loot." + loot + ".name")) {
            name = lootFile.getString("loot." + loot + ".name");
            name = lootUtils.processLootName(name, stack);
            name = ConsumeEffectHandler.hex(name);
        } else if (lootFile.isList("loot." + loot + ".name")) {
            List<String> names = lootFile.getStringList("loot." + loot + ".name");
            if (!names.isEmpty()) {
                name = names.get(plugin.rand(1, names.size()) - 1);
                name = lootUtils.processLootName(name, stack);
                name = ConsumeEffectHandler.hex(name);
            }
        }
        return name;
    }

    /**
     * Gets the lore for the item
     */
    private ArrayList<String> getLore(int loot, ItemStack stack) {
        ArrayList<String> loreList = new ArrayList<>();

        // Individual lore lines
        for (int i = 0; i <= 32; i++) {
            if (this.lootFile.getString("loot." + loot + ".lore" + i) != null) {
                String lore = this.lootFile.getString("loot." + loot + ".lore" + i);
                lore = ChatColor.translateAlternateColorCodes('&', lore);
                loreList.add(lore);
            }
        }

        // Lore list with random selection
        if (!lootFile.getStringList("loot." + loot + ".lore").isEmpty()) {
            List<String> l = lootFile.getStringList("loot." + loot + ".lore");
            int min = l.size();
            if (lootFile.getString("loot." + loot + ".minLore") != null)
                min = lootFile.getInt("loot." + loot + ".minLore");
            int max = l.size();
            if (lootFile.getString("loot." + loot + ".maxLore") != null)
                max = lootFile.getInt("loot." + loot + ".maxLore");
            if (!l.isEmpty())
                for (int i = 0; i < plugin.rand(min, max); i++) {
                    String lore = l.get(plugin.rand(1, l.size()) - 1);
                    l.remove(lore);
                    loreList.add(lootUtils.processLootName(lore, stack));
                }
        }

        return loreList;
    }

    /**
     * Applies durability to the item
     */
    private void applyDurability(ItemStack stack, int loot) {
        if (this.lootFile.getString("loot." + loot + ".durability") != null) {
            String durabilityString = this.lootFile.getString("loot." + loot + ".durability");
            int durability = lootUtils.getIntFromString(durabilityString);
            ItemMeta meta = stack.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(durability);
                stack.setItemMeta(meta);
            }
        }
    }

    /**
     * Applies enchantments to the item
     */
    private void applyEnchantments(ItemStack stack, int loot) {
        enchantmentHandler.applyEnchantments(stack, loot);
    }

    /**
     * Applies color to leather armor
     */
    private void applyColor(ItemStack stack, int loot) {
        if (this.lootFile.getString("loot." + loot + ".colour") != null
                && stack.getType().toString().toLowerCase().contains("leather")) {
            String c = this.lootFile.getString("loot." + loot + ".colour");
            String[] split = c.split(",");
            Color colour =
                    Color.fromRGB(
                            Integer.parseInt(split[0]),
                            Integer.parseInt(split[1]),
                            Integer.parseInt(split[2]));
            dye(stack, colour);
        }
    }

    /**
     * Dyes leather armor
     */
    private void dye(ItemStack item, Color color) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta(meta);
        } catch (Exception localException) {
        }
    }

    /**
     * Applies book-specific properties
     */
    private void applyBookProperties(ItemStack stack, int loot) {
        if ((stack.getType().equals(Material.WRITTEN_BOOK))
                || (stack.getType().equals(Material.WRITABLE_BOOK))) {
            BookMeta bMeta = (BookMeta) stack.getItemMeta();
            if (this.lootFile.getString("loot." + loot + ".author") != null) {
                String author = this.lootFile.getString("loot." + loot + ".author");
                author = ChatColor.translateAlternateColorCodes('&', author);
                bMeta.setAuthor(author);
            }
            if (this.lootFile.getString("loot." + loot + ".title") != null) {
                String title = this.lootFile.getString("loot." + loot + ".title");
                title = ChatColor.translateAlternateColorCodes('&', title);
                bMeta.setTitle(title);
            }
            if (this.lootFile.getConfigurationSection("loot." + loot + ".pages") != null) {
                ConfigurationSection pagesSection =
                        lootFile.getConfigurationSection("loot." + loot + ".pages");
                for (String i : pagesSection.getKeys(false)) {
                    String page = this.lootFile.getString("loot." + loot + ".pages." + i);
                    page = ChatColor.translateAlternateColorCodes('&', page);
                    bMeta.addPage(page);
                }
            }
            stack.setItemMeta(bMeta);
        }
    }

    /**
     * Applies banner-specific properties
     */
    private void applyBannerProperties(ItemStack stack, int loot) {
        if (stack.getType().toString().contains("BANNER")) {
            BannerMeta b = (BannerMeta) stack.getItemMeta();
            @SuppressWarnings("unchecked")
            List<Pattern> patList = (List<Pattern>) lootFile.getList("loot." + loot + ".patterns");
            if (patList != null && (!patList.isEmpty())) b.setPatterns(patList);
            stack.setItemMeta(b);
        }
    }

    /**
     * Applies shield-specific properties
     */
    private void applyShieldProperties(ItemStack stack, int loot) {
        if (stack.getType().equals(Material.SHIELD)) {
            try {
                ItemMeta im = stack.getItemMeta();
                BlockStateMeta bmeta = (BlockStateMeta) im;

                Banner b = (Banner) bmeta.getBlockState();
                List<?> rawPatterns = lootFile.getList("loot." + loot + ".patterns");
                List<Pattern> patList = lootUtils.convertToPatterns(rawPatterns);

                b.setBaseColor(DyeColor.valueOf(lootFile.getString("loot." + loot + ".colour")));
                b.setPatterns(patList);
                b.update();
                bmeta.setBlockState(b);
                stack.setItemMeta(bmeta);
            } catch (Exception e) {
                plugin.getLogger()
                        .log(Level.WARNING, "Error setting shield patterns: " + e.getMessage());
            }
        }
    }

    /**
     * Applies potion-specific properties
     */
    private void applyPotionProperties(ItemStack stack, int loot) {
        if (lootFile.getString("loot." + loot + ".potion") != null)
            if (stack.getType().equals(Material.POTION)
                    || stack.getType().equals(Material.SPLASH_POTION)
                    || stack.getType().equals(Material.LINGERING_POTION)) {
                PotionMeta pMeta = (PotionMeta) stack.getItemMeta();
                String pn = lootFile.getString("loot." + loot + ".potion");
                PotionEffectType effectType =
                        Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(pn.toLowerCase()));
                if (effectType != null) {
                    pMeta.addCustomEffect(new PotionEffect(effectType, 200, 0), true);
                } else {
                    plugin.getLogger().warning("Could not find potion effect type: " + pn);
                }
                stack.setItemMeta(pMeta);
            }
    }
}
