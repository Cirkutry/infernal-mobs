package jacob_vejvoda.infernal_mobs.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;

public class CommandUtils {

	public static final List<String> ALL_ABILITIES = Arrays.asList("confusing", "ghost", "morph", "mounted", "flying",
			"gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk",
			"weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up",
			"sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured",
			"poisonous");

	public static List<String> getSpawnableEntities() {
		return Arrays.stream(EntityType.values()).filter(type -> type.isSpawnable() && type.isAlive()).map(Enum::name)
				.collect(Collectors.toList());
	}

	public static List<String> getOnlinePlayerNames() {
		return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
	}

	public static List<String> getWorldNames() {
		return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
	}

	public static List<String> filterStartsWith(List<String> list, String partial) {
		return list.stream().filter(item -> item.toLowerCase().startsWith(partial.toLowerCase()))
				.collect(Collectors.toList());
	}
}
