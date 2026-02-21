package jacob_vejvoda.infernal_mobs.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jacob_vejvoda.infernal_mobs.InfernalMobs;

public class PotionEffectHandler {

    private final InfernalMobs plugin;
    private final LootManager lootManager;

    public PotionEffectHandler(InfernalMobs plugin) {
        this.plugin = plugin;
        plugin.getLootFile();
        this.lootManager = plugin.getLootManager();
    }

    public void applyPotionEffects(LivingEntity entity, String effectID) {
        ConfigurationSection effectSection =
                plugin.getLootFile().getConfigurationSection("potionEffects." + effectID);
        if (effectSection == null) {
            plugin.getLogger().warning("No potion effect found with ID: " + effectID);
            return;
        }

        List<Map<?, ?>> potionEffects = effectSection.getMapList("potion");
        if (potionEffects == null || potionEffects.isEmpty()) {
            plugin.getLogger().warning("No potion effects defined for ID: " + effectID);
            return;
        }

        String effectTypeStr = effectSection.getString("type", "target");
        for (Map<?, ?> potionConfig : potionEffects) {
            String potionTypeName = String.valueOf(potionConfig.get("type"));

            PotionEffectType potionEffectType = LootUtils.getPotionEffectType(potionTypeName);
            if (potionEffectType == null) {
                plugin.getLogger().warning("Invalid potion effect type: " + potionTypeName);
                continue;
            }

            int duration;
            if ("self".equalsIgnoreCase(effectTypeStr)) {
                duration = PotionEffect.INFINITE_DURATION;
            } else {
                Object durationObj = potionConfig.get("duration");
                if (durationObj == null) {
                    duration = PotionEffect.INFINITE_DURATION;
                } else {
                    int rawDuration = Integer.parseInt(String.valueOf(durationObj));
                    duration = rawDuration <= 0
                            ? PotionEffect.INFINITE_DURATION
                            : rawDuration * 20;
                }
            }

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
                            potionEffectType, duration, amplifier, ambient, particles, icon));
        }
    }

    public void checkPlayerPotionEffects(Player player) {
        ConfigurationSection effectsSection =
                plugin.getLootFile().getConfigurationSection("potionEffects");
        if (effectsSection == null) {
            return;
        }

        Map<PotionEffectType, Boolean> shouldHaveEffect = new HashMap<>();

        for (String effectID : effectsSection.getKeys(false)) {
            ConfigurationSection effectSection =
                    plugin.getLootFile().getConfigurationSection("potionEffects." + effectID);
            if (effectSection == null) continue;

            boolean isCharm = effectSection.getBoolean("charm", false);
            String type = effectSection.getString("type", "self");

            if (isCharm) {
                List<Integer> requiredItems = effectSection.getIntegerList("items");
                List<String> slotStrings = getSlotStrings(effectSection);

                boolean hasRequiredItem = false;
                for (int itemID : requiredItems) {
                    ItemStack requiredItem = lootManager.getItem(itemID);
                    if (requiredItem == null) continue;

                    if (hasRequiredItemInSlots(player, null, requiredItem, slotStrings, true)) {
                        hasRequiredItem = true;
                        break;
                    }
                }

                if (hasRequiredItem && "self".equalsIgnoreCase(type)) {
                    List<Map<?, ?>> potionEffects = effectSection.getMapList("potion");
                    if (potionEffects != null && !potionEffects.isEmpty()) {
                        for (Map<?, ?> potionConfig : potionEffects) {
                            String potionTypeName = String.valueOf(potionConfig.get("type"));
                            PotionEffectType effectType = LootUtils.getPotionEffectType(potionTypeName);
                            if (effectType != null) {
                                shouldHaveEffect.put(effectType, true);
                            }
                        }
                    }
                    applyPotionEffects(player, effectID);
                }
            } else if ("self".equalsIgnoreCase(type)) {
                List<Integer> requiredItems = effectSection.getIntegerList("items");
                boolean hasRequiredItem = false;

                for (int itemID : requiredItems) {
                    ItemStack requiredItem = lootManager.getItem(itemID);
                    if (requiredItem == null) continue;

                    if (hasRequiredItemInInventory(player, requiredItem)) {
                        hasRequiredItem = true;
                        break;
                    }
                }

                if (hasRequiredItem) {
                    List<Map<?, ?>> potionEffects = effectSection.getMapList("potion");
                    if (potionEffects != null && !potionEffects.isEmpty()) {
                        for (Map<?, ?> potionConfig : potionEffects) {
                            String potionTypeName = String.valueOf(potionConfig.get("type"));
                            PotionEffectType effectType = LootUtils.getPotionEffectType(potionTypeName);
                            if (effectType != null) {
                                shouldHaveEffect.put(effectType, true);
                            }
                        }
                    }
                    applyPotionEffects(player, effectID);
                }
            }
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.isInfinite() && !shouldHaveEffect.getOrDefault(effect.getType(), false)) {
                player.removePotionEffect(effect.getType());
            }
        }
    }

    private List<String> getSlotStrings(ConfigurationSection effectSection) {
        List<String> slotStrings = new ArrayList<>();
        if (effectSection.isList("slot")) {
            List<?> rawSlots = effectSection.getList("slot");
            if (rawSlots != null) {
                for (Object slot : rawSlots) {
                    if (slot != null) {
                        slotStrings.add(String.valueOf(slot));
                    }
                }
            }
        } else if (effectSection.isString("slot")) {
            slotStrings.add(effectSection.getString("slot"));
        }
        return slotStrings;
    }

    private boolean hasRequiredItemInSlots(Player player, ItemStack itemUsed, 
            ItemStack requiredItem, List<String> slotStrings, boolean isCharm) {
        if (slotStrings.isEmpty()) {
            if (isCharm || isItemMatch(itemUsed, requiredItem)) {
                return true;
            }
        } else {
            for (String slotStr : slotStrings) {
                if ("off".equalsIgnoreCase(slotStr)) {
                    ItemStack offhandItem = player.getInventory().getItemInOffHand();
                    if (offhandItem != null && isItemMatch(offhandItem, requiredItem)) {
                        return true;
                    }
                } else if ("main".equalsIgnoreCase(slotStr)) {
                    if (isItemMatch(itemUsed, requiredItem)) {
                        return true;
                    }
                } else {
                    List<Integer> slotNumbers = getSlotNumbers(slotStr);
                    for (int slotNumber : slotNumbers) {
                        ItemStack playerItem = player.getInventory().getItem(slotNumber);
                        if (playerItem != null && isItemMatch(playerItem, requiredItem)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasRequiredItemInInventory(Player player, ItemStack requiredItem) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (isItemMatch(mainHandItem, requiredItem)) {
            return true;
        }

        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        if (offhandItem != null && isItemMatch(offhandItem, requiredItem)) {
            return true;
        }

        for (int i = 36; i <= 39; i++) {
            ItemStack armorItem = player.getInventory().getItem(i);
            if (isItemMatch(armorItem, requiredItem)) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> getSlotNumbers(String slotStr) {
        List<Integer> slotNumbers = new ArrayList<>();

        if (slotStr == null) {
            return slotNumbers;
        }

        try {
            if (slotStr.matches("\\d+")) {
                int slotNumber = Integer.parseInt(slotStr);
                slotNumbers.add(slotNumber);
            } else {
                plugin.getLogger()
                        .warning(
                                "Invalid slot number: "
                                        + slotStr
                                        + ". Only numeric slot numbers are allowed.");
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid slot number: " + slotStr);
        }
        return slotNumbers;
    }

    private boolean isItemMatch(ItemStack itemA, ItemStack itemB) {
        if (itemA == null || itemB == null) return false;

        if (itemA.getType() != itemB.getType()) return false;

        if (itemA.getItemMeta() == null && itemB.getItemMeta() == null) {
            return true;
        }

        if (itemA.getItemMeta() != null && itemB.getItemMeta() != null) {
            if (itemA.getItemMeta().hasDisplayName() && itemB.getItemMeta().hasDisplayName()) {
                return itemA.getItemMeta()
                        .getDisplayName()
                        .equals(itemB.getItemMeta().getDisplayName());
            } else {
                return !itemA.getItemMeta().hasDisplayName()
                        && !itemB.getItemMeta().hasDisplayName();
            }
        }

        return false;
    }

    public void applyAttackPotionEffects(Player player, LivingEntity target, ItemStack itemUsed) {
        ConfigurationSection effectsSection =
                plugin.getLootFile().getConfigurationSection("potionEffects");
        if (effectsSection == null) {
            return;
        }

        for (String effectID : effectsSection.getKeys(false)) {
            ConfigurationSection effectSection =
                    plugin.getLootFile().getConfigurationSection("potionEffects." + effectID);
            if (effectSection == null) continue;

            List<Integer> items = effectSection.getIntegerList("items");
            if (items.isEmpty()) continue;

            boolean isCharm = effectSection.getBoolean("charm", false);
            boolean hasRequiredItem = false;

            for (int itemID : items) {
                ItemStack requiredItem = lootManager.getItem(itemID);
                if (requiredItem == null) continue;

                if (isCharm) {
                    List<String> slotStrings = getSlotStrings(effectSection);
                    if (hasRequiredItemInSlots(player, itemUsed, requiredItem, slotStrings, true)) {
                        hasRequiredItem = true;
                        break;
                    }
                } else {
                    if (isItemMatch(itemUsed, requiredItem)) {
                        hasRequiredItem = true;
                        break;
                    }

                    if (hasRequiredItemInInventory(player, requiredItem)) {
                        hasRequiredItem = true;
                        break;
                    }
                }
            }

            if (hasRequiredItem) {
                String type = effectSection.getString("type", "target");

                if ("self".equalsIgnoreCase(type)) {
                    applyPotionEffects(player, effectID);
                } else if ("target".equalsIgnoreCase(type)) {
                    applyPotionEffects(target, effectID);
                }
            }
        }
    }

    public void handleItemSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> checkPlayerPotionEffects(player));
    }
}
