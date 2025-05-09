package jacob_vejvoda.infernal_mobs;

import jacob_vejvoda.infernal_mobs.ConsumeEffectHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

public class GUI implements Listener {
    private static infernal_mobs plugin;
    private static HashMap<String, Scoreboard> playerScoreBoard = new HashMap<String, Scoreboard>();
    private static HashMap<Entity, Object> bossBars = new HashMap<Entity, Object>();
    public static ConfigManager configManager;

    GUI(infernal_mobs instance) {
        plugin = instance;
        configManager = instance.getConfigManager();
    }
    
    public static Entity getNearbyBoss(Player p) {
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

    static void fixBar(Player p) {
        Entity b = getNearbyBoss(p);
        if (b != null) {
            if (b.isDead() || ((Damageable) b).getHealth() <= 0) {
                if (configManager.getBoolean("enableBossBar")) {
                    try {
                        for (Player p2 : ((BossBar) bossBars.get(b)).getPlayers())
                            ((BossBar) bossBars.get(b)).removePlayer(p2);
                        bossBars.remove(b);
                    } catch (Exception x) {
                    }
                }
                int mobIndex = plugin.idSearch(b.getUniqueId());
                try {
                    if (mobIndex != -1)
                        plugin.removeMob(mobIndex);
                } catch (IOException e) {
                }
                clearInfo(p);
            } else {
                if (configManager.getBoolean("enableBossBar")) {
                    showBossBar(p, b);
                }
                if (configManager.getBoolean("enableScoreBoard")) {
                    fixScoreboard(p, b, plugin.findMobAbilities(b.getUniqueId()));
                }
            }
        } else
            clearInfo(p);
    }


    private static void showBossBar(Player p, Entity e) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(e.getUniqueId());
        String title = configManager.getString("bossBarsName", "&fLevel <powers> &fInfernal <mobName>");
        String mobName = e.getType().getName().replace("_", " ");
        if (e.getType().equals(EntityType.SKELETON)) {
            Skeleton sk = (Skeleton) e;
        }
        if (e.getType().equals(EntityType.WITHER_SKELETON)) {
            mobName = "WitherSkeleton";
        }
        String prefix = configManager.getString("namePrefix", "&fInfernal");
        if (configManager.contains("levelPrefixes." + oldMobAbilityList.size())) {
            prefix = configManager.getString("levelPrefixes." + oldMobAbilityList.size());
        }
        prefix = ConsumeEffectHandler.hex(prefix);
        title = title.replace("<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
        title = title.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
        title = title.replace("<mobLevel>", oldMobAbilityList.size() + "");
        String abilities = plugin.generateString(5, oldMobAbilityList);
        int count = 4;
        try {
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
                if (count <= 0) {
                    break;
                }
            } while (title.length() + abilities.length() + mobName.length() > 64);
        } catch (Exception x) {
            System.out.println("showBossBar error: ");
            x.printStackTrace();
        }
        title = title.replace("<abilities>", abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
        title = ConsumeEffectHandler.hex(title);

        if (!bossBars.containsKey(e)) {
            BarColor bc = BarColor.valueOf(configManager.getString("bossBarSettings.defaultColor"));
            BarStyle bs = BarStyle.valueOf(configManager.getString("bossBarSettings.defaultStyle"));

            String lc = configManager.getString("bossBarSettings.perLevel." + oldMobAbilityList.size() + ".color");
            if (lc != null)
                bc = BarColor.valueOf(lc);
            String ls = configManager.getString("bossBarSettings.perLevel." + oldMobAbilityList.size() + ".style");
            if (ls != null)
                bs = BarStyle.valueOf(ls);

            String mc = configManager.getString("bossBarSettings.perMob." + e.getType().getName() + ".color");
            if (mc != null)
                bc = BarColor.valueOf(mc);
            String ms = configManager.getString("bossBarSettings.perMob." + e.getType().getName() + ".style");
            if (ms != null)
                bs = BarStyle.valueOf(ms);
            BossBar bar = Bukkit.createBossBar(title, bc, bs, BarFlag.CREATE_FOG);
            bar.setVisible(true);
            bossBars.put(e, bar);
        }
        if (!((BossBar) bossBars.get(e)).getPlayers().contains(p))
            ((BossBar) bossBars.get(e)).addPlayer(p);
        float health = (float) ((Damageable) e).getHealth();
        float maxHealth = (float) ((Damageable) e).getMaxHealth();
        float setHealth = (health * 100.0f) / maxHealth;
        ((BossBar) bossBars.get(e)).setProgress(setHealth / 100.0f);
    }


    private static void clearInfo(Player player) {
        if (configManager.getBoolean("enableBossBar")) {
            for (Entry<Entity, Object> hm : bossBars.entrySet())
                if (((BossBar) hm.getValue()).getPlayers().contains(player))
                    ((BossBar) hm.getValue()).removePlayer(player);
        }
        if (configManager.getBoolean("enableScoreBoard")) {
            try {
                player.getScoreboard().resetScores(player);
                player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
            } catch (Exception localException1) {
            }
        }
    }


    private static void fixScoreboard(Player player, Entity e, List<String> abilityList) {
        if (configManager.getBoolean("enableScoreBoard") && (e instanceof Damageable)) {
            if (playerScoreBoard.get(player.getName()) == null) {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getNewScoreboard();
                playerScoreBoard.put(player.getName(), board);
            }
            Objective o;
            Scoreboard board = playerScoreBoard.get(player.getName());
            if (board.getObjective(DisplaySlot.SIDEBAR) == null) {
                o = board.registerNewObjective(player.getName(), "dummy", player.getName());
                o.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                o = board.getObjective(DisplaySlot.SIDEBAR);
            }
            
            String title = configManager.getString("scoreboardTitle", e.getType().getName());
            title = processMobPlaceholders(title, e, abilityList);
            o.setDisplayName(title);
            
            for (String s : board.getEntries())
                board.resetScores(s);
            
            int score = 1;
            
            List<String> scoreboardLines = configManager.getStringList("scoreboard");
            if (scoreboardLines != null && !scoreboardLines.isEmpty()) {
                for (String line : scoreboardLines) {
                    String processedLine = processMobPlaceholders(line, e, abilityList);
                    o.getScore(processedLine).setScore(score);
                    score++;
                }
            } else {
                for (String ability : abilityList) {
                    o.getScore("§r" + ability).setScore(score);
                    score = score + 1;
                }
                o.getScore("§e§lAbilities:").setScore(score);
                if (configManager.getBoolean("showHealthOnScoreBoard")) {
                    score = score + 1;
                    float health = (float) ((Damageable) e).getHealth();
                    float maxHealth = (float) ((Damageable) e).getMaxHealth();
                    double roundOff = Math.round(health * 100.0) / 100.0;

                    o.getScore(roundOff + "/" + maxHealth).setScore(score);
                    score = score + 1;

                    o.getScore("§e§lHealth:").setScore(score);
                }
            }

            if ((player.getScoreboard() == null) || (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null) || (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName() == null) || (!player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(board.getObjective(DisplaySlot.SIDEBAR).getName()))) {
                player.setScoreboard(board);
            }
        }
    }
    
    private static String processMobPlaceholders(String text, Entity entity, List<String> abilityList) {
        if (text == null) return "";
        
        String mobName = entity.getType().getName().replace("_", " ");
        String abilities = plugin.generateString(5, abilityList);
        
        String prefix = configManager.getString("namePrefix", "&fInfernal");
        if (configManager.contains("levelPrefixes." + abilityList.size())) {
            prefix = configManager.getString("levelPrefixes." + abilityList.size());
        }
        
        float health = (float) ((Damageable) entity).getHealth();
        float maxHealth = (float) ((Damageable) entity).getMaxHealth();
        double roundHealth = Math.round(health * 100.0) / 100.0;
        
        text = text.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
        text = text.replace("<mobLevel>", String.valueOf(abilityList.size()));
        text = text.replace("<abilities>", abilities);
        text = text.replace("<prefix>", prefix);
        text = text.replace("<health>", String.valueOf(roundHealth));
        text = text.replace("<maxHealth>", String.valueOf(maxHealth));
        
        return ConsumeEffectHandler.hex(text);
    }

    public void setName(Entity ent) {
        try {
            if (configManager.getInt("nameTagsLevel") != 0) {
                String title = getMobNameTag(ent);
                ent.setCustomName(title);
                if (configManager.getInt("nameTagsLevel") == 2) {
                    ent.setCustomNameVisible(true);
                }
            }
        } catch (Exception x) {
            System.out.println("Error in setName: ");
            x.printStackTrace();
        }
    }


    public String getMobNameTag(Entity entity) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(entity.getUniqueId());
        String title = null;
        try {
            title = configManager.getString("nameTagsName", "&fInfernal <mobName>");
            String mobName = entity.getType().getName().replace("_", " ");

            title = title.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
            title = title.replace("<mobLevel>", "" + oldMobAbilityList.size());
            String abilities;
            int count = 4;
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
            } while ((title.length() + abilities.length() + mobName.length()) > 64);
            title = title.replace("<abilities>", abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
            String prefix = configManager.getString("namePrefix");
            if (configManager.contains("levelPrefixes." + oldMobAbilityList.size()))
                prefix = configManager.getString("levelPrefixes." + oldMobAbilityList.size());
            prefix = ConsumeEffectHandler.hex(prefix);
            title = title.replace("<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
            title = ConsumeEffectHandler.hex(title);
        } catch (Exception x) {
            plugin.getLogger().log(Level.SEVERE, x.getMessage());
            x.printStackTrace();
        }
        return title;
    }
}