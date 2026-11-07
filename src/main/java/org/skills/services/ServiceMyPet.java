package org.skills.services;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.MyPetGlobal;
import de.Keyle.MyPet.api.entity.Pet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ServiceMyPet implements Listener {
    public static boolean isMyPet(Entity entity) {
        // Previously (entity instanceof MyPetBukkitEntity) in v3
        return getPet(entity) != null;
    }

    public static Player getPetOwner(Entity entity) {
        Pet pet = getPet(entity);
        if (pet == null) return null;
        return pet.getOwner().getPlayer();
    }

    private static Pet getPet(Entity entity) {
        return MyPetApi.getPetManager().getPetFromEntity(entity);
    }
}
