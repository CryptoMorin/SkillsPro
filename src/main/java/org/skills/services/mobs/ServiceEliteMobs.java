package org.skills.services.mobs;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.LivingEntity;
import org.skills.utils.Pair;

public class ServiceEliteMobs {
    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        EliteEntity mob = EntityTracker.getEliteMobEntity(entity);
        return mob == null ? null : Pair.of(mob.getName(), mob.getLevel());
    }
}
