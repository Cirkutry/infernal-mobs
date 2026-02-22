package jacob_vejvoda.infernal_mobs.infernal;

import jacob_vejvoda.infernal_mobs.ArrowHomingTask;
import jacob_vejvoda.infernal_mobs.InfernalMob;
import jacob_vejvoda.infernal_mobs.InfernalMobs;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Horse;
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
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Magic {
    private final InfernalMobs plugin;

    public Magic(InfernalMobs plugin) {
        this.plugin = plugin;
    }

    public void levitate(final Entity e, final int time) {
        if ((e instanceof LivingEntity)) {
            ((LivingEntity) e)
                    .addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, time * 20, 0));
        }
    }

    public void doMagic(Entity vic, Entity atc, boolean playerIsVictim, String ability, UUID id) {
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
                        if ((!(vic instanceof Player))
                                || ((!((Player) vic).isSneaking())
                                        && (!((Player) vic)
                                                .getGameMode()
                                                .equals(GameMode.CREATIVE)))) {
                            vic.setVelocity(
                                    atc.getLocation()
                                            .toVector()
                                            .subtract(vic.getLocation().toVector()));
                        }
                        break;
                    case "gravity":
                        if ((!(vic instanceof Player))
                                || ((!((Player) vic).isSneaking())
                                        && (!((Player) vic)
                                                .getGameMode()
                                                .equals(GameMode.CREATIVE)))) {
                            Location feetBlock = vic.getLocation();
                            feetBlock.setY(feetBlock.getY() - 2.0D);
                            Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                            if (!block.getType().equals(Material.AIR)) {
                                int amount = 6;
                                if (plugin.getConfig().getString("gravityLevitateLength") != null) {
                                    amount = plugin.getConfig().getInt("gravityLevitateLength");
                                }
                                levitate(vic, amount);
                            }
                        }
                        break;
                    case "ghastly":
                    case "necromancer":
                        if ((!vic.isDead())
                                && ((!(vic instanceof Player))
                                        || ((!((Player) vic).isSneaking())
                                                && (!((Player) vic)
                                                        .getGameMode()
                                                        .equals(GameMode.CREATIVE))))) {
                            Fireball fb = null;
                            if (ability.equals("ghastly")) {
                                fb = ((LivingEntity) atc).launchProjectile(Fireball.class);
                                vic.getWorld()
                                        .playSound(vic.getLocation(), Sound.AMBIENT_CAVE, 5, 1);
                            } else {
                                fb = ((LivingEntity) atc).launchProjectile(WitherSkull.class);
                            }
                            plugin.moveToward(fb, vic.getLocation(), 0.6D);
                        }
                        break;
                }
            }
            if (ability.equals("ender")) {
                atc.teleport(vic.getLocation());
            } else if ((ability.equals("poisonous"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic)
                        .addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
            } else if ((ability.equals("morph")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                try {
                    Entity newEnt;
                    int mc = new Random().nextInt(25) + 1;
                    if (mc != 20) {
                        return;
                    }
                    Location l = atc.getLocation().clone();
                    double h = ((org.bukkit.entity.Damageable) atc).getHealth();
                    List<String> aList = plugin.findMobAbilities(id);
                    double dis = 46.0D;
                    for (Entity e : atc.getNearbyEntities(dis, dis, dis))
                        if (e instanceof Player) plugin.fixBar(((Player) e));
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0D, l.getZ()));
                    atc.remove();
                    plugin.getLogger().log(Level.INFO, "Entity remove due to Morph");
                    List<String> mList = plugin.getConfig().getStringList("enabledMobs");
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
                        plugin.getLogger()
                                .log(
                                        Level.WARNING,
                                        "Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    InfernalMob newMob;
                    if (aList.contains("1up")) {
                        newMob =
                                new InfernalMob(
                                        newEnt,
                                        newEnt.getUniqueId(),
                                        true,
                                        aList,
                                        2,
                                        plugin.getEffect());
                    } else {
                        newMob =
                                new InfernalMob(
                                        newEnt,
                                        newEnt.getUniqueId(),
                                        true,
                                        aList,
                                        1,
                                        plugin.getEffect());
                    }
                    if (aList.contains("flying")) {
                        plugin.makeFly(newEnt);
                    }
                    plugin.getInfernalList().set(plugin.idSearch(id), newMob);
                    plugin.getGui().setName(newEnt);

                    plugin.giveMobGear(newEnt, true);

                    plugin.addHealth(newEnt, aList);
                    if (h
                            >= ((LivingEntity) newEnt)
                                    .getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)
                                    .getBaseValue()) {
                        return;
                    }
                    ((org.bukkit.entity.Damageable) newEnt).setHealth(h);
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "Morph Error: ", ex);
                }
            }
            if ((ability.equals("molten")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                int amount;
                if (plugin.getConfig().contains("moltenBurnLength")) {
                    amount = plugin.getConfig().getInt("moltenBurnLength");
                } else {
                    amount = 5;
                }
                vic.setFireTicks(amount * 20);
            } else if ((ability.equals("blinding"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic)
                        .addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            } else if ((ability.equals("confusing"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic)
                        .addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 80, 2));
            } else if ((ability.equals("withering"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic)
                        .addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1));
            } else if ((ability.equals("thief")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                if ((vic instanceof Player)) {
                    if ((!((Player) vic)
                                    .getInventory()
                                    .getItemInMainHand()
                                    .getType()
                                    .equals(Material.AIR))
                            && ((randomNum <= 1) || (randomNum == 1))) {
                        vic.getWorld()
                                .dropItemNaturally(
                                        atc.getLocation(),
                                        ((Player) vic).getInventory().getItemInMainHand());
                        int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, null);
                    }
                } else if (vic instanceof Zombie || vic instanceof Skeleton) {
                    EntityEquipment eq = ((LivingEntity) vic).getEquipment();
                    vic.getWorld().dropItemNaturally(atc.getLocation(), eq.getItemInMainHand());
                    eq.setItemInMainHand(null);
                }
            } else if ((ability.equals("quicksand"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic)
                        .addPotionEffect(
                                new PotionEffect(
                                        org.bukkit.potion.PotionEffectType.SLOWNESS, 180, 1));
            } else if ((ability.equals("bullwark"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) atc)
                        .addPotionEffect(
                                new PotionEffect(
                                        org.bukkit.potion.PotionEffectType.RESISTANCE, 500, 2));
            } else if ((ability.equals("rust")) && (isLegitVictim(atc, playerIsVictim, ability))) {
                ItemStack damItem = ((Player) vic).getInventory().getItemInMainHand();
                if (((randomNum <= 3) || (randomNum == 1)) && (damItem.getMaxStackSize() == 1)) {
                    int cDur = ((Damageable) damItem.getItemMeta()).getDamage();
                    ((Damageable) damItem.getItemMeta()).setDamage(cDur + 20);
                }
            } else if ((ability.equals("sapper"))
                    && (isLegitVictim(atc, playerIsVictim, ability))) {
                ((LivingEntity) vic)
                        .addPotionEffect(
                                new PotionEffect(org.bukkit.potion.PotionEffectType.HUNGER, 500, 1),
                                true);
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
                        if (((targetLocation.getBlock().getType().equals(Material.AIR))
                                        || (targetLocation
                                                .getBlock()
                                                .getType()
                                                .equals(Material.TORCH)))
                                && ((needAir2.getBlock().getType().equals(Material.AIR))
                                        || (needAir2.getBlock().getType().equals(Material.TORCH)))
                                && ((targetLocation.getBlock().getType().equals(Material.AIR))
                                        || (targetLocation
                                                .getBlock()
                                                .getType()
                                                .equals(Material.TORCH)))) {
                            atc.teleport(targetLocation);
                        }
                    }
                } else if ((ability.equals("lifesteal"))
                        && (isLegitVictim(atc, playerIsVictim, ability))) {
                    ((LivingEntity) atc)
                            .addPotionEffect(
                                    new PotionEffect(
                                            org.bukkit.potion.PotionEffectType.REGENERATION,
                                            20,
                                            1));
                } else if ((!ability.equals("cloaked"))
                        || (!isLegitVictim(atc, playerIsVictim, ability))) {
                    if ((ability.equals("storm"))
                            && (isLegitVictim(atc, playerIsVictim, ability))) {
                        if (((randomNum <= 2) || (randomNum == 1)) && (!atc.isDead())) {
                            vic.getWorld().strikeLightning(vic.getLocation());
                        }
                    } else if ((!ability.equals("sprint"))
                            || (!isLegitVictim(atc, playerIsVictim, ability))) {
                        if ((ability.equals("webber"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum >= 8) || (randomNum == 1)) {
                                Location feet = vic.getLocation();
                                feet.getBlock().setType(Material.COBWEB);
                                setAir(feet, 60);

                                int rNum = new Random().nextInt(max - min) + min;
                                if ((rNum == 5)
                                        && ((atc.getType().equals(EntityType.SPIDER))
                                                || (atc.getType()
                                                        .equals(EntityType.CAVE_SPIDER)))) {
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
                        } else if ((ability.equals("vengeance"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum >= 5) || (randomNum == 1)) {
                                int amount;
                                if (plugin.getConfig().getString("vengeanceDamage") != null) {
                                    amount = plugin.getConfig().getInt("vengeanceDamage");
                                } else {
                                    amount = 6;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("weakness"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            ((LivingEntity) vic)
                                    .addPotionEffect(
                                            new PotionEffect(
                                                    org.bukkit.potion.PotionEffectType.WEAKNESS,
                                                    500,
                                                    1));
                            ((LivingEntity) vic)
                                    .addPotionEffect(
                                            new PotionEffect(PotionEffectType.WEAKNESS, 500, 1));
                        } else if ((ability.equals("berserk"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum >= 5) && (!atc.isDead())) {
                                double health = ((org.bukkit.entity.Damageable) atc).getHealth();
                                ((org.bukkit.entity.Damageable) atc).setHealth(health - 1.0D);
                                int amount;
                                if (plugin.getConfig().getString("berserkDamage") != null) {
                                    amount = plugin.getConfig().getInt("berserkDamage");
                                } else {
                                    amount = 3;
                                }
                                if ((vic instanceof LivingEntity)) {
                                    ((LivingEntity) vic).damage((int) Math.round(2.0D * amount));
                                }
                            }
                        } else if ((ability.equals("potions"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            ItemStack iStack = new ItemStack(Material.POTION);
                            PotionMeta potion = (PotionMeta) iStack.getItemMeta();
                            switch (randomNum) {
                                case 5:
                                    potion.addCustomEffect(
                                            new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 2),
                                            true);
                                case 6:
                                    potion.addCustomEffect(
                                            new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1),
                                            true);
                                case 7:
                                    potion.addCustomEffect(
                                            new PotionEffect(
                                                    PotionEffectType.WEAKNESS, (20 * 15), 2),
                                            true);
                                case 8:
                                    potion.addCustomEffect(
                                            new PotionEffect(PotionEffectType.POISON, (20 * 5), 2),
                                            true);
                                case 9:
                                    potion.addCustomEffect(
                                            new PotionEffect(
                                                    PotionEffectType.SLOWNESS, (20 * 10), 2),
                                            true);
                            }
                            iStack.setItemMeta(potion);
                            Location sploc = atc.getLocation();
                            sploc.setY(sploc.getY() + 3.0D);
                            ThrownPotion thrownPotion =
                                    (ThrownPotion)
                                            vic.getWorld()
                                                    .spawnEntity(sploc, EntityType.SPLASH_POTION);
                            thrownPotion.setItem(iStack);
                            Vector direction = atc.getLocation().getDirection();
                            direction.normalize();
                            direction.add(new Vector(0.0D, 0.2D, 0.0D));

                            double dist = atc.getLocation().distance(vic.getLocation());

                            dist /= 15.0D;
                            direction.multiply(dist);
                            thrownPotion.setVelocity(direction);
                        } else if ((ability.equals("mama"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if (randomNum == 1) {
                                int amount;
                                if (plugin.getConfig().getString("mamaSpawnAmount") != null) {
                                    amount = plugin.getConfig().getInt("mamaSpawnAmount");
                                } else {
                                    amount = 3;
                                }
                                if (atc.getType().equals(EntityType.MOOSHROOM)) {
                                    for (int i = 0; i < amount; i++) {
                                        MushroomCow minion =
                                                (MushroomCow)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.MOOSHROOM);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.COW)) {
                                    for (int i = 0; i < amount; i++) {
                                        Cow minion =
                                                (Cow)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.COW);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.SHEEP)) {
                                    for (int i = 0; i < amount; i++) {
                                        Sheep minion =
                                                (Sheep)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.SHEEP);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.PIG)) {
                                    for (int i = 0; i < amount; i++) {
                                        Pig minion =
                                                (Pig)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.PIG);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.CHICKEN)) {
                                    for (int i = 0; i < amount; i++) {
                                        Chicken minion =
                                                (Chicken)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.CHICKEN);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.WOLF)) {
                                    for (int i = 0; i < amount; i++) {
                                        Wolf minion =
                                                (Wolf)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.WOLF);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.ZOMBIE)) {
                                    for (int i = 0; i < amount; i++) {
                                        Zombie minion =
                                                (Zombie)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.ZOMBIE);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.PIGLIN)) {
                                    for (int i = 0; i < amount; i++) {
                                        PigZombie minion =
                                                (PigZombie)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.PIGLIN);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.OCELOT)) {
                                    for (int i = 0; i < amount; i++) {
                                        Ocelot minion =
                                                (Ocelot)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.OCELOT);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.HORSE)) {
                                    for (int i = 0; i < amount; i++) {
                                        Horse minion =
                                                (Horse)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.HORSE);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals(EntityType.VILLAGER)) {
                                    for (int i = 0; i < amount; i++) {
                                        Villager minion =
                                                (Villager)
                                                        atc.getWorld()
                                                                .spawnEntity(
                                                                        atc.getLocation(),
                                                                        EntityType.VILLAGER);
                                        minion.setBaby();
                                    }
                                } else {
                                    for (int i = 0; i < amount; i++) {
                                        atc.getWorld()
                                                .spawnEntity(atc.getLocation(), atc.getType());
                                    }
                                }
                            }
                        } else if ((ability.equals("archer"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            if ((randomNum > 7) || (randomNum == 1)) {
                                ArrayList<Arrow> arrowList = new ArrayList<>();
                                Location loc1 = vic.getLocation();
                                Location loc2 = atc.getLocation();
                                if (!plugin.isSmall(atc)) {
                                    loc2.setY(loc2.getY() + 1.0D);
                                }
                                Arrow a = ((LivingEntity) atc).launchProjectile(Arrow.class);
                                int arrowSpeed = 1;
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() + 0.5D);
                                loc2.setZ(loc2.getBlockZ() + 0.5D);
                                Arrow a2 =
                                        a.getWorld()
                                                .spawnArrow(
                                                        loc2,
                                                        new Vector(
                                                                loc1.getX() - loc2.getX(),
                                                                loc1.getY() - loc2.getY(),
                                                                loc1.getZ() - loc2.getZ()),
                                                        arrowSpeed,
                                                        12.0F);
                                a2.setShooter((LivingEntity) atc);
                                loc2.setY(loc2.getBlockY() + 2);
                                loc2.setX(loc2.getBlockX() - 1);
                                loc2.setZ(loc2.getBlockZ() - 1);
                                Arrow a3 =
                                        a.getWorld()
                                                .spawnArrow(
                                                        loc2,
                                                        new Vector(
                                                                loc1.getX() - loc2.getX(),
                                                                loc1.getY() - loc2.getY(),
                                                                loc1.getZ() - loc2.getZ()),
                                                        arrowSpeed,
                                                        12.0F);
                                a3.setShooter((LivingEntity) atc);
                                arrowList.add(a);
                                arrowList.add(a2);
                                arrowList.add(a3);
                                for (Arrow ar : arrowList) {
                                    double minAngle = 6.283185307179586D;
                                    Entity minEntity = null;
                                    for (Entity entity :
                                            atc.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                                        if ((((LivingEntity) atc).hasLineOfSight(entity))
                                                && ((entity instanceof LivingEntity))
                                                && (!entity.isDead())) {
                                            Vector toTarget =
                                                    entity.getLocation()
                                                            .toVector()
                                                            .clone()
                                                            .subtract(atc.getLocation().toVector());
                                            double angle = ar.getVelocity().angle(toTarget);
                                            if (angle < minAngle) {
                                                minAngle = angle;
                                                minEntity = entity;
                                            }
                                        }
                                    }
                                    if (minEntity != null) {
                                        new ArrowHomingTask(ar, (LivingEntity) minEntity, plugin);
                                    }
                                }
                            }
                        } else if ((ability.equals("firework"))
                                && (isLegitVictim(atc, playerIsVictim, ability))) {
                            int red = plugin.getConfig().getInt("fireworkColour.red");
                            int green = plugin.getConfig().getInt("fireworkColour.green");
                            int blue = plugin.getConfig().getInt("fireworkColour.blue");
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
        List<Block> blocks = new LinkedList<>();
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
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(
                        block1.getWorld()
                                .getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(
                            block1.getWorld()
                                    .getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)));
                }
            }
        }
        return blocks;
    }

    private void launchFirework(Location l, Color c, int speed) {
        Firework fw = l.getWorld().spawn(l, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(
                org.bukkit.FireworkEffect.builder()
                        .withColor(c)
                        .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                        .build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(speed));
        detonate(fw);
    }

    private void detonate(final Firework fw) {
        Bukkit.getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(
                        plugin,
                        () -> {
                            try {
                                fw.detonate();
                            } catch (Exception ignored) {
                            }
                        },
                        2L);
    }

    private boolean isLegitVictim(Entity e, boolean playerIsVictim, String ability) {
        if ((e instanceof Player)) {
            return true;
        }
        if (plugin.getConfig().getBoolean("effectAllPlayerAttacks")) {
            return true;
        }
        ArrayList<String> attackAbilityList = new ArrayList<>();
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
        ArrayList<String> defendAbilityList = new ArrayList<>();
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
        Bukkit.getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(
                        plugin,
                        () -> {
                            if (block.getBlock().getType().equals(Material.COBWEB)) {
                                block.getBlock().setType(Material.AIR);
                            }
                        },
                        time * 20);
    }
}
