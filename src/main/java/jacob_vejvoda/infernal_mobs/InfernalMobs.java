package jacob_vejvoda.InfernalMobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.bukkit.Registry;

import jacob_vejvoda.InfernalMobs.loot.ConsumeEffectHandler;
import jacob_vejvoda.InfernalMobs.loot.PotionEffectHandler;
import jacob_vejvoda.InfernalMobs.loot.LootManager;
import jacob_vejvoda.InfernalMobs.loot.LootUtils;

public class InfernalMobs extends JavaPlugin implements Listener {
    GUI gui;
    long serverTime = 0L;
    private int loops;
    ArrayList<InfernalMob> infernalList = new ArrayList();
    private File lootYML = new File(getDataFolder(), "loot.yml");
    File saveYML = new File(getDataFolder(), "save.yml");
    public FileConfiguration saveFile;
    public FileConfiguration lootFile;
    private HashMap<Entity, Entity> mountList = new HashMap();
    ArrayList<Player> errorList = new ArrayList();
    ArrayList<Player> levitateList = new ArrayList();
    public ArrayList<Player> fertileList = new ArrayList();
    private PotionEffectHandler potionEffectHandler;
    private ConsumeEffectHandler consumeEffectHandler;
    private LootManager lootManager;
    private LootUtils lootUtils;

	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        File dir = new File(this.getDataFolder().getParentFile().getPath(), this.getName());
        if (!dir.exists())
            dir.mkdir();
        
        try {
            // Save default config if it doesn't exist
            saveDefaultConfig();
            
            if (!lootYML.exists()) {
                saveResource("loot.yml", false);
            }
            lootFile = YamlConfiguration.loadConfiguration(lootYML);
            
            if (!saveYML.exists()) {
                saveYML.createNewFile();
            }
            saveFile = YamlConfiguration.loadConfiguration(saveYML);
            

            this.potionEffectHandler = new PotionEffectHandler(this);
            this.consumeEffectHandler = new ConsumeEffectHandler(this);
            this.lootManager = new LootManager(this, lootFile);
            
            this.getLogger().log(Level.INFO, "Configuration files loaded successfully!");
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Failed to load configuration files!", e);
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        

        this.gui = new GUI(this);
        getServer().getPluginManager().registerEvents(this.gui, this);
        
        EventListener events = new EventListener(this);
        getServer().getPluginManager().registerEvents(events, this);
        
        this.getLogger().log(Level.INFO, "Registered Events.");
        
        applyEffect();
        reloadPowers();
        showEffect();
        addRecipes();
    }

    private void reloadPowers() {
        ArrayList<World> wList = new ArrayList();
        for (Player p : getServer().getOnlinePlayers()) {
            if (!wList.contains(p.getWorld())) {
                wList.add(p.getWorld());
            }
        }
        for (World world : wList) {
            giveMobsPowers(world);
        }
    }

    private void scoreCheck() {
        for (Player p : getServer().getOnlinePlayers())
            GUI.fixBar(p);
        HashMap<Entity, Entity> tmp = (HashMap<Entity, Entity>) mountList.clone();
        for (Map.Entry<Entity, Entity> hm : tmp.entrySet()) {
            if ((hm.getKey() != null) && (!hm.getKey().isDead())) {
                if ((hm.getValue().isDead()) && ((hm.getKey() instanceof LivingEntity))) {
                    String fate = getConfig().getString("mountFate", "nothing");
                    if (fate.equals("death")) {
                        LivingEntity le = (LivingEntity) hm.getKey();
                        le.damage(9.99999999E8D);
                        this.mountList.remove(hm.getKey());
                    } else if (fate.equals("removal")) {
                        hm.getKey().remove();
                        this.getLogger().log(Level.INFO, "Entity remove due to Fate");
                        this.mountList.remove(hm.getKey());
                    }
                }
            } else {
                this.mountList.remove(hm.getKey());
            }
        }
    }

    void giveMobsPowers(World world) {
        for (Entity ent : world.getEntities()) {
            if (((ent instanceof LivingEntity)) && (this.saveFile.getString(ent.getUniqueId().toString()) != null)) {
                giveMobPowers(ent);
            }
        }
    }

    void giveMobPowers(Entity ent) {
        UUID id = ent.getUniqueId();
        if (idSearch(id) == -1) {
            List<String> aList = null;
            for (MetadataValue v : ent.getMetadata("infernalMetadata")) {
                aList = new ArrayList(Arrays.asList(v.asString().split(",")));
            }
            if (aList == null) {
                if (this.saveFile.getString(ent.getUniqueId().toString()) != null) {
                    aList = new ArrayList(Arrays.asList(this.saveFile.getString(ent.getUniqueId().toString()).split(",")));
                    String list = getPowerString(ent, aList);
                    ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
                } else {
                    aList = getAbilitiesAmount(ent);
                }
            }
            InfernalMob newMob;
            if (aList.contains("1up")) {
                newMob = new InfernalMob(ent, id, true, aList, 2, getEffect());
            } else {
                newMob = new InfernalMob(ent, id, true, aList, 1, getEffect());
            }
            if (aList.contains("flying")) {
                makeFly(ent);
            }
            this.infernalList.add(newMob);
        }
    }

    void makeInfernal(final Entity e, final boolean fixed) {
        String entName = e.getType().name();
        if ((!e.hasMetadata("NPC")) && (!e.hasMetadata("shopkeeper"))) {
            if (!fixed) {
                ArrayList<String> babyList = (ArrayList) getConfig().getList("disabledBabyMobs", new ArrayList<>());
                if (e instanceof Ageable) {
                    Ageable age = (Ageable) e;
                    boolean baby = !age.isAdult();
                    if (baby && babyList.contains(entName)) {
                        return;
                    }
                }
            }
            final UUID id = e.getUniqueId();
            final int chance = getConfig().getInt("chance");
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                String entName1 = e.getType().name();
                if ((!e.isDead()) && (e.isValid()) && (
                        ((getConfig().getStringList("enabledMobs").contains(entName1))) || ((fixed) &&
                                (idSearch(id) == -1)))) {
                    int min = 1;
                    int max = chance;
                    int mc = getConfig().getInt("mobChances." + entName1);
                    if (mc > 0)
                        max = mc;
                    if (fixed)
                        max = 1;
                    int randomNum = rand(min, max);
                    if (randomNum == 1) {
                        List<String> aList = getAbilitiesAmount(e);
                        if (getConfig().contains("levelChance." + aList.size())) {
                            int sc = getConfig().getInt("levelChance." + aList.size());
                            int randomNum2 = new Random().nextInt(sc - min) + min;
                            if (randomNum2 != 1) {
                                return;
                            }
                        }
                        InfernalMob newMob;
                        if (aList.contains("1up")) {
                            newMob = new InfernalMob(e, id, true, aList, 2, InfernalMobs.this.getEffect());
                        } else {
                            newMob = new InfernalMob(e, id, true, aList, 1, InfernalMobs.this.getEffect());
                        }

                        SpawnEvent infernalEvent = new SpawnEvent(e, newMob);
                        Bukkit.getPluginManager().callEvent(infernalEvent);
                        if (infernalEvent.isCancelled()) {
                            return;
                        }

                        if (aList.contains("flying")) {
                            InfernalMobs.this.makeFly(e);
                        }
                        InfernalMobs.this.infernalList.add(newMob);
                        InfernalMobs.this.gui.setName(e);
                        InfernalMobs.this.giveMobGear(e, true);
                        InfernalMobs.this.addHealth(e, aList);
                        if (getConfig().getBoolean("enableSpawnMessages")) {
                            if (getConfig().getList("spawnMessages") != null) {
                                ArrayList<String> spawnMessageList = (ArrayList) getConfig().getList("spawnMessages");
                                Random randomGenerator = new Random();
                                int index = randomGenerator.nextInt(spawnMessageList.size());
                                String spawnMessage = spawnMessageList.get(index);

                                spawnMessage = ConsumeEffectHandler.hex(spawnMessage);
                                if (e.getCustomName() != null) {
                                    spawnMessage = spawnMessage.replace("mob", e.getCustomName());
                                } else {
                                    spawnMessage = spawnMessage.replace("mob", e.getType().toString().toLowerCase());
                                }
                                int r = getConfig().getInt("spawnMessageRadius");
                                if (r == -1) {
                                    for (Player p : e.getWorld().getPlayers()) {
                                        p.sendMessage(spawnMessage);
                                    }
                                } else if (r == -2) {
                                    Bukkit.broadcastMessage(spawnMessage);
                                } else {
                                    for (Entity e1 : e.getNearbyEntities(r, r, r)) {
                                        if ((e1 instanceof Player)) {
                                            Player p = (Player) e1;
                                            p.sendMessage(spawnMessage);
                                        }
                                    }
                                }
                            } else {
                                System.out.println("No valid spawn messages found!");
                            }
                        }
                    }
                }
            }, 10L);
        }
    }

    private void addHealth(Entity ent, List<String> powerList) {
    	double maxHealth = ((LivingEntity) ent).getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue();
        float setHealth;
        if (getConfig().getBoolean("healthByPower")) {
            int mobIndex = idSearch(ent.getUniqueId());
            try {
                InfernalMob m = this.infernalList.get(mobIndex);
                setHealth = (float) (maxHealth * m.abilityList.size());
            } catch (Exception e) {
                setHealth = (float) (maxHealth * 5.0D);
            }
        } else {
            if (getConfig().getBoolean("healthByDistance")) {
                Location l = ent.getWorld().getSpawnLocation();
                int m = (int) l.distance(ent.getLocation()) / getConfig().getInt("addDistance");
                if (m < 1) {
                    m = 1;
                }
                int add = getConfig().getInt("healthToAdd");
                setHealth = m * add;
            } else {
                int healthMultiplier = getConfig().getInt("healthMultiplier");
                setHealth = (float) (maxHealth * healthMultiplier);
            }
        }
        if (setHealth >= 1.0F) {
            try {
                ((LivingEntity) ent).getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(setHealth);
                ((LivingEntity) ent).setHealth(setHealth);
            } catch (Exception e) {
                System.out.println("addHealth: " + e);
            }
        }
        String list = getPowerString(ent, powerList);
        ent.setMetadata("infernalMetadata", new FixedMetadataValue(this, list));
        this.saveFile.set(ent.getUniqueId().toString(), list);
        try {
            this.saveFile.save(this.saveYML);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Failed to save mob data!", e);
        }
    }

    private String getPowerString(Entity ent, List<String> powerList) {
        StringBuilder list = new StringBuilder();
        for (String s : powerList) {
            if (powerList.indexOf(s) != powerList.size() - 1) {
                list.append(s).append(",");
            } else {
                list.append(s);
            }
        }
        return list.toString();
    }

    void removeMob(int mobIndex) throws IOException {
        String id = this.infernalList.get(mobIndex).id.toString();
        this.infernalList.remove(mobIndex);
        this.saveFile.set(id, null);
        try {
            this.saveFile.save(this.saveYML);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Failed to save mob data!", e);
        }
    }

    void spawnGhost(Location l) {
        boolean evil = false;
        if (new Random().nextInt(3) == 1) {
            evil = true;
        }
        Zombie g = (Zombie) l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
        g.addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 199999980, 1));
        g.setCanPickupItems(false);

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack skull;
        if (evil) {
            skull = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
            dye(chest, Color.BLACK);
        } else {
            skull = new ItemStack(Material.SKELETON_SKULL, 1);
            dye(chest, Color.WHITE);
        }
        chest.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, new Random().nextInt(10) + 1);
        ItemMeta m = skull.getItemMeta();
        m.setDisplayName("§fGhost Head");
        skull.setItemMeta(m);
        g.getEquipment().setHelmet(skull);
        g.getEquipment().setChestplate(chest);
        g.getEquipment().setHelmetDropChance(0.0F);
        g.getEquipment().setChestplateDropChance(0.0F);
        int min = 1;
        int max = 5;
        int rn = new Random().nextInt(max - min) + min;
        if (rn == 1) {
            g.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_HOE, 1));
            g.getEquipment().setItemInMainHandDropChance(0.0F);
        }
        ghostMove(g);

        ArrayList<String> aList = new ArrayList();
        aList.add("ender");
        if (evil) {
            aList.add("necromancer");
            aList.add("withering");
            aList.add("blinding");
        } else {
            aList.add("ghastly");
            aList.add("sapper");
            aList.add("confusing");
        }
        InfernalMob newMob;
        if (evil) {
            newMob = new InfernalMob(g, g.getUniqueId(), false, aList, 1, "smoke:2:12");
        } else {
            newMob = new InfernalMob(g, g.getUniqueId(), false, aList, 1, "cloud:0:8");
        }
        this.infernalList.add(newMob);
    }

    private void ghostMove(final Entity g) {
        if (g.isDead()) {
            return;
        }
        Vector v = g.getLocation().getDirection().multiply(0.3D);
        g.setVelocity(v);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                try {
                    InfernalMobs.this.ghostMove(g);
                } catch (Exception ignored) {
                }
            }
        }, 2L);
    }

    private void dye(ItemStack item, Color color) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta(meta);
        } catch (Exception localException) {
        }
    }

    void keepAlive(Item item) {
        lootManager.keepAlive(item);
    }

    ItemStack getRandomLoot(Player player, String mob, int powers) {
        return lootManager.getRandomLoot(player, mob, powers);
    }
    
    /**
     * Gets specific loot item by ID for command usage
     */
    private ItemStack getLoot(Player player, int loot) {
        return lootManager.getLoot(player, loot);
    }
    
    /**
     * Gets item by loot ID (for compatibility with PotionEffectHandler)
     */
    public ItemStack getItem(int loot) {
        return lootManager.getItem(loot);
    }

    private boolean isBaby(Entity mob) {
    	if(mob instanceof Ageable) {
    		return !((Ageable)mob).isAdult();
    	}
        return false;
    }

    private String getEffect() {
        String effect = "FLAME:1:1";
        try {
            List<String> partTypes = getConfig().getStringList("mobParticles");
            if (partTypes != null && !partTypes.isEmpty()) {
                String selectedEffect = partTypes.get(new Random().nextInt(partTypes.size()));
                if (selectedEffect.contains(":")) {
                    String[] parts = selectedEffect.split(":");
                    if (parts.length >= 3) {
                        effect = selectedEffect;
                    } else {
                        effect = parts[0] + ":1:1";
                    }
                } else {
                    effect = selectedEffect + ":1:1";
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error getting particle effect: " + e.getMessage());
        }
        return effect;
    }

    public void displayEffect(Location l, String effect) {
        if ((effect == null) || (effect.equals(""))) {
            effect = getEffect();
        }
        String[] split = effect.split(":");
        effect = split[0];
        int data1 = Integer.parseInt(split[1]);
        int data2 = Integer.parseInt(split[2]);
        try {
            displayParticle(effect, l, 1.0, data1, data2);
        } catch (Exception x) {
            try {
                displayParticle("FLAME", l, 1.0, data1, data2);
            } catch (Exception ignored) {
                l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, data2);
            }
        }
    }

    private void showEffect() {
        try {
            scoreCheck();
            ArrayList<InfernalMob> tmp = (ArrayList<InfernalMob>) infernalList.clone();
            for (InfernalMob m : tmp) {
                final Entity mob = m.entity;
                UUID id = mob.getUniqueId();
                int index = idSearch(id);
                if (mob.isValid() && (!mob.isDead()) && (index != -1) && (mob.getLocation().getChunk().isLoaded())) {
                    Location feet = mob.getLocation();
                    Location head = mob.getLocation();
                    head.setY(head.getY() + 1);
                    if (getConfig().getBoolean("enableParticles")) {
                        displayEffect(feet, m.effect);
                        if (!isSmall(mob)) {
                            displayEffect(head, m.effect);
                        }
                        if ((mob.getType().equals(EntityType.ENDERMAN)) || (mob.getType().equals(EntityType.IRON_GOLEM))) {
                            head.setY(head.getY() + 1);
                            displayEffect(head, m.effect);
                        }
                    }
                    List<String> abilityList = findMobAbilities(id);
                    if (!mob.isDead()) {
                        for (String ability : abilityList) {
                            Random rand = new Random();
                            int min = 1;
                            int max = 10;
                            int randomNum = rand.nextInt(max - min) + min;
                            if (ability.equals("cloaked")) {
                                ((LivingEntity) mob).addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 40, 1));
                            } else if (ability.equals("armoured")) {
                                if ((!(mob instanceof Skeleton)) && (!(mob instanceof Zombie))) {
                                    ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));
                                }
                            } else if (ability.equals("1up")) {
                                if (((org.bukkit.entity.Damageable) mob).getHealth() <= 5) {
                                    InfernalMob oneUpper = infernalList.get(index);
                                    if (oneUpper.lives > 1) {
                                    	((LivingEntity) mob).setHealth(((LivingEntity) mob).getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue());
                                        oneUpper.setLives(oneUpper.lives - 1);
                                    }
                                }
                            } else if (ability.equals("sprint")) {
                                ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
                            } else if (ability.equals("molten")) {
                                ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1));
                            } else if (ability.equals("tosser")) {
                                if (randomNum < 6) {
                                    double radius = 6D;
                                    ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                                    for (Player player : near) {
                                        if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                            if ((!player.isSneaking()) && (!player.getGameMode().equals(GameMode.CREATIVE))) {
                                                player.setVelocity(mob.getLocation().toVector().subtract(player.getLocation().toVector()));
                                            }
                                        }
                                    }
                                }
                            } else if (ability.equals("gravity")) {
                                if (randomNum >= 9) {
                                    double radius = 10D;
                                    ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                                    for (Player player : near) {
                                        if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                            Location feetBlock = player.getLocation();
                                            feetBlock.setY(feetBlock.getY() - 2);
                                            Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                                            if ((!block.getType().equals(Material.AIR)) && (!player.getGameMode().equals(GameMode.CREATIVE))) {
                                                int amount = 6;
                                                if (getConfig().getString("gravityLevitateLength") != null) {
                                                    amount = getConfig().getInt("gravityLevitateLength");
                                                }
                                                levitate(player, amount);
                                            }
                                        }
                                    }
                                }
                            } else if ((ability.equals("ghastly")) || (ability.equals("necromancer"))) {
                                if ((randomNum == 6) && (!mob.isDead())) {
                                    double radius = 20D;
                                    ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                                    for (Player player : near) {
                                        if ((player.getLocation().distance(mob.getLocation()) <= radius) && (!player.getGameMode().equals(GameMode.CREATIVE))) {
                                            Fireball fb = null;
                                            if (ability.equals("ghastly")) {
                                                fb = ((LivingEntity) mob).launchProjectile(Fireball.class);
                                                player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 5, 1);
                                            } else {
                                                fb = ((LivingEntity) mob).launchProjectile(WitherSkull.class);
                                            }
                                            moveToward(fb, player.getLocation(), 0.6D);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        serverTime = serverTime + 1;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> showEffect(),  20);
    }

    public boolean isSmall(Entity mob) {
        return (isBaby(mob)) && (mob.getType().equals(EntityType.BAT)) && (mob.getType().equals(EntityType.CAVE_SPIDER)) && (mob.getType().equals(EntityType.CHICKEN)) && (mob.getType().equals(EntityType.COW)) && (mob.getType().equals(EntityType.MOOSHROOM)) && (mob.getType().equals(EntityType.PIG)) && (mob.getType().equals(EntityType.OCELOT)) && (mob.getType().equals(EntityType.SHEEP)) && (mob.getType().equals(EntityType.SILVERFISH)) && (mob.getType().equals(EntityType.SPIDER)) && (mob.getType().equals(EntityType.WOLF));
    }

    public void moveToward(final Entity e, final Location to, final double speed) {
        if (e.isDead()) {
            return;
        }
        Vector direction = to.toVector().subtract(e.getLocation().toVector()).normalize();
        e.setVelocity(direction.multiply(speed));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                try {
                    InfernalMobs.this.moveToward(e, to, speed);
                } catch (Exception localException) {
                }
            }
        }, 1L);
    }

    public void applyEffect() {
        for (Player p : this.getServer().getOnlinePlayers()) {
            World world = p.getWorld();
            if (getConfig().getStringList("effectWorlds").contains(world.getName()) || (getConfig().getStringList("effectWorlds").contains("<all>"))) {
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    if (effect.isInfinite()) {
                        p.removePotionEffect(effect.getType());
                    }
                }
                this.potionEffectHandler.checkPlayerPotionEffects(p);
            }
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, this::applyEffect, (10 * 20));
    }

    private boolean isArmor(ItemStack s) {
        String t = s.getType().toString().toLowerCase();
        return t.contains("helm") || t.contains("plate") || t.contains("leg") || t.contains("boot");
    }

    public void applyEatEffects(LivingEntity e, int effectID) {
        this.consumeEffectHandler.applyConsumeEffects(e, effectID);
    }

    private void showEffectParticles(final Entity p, final String e, int time) {
        displayEffect(p.getLocation(), e);
        final int nt = time - 1;
        if (time > 0) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> InfernalMobs.this.showEffectParticles(p, e, nt), 20L);
        }
    }

    private void levitate(final Entity e, final int time) {
        if ((e instanceof LivingEntity)) {
            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, time * 20, 0));
        }
    }

    public void airHold(final Entity e, int time) {
        for (int i = 0; i < time * 20; i++) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                Vector vec = e.getVelocity();
                vec.setY(0.01D);
                e.setVelocity(vec);
            }, i);
            i++;
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if ((e instanceof Player) && levitateList.contains(e)) {
                ((Player) e).setAllowFlight(false);
                levitateList.remove(e);
            }
        }, 20 * time);
    }

    void doEffect(Player player, final Entity mob, boolean playerIsVictim) {
        if (!playerIsVictim && mob instanceof LivingEntity) {
            ItemStack itemUsed = player.getInventory().getItemInMainHand();
            this.potionEffectHandler.applyAttackPotionEffects(player, (LivingEntity)mob, itemUsed);
        }
        
        try {
            UUID id = mob.getUniqueId();
            if (idSearch(id) != -1) {
                List<String> abilityList = findMobAbilities(id);
                if ((!player.isDead()) && (!mob.isDead())) {
                    for (String ability : abilityList)
                        doMagic(player, mob, playerIsVictim, ability, id);
                }
            }
        } catch (Exception e) {/**System.out.println("Do Effect Error: " + e);**/}
    }

    private void doMagic(Entity vic, Entity atc, boolean playerIsVictim, String ability, UUID id) {
        int min = 1;
        int max = 10;
        int randomNum = new Random().nextInt(max - min) + min;
        if ((atc instanceof Player)) {
            randomNum = 1;
        }
        try {
            if ((atc instanceof Player)) {
                switch (ability) {
                    case "tosser":
                        if ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (!((Player) vic).getGameMode().equals(GameMode.CREATIVE)))) {
                            vic.setVelocity(atc.getLocation().toVector().subtract(vic.getLocation().toVector()));
                        }
                        break;
                    case "gravity":
                        if ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (!((Player) vic).getGameMode().equals(GameMode.CREATIVE)))) {
                            Location feetBlock = vic.getLocation();
                            feetBlock.setY(feetBlock.getY() - 2.0D);
                            Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                            if (!block.getType().equals(Material.AIR)) {
                                int amount = 6;
                                if (getConfig().getString("gravityLevitateLength") != null) {
                                    amount = getConfig().getInt("gravityLevitateLength");
                                }
                                levitate(vic, amount);
                            }
                        }
                        break;
                    case "ghastly":
                    case "necromancer":
                        if ((!vic.isDead()) && ((!(vic instanceof Player)) || ((!((Player) vic).isSneaking()) && (!((Player) vic).getGameMode().equals(GameMode.CREATIVE))))) {
                            Fireball fb = null;
                            if (ability.equals("ghastly")) {
                                fb = ((LivingEntity) atc).launchProjectile(Fireball.class);
                                vic.getWorld().playSound(vic.getLocation(), Sound.AMBIENT_CAVE, 5, 1);
                            } else {
                                fb = ((LivingEntity) atc).launchProjectile(WitherSkull.class);
                            }
                            moveToward(fb, vic.getLocation(), 0.6D);
                        }
                        break;
                }
            }
            if (ability.equals("ender")) {
                atc.teleport(vic.getLocation());
            } else if ((ability.equals("poisonous")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
            } else if ((ability.equals("morph")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                try {
                    Entity newEnt;
                    int mc = new Random().nextInt(25) + 1;
                    if (mc != 20) {
                        return;
                    }
                    Location l = atc.getLocation().clone();
                    double h = ((org.bukkit.entity.Damageable) atc).getHealth();
                    List<String> aList = this.infernalList.get(idSearch(id)).abilityList;
                    double dis = 46.0D;
                    for (Entity e : atc.getNearbyEntities(dis, dis, dis))
                        if (e instanceof Player)
                            GUI.fixBar(((Player) e));
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0D, l.getZ()));
                    atc.remove();
                    this.getLogger().log(Level.INFO, "Entity remove due to Morph");
                    List<String> mList = getConfig().getStringList("enabledMobs");
                    int index = new Random().nextInt(mList.size());
                    String mobName = mList.get(index);

                    newEnt = null;
                    EntityType[] arrayOfEntityType;
                    int j = (arrayOfEntityType = EntityType.values()).length;
                    for (int i = 0; i < j; i++) {
                        EntityType e = arrayOfEntityType[i];
                        try {
                            if ((e.getName() != null) && (e.getName().equalsIgnoreCase(mobName))) {
                                newEnt = vic.getWorld().spawnEntity(l, e);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if (newEnt == null) {
                        System.out.println("Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    InfernalMob newMob;
                    if (aList.contains("1up")) {
                        newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, aList, 2, getEffect());
                    } else {
                        newMob = new InfernalMob(newEnt, newEnt.getUniqueId(), true, aList, 1, getEffect());
                    }
                    if (aList.contains("flying")) {
                        makeFly(newEnt);
                    }
                    this.infernalList.set(idSearch(id), newMob);
                    this.gui.setName(newEnt);

                    giveMobGear(newEnt, true);

                    addHealth(newEnt, aList);
                    if (h >= ((LivingEntity) newEnt).getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue()) {
                        return;
                    }
                    ((org.bukkit.entity.Damageable) newEnt).setHealth(h);
                } catch (Exception ex) {
                    System.out.print("Morph Error: ");
                    ex.printStackTrace();
                }
            }
            if ((ability.equals("molten")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                int amount;
                if (getConfig().contains("moltenBurnLength")) {
                    amount = getConfig().getInt("moltenBurnLength");
                } else {
                    amount = 5;
                }
                vic.setFireTicks(amount * 20);
            } else if ((ability.equals("blinding")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            } else if ((ability.equals("confusing")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 80, 2));
            } else if ((ability.equals("withering")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1));
            } else if ((ability.equals("thief")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                if ((vic instanceof Player)) {
                    if ((!((Player) vic).getInventory().getItemInMainHand().getType().equals(Material.AIR)) && ((randomNum <= 1) || (randomNum == 1))) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), ((Player) vic).getInventory().getItemInMainHand());
                        int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, null);
                    }
                } else if (vic instanceof Zombie || vic instanceof Skeleton) {
                    EntityEquipment eq = ((LivingEntity) vic).getEquipment();
                    vic.getWorld().dropItemNaturally(atc.getLocation(), eq.getItemInMainHand());
                    eq.setItemInMainHand(null);
                }
            } else if ((ability.equals("quicksand")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 180, 1));
            } else if ((ability.equals("bullwark")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) atc).addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.RESISTANCE, 500, 2));
            } else if ((ability.equals("rust")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ItemStack damItem = ((Player) vic).getInventory().getItemInMainHand();
                if (((randomNum <= 3) || (randomNum == 1)) && (damItem.getMaxStackSize() == 1)) {
                    int cDur = ((Damageable)damItem.getItemMeta()).getDamage();
                    ((Damageable)damItem.getItemMeta()).setDamage(cDur + 20);
                }
            } else if ((ability.equals("sapper")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.HUNGER, 500, 1), true);
            } else if ((!ability.equals("1up")) || (!isLegitVictim(atc, playerIsVictim, ability))) {
                Location needAir2;
                if ((ability.equals("ender")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                    Location targetLocation = vic.getLocation();
                    if (randomNum >= 8) {
                        Random rand2 = new Random();
                        int min2 = 1;
                        int max2 = 4;
                        int randomNum2 = rand2.nextInt(max2 - min2 + 1) + min2;
                        if (randomNum2 == 1) {
                            targetLocation.setZ(targetLocation.getZ() + 6.0D);
                        } else if (randomNum2 == 2) {
                            targetLocation.setZ(targetLocation.getZ() - 5.0D);
                        } else if (randomNum2 == 3) {
                            targetLocation.setX(targetLocation.getX() + 8.0D);
                        } else if (randomNum2 == 4) {
                            targetLocation.setX(targetLocation.getX() - 10.0D);
                        }
                        needAir2 = targetLocation;
                        needAir2.setY(needAir2.getY() + 1.0D);
                        targetLocation.setY(targetLocation.getY() + 2.0D);
                        if (((targetLocation.getBlock().getType().equals(Material.AIR)) || (targetLocation.getBlock().getType().equals(Material.TORCH))) &&
                                ((needAir2.getBlock().getType().equals(Material.AIR)) || (needAir2.getBlock().getType().equals(Material.TORCH))) && (
                                (targetLocation.getBlock().getType().equals(Material.AIR)) || (targetLocation.getBlock().getType().equals(Material.TORCH)))) {
                            atc.teleport(targetLocation);
                        }
                    }
                } else if ((ability.equals("lifesteal")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                    ((LivingEntity) atc).addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 20, 1));
                } else if ((!ability.equals("cloaked")) || (!isLegitVictim(atc, playerIsVictim, ability))) {
                    if ((ability.equals("storm")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                        if (((randomNum <= 2) || (randomNum == 1)) && (!atc.isDead())) {
                            vic.getWorld().strikeLightning(vic.getLocation());
                        }
                    } else if ((!ability.equals("sprint")) || (!isLegitVictim(atc, playerIsVictim, ability))) {
                        if ((ability.equals("webber")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum >= 8) || (randomNum == 1)) {
                                Location feet = vic.getLocation();
                                feet.getBlock().setType(Material.COBWEB);
                                setAir(feet, 60);

                                int rNum = new Random().nextInt(max - min) + min;
                                if ((rNum == 5) && (
                                        (atc.getType().equals(EntityType.SPIDER)) || (atc.getType().equals(EntityType.CAVE_SPIDER)))) {
                                    Location l = atc.getLocation();
                                    Block b = l.getBlock();
                                    List<Block> blocks = getSphere(b);
                                    for (Block bl : blocks) {
                                        if (bl.getType().equals(Material.AIR)) {
                                            bl.setType(Material.COBWEB);
                                            setAir(bl.getLocation(), 30);
                                        }
                                    }
                                }
                            }
                        } else if ((ability.equals("vengeance")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum >= 5) || (randomNum == 1)) {
                                int amount;
                                if (getConfig().getString("vengeanceDamage") != null) {
                                    amount = getConfig().getInt("vengeanceDamage");
                                } else {
                                    amount = 6;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("weakness")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS, 500, 1));
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1));
                        } else if ((ability.equals("berserk")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum >= 5) && (!atc.isDead())) {
                                double health = ((org.bukkit.entity.Damageable) atc).getHealth();
                                ((org.bukkit.entity.Damageable) atc).setHealth(health - 1.0D);
                                int amount;
                                if (getConfig().getString("berserkDamage") != null) {
                                    amount = getConfig().getInt("berserkDamage");
                                } else {
                                    amount = 3;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("potions")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            ItemStack iStack = new ItemStack(Material.POTION);
                            PotionMeta potion = (PotionMeta) iStack.getItemMeta();
                            switch (randomNum) {
                                case 5:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 2), true);
                                case 6:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);
                                case 7:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, (20 * 15), 2), true);
                                case 8:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.POISON, (20 * 5), 2), true);
                                case 9:
                                    potion.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, (20 * 10), 2), true);
                            }
                            iStack.setItemMeta(potion);
                            Location sploc = atc.getLocation();
                            sploc.setY(sploc.getY() + 3.0D);
                            ThrownPotion thrownPotion = (ThrownPotion) vic.getWorld().spawnEntity(sploc, EntityType.SPLASH_POTION);
                            thrownPotion.setItem(iStack);
                            Vector direction = atc.getLocation().getDirection();
                            direction.normalize();
                            direction.add(new Vector(0.0D, 0.2D, 0.0D));

                            double dist = atc.getLocation().distance(vic.getLocation());

                            dist /= 15.0D;
                            direction.multiply(dist);
                            thrownPotion.setVelocity(direction);
                        } else if ((ability.equals("mama")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if (randomNum == 1) {
                                int amount;
                                if (getConfig().getString("mamaSpawnAmount") != null) {
                                    amount = getConfig().getInt("mamaSpawnAmount");
                                } else {
                                    amount = 3;
                                }
                                if (atc.getType().equals(EntityType.MOOSHROOM)) {
                                    for (int i = 0; i < amount; i++) {
                                        MushroomCow minion = (MushroomCow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.MOOSHROOM);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.COW)) {
                                    for (int i = 0; i < amount; i++) {
                                        Cow minion = (Cow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.COW);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.SHEEP)) {
                                    for (int i = 0; i < amount; i++) {
                                        Sheep minion = (Sheep) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.SHEEP);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.PIG)) {
                                    for (int i = 0; i < amount; i++) {
                                        Pig minion = (Pig) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIG);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.CHICKEN)) {
                                    for (int i = 0; i < amount; i++) {
                                        Chicken minion = (Chicken) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.CHICKEN);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.WOLF)) {
                                    for (int i = 0; i < amount; i++) {
                                        Wolf minion = (Wolf) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.WOLF);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.ZOMBIE)) {
                                    for (int i = 0; i < amount; i++) {
                                        Zombie minion = (Zombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.ZOMBIE);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.PIGLIN)) {
                                    for (int i = 0; i < amount; i++) {
                                        PigZombie minion = (PigZombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIGLIN);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.OCELOT)) {
                                    for (int i = 0; i < amount; i++) {
                                        Ocelot minion = (Ocelot) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.OCELOT);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.HORSE)) {
                                    for (int i = 0; i < amount; i++) {
                                        Horse minion = (Horse) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.HORSE);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.VILLAGER)) {
                                    for (int i = 0; i < amount; i++) {
                                        Villager minion = (Villager) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.VILLAGER);
                                        minion.setBaby();
                                    }
                                } else {
                                    for (int i = 0; i < amount; i++) {
                                        atc.getWorld().spawnEntity(atc.getLocation(), atc.getType());
                                    }
                                }
                            }
                        } else if ((ability.equals("archer")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum > 7) || (randomNum == 1)) {
                                ArrayList<Arrow> arrowList = new ArrayList();
                                Location loc1 = vic.getLocation();
                                Location loc2 = atc.getLocation();
                                if (!isSmall(atc)) {
                                    loc2.setY(loc2.getY() + 1.0D);
                                }
                                Arrow a = ((LivingEntity) atc).launchProjectile(Arrow.class);
                                int arrowSpeed = 1;
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() + 0.5D);
                                loc2.setZ(loc2.getBlockZ() + 0.5D);
                                Arrow a2 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
                                a2.setShooter((LivingEntity) atc);
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() - 1);
                                loc2.setZ(loc2.getBlockZ() - 1);
                                Arrow a3 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), arrowSpeed, 12.0F);
                                a3.setShooter((LivingEntity) atc);
                                arrowList.add(a);
                                arrowList.add(a2);
                                arrowList.add(a3);
                                for (Arrow ar : arrowList) {
                                    double minAngle = 6.283185307179586D;
                                    Entity minEntity = null;
                                    for (Entity entity : atc.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                                        if ((((LivingEntity) atc).hasLineOfSight(entity)) && ((entity instanceof LivingEntity)) && (!entity.isDead())) {
                                            Vector toTarget = entity.getLocation().toVector().clone().subtract(atc.getLocation().toVector());
                                            double angle = ar.getVelocity().angle(toTarget);
                                            if (angle < minAngle) {
                                                minAngle = angle;
                                                minEntity = entity;
                                            }
                                        }
                                    }
                                    if (minEntity != null) {
                                        new ArrowHomingTask(ar, (LivingEntity) minEntity, this);
                                    }
                                }
                            }
                        } else if ((ability.equals("firework")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                            int red = getConfig().getInt("fireworkColour.red");
                            int green = getConfig().getInt("fireworkColour.green");
                            int blue = getConfig().getInt("fireworkColour.blue");
                            ItemStack tmpCol = new ItemStack(Material.LEATHER_HELMET, 1);
                            LeatherArmorMeta tmpCol2 = (LeatherArmorMeta) tmpCol.getItemMeta();
                            tmpCol2.setColor(Color.fromRGB(red, green, blue));

                            Color col = tmpCol2.getColor();
                            launchFirework(atc.getLocation(), col, 1);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
    
    private static List<Block> getSphere(Block block1) {
        List<Block> blocks = new LinkedList();
        double xi = block1.getLocation().getX() + 0.5D;
        double yi = block1.getLocation().getY() + 0.5D;
        double zi = block1.getLocation().getZ() + 0.5D;
        for (int v1 = 0; v1 <= 90; v1++) {
            double y = Math.sin(0.017453292519943295D * v1) * 4;
            double r = Math.cos(0.017453292519943295D * v1) * 4;
            if (v1 == 90) {
                r = 0.0D;
            }
            for (int v2 = 0; v2 <= 90; v2++) {
                double x = Math.sin(0.017453292519943295D * v2) * r;
                double z = Math.cos(0.017453292519943295D * v2) * r;
                if (v2 == 90) {
                    z = 0.0D;
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)));
                }
            }
        }
        return blocks;
    }

    private void launchFirework(Location l, Color c, int speed) {
        Firework fw = l.getWorld().spawn(l, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(speed));
        detonate(fw);
    }

    private void detonate(final Firework fw) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                fw.detonate();
            } catch (Exception ignored) {
            }
        }, 2L);
    }

    private boolean isLegitVictim(Entity e, boolean playerIsVictim, String ability) {
        if ((e instanceof Player)) {
            return true;
        }
        if (getConfig().getBoolean("effectAllPlayerAttacks")) {
            return true;
        }
        ArrayList<String> attackAbilityList = new ArrayList();
        attackAbilityList.add("poisonous");
        attackAbilityList.add("blinding");
        attackAbilityList.add("withering");
        attackAbilityList.add("thief");
        attackAbilityList.add("sapper");
        attackAbilityList.add("lifesteal");
        attackAbilityList.add("storm");
        attackAbilityList.add("webber");
        attackAbilityList.add("weakness");
        attackAbilityList.add("berserk");
        attackAbilityList.add("potions");
        attackAbilityList.add("archer");
        attackAbilityList.add("confusing");
        if ((playerIsVictim) && (attackAbilityList.contains(ability))) {
            return true;
        }
        ArrayList<String> defendAbilityList = new ArrayList();
        defendAbilityList.add("thief");
        defendAbilityList.add("storm");
        defendAbilityList.add("webber");
        defendAbilityList.add("weakness");
        defendAbilityList.add("potions");
        defendAbilityList.add("archer");
        defendAbilityList.add("quicksand");
        defendAbilityList.add("bullwark");
        defendAbilityList.add("rust");
        defendAbilityList.add("ender");
        defendAbilityList.add("vengeance");
        defendAbilityList.add("mama");
        defendAbilityList.add("firework");
        defendAbilityList.add("morph");
        return (!playerIsVictim) && (defendAbilityList.contains(ability));
    }

    private void setAir(final Location block, int time) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (block.getBlock().getType().equals(Material.COBWEB)) {
                block.getBlock().setType(Material.AIR);
            }
        }, time * 20);
    }

    private List<String> getAbilitiesAmount(Entity e) {
        int power;
        if (getConfig().getBoolean("powerByDistance")) {
            Location l = e.getWorld().getSpawnLocation();
            int m = (int) l.distance(e.getLocation()) / getConfig().getInt("addDistance");
            if (m < 1) {
                m = 1;
            }
            int add = getConfig().getInt("powerToAdd");
            power = m * add;
        } else {
            int min = getConfig().getInt("minPowers");
            int max = getConfig().getInt("maxPowers");
            power = rand(min, max);
        }
        return getAbilities(power);
    }

    private List<String> getAbilities(int amount) {
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        List<String> abilityList = new ArrayList();
        int min = 1;
        for (int i = 0; i < amount; i++) {
            int max = allAbilitiesList.size();
            int randomNum = new Random().nextInt(max - min) + min;
            String ab = allAbilitiesList.get(randomNum);
            if (getConfig().contains(ab)) {
                if ((getConfig().getString(ab, "always").equals("always")) || (getConfig().getBoolean(ab))) {
                    abilityList.add(ab);
                    allAbilitiesList.remove(randomNum);
                } else {
                    allAbilitiesList.remove(randomNum);
                    i = i - 1;
                }
            } else
                this.getLogger().log(Level.WARNING, "Ability: " + ab + " is not set!");
        }
        return abilityList;
    }

    public int idSearch(UUID id) {
        InfernalMob idMob = null;
        for (InfernalMob mob : this.infernalList) {
            if (mob.id.equals(id)) {
                idMob = mob;
            }
        }
        if (idMob != null) {
            return this.infernalList.indexOf(idMob);
        }
        return -1;
    }

    public List<String> findMobAbilities(UUID id) {
        for (InfernalMob mob : this.infernalList) {
            if (mob.id.equals(id)) {
                return mob.abilityList;
            }
        }
        return null;
    }

    private Entity getTarget(final Player player) {

        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, 100);
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                return entity;
                            }
            }
        }
        return null;
    }

    private void makeFly(Entity ent) {
        Entity bat = ent.getWorld().spawnEntity(ent.getLocation(), EntityType.BAT);
        bat.setVelocity(new Vector(0, 1, 0));
        bat.addPassenger(ent);
        ((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1));
    }

    private void giveMobGear(Entity mob, boolean naturalSpawn) {
        UUID mobId = mob.getUniqueId();
        List<String> mobAbilityList = null;
        boolean armoured = false;
        if (idSearch(mobId) != -1) {
            mobAbilityList = findMobAbilities(mobId);
            if (mobAbilityList.contains("armoured")) {
                armoured = true;
                ((LivingEntity) mob).setCanPickupItems(false);
            }
        }
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET, 1);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        ItemStack pants = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 4);
        EntityEquipment ee = ((LivingEntity) mob).getEquipment();
        if (mob.getType().equals(EntityType.WITHER_SKELETON)) {
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setLeggingsDropChance(0.0F);
                ee.setBootsDropChance(0.0F);
                ee.setItemInMainHandDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                ee.setLeggings(pants);
                ee.setBoots(boots);
                ee.setItemInMainHand(sword);
            }
        } else if (mob.getType().equals(EntityType.SKELETON)) {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            ee.setItemInMainHand(bow);
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggingsDropChance(0.0F);
                    ee.setBootsDropChance(0.0F);
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setItemInMainHandDropChance(0.0F);
                ee.setItemInMainHand(sword);
            } else if (mobAbilityList.contains("cloaked")) {
                ItemStack skull = new ItemStack(Material.GLASS_BOTTLE, 1);
                ee.setHelmet(skull);
            }
        } else if (mob instanceof Zombie) {
            if (armoured) {
                ee.setHelmetDropChance(0.0F);
                ee.setChestplateDropChance(0.0F);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setLeggingsDropChance(0.0F);
                ee.setBootsDropChance(0.0F);
                ee.setItemInMainHandDropChance(0.0F);
                ee.setItemInMainHand(sword);
            } else if (mobAbilityList.contains("cloaked")) {
                ItemStack skull = new ItemStack(Material.GLASS_BOTTLE);
                ee.setHelmet(skull);
            }
        }
        if (((mobAbilityList.contains("mounted")) && (getConfig().getStringList("enabledRiders").contains(mob.getType().name()))) || ((!naturalSpawn) && (mobAbilityList.contains("mounted")))) {
            List<String> mounts;

            mounts = getConfig().getStringList("enabledMounts");

            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(mounts.size());
            String mount = mounts.get(index);
            String type = null;
            if (mount.contains(":")) {
                String[] s = mount.split(":");
                mount = s[0];
                type = s[1];
            }
            if (EntityType.valueOf(mount.toUpperCase()) != null && (!EntityType.valueOf(mount.toUpperCase()).equals(EntityType.ENDER_DRAGON))) {
                Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(), EntityType.valueOf(mount.toUpperCase()));

                this.mountList.put(liveMount, mob);
                liveMount.addPassenger(mob);
                if (liveMount.getType().equals(EntityType.HORSE)) {
                    Horse hm = (Horse) liveMount;
                    if (getConfig().getBoolean("horseMountsHaveSaddles")) {
                        ItemStack saddle = new ItemStack(Material.SADDLE);
                        hm.getInventory().setSaddle(saddle);
                    }
                    hm.setTamed(true);
                    int randomNum3 = rand(1, 7);
                    if (randomNum3 == 1) {
                        hm.setColor(Horse.Color.BLACK);
                    } else if (randomNum3 == 2) {
                        hm.setColor(Horse.Color.BROWN);
                    } else if (randomNum3 == 3) {
                        hm.setColor(Horse.Color.CHESTNUT);
                    } else if (randomNum3 == 4) {
                        hm.setColor(Horse.Color.CREAMY);
                    } else if (randomNum3 == 5) {
                        hm.setColor(Horse.Color.DARK_BROWN);
                    } else if (randomNum3 == 6) {
                        hm.setColor(Horse.Color.GRAY);
                    } else {
                        hm.setColor(Horse.Color.WHITE);
                    }
                    if ((armoured) && (getConfig().getBoolean("armouredMountsHaveArmour"))) {
                        ItemStack armour = new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1);
                        hm.getInventory().setArmor(armour);
                    }
                } else if (liveMount.getType().equals(EntityType.SHEEP)) {
                    Sheep sh = (Sheep) liveMount;
                    if (type != null) {
                        sh.setColor(DyeColor.valueOf(type));
                    }
                }
            } else {
                System.out.println("Can't spawn mount!");
                System.out.println(mount + " is not a valid Entity!");
            }
        }
    }

    private void displayParticle(String effect, Location l, double radius, int speed, int amount) {
        displayParticle(effect, l.getWorld(), l.getX(), l.getY(), l.getZ(), radius, speed, amount);
    }

    void displayParticle(String effect, World w, double x, double y, double z, double radius, int speed, int amount) {
        amount = (amount <= 0) ? 1 : amount;
        Location l = new Location(w, x, y, z);
        try {
            Particle particle = Particle.valueOf(effect);
            if (radius <= 0) {
                w.spawnParticle(particle, l, amount, 0, 0, 0, speed);
            } else {
                List<Location> ll = getArea(l, radius, 0.2);
                if (ll.size() > 0){
                    for (int i = 0; i < amount; i++) {
                        int index = new Random().nextInt(ll.size());
                        w.spawnParticle(particle, ll.get(index), 1, 0, 0, 0, 0);
                        ll.remove(index);
                    }
                }
            }
        } catch (Exception ex) {
            getLogger().warning("Error displaying particle: " + ex.getMessage());
        }
    }

    private List<Location> getArea(Location l, double r, double t) {
        List<Location> ll = new ArrayList();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t) {
                    ll.add(new Location(l.getWorld(), x, y, z));
                }
            }
        }
        return ll;
    }

    private String getRandomMob() {
        List<String> mobList = getConfig().getStringList("enabledMobs");
        if (mobList.isEmpty()) {
            return "Zombie";
        }
        String mob = mobList.get(rand(1, mobList.size()) - 1);
        if (mob != null) {
            return mob;
        }
        return "Zombie";
    }

    String generateString(int maxNames, List<String> names) {
        StringBuilder namesString = new StringBuilder();
        if (maxNames > names.size()) {
            maxNames = names.size();
        }
        for (int i = 0; i < maxNames; i++) {
            namesString.append(names.get(i)).append(" ");
        }
        if (names.size() > maxNames) {
            namesString.append("... ");
        }
        return namesString.toString();
    }

    private void checkEnchantmentLimits() {
        if (lootFile == null) return;
        
        ConfigurationSection lootSection = lootFile.getConfigurationSection("loot");
        if (lootSection == null) return;
        
        int issuesFound = 0;
        
        for (String lootId : lootSection.getKeys(false)) {
            String itemType = lootFile.getString("loot." + lootId + ".item");
            ConfigurationSection enchantmentsSection = lootFile.getConfigurationSection("loot." + lootId + ".enchantments");
            if (enchantmentsSection == null) continue;
            
            for (String key : enchantmentsSection.getKeys(false)) {
                String enchantmentName = lootFile.getString("loot." + lootId + ".enchantments." + key + ".enchantment");
                if (enchantmentName == null) continue;
                
                String levelStr = lootFile.getString("loot." + lootId + ".enchantments." + key + ".level");
                if (levelStr == null) continue;
                
                try {
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName.toLowerCase()));
                    if (enchant == null) {
                        getLogger().warning("Unknown enchantment: " + enchantmentName + " on item " + lootId);
                        continue;
                    }
                    
                    int level = lootUtils.getIntFromString(levelStr);
                    int maxAllowedLevel = getMaxAllowedEnchantmentLevel(enchant);
                    
                    if (level > maxAllowedLevel) {
                        issuesFound++;
                        getLogger().warning("Item at index " + lootId + " (" + itemType + ") has " + 
                                           enchant.getKey().getKey() + " enchant level " + 
                                           level + " but max is " + maxAllowedLevel);
                    }
                } catch (Exception e) {
                    getLogger().warning("Error checking enchantment " + enchantmentName + ": " + e.getMessage());
                }
            }
        }
    }

    private void reloadLoot() {
        try {
            if (this.lootYML == null) {
                this.lootYML = new File(getDataFolder(), "loot.yml");
            }
            
            if (!lootYML.exists()) {
                saveResource("loot.yml", false);
                this.lootFile = YamlConfiguration.loadConfiguration(lootYML);
            } else {
                this.lootFile = YamlConfiguration.loadConfiguration(lootYML);
            }
            
            checkEnchantmentLimits();
            
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Error reloading loot configuration", e);
        }
    }
    
    private int getMaxAllowedEnchantmentLevel(Enchantment enchant) {
        String enchantKey = enchant.getKey().getKey().toLowerCase();
        if (enchantKey.equals("mending") || 
            enchantKey.equals("silk_touch") ||
            enchantKey.equals("infinity") ||
            enchantKey.equals("channeling") ||
            enchantKey.equals("aqua_affinity")) {
            return 1;
        }
        return 255;
    }
    
    public Set<String> getConfigurationSectionKeys(String path) {
        ConfigurationSection section = lootFile.getConfigurationSection(path);
        return section != null ? section.getKeys(false) : new HashSet<>();
    }
    
    public List<Integer> getIntegerList(String path) {
        return lootFile.getIntegerList(path);
    }

    String getLocationName(Location l) {
        return (l.getX() + "." + l.getY() + "." + l.getZ() + l.getWorld().getName()).replace(".", "");
    }

    Block blockNear(Location l, Material mat, int radius) {
        double xTmp = l.getX();
        double yTmp = l.getY();
        double zTmp = l.getZ();
        int finalX = (int) Math.round(xTmp);
        int finalY = (int) Math.round(yTmp);
        int finalZ = (int) Math.round(zTmp);
        for (int x = finalX - radius; x <= finalX + radius; x++) {
            for (int y = finalY - radius; y <= finalY + radius; y++) {
                for (int z = finalZ - radius; z <= finalZ + radius; z++) {
                    Location loc = new Location(l.getWorld(), x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType().equals(mat)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    private boolean cSpawn(CommandSender sender, String mob, Location l, ArrayList<String> abList) {
        if ((EntityType.valueOf(mob.toUpperCase()) != null)) {
            Entity ent = l.getWorld().spawnEntity(l, EntityType.valueOf(mob.toUpperCase()));
            InfernalMob newMob;
            UUID id = ent.getUniqueId();
            if (abList.contains("1up")) {
                newMob = new InfernalMob(ent, id, true, abList, 2, getEffect());
            } else {
                newMob = new InfernalMob(ent, id, true, abList, 1, getEffect());
            }
            if (abList.contains("flying")) {
                makeFly(ent);
            }
            this.infernalList.add(newMob);
            this.gui.setName(ent);

            giveMobGear(ent, false);
            addHealth(ent, abList);
            return true;
        } else {
            sender.sendMessage("Can't spawn a " + mob + "!");
            return false;
        }
    }

    public int rand(int min, int max) {
        return min + (int) (Math.random() * (1 + max - min));
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        List<String> allAbilitiesList = new ArrayList<>(Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        Set<String> commands = new HashSet<>(Arrays.asList("reload", "worldInfo", "error", "getloot", "setloot", "giveloot", "abilities", "showAbilities", "setInfernal", "spawn", "cspawn", "pspawn", "kill", "killall"));
        if (sender.hasPermission("InfernalMobs.commands")) {

            List<String> newTab = new ArrayList<>();
            if (args.length == 1){
                if (args[0].isEmpty())
                    return new ArrayList<>(commands);
                commands.forEach(tab->{
                    if (tab.toLowerCase().startsWith(args[0].toLowerCase()))
                    newTab.add(tab);
                });
            }
            if (args[0].equalsIgnoreCase("getloot") || args[0].equalsIgnoreCase("setloot")){
                if (args.length == 2){
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("giveloot")){
                if (args.length == 2){
                    newTab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                }
                if (args.length == 3){
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("setinfernal")){
                if (args.length == 2){
                    newTab.add("10");
                }
            }
            if (args.length == 2){
                if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("cspawn") || args[0].equalsIgnoreCase("pspawn")){
                    if (args[1].isEmpty())
                        newTab.addAll(Arrays.stream(EntityType.values()).filter(m->m.isSpawnable() && m.isAlive()).map(Enum::name).collect(Collectors.toList()));
                    else
                        Arrays.stream(EntityType.values()).filter(m->m.isSpawnable() && m.isAlive()).map(Enum::name).collect(Collectors.toList()).forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args[0].equalsIgnoreCase("killall")){
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args[0].equalsIgnoreCase("kill")){
                    newTab.add("1");
                }
            }
            if (args[0].equalsIgnoreCase("cspawn")) {
                if (args.length == 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args.length > 3 && args.length < 7) {
                    newTab.add("~");
                }
                if (args.length >= 7){
                    if (args[args.length-1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            if (args[0].equalsIgnoreCase("pspawn")) {
                if (args.length == 3) {
                    if (args[args.length - 1].isEmpty())
                        newTab.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()));
                    else
                        Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()).forEach(tab -> {
                            if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
                if (args.length > 3){
                    if (args[args.length-1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            if (args.length >= 3){
                if (args[0].equalsIgnoreCase("spawn")){
                    if (args[args.length-1].isEmpty())
                        newTab.addAll(allAbilitiesList);
                    else
                        allAbilitiesList.forEach(tab->{
                            if (tab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                newTab.add(tab);
                        });
                }
            }
            return newTab;
        }
        return null;
    }
    
    public ItemStack getDiviningStaff(){
    	ItemStack s = getItem(Material.BLAZE_ROD, "§6§lDivining Rod", 1, Arrays.asList("Click to find infernal mobs."));
    	ItemMeta m = s.getItemMeta();
    	m.addEnchant(Enchantment.CHANNELING, 1, true);
    	s.setItemMeta(m);
    	return s;
    }

    public void addRecipes() {
    	ItemStack staff = getDiviningStaff();
    	NamespacedKey key = new NamespacedKey(this, "divining_staff");
    	ShapedRecipe sr = new ShapedRecipe(key, staff);
		sr.shape("ANA", "ASA", "ASA");
		sr.setIngredient('N', Material.NETHER_STAR);
		sr.setIngredient('S', Material.BLAZE_ROD);
		Bukkit.addRecipe(sr);
    }
    
    private ItemStack getItem(Material mat, String name, int amount, List<String> loreList){
    	ItemStack item = new ItemStack(mat, amount);
    	ItemMeta m = item.getItemMeta();
    	if(name != null)
    		m.setDisplayName(name);
    	if(loreList != null)
    		m.setLore(loreList);
    	item.setItemMeta(m);
  	  	return item;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("infernalmobs")) || (cmd.getName().equalsIgnoreCase("im"))) {
            try {
                Player player = null;
                if (!(sender instanceof Player)) {
                    if (args != null && args.length > 0 && (!args[0].equalsIgnoreCase("cspawn")) && (!args[0].equalsIgnoreCase("pspawn")) && (!args[0].equalsIgnoreCase("giveloot")) && (!args[0].equalsIgnoreCase("reload")) && (!args[0].equalsIgnoreCase("killall"))) {
                        sender.sendMessage("This command can only be run by a player!");
                        return true;
                    }
                } else
                    player = (Player) sender;
                if (sender.hasPermission("InfernalMobs.commands")) {
                    if (args.length == 0) {
                        throwError(sender);
                        return true;
                    }
                    if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {
                        reloadConfig();
                        refreshLoot();
                        reloadMobSave();
                        sender.sendMessage("§eConfig files reloaded successfully!");
                        return true;
                    } else if (args[0].equals("mobList")) {
                        sender.sendMessage("§6Mob List:");
                        for (EntityType et : EntityType.values())
                            if (et != null && et.getName() != null)
                                sender.sendMessage("§e" + et.getName());
                        return true;
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("error"))) {
                        this.errorList.add(player);
                        sender.sendMessage("§eClick on a mob to send an error report about it.");
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("info"))) {
                        sender.sendMessage("§eMounts: " + this.mountList.size());
                        sender.sendMessage("§eLoops: " + this.loops);
                        sender.sendMessage("§eInfernals: " + this.infernalList.size());
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("worldInfo"))) {
                        List<String> enWorldList = getConfig().getStringList("mobWorlds");
                        World world = player.getWorld();
                        String enabled = "is not";
                        if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
                            enabled = "is";
                        }
                        sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                        sender.sendMessage("All the worlds that are enabled are: " + enWorldList.toString());
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("help"))) {
                        throwError(sender);
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("getloot"))) {
                        int min = getConfig().getInt("minPowers");
                        int max = getConfig().getInt("maxPowers");
                        int powers = rand(min, max);
                        ItemStack gottenLoot = getRandomLoot(player, getRandomMob(), powers);
                        if (gottenLoot != null) {
                            player.getInventory().addItem(gottenLoot);
                        }
                        sender.sendMessage("§eGave you some random loot!");
                    } else if ((args.length == 2) && (args[0].equalsIgnoreCase("getloot"))) {
                        try {
                            int index = Integer.parseInt(args[1]);
                            ItemStack i = getLoot(player, index);
                            if (i != null) {
                                player.getInventory().addItem(i);
                                sender.sendMessage("§eGave you the loot at index §9" + index);
                                return true;
                            }
                        } catch (Exception ignored) {
                        }
                        sender.sendMessage("§cUnable to get that loot!");
                    } else if ((args.length == 3) && (args[0].equalsIgnoreCase("giveloot"))) {
                        try {
                            Player p = getServer().getPlayer(args[1]);
                            if (p != null) {
                                int index = Integer.parseInt(args[2]);
                                ItemStack i = getLoot(p, index);
                                if (i != null) {
                                    p.getInventory().addItem(i);
                                    sender.sendMessage("§eGave the player the loot at index §9" + index);
                                    return true;
                                }
                            } else {
                                sender.sendMessage("§cPlayer not found!!");
                                return true;
                            }
                        } catch (Exception ignored) {
                        }
                        sender.sendMessage("§cUnable to get that loot!");
                    } else if (((args.length == 2) && (args[0].equalsIgnoreCase("spawn"))) || ((args[0].equalsIgnoreCase("cspawn")) && (args.length == 6))) {
                        if ((EntityType.valueOf(args[1].toUpperCase()) != null)) {
                            boolean exmsg;
                            World world;
                            Entity ent;
                            if ((args[0].equalsIgnoreCase("cspawn")) && (args[2] != null) && (args[3] != null) && (args[4] != null) && (args[5] != null)) {
                                if (Bukkit.getServer().getWorld(args[2]) == null) {
                                    sender.sendMessage(args[2] + " does not exist!");
                                    return true;
                                }
                                world = Bukkit.getServer().getWorld(args[2]);
                                Location spoint = new Location(Bukkit.getServer().getWorld(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                                ent = world.spawnEntity(spoint, EntityType.valueOf(args[1].toUpperCase()));
                                exmsg = true;
                            } else {
                                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                                ent = player.getWorld().spawnEntity(farSpawnLoc, EntityType.valueOf(args[1].toUpperCase()));
                                exmsg = false;
                            }
                            List<String> abList = getAbilitiesAmount(ent);
                            InfernalMob newMob;
                            UUID id = ent.getUniqueId();
                            if (abList.contains("1up")) {
                                newMob = new InfernalMob(ent, id, true, abList, 2, getEffect());
                            } else {
                                newMob = new InfernalMob(ent, id, true, abList, 1, getEffect());
                            }

                            if (abList.contains("flying")) {
                                makeFly(ent);
                            }
                            this.infernalList.add(newMob);
                            this.gui.setName(ent);

                            giveMobGear(ent, false);
                            addHealth(ent, abList);
                            if (!exmsg) {
                                sender.sendMessage("Spawned a " + args[1]);
                            } else if (sender instanceof Player) {
                                sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5]);
                            }
                        } else {
                            sender.sendMessage("Can't spawn a " + args[1] + "!");
                            return true;
                        }
                    } else if (((args.length >= 3) && (args[0].equalsIgnoreCase("spawn"))) || ((args[0].equalsIgnoreCase("cspawn")) && (args.length >= 6)) || ((args[0].equalsIgnoreCase("pspawn")) && (args.length >= 3))) {
                        if (args[0].equalsIgnoreCase("spawn")) {
                            if ((EntityType.valueOf(args[1].toUpperCase()) != null)) {
                                Location farSpawnLoc = player.getTargetBlock(null, 200).getLocation();
                                farSpawnLoc.setY(farSpawnLoc.getY() + 1.0D);
                                Entity ent = player.getWorld().spawnEntity(farSpawnLoc, EntityType.valueOf(args[1].toUpperCase()));
                                ArrayList<String> specificAbList = new ArrayList();
                                for (int i = 0; i <= args.length - 3; i++) {
                                    if (getConfig().getString(args[(i + 2)]) != null) {
                                        specificAbList.add(args[(i + 2)]);
                                    } else {
                                        sender.sendMessage(args[(i + 2)] + " is not a valid ability!");
                                        return true;
                                    }
                                }
                                InfernalMob newMob;
                                UUID id = ent.getUniqueId();
                                if (specificAbList.contains("1up")) {
                                    newMob = new InfernalMob(ent, id, true, specificAbList, 2, getEffect());
                                } else {
                                    newMob = new InfernalMob(ent, id, true, specificAbList, 1, getEffect());
                                }
                                if (specificAbList.contains("flying")) {
                                    makeFly(ent);
                                }
                                this.infernalList.add(newMob);
                                this.gui.setName(ent);
                                giveMobGear(ent, false);

                                addHealth(ent, specificAbList);

                                sender.sendMessage("Spawned a " + args[1] + " with the abilities:");
                                sender.sendMessage(specificAbList.toString());
                            } else {
                                sender.sendMessage("Can't spawn a " + args[1] + "!");
                            }
                        } else if (args[0].equalsIgnoreCase("cspawn")) {
                            if (Bukkit.getServer().getWorld(args[2]) == null) {
                                sender.sendMessage(args[2] + " does not exist!");
                                return true;
                            }
                            World world = Bukkit.getServer().getWorld(args[2]);
                            Location spoint = new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                            ArrayList<String> abList = new ArrayList(Arrays.asList(args).subList(6, args.length));
                            if (cSpawn(sender, args[1], spoint, abList)) {
                                sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5] + " with the abilities:");
                                sender.sendMessage(abList.toString());
                            }
                        } else {
                            Player p = getServer().getPlayer(args[2]);
                            if (p == null) {
                                sender.sendMessage(args[2] + " is not online!");
                                return true;
                            }
                            ArrayList<String> abList = new ArrayList(Arrays.asList(args).subList(3, args.length));
                            if (cSpawn(sender, args[1], p.getLocation(), abList)) {
                                sender.sendMessage("Spawned a " + args[1] + " at " + p.getName() + " with the abilities:");
                                sender.sendMessage(abList.toString());
                            }
                        }
                    } else if ((args.length == 1) && (args[0].equalsIgnoreCase("abilities"))) {
                        sender.sendMessage("--Infernal Mobs Abilities--");
                        sender.sendMessage("mama, molten, weakness, vengeance, webber, storm, sprint, lifesteal, ghastly, ender, cloaked, berserk, 1up, sapper, rust, bullwark, quicksand, thief, tosser, withering, blinding, armoured, poisonous, potions, explode, gravity, archer, necromancer, firework, flying, mounted, morph, ghost, confusing");
                    } else {
                        List<String> oldMobAbilityList;
                        if ((args.length == 1) && (args[0].equalsIgnoreCase("showAbilities"))) {
                            if (getTarget(player) != null) {
                                Entity targeted = getTarget(player);
                                UUID mobId = targeted.getUniqueId();
                                if (idSearch(mobId) != -1) {
                                    oldMobAbilityList = findMobAbilities(mobId);
                                    if (!targeted.isDead()) {
                                        sender.sendMessage("--Targeted InfernalMob's Abilities--");
                                        sender.sendMessage(oldMobAbilityList.toString());
                                    }
                                } else {
                                    sender.sendMessage("§cThis " + targeted.getType().getName() + " §cis not an infernal mob!");
                                }
                            } else {
                                sender.sendMessage("§cUnable to find mob!");
                            }
                        } else if ((args[0].equalsIgnoreCase("setInfernal")) && (args.length == 2)) {
                            if (player.getTargetBlock(null, 25).getType().equals(Material.SPAWNER)) {
                                int delay = Integer.parseInt(args[1]);

                                String name = getLocationName(player.getTargetBlock(null, 25).getLocation());

                                this.saveFile.set("infernalSpawners." + name, delay);
                                try {
                                    this.saveFile.save(this.saveYML);
                                } catch (IOException e) {
                                    this.getLogger().log(Level.SEVERE, "Failed to save spawner data!", e);
                                }
                                sender.sendMessage("§cSpawner set to infernal with a " + delay + " second delay!");
                            } else {
                                sender.sendMessage("§cYou must be looking at a spawner to make it infernal!");
                            }
                        } else if ((args[0].equalsIgnoreCase("kill")) && (args.length == 2)) {
                            int size = Integer.parseInt(args[1]);
                            for (Entity e : player.getNearbyEntities(size, size, size)) {
                                int id = idSearch(e.getUniqueId());
                                if (id != -1) {
                                    removeMob(id);
                                    e.remove();
                                    this.getLogger().log(Level.INFO, "Entity remove due to /kill");
                                }
                            }
                            sender.sendMessage("§eKilled all infernal mobs near you!");
                        } else if ((args[0].equalsIgnoreCase("killall")) && (args.length == 1 || args.length == 2)) {
                            World w = null;
                            if (args.length == 1 && sender instanceof Player){
                                w = ((Player) sender).getWorld();
                            } else if (args.length == 2){
                                w = getServer().getWorld(args[1]);
                            }

                            if (w != null) {
                                for (Entity e : w.getEntities()) {
                                    int id = idSearch(e.getUniqueId());
                                    if (id != -1) {
                                        removeMob(id);
                                        if(e instanceof LivingEntity) {
                                        	((LivingEntity)e).setCustomName(null);
                                        }
                                        this.getLogger().log(Level.INFO, "Entity remove due to /killall");
                                        e.remove();
                                    }
                                }
                                sender.sendMessage("§eKilled all loaded infernal mobs in that world!");
                            } else {
                                sender.sendMessage("§cWorld not found!");
                            }
                        } else if (args[0].equalsIgnoreCase("mobs")) {
                            sender.sendMessage("§6List of Mobs:");
                            for (EntityType e : EntityType.values())
                                if (e != null)
                                    sender.sendMessage(e.toString());
                        } else if (args[0].equalsIgnoreCase("setloot")) {
                            try {
                                ItemStack item = player.getInventory().getItemInMainHand();
                                String lootPath = "loot." + args[1];
                                lootFile.set(lootPath + ".item", item.getType().toString());
                                lootFile.set(lootPath + ".amount", item.getAmount());
                                if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                                    lootFile.set(lootPath + ".name", item.getItemMeta().getDisplayName());
                                }
                                if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
                                    lootFile.set(lootPath + ".lore", item.getItemMeta().getLore());
                                }
                                if (item.getItemMeta() instanceof Damageable) {
                                    lootFile.set(lootPath + ".durability", ((Damageable)item.getItemMeta()).getDamage());
                                }
                                
                                lootFile.save(lootYML);
                                sender.sendMessage("§eSet loot at index " + args[1] + " §eto item in hand and saved to file.");
                            } catch (IOException e) {
                                sender.sendMessage("§cFailed to save loot file: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            throwError(sender);
                        }
                    }
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                }
            } catch (Exception x) {
                throwError(sender);
                x.printStackTrace();
            }
        }
        return true;
    }

    private void throwError(CommandSender sender) {
        sender.sendMessage("--Infernal Mobs v" + this.getDescription().getVersion() + "--");
        sender.sendMessage("Usage: /im reload");
        sender.sendMessage("Usage: /im worldInfo");
        sender.sendMessage("Usage: /im error");
        sender.sendMessage("Usage: /im getloot <index>");
        sender.sendMessage("Usage: /im setloot <index>");
        sender.sendMessage("Usage: /im giveloot <player> <index>");
        sender.sendMessage("Usage: /im abilities");
        sender.sendMessage("Usage: /im showAbilities");
        sender.sendMessage("Usage: /im setInfernal <time delay>");
        sender.sendMessage("Usage: /im spawn <mob> <ability> <ability>");
        sender.sendMessage("Usage: /im cspawn <mob> <world> <x> <y> <z> <ability> <ability>");
        sender.sendMessage("Usage: /im pspawn <mob> <player> <ability> <ability>");
        sender.sendMessage("Usage: /im kill <size>");
        sender.sendMessage("Usage: /im killall <world>");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.getLogger().info("Config reloaded successfully.");
    }
    
    public void refreshLoot() {
        try {
            if (!lootYML.exists()) {
                saveResource("loot.yml", false);
                this.lootFile = YamlConfiguration.loadConfiguration(lootYML);
            } else {
                this.lootFile = YamlConfiguration.loadConfiguration(lootYML);
            }
            checkEnchantmentLimits();
            if (this.potionEffectHandler != null) {
                this.potionEffectHandler = new PotionEffectHandler(this);
            }
            this.getLogger().info("Loot configuration reloaded successfully.");
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to reload loot configuration!", e);
        }
    }
    
    public void reloadMobSave() {
        try {
            this.saveFile = YamlConfiguration.loadConfiguration(this.saveYML);
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to reload mob save configuration!", e);
        }
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return lootFile.getConfigurationSection(path);
    }

    public FileConfiguration getLootFile() {
        return this.lootFile;
    }

    public LootManager getLootManager() {
        return this.lootManager;
    }

    public PotionEffectHandler getPotionEffectHandler() {
        return this.potionEffectHandler;
    }
}