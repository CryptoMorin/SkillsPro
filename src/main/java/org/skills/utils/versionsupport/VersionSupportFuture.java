package org.skills.utils.versionsupport;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class VersionSupportFuture {
    public static void spawnColouredDust(Location loc) {
        spawnColouredDust(loc, Color.AQUA);
    }

    public static void spawnColouredDust(Location loc, Color color) {
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(color, 2));
    }

    public static boolean isPassenger(Entity e, Entity pass) {
        return e.getPassengers().contains(pass);
    }

    public static double getMaxHealth(LivingEntity e) {
        return e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    public static void setMaxHealth(LivingEntity e, double amount) {
        if (getMaxHealth(e) != amount) e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(amount);
    }

    public static boolean isCropFullyGrown(Block crop) {
        BlockData bdata = crop.getBlockData();
        if (bdata instanceof Ageable) {
            Ageable age = (Ageable) bdata;
            return age.getAge() == age.getMaximumAge();
        }
        return false;
    }
}

