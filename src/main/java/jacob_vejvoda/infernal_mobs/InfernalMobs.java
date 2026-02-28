package jacob_vejvoda.infernal_mobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import jacob_vejvoda.infernal_mobs.command.CommandManager;
import jacob_vejvoda.infernal_mobs.infernal.MagicManager;
import jacob_vejvoda.infernal_mobs.listeners.EventListener;
import jacob_vejvoda.infernal_mobs.loot.ConsumeEffectHandler;
import jacob_vejvoda.infernal_mobs.loot.DiviningStaff;
import jacob_vejvoda.infernal_mobs.loot.LootManager;
import jacob_vejvoda.infernal_mobs.loot.PotionEffectHandler;
import jacob_vejvoda.infernal_mobs.utils.LootUtils;

public class InfernalMobs extends JavaPlugin implements Listener {
	GUI gui;
	public long serverTime = 0L;
	public ArrayList<InfernalMob> infernalList = new ArrayList<>();
	private File lootYML = new File(getDataFolder(), "loot.yml");
	public File saveYML = new File(getDataFolder(), "save.yml");
	public FileConfiguration saveFile;
	public FileConfiguration lootFile;
	private HashMap<Entity, Entity> mountList = new HashMap<>();
	public ArrayList<Player> levitateList = new ArrayList<>();
	public ArrayList<Player> fertileList = new ArrayList<>();
	private PotionEffectHandler potionEffectHandler;
	private ConsumeEffectHandler consumeEffectHandler;
	private LootManager lootManager;
	private LootUtils lootUtils;
	private DiviningStaff diviningStaff;
	private final MagicManager magic = new MagicManager(this);

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		File dir = new File(this.getDataFolder().getParentFile().getPath(), this.getName());
		if (!dir.exists())
			dir.mkdir();

		try {
			saveDefaultConfig();
			getLogger().info("config.yml loaded successfully.");
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to load config.yml!", e);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			if (!lootYML.exists()) {
				saveResource("loot.yml", false);
			}

			lootFile = new YamlConfiguration();
			lootFile.load(lootYML);
			getLogger().info("loot.yml loaded successfully.");
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to load loot.yml!", e);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			if (!saveYML.exists()) {
				saveYML.createNewFile();
			}

			saveFile = new YamlConfiguration();
			saveFile.load(saveYML);
			getLogger().info("save.yml loaded successfully.");
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Failed to load save.yml!", e);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		try {
			this.lootManager = new LootManager(this, lootFile);
			this.potionEffectHandler = new PotionEffectHandler(this);
			this.consumeEffectHandler = new ConsumeEffectHandler(this);

			getLogger().info("Configuration files initialized successfully!");
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to initialize configuration managers!", e);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.gui = new GUI(this);
		getServer().getPluginManager().registerEvents(this.gui, this);

		this.diviningStaff = new DiviningStaff(this);

		EventListener events = new EventListener(this);
		getServer().getPluginManager().registerEvents(events, this);

		this.getLogger().log(Level.INFO, "Registered Events.");

		applyEffect();
		loadPowers();
		showEffect();
		diviningStaff.addRecipe();

		try {
			CommandManager commandManager = new CommandManager(this);
			this.getCommand("infernalmobs").setExecutor(commandManager);
			this.getCommand("infernalmobs").setTabCompleter(commandManager);
			this.getCommand("im").setExecutor(commandManager);
			this.getCommand("im").setTabCompleter(commandManager);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to load language configuration!", e);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	private void loadPowers() {
		ArrayList<World> wList = new ArrayList<>();
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
		HashMap<Entity, Entity> tmp = new HashMap<>(mountList);
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

	public void giveMobsPowers(World world) {
		for (Entity ent : world.getEntities()) {
			if (((ent instanceof LivingEntity)) && (this.saveFile.getString(ent.getUniqueId().toString()) != null)) {
				giveMobPowers(ent);
			}
		}
	}

	public void giveMobPowers(Entity ent) {
		UUID id = ent.getUniqueId();
		if (idSearch(id) == -1) {
			List<String> aList = null;
			for (MetadataValue v : ent.getMetadata("infernalMetadata")) {
				aList = new ArrayList<>(Arrays.asList(v.asString().split(",")));
			}
			if (aList == null) {
				if (this.saveFile.getString(ent.getUniqueId().toString()) != null) {
					aList = new ArrayList<>(
							Arrays.asList(this.saveFile.getString(ent.getUniqueId().toString()).split(",")));
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

	public void makeInfernal(final Entity e, final boolean fixed) {
		String entName = e.getType().name();
		if ((!e.hasMetadata("NPC")) && (!e.hasMetadata("shopkeeper"))) {
			if (!fixed) {
				List<String> babyList = getConfig().getStringList("disabledBabyMobs");
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
				if ((!e.isDead()) && (e.isValid()) && (((getConfig().getStringList("enabledMobs").contains(entName1)))
						|| ((fixed) && (idSearch(id) == -1)))) {
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

								spawnMessage = LootUtils.hex(spawnMessage);
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
								this.getLogger().warning("No valid spawn messages found!");
							}
						}
					}
				}
			}, 10L);
		}
	}

	public void addHealth(Entity ent, List<String> powerList) {
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
				this.getLogger().log(Level.WARNING, "addHealth: " + e);
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

	public void removeMob(int mobIndex) throws IOException {
		String id = this.infernalList.get(mobIndex).id.toString();
		this.infernalList.remove(mobIndex);
		this.saveFile.set(id, null);
		try {
			this.saveFile.save(this.saveYML);
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Failed to save mob data!", e);
		}
	}

	public void spawnGhost(Location l) {
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
		} else {
			skull = new ItemStack(Material.SKELETON_SKULL, 1);
		}

		if (chest.getItemMeta() instanceof LeatherArmorMeta meta) {
			meta.setColor(evil ? Color.BLACK : Color.WHITE);
			chest.setItemMeta(meta);
		}
		chest.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, new Random().nextInt(10) + 1);
		ItemMeta m = skull.getItemMeta();
		m.setDisplayName(LootUtils.hex("&fGhost Head"));
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

		ArrayList<String> aList = new ArrayList<>();
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

	private boolean isBaby(Entity mob) {
		if (mob instanceof Ageable) {
			return !((Ageable) mob).isAdult();
		}
		return false;
	}

	public String getEffect() {
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
						if ((mob.getType().equals(EntityType.ENDERMAN))
								|| (mob.getType().equals(EntityType.IRON_GOLEM))) {
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
								((LivingEntity) mob).addPotionEffect(
										new PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 40, 1));
							} else if (ability.equals("armoured")) {
								if ((!(mob instanceof Skeleton)) && (!(mob instanceof Zombie))) {
									((LivingEntity) mob)
											.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));
								}
							} else if (ability.equals("1up")) {
								if (((org.bukkit.entity.Damageable) mob).getHealth() <= 5) {
									InfernalMob oneUpper = infernalList.get(index);
									if (oneUpper.lives > 1) {
										((LivingEntity) mob).setHealth(((LivingEntity) mob)
												.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)
												.getBaseValue());
										oneUpper.setLives(oneUpper.lives - 1);
									}
								}
							} else if (ability.equals("sprint")) {
								((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
							} else if (ability.equals("molten")) {
								((LivingEntity) mob)
										.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1));
							} else if (ability.equals("tosser")) {
								if (randomNum < 6) {
									double radius = 6D;
									ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
									for (Player player : near) {
										if (player.getLocation().distance(mob.getLocation()) <= radius) {
											if ((!player.isSneaking())
													&& (!player.getGameMode().equals(GameMode.CREATIVE))) {
												player.setVelocity(mob.getLocation().toVector()
														.subtract(player.getLocation().toVector()));
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
											if ((!block.getType().equals(Material.AIR))
													&& (!player.getGameMode().equals(GameMode.CREATIVE))) {
												int amount = 6;
												if (getConfig().getString("gravityLevitateLength") != null) {
													amount = getConfig().getInt("gravityLevitateLength");
												}
												magic.levitate(player, amount);
											}
										}
									}
								}
							} else if ((ability.equals("ghastly")) || (ability.equals("necromancer"))) {
								if ((randomNum == 6) && (!mob.isDead())) {
									double radius = 20D;
									ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
									for (Player player : near) {
										if ((player.getLocation().distance(mob.getLocation()) <= radius)
												&& (!player.getGameMode().equals(GameMode.CREATIVE))) {
											Fireball fb = null;
											if (ability.equals("ghastly")) {
												fb = ((LivingEntity) mob).launchProjectile(Fireball.class);
												player.getWorld().playSound(player.getLocation(), Sound.AMBIENT_CAVE, 5,
														1);
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
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> showEffect(), 20);
	}

	public boolean isSmall(Entity mob) {
		return (isBaby(mob)) && (mob.getType().equals(EntityType.BAT)) && (mob.getType().equals(EntityType.CAVE_SPIDER))
				&& (mob.getType().equals(EntityType.CHICKEN)) && (mob.getType().equals(EntityType.COW))
				&& (mob.getType().equals(EntityType.MOOSHROOM)) && (mob.getType().equals(EntityType.PIG))
				&& (mob.getType().equals(EntityType.OCELOT)) && (mob.getType().equals(EntityType.SHEEP))
				&& (mob.getType().equals(EntityType.SILVERFISH)) && (mob.getType().equals(EntityType.SPIDER))
				&& (mob.getType().equals(EntityType.WOLF));
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
			if (getConfig().getStringList("effectWorlds").contains(world.getName())
					|| (getConfig().getStringList("effectWorlds").contains("<all>"))) {
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

	public void doEffect(Player player, final Entity mob, boolean playerIsVictim) {
		if (!playerIsVictim && mob instanceof LivingEntity) {
			ItemStack itemUsed = player.getInventory().getItemInMainHand();
			this.potionEffectHandler.applyAttackPotionEffects(player, (LivingEntity) mob, itemUsed);
		}

		try {
			UUID id = mob.getUniqueId();
			if (idSearch(id) != -1) {
				List<String> abilityList = findMobAbilities(id);
				if ((!player.isDead()) && (!mob.isDead())) {
					for (String ability : abilityList)
						magic.doMagic(player, mob, playerIsVictim, ability, id);
				}
			}
		} catch (Exception e) {
			/** this.getLogger().log(Level.WARNING, "Do Effect Error: ", e); **/
		}
	}

	public List<String> getAbilitiesAmount(Entity e) {
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
		List<String> allAbilitiesList = new ArrayList<>(
				Arrays.asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer",
						"archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber",
						"storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust",
						"bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
		List<String> abilityList = new ArrayList<>();
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

	public Entity getTarget(final Player player) {

		BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(),
				player.getEyeLocation().getDirection(), 0, 100);
		while (iterator.hasNext()) {
			Block item = iterator.next();
			for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
				int acc = 2;
				for (int x = -acc; x < acc; x++)
					for (int z = -acc; z < acc; z++)
						for (int y = -acc; y < acc; y++)
							if (entity.getLocation().getBlock().getRelative(x, y, z).equals(item)) {
								return entity;
							}
			}
		}
		return null;
	}

	public void makeFly(Entity ent) {
		Entity bat = ent.getWorld().spawnEntity(ent.getLocation(), EntityType.BAT);
		bat.setVelocity(new Vector(0, 1, 0));
		bat.addPassenger(ent);
		((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1));
	}

	public void giveMobGear(Entity mob, boolean naturalSpawn) {
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
		if (((mobAbilityList.contains("mounted"))
				&& (getConfig().getStringList("enabledRiders").contains(mob.getType().name())))
				|| ((!naturalSpawn) && (mobAbilityList.contains("mounted")))) {
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
			if (EntityType.valueOf(mount.toUpperCase()) != null
					&& (!EntityType.valueOf(mount.toUpperCase()).equals(EntityType.ENDER_DRAGON))) {
				Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(),
						EntityType.valueOf(mount.toUpperCase()));

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
				this.getLogger().warning("Can't spawn mount!");
				this.getLogger().warning(mount + " is not a valid Entity!");
			}
		}
	}

	public void displayParticle(String effect, Location l, double radius, int speed, int amount) {
		displayParticle(effect, l.getWorld(), l.getX(), l.getY(), l.getZ(), radius, speed, amount);
	}

	public void displayParticle(String effect, World w, double x, double y, double z, double radius, int speed,
			int amount) {
		amount = (amount <= 0) ? 1 : amount;
		Location l = new Location(w, x, y, z);
		try {
			Particle particle = Particle.valueOf(effect);
			if (radius <= 0) {
				w.spawnParticle(particle, l, amount, 0, 0, 0, speed);
			} else {
				List<Location> ll = getArea(l, radius, 0.2);
				if (ll.size() > 0) {
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
		List<Location> ll = new ArrayList<>();
		for (double x = l.getX() - r; x < l.getX() + r; x += t) {
			for (double y = l.getY() - r; y < l.getY() + r; y += t) {
				for (double z = l.getZ() - r; z < l.getZ() + r; z += t) {
					ll.add(new Location(l.getWorld(), x, y, z));
				}
			}
		}
		return ll;
	}

	public String getRandomMob() {
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

	public String generateString(int maxNames, List<String> names) {
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

	public String getLocationName(Location l) {
		return (l.getX() + "." + l.getY() + "." + l.getZ() + l.getWorld().getName()).replace(".", "");
	}

	public Block blockNear(Location l, Material mat, int radius) {
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

	public int rand(int min, int max) {
		return min + (int) (Math.random() * (1 + max - min));
	}

	public void applyEatEffects(LivingEntity e, int effectID) {
		this.consumeEffectHandler.applyConsumeEffects(e, effectID);
	}

	public List<Integer> getIntegerList(String path) {
		return lootFile.getIntegerList(path);
	}

	public void keepAlive(Item item) {
		lootManager.keepAlive(item);
	}

	public ItemStack getDiviningStaff() {
		return diviningStaff.getDiviningStaff();
	}

	public DiviningStaff getDiviningStaffManager() {
		return diviningStaff;
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

	public ItemStack getRandomLoot(Player player, String mob, int powers) {
		return lootManager.getRandomLoot(player, mob, powers);
	}

	public ItemStack getLoot(Player player, int loot) {
		return lootManager.getLoot(player, loot);
	}

	public ItemStack getItem(int loot) {
		return lootManager.getItem(loot);
	}

	public LootUtils getLootUtils() {
		return this.lootUtils;
	}

	public PotionEffectHandler getPotionEffectHandler() {
		return this.potionEffectHandler;
	}

	public ConsumeEffectHandler getConsumeEffectHandler() {
		return this.consumeEffectHandler;
	}

	public ArrayList<InfernalMob> getInfernalList() {
		return this.infernalList;
	}

	public HashMap<Entity, Entity> getMountList() {
		return this.mountList;
	}

	public GUI getGui() {
		return this.gui;
	}

	public FileConfiguration getSaveFile() {
		return this.saveFile;
	}

	public File getSaveYML() {
		return this.saveYML;
	}

	public File getLootYML() {
		return this.lootYML;
	}

	public void setLootFile(FileConfiguration lootFile) {
		this.lootFile = lootFile;
	}

	public void setSaveFile(FileConfiguration saveFile) {
		this.saveFile = saveFile;
	}

	public void setLootUtils(LootUtils lootUtils) {
		this.lootUtils = lootUtils;
	}

	public void setLootManager(LootManager lootManager) {
		this.lootManager = lootManager;
	}

	public void setConsumeEffectHandler(ConsumeEffectHandler handler) {
		this.consumeEffectHandler = handler;
	}

	public void setPotionEffectHandler(PotionEffectHandler handler) {
		this.potionEffectHandler = handler;
	}
}
