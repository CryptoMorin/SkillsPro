package org.skills.utils;

import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.skills.services.manager.ServiceHandler;

public class EntityUtil {
    public static boolean isInvalidEntity(Entity entity) {
        EntityType type = entity.getType();
        if (type == EntityType.ARMOR_STAND || type == EntityType.SNOWMAN) return true;
        if (!(entity instanceof LivingEntity)) return true; // projectiles, hanging (items frames, paintings, etc)
        if (entity instanceof NPC || entity.hasMetadata("NPC")) return true;

        if (entity.isDead() || entity.isInvulnerable()) return true;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            GameMode gameMode = player.getGameMode();
            return gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
        }

        return false;
    }

    public static boolean filterEntity(Player src, Entity target) {
        return isInvalidEntity(target) || !ServiceHandler.canFight(src, target) || isPetOf(src, target);
    }

    public static boolean isPetOf(Player player, Entity pet) {
        if (!(pet instanceof Tameable)) return false;
        Tameable tameable = (Tameable) pet;
        return player == tameable.getOwner();
    }
}
