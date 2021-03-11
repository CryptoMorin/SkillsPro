package org.skills.services;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ServiceMyPet implements Listener {
    public static boolean isMyPet(Entity entity) {
        return entity instanceof MyPetBukkitEntity;
    }

    public static Player getPetOwner(Entity entity) {
        return ((MyPetBukkitEntity) entity).getMyPet().getOwner().getPlayer();
    }
}
