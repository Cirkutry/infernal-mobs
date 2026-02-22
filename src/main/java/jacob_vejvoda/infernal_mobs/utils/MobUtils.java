package jacob_vejvoda.infernal_mobs.utils;

import jacob_vejvoda.infernal_mobs.InfernalMob;
import jacob_vejvoda.infernal_mobs.InfernalMobs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MobUtils {

    private final InfernalMobs plugin;

    public MobUtils(InfernalMobs instance) {
        plugin = instance;
    }

    public static Entity getNearbyBoss(Player p, InfernalMobs plugin) {
        double dis = 26.0D;
        for (InfernalMob m : plugin.infernalList) {
            if (m.entity.getWorld().equals(p.getWorld())) {
                Entity boss = m.entity;
                if (p.getLocation().distance(boss.getLocation()) < dis) {
                    dis = p.getLocation().distance(boss.getLocation());
                    return boss;
                }
            }
        }
        return null;
    }
}
