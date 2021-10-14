package org.skills.services.mobs;

import me.lokka30.levelledmobs.LevelledMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.skills.utils.Pair;

public class ServiceLevelledMobs {
    private static final LevelledMobs LEVELLED_MOBS = (LevelledMobs) Bukkit.getPluginManager().getPlugin("LevelledMobs");

    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        if (!LEVELLED_MOBS.levelInterface.isLevelled(entity)) return null;
        return Pair.of(null, LEVELLED_MOBS.levelInterface.getLevelOfMob(entity));
    }
}
