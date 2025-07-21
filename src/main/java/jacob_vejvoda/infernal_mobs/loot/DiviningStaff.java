package jacob_vejvoda.InfernalMobs.loot;

import jacob_vejvoda.InfernalMobs.GUI;
import jacob_vejvoda.InfernalMobs.InfernalMobs;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class DiviningStaff {

    private final InfernalMobs plugin;

    public DiviningStaff(InfernalMobs plugin) {
        this.plugin = plugin;
    }

    public ItemStack getDiviningStaff() {
        ItemStack s =
                getItem(
                        Material.BLAZE_ROD,
                        "§6§lDivining Rod",
                        1,
                        Arrays.asList("Click to find infernal mobs."));
        ItemMeta m = s.getItemMeta();
        m.addEnchant(Enchantment.CHANNELING, 1, true);
        s.setItemMeta(m);
        return s;
    }

    public void addRecipes() {
        ItemStack staff = getDiviningStaff();
        NamespacedKey key = new NamespacedKey(plugin, "divining_staff");
        ShapedRecipe sr = new ShapedRecipe(key, staff);
        sr.shape("ANA", "ASA", "ASA");
        sr.setIngredient('N', Material.NETHER_STAR);
        sr.setIngredient('S', Material.BLAZE_ROD);
        Bukkit.addRecipe(sr);
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
        plugin.displayParticle(
                "DRIP_LAVA", loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), r, speed, amount);
    }

    private float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }

    private Vector getVector(Entity entity) {
        if (entity instanceof Player) return ((Player) entity).getEyeLocation().toVector();
        else return entity.getLocation().toVector();
    }
}
