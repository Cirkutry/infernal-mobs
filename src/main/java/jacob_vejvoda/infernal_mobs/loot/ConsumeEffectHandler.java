package jacob_vejvoda.infernal_mobs.loot;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jacob_vejvoda.infernal_mobs.InfernalMobs;

public class ConsumeEffectHandler {

    private final InfernalMobs plugin;
    public ConsumeEffectHandler(InfernalMobs plugin) {
        this.plugin = plugin;
        plugin.getLootFile();
    }

    public void applyConsumeEffects(LivingEntity entity, int effectID) {
        ConfigurationSection effectSection =
                plugin.getLootFile().getConfigurationSection("consumeEffects." + effectID);
        if (effectSection == null) {
            plugin.getLogger().warning("No consume effect found with ID: " + effectID);
            return;
        }

        List<Map<?, ?>> potionEffects = effectSection.getMapList("potion");
        if (potionEffects == null || potionEffects.isEmpty()) {
            plugin.getLogger().warning("No potion effects defined for ID: " + effectID);
            return;
        }

        for (Map<?, ?> potionConfig : potionEffects) {
            String potionTypeName = String.valueOf(potionConfig.get("type"));

            PotionEffectType potionEffectType = LootUtils.getPotionEffectType(potionTypeName);
            if (potionEffectType == null) {
                continue;
            }

            if (potionTypeName.equalsIgnoreCase("fertility") && entity instanceof Player) {
                Player player = (Player) entity;
                plugin.fertileList.add(player);
                int duration =
                        potionConfig.containsKey("duration")
                                ? Integer.parseInt(String.valueOf(potionConfig.get("duration")))
                                : 5;
                Bukkit.getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                plugin,
                                () -> {
                                    plugin.fertileList.remove(player);
                                },
                                duration * 20);
                continue;
            }

            int duration =
                    potionConfig.containsKey("duration")
                            ? Integer.parseInt(String.valueOf(potionConfig.get("duration")))
                            : 5;
            int amplifier =
                    potionConfig.containsKey("amplifier")
                            ? Integer.parseInt(String.valueOf(potionConfig.get("amplifier")))
                            : 0;

            boolean ambient =
                    potionConfig.containsKey("ambient")
                            ? Boolean.parseBoolean(String.valueOf(potionConfig.get("ambient")))
                            : false;
            boolean particles =
                    potionConfig.containsKey("particles")
                            ? Boolean.parseBoolean(String.valueOf(potionConfig.get("particles")))
                            : true;
            boolean icon =
                    potionConfig.containsKey("icon")
                            ? Boolean.parseBoolean(String.valueOf(potionConfig.get("icon")))
                            : true;

            entity.addPotionEffect(
                    new PotionEffect(
                            potionEffectType, duration * 20, amplifier, ambient, particles, icon));
        }

        if (entity instanceof Player) {
            String message = effectSection.getString("message");
            if (message != null) {
                ((Player) entity).sendMessage(LootUtils.hex(message));
            }
        }
    }
}
