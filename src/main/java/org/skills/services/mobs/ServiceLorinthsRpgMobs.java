package org.skills.services.mobs;

import me.lorinth.rpgmobs.LorinthsRpgMobs;
import org.bukkit.entity.LivingEntity;
import org.skills.utils.Pair;

public class ServiceLorinthsRpgMobs {
    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        // LorinthsRpgMobs.IsMythicMob(entity)
        Integer lvl = LorinthsRpgMobs.GetLevelOfEntity(entity);
        return lvl == null ? null : Pair.of("LRM", lvl);
    }
}
