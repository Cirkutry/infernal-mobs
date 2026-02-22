package jacob_vejvoda.infernal_mobs;

import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Entity;

public class InfernalMob {
    private boolean infernal;
    public Entity entity;
    UUID id;
    int lives;
    String effect;
    List<String> abilityList;

    public InfernalMob(Entity type, UUID i, boolean in, List<String> l, int li, String e) {
        this.entity = type;
        this.id = i;
        this.infernal = in;
        this.abilityList = l;
        this.lives = li;
        this.effect = e;
    }

    public String toString() {
        return "Name: "
                + this.entity.getType().name()
                + " Infernal: "
                + this.infernal
                + "Abilities:"
                + this.abilityList;
    }

    void setLives(int i) {
        this.lives = i;
    }
}
