package org.skills.utils;

import com.cryptomorin.xseries.XPotion;
import org.bukkit.Location;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public final class BeeUtils {
    public static final boolean SUPPORTS_BEES = Reflect.classExists("org.bukkit.entity.Bee");

    public static LivingEntity spawn(Location loc, LivingEntity target) {
        Bee bee = (Bee) loc.getWorld().spawnEntity(loc, EntityType.BEE);
        bee.setCannotEnterHiveTicks(Integer.MAX_VALUE);
        bee.setHive(null);
        bee.setFlower(null);
        bee.setHasNectar(false);
        bee.setHasStung(false);
        bee.setAnger(Integer.MAX_VALUE);
        bee.setTarget(target);
        bee.addPotionEffect(XPotion.SPEED.buildPotionEffect(1000000, 6));
        return bee;
    }

    public static void setHasStung(Entity entity, boolean enabled) {
        ((Bee) entity).setHasStung(enabled);
    }

    public static boolean isBee(Entity entity) {
        return SUPPORTS_BEES && entity instanceof Bee;
    }
}
