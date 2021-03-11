package org.skills.utils.versionsupport;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@SuppressWarnings("deprecation")
public class VersionSupportOld {
    public static void spawnColouredDust(Location loc) {
        spawnColouredDust(loc, 0);
    }

    public static void spawnColouredDust(Location loc, int rgb) {
        loc.getWorld().playEffect(loc, Effect.valueOf("COLOURED_DUST"), rgb);
    }

    public static boolean isPassenger(Entity e, Entity pass) {
        if (e == null) return false;
        if (pass == null) return false;
        if (e.getPassenger() == null) return false;
        return e.getPassenger().equals(pass);
    }

    public static double getMaxHealth(LivingEntity e) {
        return e.getMaxHealth();
    }

    public static void setMaxHealth(LivingEntity entity, double amount) {
        if (entity.getMaxHealth() != amount) entity.setMaxHealth(amount);
    }

    public static boolean isCropFullyGrown(Block crop) {
        if (crop.getData() < 7) return false;
        Material type = crop.getType();
        // New materials from 1.13+ are not added.
        return type == XMaterial.WHEAT_SEEDS.parseMaterial() ||
                type == XMaterial.NETHER_WART.parseMaterial() ||
                type == XMaterial.CARROTS.parseMaterial() ||
                type == XMaterial.POTATOES.parseMaterial() ||
                type == XMaterial.BEETROOT.parseMaterial();
    }
}
