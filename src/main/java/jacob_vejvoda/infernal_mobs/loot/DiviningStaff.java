package jacob_vejvoda.infernal_mobs.loot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import jacob_vejvoda.infernal_mobs.GUI;
import jacob_vejvoda.infernal_mobs.InfernalMobs;
import net.md_5.bungee.api.ChatColor;

public class DiviningStaff {

    private final InfernalMobs plugin;

    public DiviningStaff(InfernalMobs plugin) {
        this.plugin = plugin;
    }

    private ItemStack createRodFromConfig(String configPath, FileConfiguration config) {
        if (config == null || !config.isConfigurationSection(configPath)) {
            return null;
        }

        String materialStr = config.getString(configPath + ".material", "STICK");
        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger()
                    .warning("Invalid material '" + materialStr + "' in config at " + configPath);
            material = Material.STICK;
        }

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        String name = config.getString(configPath + ".name");
        if (name != null) {
            name = LootUtils.hex(name);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        List<String> loreConfig = config.getStringList(configPath + ".lore");
        if (loreConfig != null && !loreConfig.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : loreConfig) {
                line = LootUtils.hex(line);
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);
        }

        if (config.isInt(configPath + ".customModelData")) {
            int customModelData = config.getInt(configPath + ".customModelData");
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getDiviningStaff() {
        ItemStack staff = createRodFromConfig("diviningRod", plugin.getConfig());

        if (staff == null) {
            staff =
                    getItem(
                            Material.BLAZE_ROD,
                            "§6§lDivining Rod",
                            1,
                            Arrays.asList("Click to find infernal mobs."));
        }

        if (LootUtils.isEnchantedFromConfig("diviningRod", plugin.getConfig())) {
            ItemMeta meta = staff.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.CHANNELING, 1, true);
                staff.setItemMeta(meta);
            }
        }

        return staff;
    }

    public void addRecipes() {
        if (!plugin.getConfig().getBoolean("diviningRod.recipe.enabled", true)) {
            return;
        }

        ItemStack staff = getDiviningStaff();
        NamespacedKey key = new NamespacedKey(plugin, "divining_staff");
        ShapedRecipe sr = new ShapedRecipe(key, staff);

        List<String> shape = plugin.getConfig().getStringList("diviningRod.recipe.shape");
        if (shape == null || shape.isEmpty()) {

            shape = Arrays.asList("121", "131", "131");
        }

        sr.shape(shape.toArray(new String[0]));

        if (plugin.getConfig().isConfigurationSection("diviningRod.recipe.ingredients")) {
            var ingredients =
                    plugin.getConfig().getConfigurationSection("diviningRod.recipe.ingredients");
            if (ingredients != null) {
                for (String key1 : ingredients.getKeys(false)) {
                    String materialName = ingredients.getString(key1);
                    if (materialName != null && !materialName.equals("AIR")) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            sr.setIngredient(key1.charAt(0), material);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger()
                                    .warning(
                                            "Invalid material '"
                                                    + materialName
                                                    + "' in divining rod recipe");
                        }
                    }
                }
            }
        } else {

            sr.setIngredient('2', Material.NETHER_STAR);
            sr.setIngredient('3', Material.BLAZE_ROD);
        }

        Bukkit.addRecipe(sr);
    }

    public void removeRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "divining_staff");
        Bukkit.removeRecipe(key);
    }

    private ItemStack getItem(Material mat, String name, int amount, List<String> loreList) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta m = item.getItemMeta();
        if (name != null) m.setDisplayName(name);
        if (loreList != null) m.setLore(loreList);
        item.setItemMeta(m);
        return item;
    }

    public void handlePlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        try {
            ItemStack s = getDiviningStaff();
            if (p.getInventory()
                    .getItemInMainHand()
                    .getItemMeta()
                    .getDisplayName()
                    .equals(s.getItemMeta().getDisplayName())) {
                Entity b = GUI.getNearbyBoss(p);

                if (b != null) {
                    boolean took = false;
                    for (ItemStack i : p.getInventory())
                        if (i != null && i.getType().equals(Material.BLAZE_POWDER)) {
                            if (i.getAmount() == 1) {
                                p.getInventory().remove(i);
                            } else i.setAmount(i.getAmount() - 1);
                            took = true;
                            break;
                        }
                    if (!took) {
                        p.sendMessage("§cYou need blaze powder to use this!");
                        return;
                    }

                    Entity source = b;
                    Entity target = p;

                    Vector direction = getVector(target).subtract(getVector(source)).normalize();
                    double x = direction.getX();
                    double y = direction.getY();
                    double z = direction.getZ();

                    Location changed = target.getLocation().clone();
                    changed.setYaw(180 - toDegree(Math.atan2(x, z)));
                    changed.setPitch(90 - toDegree(Math.acos(y)));
                    target.teleport(changed);

                    Bukkit.getServer()
                            .getScheduler()
                            .scheduleSyncDelayedTask(
                                    plugin,
                                    new Runnable() {
                                        public void run() {
                                            Location eyeLoc = p.getEyeLocation();
                                            double px = eyeLoc.getX();
                                            double py = eyeLoc.getY();
                                            double pz = eyeLoc.getZ();
                                            double yaw = Math.toRadians(eyeLoc.getYaw() + 90);
                                            double pitch = Math.toRadians(eyeLoc.getPitch() + 90);
                                            double x = Math.sin(pitch) * Math.cos(yaw);
                                            double y = Math.sin(pitch) * Math.sin(yaw);
                                            double z = Math.cos(pitch);
                                            for (int j = 1; j <= 10; j++) {
                                                for (int i = 1; i <= 10; i++) {
                                                    Location loc =
                                                            new Location(
                                                                    p.getWorld(),
                                                                    px + (i * x),
                                                                    py + (i * z),
                                                                    pz + (i * y));
                                                    beamParticles(loc);
                                                }
                                            }
                                        }
                                    },
                                    5);
                }
            }
        } catch (Exception x) {
        }
    }

    private void beamParticles(Location loc) {
        int speed = -1;
        int amount = 1;
        double r = 0;
        String particle = LootUtils.getParticleFromConfig("diviningRod", plugin.getConfig());
        plugin.displayParticle(
                particle, loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), r, speed, amount);
    }

    private float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }

    private Vector getVector(Entity entity) {
        if (entity instanceof Player) return ((Player) entity).getEyeLocation().toVector();
        else return entity.getLocation().toVector();
    }
}
