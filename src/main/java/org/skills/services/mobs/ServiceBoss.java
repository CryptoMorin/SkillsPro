package org.skills.services.mobs;

import org.bukkit.entity.LivingEntity;
import org.mineacademy.boss.api.Boss;
import org.mineacademy.boss.api.BossAPI;
import org.skills.utils.Pair;

public class ServiceBoss {
    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        Boss boss = BossAPI.getBoss(entity);
        return boss == null ? null : Pair.of(boss.getName(), 0);
    }
}
