package org.skills.utils;

import com.cryptomorin.xseries.XEntityType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.skills.services.manager.ServiceHandler;

public class EntityUtil {
    private static final double MAX_VELOCITY_CHANGE = 4.0D;

    public static boolean isInvalidEntity(Entity entity) {
        EntityType type = entity.getType();
        if (type == EntityType.ARMOR_STAND || type == XEntityType.SNOW_GOLEM.get()) return true;
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

    public static void knockBack(Entity entity, Location from, double intensity) {
        entity.setVelocity(entity.getLocation().toVector().subtract(from.toVector()).multiply(intensity));
    }

    public static void knockBack(Entity entity, Vector direction, double intensity) {
        entity.setVelocity(direction.multiply(intensity));
    }

    public static Vector validateExcessiveVelocity(Vector vel) {
        double x = vel.getX();
        double y = vel.getY();
        double z = vel.getZ();

        if (x > MAX_VELOCITY_CHANGE) vel.setX(MAX_VELOCITY_CHANGE);
        if (x < -MAX_VELOCITY_CHANGE) vel.setX(-MAX_VELOCITY_CHANGE);

        if (y > MAX_VELOCITY_CHANGE) vel.setY(MAX_VELOCITY_CHANGE);
        if (y < -MAX_VELOCITY_CHANGE) vel.setY(-MAX_VELOCITY_CHANGE);

        if (z > MAX_VELOCITY_CHANGE) vel.setZ(MAX_VELOCITY_CHANGE);
        if (z < -MAX_VELOCITY_CHANGE) vel.setZ(-MAX_VELOCITY_CHANGE);

        return vel;
    }
}
