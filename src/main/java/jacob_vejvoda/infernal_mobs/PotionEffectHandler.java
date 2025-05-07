package jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionEffectHandler {
    
    private final infernal_mobs plugin;
    private final FileConfiguration lootFile;
    
    public PotionEffectHandler(infernal_mobs plugin) {
        this.plugin = plugin;
        this.lootFile = plugin.getLootFile();
    }

    public void applyPotionEffects(LivingEntity entity, String effectID) {
        ConfigurationSection effectSection = plugin.getLootFile().getConfigurationSection("potionEffects." + effectID);
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
        boolean isCharm = effectSection.getBoolean("charm", false);
        
        for (Map<?, ?> potionConfig : potionEffects) {
            String potionTypeName = String.valueOf(potionConfig.get("type"));
            
            PotionEffectType potionEffectType = getPotionEffectType(potionTypeName);
            if (potionEffectType == null) {
                plugin.getLogger().warning("Invalid potion effect type: " + potionTypeName);
                continue;
            }
            
            int duration;
            if (isCharm && "self".equalsIgnoreCase(effectTypeStr)) {
                duration = PotionEffect.INFINITE_DURATION;
            } else if (!isCharm && "self".equalsIgnoreCase(effectTypeStr)) {
                duration = PotionEffect.INFINITE_DURATION;
            } else {
                Object durationObj = potionConfig.get("duration");
                if (durationObj == null) {
                    duration = PotionEffect.INFINITE_DURATION;
                } else {
                    int rawDuration = Integer.parseInt(String.valueOf(durationObj));
                    duration = rawDuration <= 0 ? PotionEffect.INFINITE_DURATION : rawDuration * 20;
                }
            }
            
            int amplifier = potionConfig.containsKey("amplifier") ? 
                    Integer.parseInt(String.valueOf(potionConfig.get("amplifier"))) : 0;
            
            boolean ambient = potionConfig.containsKey("ambient") ? 
                    Boolean.parseBoolean(String.valueOf(potionConfig.get("ambient"))) : false;
            boolean particles = potionConfig.containsKey("particles") ? 
                    Boolean.parseBoolean(String.valueOf(potionConfig.get("particles"))) : true;
            boolean icon = potionConfig.containsKey("icon") ? 
                    Boolean.parseBoolean(String.valueOf(potionConfig.get("icon"))) : true;
            
            entity.addPotionEffect(new PotionEffect(
                potionEffectType,
                duration,
                amplifier,
                ambient,
                particles,
                icon
            ));
        }
    }
    
    public void checkPlayerPotionEffects(Player player) {
        ConfigurationSection effectsSection = plugin.getLootFile().getConfigurationSection("potionEffects");
        if (effectsSection == null) {
            return;
        }
        
        Map<PotionEffectType, Boolean> shouldHaveEffect = new HashMap<>();
        
        for (String effectID : effectsSection.getKeys(false)) {
            ConfigurationSection effectSection = plugin.getLootFile().getConfigurationSection("potionEffects." + effectID);
            if (effectSection == null) continue;
            
            boolean isCharm = effectSection.getBoolean("charm", false);
            String type = effectSection.getString("type", "self");
            
            if (isCharm) {
                List<Integer> requiredItems = effectSection.getIntegerList("items");
                
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
                
                boolean hasRequiredItem = false;
                for (int itemID : requiredItems) {
                    ItemStack requiredItem = plugin.getItem(itemID);
                    if (requiredItem == null) continue;
                    
                    for (String slotStr : slotStrings) {
                        List<Integer> slotNumbers = getSlotNumbers(slotStr);
                        for (int slotNumber : slotNumbers) {
                            ItemStack playerItem = player.getInventory().getItem(slotNumber);
                            if (playerItem != null && isItemMatch(playerItem, requiredItem)) {
                                hasRequiredItem = true;
                                break;
                            }
                        }
                        if (hasRequiredItem) break;
                    }
                    
                    if (hasRequiredItem) break;
                }
                
                if (hasRequiredItem) {
                    if ("self".equalsIgnoreCase(type)) {
                        List<Map<?, ?>> potionEffects = effectSection.getMapList("potion");
                        if (potionEffects != null && !potionEffects.isEmpty()) {
                            for (Map<?, ?> potionConfig : potionEffects) {
                                String potionTypeName = String.valueOf(potionConfig.get("type"));
                                PotionEffectType effectType = getPotionEffectType(potionTypeName);
                                if (effectType != null) {
                                    shouldHaveEffect.put(effectType, true);
                                }
                            }
                        }
                        
                        applyPotionEffects(player, effectID);
                    }
                }
            } else if ("self".equalsIgnoreCase(type)) {
                List<Integer> requiredItems = effectSection.getIntegerList("items");
                boolean hasRequiredItem = false;
                
                for (int itemID : requiredItems) {
                    ItemStack requiredItem = plugin.getItem(itemID);
                    if (requiredItem == null) continue;
                    
                    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                    if (isItemMatch(mainHandItem, requiredItem)) {
                        hasRequiredItem = true;
                        break;
                    }
                    
                    ItemStack offhandItem = player.getInventory().getItemInOffHand();
                    if (offhandItem != null && isItemMatch(offhandItem, requiredItem)) {
                        hasRequiredItem = true;
                        break;
                    }
                    
                    for (int i = 36; i <= 39; i++) {
                        ItemStack armorItem = player.getInventory().getItem(i);
                        if (isItemMatch(armorItem, requiredItem)) {
                            hasRequiredItem = true;
                            break;
                        }
                    }
                    
                    if (hasRequiredItem) break;
                }
                
                if (hasRequiredItem) {
                    List<Map<?, ?>> potionEffects = effectSection.getMapList("potion");
                    if (potionEffects != null && !potionEffects.isEmpty()) {
                        for (Map<?, ?> potionConfig : potionEffects) {
                            String potionTypeName = String.valueOf(potionConfig.get("type"));
                            PotionEffectType effectType = getPotionEffectType(potionTypeName);
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
                plugin.getLogger().warning("Invalid slot number: " + slotStr + ". Only numeric slot numbers are allowed.");
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
                return itemA.getItemMeta().getDisplayName().equals(itemB.getItemMeta().getDisplayName());
            } else {
                return !itemA.getItemMeta().hasDisplayName() && !itemB.getItemMeta().hasDisplayName();
            }
        }
        
        return false;
    }
    
    public void applyAttackPotionEffects(Player player, LivingEntity target, ItemStack itemUsed) {
        ConfigurationSection effectsSection = plugin.getLootFile().getConfigurationSection("potionEffects");
        if (effectsSection == null) {
            return;
        }
        
        for (String effectID : effectsSection.getKeys(false)) {
            ConfigurationSection effectSection = plugin.getLootFile().getConfigurationSection("potionEffects." + effectID);
            if (effectSection == null) continue;
            
            List<Integer> items = effectSection.getIntegerList("items");
            if (items.isEmpty()) continue;
            
            boolean isCharm = effectSection.getBoolean("charm", false);
            boolean slotRequirementMet = false;
            boolean hasRequiredItem = false;
            
            for (int itemID : items) {
                ItemStack requiredItem = plugin.getItem(itemID);
                if (requiredItem == null) continue;
                
                if (isCharm) {
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
                    
                    if (slotStrings.isEmpty()) {
                        if (isItemMatch(itemUsed, requiredItem)) {
                            hasRequiredItem = true;
                            slotRequirementMet = true;
                            break;
                        }
                    } else {
                        for (String slotStr : slotStrings) {
                            if ("off".equalsIgnoreCase(slotStr)) {
                                ItemStack offhandItem = player.getInventory().getItemInOffHand();
                                if (offhandItem != null && isItemMatch(offhandItem, requiredItem)) {
                                    hasRequiredItem = true;
                                    slotRequirementMet = true;
                                    break;
                                }
                            } else if ("main".equalsIgnoreCase(slotStr)) {
                                if (isItemMatch(itemUsed, requiredItem)) {
                                    hasRequiredItem = true;
                                    slotRequirementMet = true;
                                    break;
                                }
                            } else {
                                List<Integer> slotNumbers = getSlotNumbers(slotStr);
                                for (int slotNumber : slotNumbers) {
                                    ItemStack playerItem = player.getInventory().getItem(slotNumber);
                                    if (playerItem != null && isItemMatch(playerItem, requiredItem)) {
                                        hasRequiredItem = true;
                                        slotRequirementMet = true;
                                        break;
                                    }
                                }
                            }
                            if (slotRequirementMet) break;
                        }
                    }
                } else {
                    if (isItemMatch(itemUsed, requiredItem)) {
                        hasRequiredItem = true;
                        slotRequirementMet = true;
                        break;
                    }
                    
                    ItemStack offhandItem = player.getInventory().getItemInOffHand();
                    if (offhandItem != null && isItemMatch(offhandItem, requiredItem)) {
                        hasRequiredItem = true;
                        slotRequirementMet = true;
                        break;
                    }
                    
                    for (int i = 36; i <= 39; i++) {
                        ItemStack armorItem = player.getInventory().getItem(i);
                        if (armorItem != null && isItemMatch(armorItem, requiredItem)) {
                            hasRequiredItem = true;
                            slotRequirementMet = true;
                            break;
                        }
                    }
                }
                
                if (slotRequirementMet) break;
            }
            
            if (hasRequiredItem && slotRequirementMet) {
                String type = effectSection.getString("type", "target");
                
                if ("self".equalsIgnoreCase(type)) {
                    applyPotionEffects(player, effectID);
                } else if ("target".equalsIgnoreCase(type)) {
                    applyPotionEffects(target, effectID);
                }
            }
        }
    }
    
    private PotionEffectType getPotionEffectType(String name) {
        PotionEffectType effectType = PotionEffectType.getByName(name);
        
        if (effectType == null) {
            effectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(name.toLowerCase()));
        }
        
        return effectType;
    }

    public void handleItemSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> checkPlayerPotionEffects(player));
    }
} 