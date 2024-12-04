package org.skills.services;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.kingdoms.managers.entity.KingdomEntityRegistry;
import org.kingdoms.managers.pvp.PvPManager;

public class ServiceKingdoms {
    public static boolean canFight(Player player, Player other) {
        return PvPManager.canFight(player, other);
    }

    public static boolean isKingdomMob(LivingEntity entity) {
        return KingdomEntityRegistry.isKingdomEntity(entity);
    }
}
