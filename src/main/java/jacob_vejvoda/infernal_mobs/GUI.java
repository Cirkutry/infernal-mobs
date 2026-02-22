package jacob_vejvoda.infernal_mobs;

import jacob_vejvoda.infernal_mobs.utils.LootUtils;
import jacob_vejvoda.infernal_mobs.utils.MobUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class GUI implements Listener {
    private static InfernalMobs plugin;
    private static HashMap<String, Scoreboard> playerScoreBoard = new HashMap<String, Scoreboard>();
    private static final Map<Entity, BossBar> bossBars = new HashMap<>();

    public GUI(InfernalMobs instance) {
        plugin = instance;
    }

    public static void fixBar(Player p) {
        Entity b = MobUtils.getNearbyBoss(p, plugin);
        if (b != null) {
            if (b.isDead() || ((Damageable) b).getHealth() <= 0) {
                if (plugin.getConfig().getBoolean("enableBossBar")) {
                    try {
                        for (Player p2 : ((BossBar) bossBars.get(b)).getPlayers())
                            ((BossBar) bossBars.get(b)).removePlayer(p2);
                        bossBars.remove(b);
                    } catch (Exception x) {
                    }
                }
                int mobIndex = plugin.idSearch(b.getUniqueId());
                try {
                    if (mobIndex != -1) plugin.removeMob(mobIndex);
                } catch (IOException e) {
                }
                clearInfo(p);
            } else {
                if (plugin.getConfig().getBoolean("enableBossBar")) {
                    showBossBar(p, b);
                }
                if (plugin.getConfig().getBoolean("enableScoreBoard")) {
                    fixScoreboard(p, b, plugin.findMobAbilities(b.getUniqueId()));
                }
            }
        } else clearInfo(p);
    }

    private static void showBossBar(Player p, Entity e) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(e.getUniqueId());

        String title =
                plugin.getConfig()
                        .getString("bossBarsName", "&fLevel <mobLevel> &fInfernal <mobName>");

        // Properly format mob name
        String mobName =
                Arrays.stream(e.getType().name().split("_"))
                        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));

        // Prefix handling
        String prefix = plugin.getConfig().getString("namePrefix", "&fInfernal");

        if (plugin.getConfig().contains("levelPrefixes." + oldMobAbilityList.size())) {
            prefix = plugin.getConfig().getString("levelPrefixes." + oldMobAbilityList.size());
        }

        prefix = LootUtils.hex(prefix);

        title = title.replace("<prefix>", prefix);
        title = title.replace("<mobName>", mobName);
        title = title.replace("<mobLevel>", String.valueOf(oldMobAbilityList.size()));

        String abilities = plugin.generateString(5, oldMobAbilityList);

        int count = 4;
        try {
            while (title.length() + abilities.length() > 64 && count > 0) {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
            }
        } catch (Exception x) {
            plugin.getLogger().log(Level.WARNING, "showBossBar error: ", x);
        }

        if (!abilities.isEmpty()) {
            abilities = abilities.substring(0, 1).toUpperCase() + abilities.substring(1);
        }

        title = title.replace("<abilities>", abilities);
        title = LootUtils.hex(title);

        if (!bossBars.containsKey(e)) {

            BarColor bc = BarColor.WHITE;
            BarStyle bs = BarStyle.SOLID;

            try {
                bc =
                        BarColor.valueOf(
                                plugin.getConfig()
                                        .getString("bossBarSettings.defaultColor", "WHITE"));
            } catch (IllegalArgumentException ignored) {
            }

            try {
                bs =
                        BarStyle.valueOf(
                                plugin.getConfig()
                                        .getString("bossBarSettings.defaultStyle", "SOLID"));
            } catch (IllegalArgumentException ignored) {
            }

            // Per-level override
            String levelPath = "bossBarSettings.perLevel." + oldMobAbilityList.size();

            if (plugin.getConfig().contains(levelPath + ".color")) {
                try {
                    bc = BarColor.valueOf(plugin.getConfig().getString(levelPath + ".color"));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (plugin.getConfig().contains(levelPath + ".style")) {
                try {
                    bs = BarStyle.valueOf(plugin.getConfig().getString(levelPath + ".style"));
                } catch (IllegalArgumentException ignored) {
                }
            }

            // Per-mob override
            String mobPath = "bossBarSettings.perMob." + e.getType().name();

            if (plugin.getConfig().contains(mobPath + ".color")) {
                try {
                    bc = BarColor.valueOf(plugin.getConfig().getString(mobPath + ".color"));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (plugin.getConfig().contains(mobPath + ".style")) {
                try {
                    bs = BarStyle.valueOf(plugin.getConfig().getString(mobPath + ".style"));
                } catch (IllegalArgumentException ignored) {
                }
            }

            BossBar bar = Bukkit.createBossBar(title, bc, bs, BarFlag.CREATE_FOG);
            bar.setVisible(true);
            bossBars.put(e, bar);
        }

        BossBar bar = bossBars.get(e);

        if (!bar.getPlayers().contains(p)) {
            bar.addPlayer(p);
        }

        if (e instanceof LivingEntity living) {
            double health = living.getHealth();

            AttributeInstance attribute = living.getAttribute(Attribute.MAX_HEALTH);
            if (attribute != null) {
                double maxHealth = attribute.getValue();

                if (maxHealth > 0) {
                    bar.setProgress(Math.max(0.0, Math.min(1.0, health / maxHealth)));
                }
            }
        }
    }

    private static void clearInfo(Player player) {
        if (plugin.getConfig().getBoolean("enableBossBar")) {
            for (BossBar bar : bossBars.values()) {
                if (bar.getPlayers().contains(player)) {
                    bar.removePlayer(player);
                }
            }
        }
        if (plugin.getConfig().getBoolean("enableScoreBoard")) {
            try {
                player.getScoreboard().resetScores(player);
                player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
            } catch (Exception localException1) {
            }
        }
    }

    private static void fixScoreboard(Player player, Entity e, List<String> abilityList) {
        if (plugin.getConfig().getBoolean("enableScoreBoard") && (e instanceof Damageable)) {
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

            String title = plugin.getConfig().getString("scoreboardTitle", e.getType().getName());
            title = processMobPlaceholders(title, e, abilityList);
            o.setDisplayName(title);

            for (String s : board.getEntries()) board.resetScores(s);

            int score = 1;

            List<String> scoreboardLines = plugin.getConfig().getStringList("scoreboard");
            if (scoreboardLines != null && !scoreboardLines.isEmpty()) {
                for (String line : scoreboardLines) {
                    String processedLine = processMobPlaceholders(line, e, abilityList);
                    o.getScore(processedLine).setScore(score);
                    score++;
                }
            } else {
                for (String ability : abilityList) {
                    o.getScore(LootUtils.hex("&r" + ability)).setScore(score);
                    score = score + 1;
                }
                o.getScore(LootUtils.hex("&e&lAbilities:")).setScore(score);
                if (plugin.getConfig().getBoolean("showHealthOnScoreBoard")) {
                    score = score + 1;
                    float health = (float) ((Damageable) e).getHealth();
                    float maxHealth = (float) ((Damageable) e).getMaxHealth();
                    double roundOff = Math.round(health * 100.0) / 100.0;

                    o.getScore(roundOff + "/" + maxHealth).setScore(score);
                    score = score + 1;

                    o.getScore(LootUtils.hex("&e&lHealth:")).setScore(score);
                }
            }

            if ((player.getScoreboard() == null)
                    || (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null)
                    || (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName() == null)
                    || (!player.getScoreboard()
                            .getObjective(DisplaySlot.SIDEBAR)
                            .getName()
                            .equals(board.getObjective(DisplaySlot.SIDEBAR).getName()))) {
                player.setScoreboard(board);
            }
        }
    }

    private static String processMobPlaceholders(
            String text, Entity entity, List<String> abilityList) {
        if (text == null) return "";

        String mobName = entity.getType().getName().replace("_", " ");
        String abilities = plugin.generateString(5, abilityList);

        String prefix = plugin.getConfig().getString("namePrefix", "&fInfernal");
        if (plugin.getConfig().contains("levelPrefixes." + abilityList.size())) {
            prefix = plugin.getConfig().getString("levelPrefixes." + abilityList.size());
        }

        float health = (float) ((Damageable) entity).getHealth();
        float maxHealth = (float) ((Damageable) entity).getMaxHealth();
        double roundHealth = Math.round(health * 100.0) / 100.0;

        text =
                text.replace(
                        "<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
        text = text.replace("<mobLevel>", String.valueOf(abilityList.size()));
        text = text.replace("<abilities>", abilities);
        text = text.replace("<prefix>", prefix);
        text = text.replace("<health>", String.valueOf(roundHealth));
        text = text.replace("<maxHealth>", String.valueOf(maxHealth));

        return LootUtils.hex(text);
    }

    public void setName(Entity ent) {
        try {
            if (plugin.getConfig().getInt("nameTagsLevel") != 0) {
                String title = getMobNameTag(ent);
                ent.setCustomName(title);
                if (plugin.getConfig().getInt("nameTagsLevel") == 2) {
                    ent.setCustomNameVisible(true);
                }
            }
        } catch (Exception x) {
            plugin.getLogger().log(Level.WARNING, "Error in setName: ", x);
        }
    }

    public String getMobNameTag(Entity entity) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(entity.getUniqueId());
        String title = null;
        try {
            title = plugin.getConfig().getString("nameTagsName", "&fInfernal <mobName>");
            String mobName = entity.getType().getName().replace("_", " ");

            title =
                    title.replace(
                            "<mobName>",
                            mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
            title = title.replace("<mobLevel>", "" + oldMobAbilityList.size());
            String abilities;
            int count = 4;
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
            } while ((title.length() + abilities.length() + mobName.length()) > 64);
            title =
                    title.replace(
                            "<abilities>",
                            abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
            String prefix = plugin.getConfig().getString("namePrefix");
            if (plugin.getConfig().contains("levelPrefixes." + oldMobAbilityList.size()))
                prefix = plugin.getConfig().getString("levelPrefixes." + oldMobAbilityList.size());
            prefix = LootUtils.hex(prefix);
            title =
                    title.replace(
                            "<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
            title = LootUtils.hex(title);
        } catch (Exception x) {
            plugin.getLogger().log(Level.SEVERE, x.getMessage());
            x.printStackTrace();
        }
        return title;
    }
}
