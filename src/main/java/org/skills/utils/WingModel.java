package org.skills.utils;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class WingModel {
    private static final Vector[] outline =
            {
                    new Vector(0, 0, -0.5),
                    new Vector(0.1, 0.01, -0.5),
                    new Vector(0.3, 0.03, -0.5),
                    new Vector(0.4, 0.04, -0.5),
                    new Vector(0.6, 0.1, -0.5),
                    new Vector(0.61, 0.2, -0.5),
                    new Vector(0.62, 0.4, -0.5),
                    new Vector(0.63, 0.6, -0.5),
                    new Vector(0.635, 0.7, -0.5),
                    new Vector(0.7, 0.7, -0.5),
                    new Vector(0.9, 0.75, -0.5),
                    new Vector(1.2, 0.8, -0.5),
                    new Vector(1.4, 0.9, -0.5),
                    new Vector(1.6, 1, -0.5),
                    new Vector(1.8, 1.1, -0.5),
                    new Vector(1.85, 0.9, -0.5),
                    new Vector(1.9, 0.7, -0.5),
                    new Vector(1.85, 0.5, -0.5),
                    new Vector(1.8, 0.3, -0.5),
                    new Vector(1.75, 0.1, -0.5),
                    new Vector(1.7, -0.1, -0.5),
                    new Vector(1.65, -0.3, -0.5),
                    new Vector(1.55, -0.5, -0.5),
                    new Vector(1.45, -0.7, -0.5),
                    new Vector(1.30, -0.75, -0.5),
                    new Vector(1.15, -0.8, -0.5),
                    new Vector(1.0, -0.85, -0.5),
                    new Vector(0.8, -0.87, -0.5),
                    new Vector(0.6, -0.7, -0.5),
                    new Vector(0.5, -0.5, -0.5),
                    new Vector(0.4, -0.3, -0.5),
                    new Vector(0.3, -0.3, -0.5),
                    new Vector(0.15, -0.3, -0.5),
                    new Vector(0, -0.3, -0.5),

                    //
                    new Vector(0.9, 0.55, -0.5),
                    new Vector(1.2, 0.6, -0.5),
                    new Vector(1.4, 0.7, -0.5),
                    new Vector(1.6, 0.9, -0.5),
                    //
                    new Vector(0.9, 0.35, -0.5),
                    new Vector(1.2, 0.4, -0.5),
                    new Vector(1.4, 0.5, -0.5),
                    new Vector(1.6, 0.7, -0.5),
                    //
                    new Vector(0.9, 0.15, -0.5),
                    new Vector(1.2, 0.2, -0.5),
                    new Vector(1.4, 0.3, -0.5),
                    new Vector(1.6, 0.5, -0.5),
                    //
                    new Vector(0.9, -0.05, -0.5),
                    new Vector(1.2, 0, -0.5),
                    new Vector(1.4, 0.1, -0.5),
                    new Vector(1.6, 0.3, -0.5),
                    //
                    new Vector(0.7, -0.25, -0.5),
                    new Vector(1.0, -0.2, -0.5),
                    new Vector(1.2, -0.1, -0.5),
                    new Vector(1.4, 0.1, -0.5),
                    //
                    new Vector(0.7, -0.45, -0.5),
                    new Vector(1.0, -0.4, -0.5),
                    new Vector(1.2, -0.3, -0.5),
                    new Vector(1.4, -0.1, -0.5),
                    //
                    new Vector(1.30, -0.55, -0.5),
                    new Vector(1.15, -0.6, -0.5),
                    new Vector(1.0, -0.65, -0.5)
            };

    private static final Vector[] fill =
            {
                    new Vector(1.2, 0.6, -0.5),
                    new Vector(1.4, 0.7, -0.5),

                    new Vector(1.1, 0.2, -0.5),
                    new Vector(1.3, 0.3, -0.5),

                    new Vector(1.0, -0.2, -0.5),
                    new Vector(1.2, -0.1, -0.5),
            };

    public static Vector rotate(Vector point, float rot) {
        double x = point.getX();
        double y = point.getY();
        double z = point.getZ();

        double cos = Math.cos(rot);
        double sin = Math.sin(rot);

        return new Vector(
                (float) (x * cos + z * sin),
                y,
                (float) (x * -sin + z * cos)
        );
    }

    public static void display(Player player) {
        Location playerLocation = player.getEyeLocation();
        World playerWorld = player.getWorld();
        float x = (float) playerLocation.getX();
        float y = (float) playerLocation.getY() - 0.2f;
        float z = (float) playerLocation.getZ();
        float rot = -playerLocation.getYaw() * 0.017453292F;

        Vector rotated = null;
        for (Vector point : outline) {
            rotated = rotate(point, rot);

            ParticleDisplay.display(new Location(playerWorld, rotated.getX() + x, rotated.getY() + y, rotated.getZ() + z), XParticle.DRAGON_BREATH.get());

            point.setZ(point.getZ() * -1);
            rotated = rotate(point, rot + 3.1415f);
            point.setZ(point.getZ() * -1);

            ParticleDisplay.display(new Location(playerWorld, rotated.getX() + x, rotated.getY() + y, rotated.getZ() + z), XParticle.FLAME.get());
        }

        for (Vector point : fill) {
            rotated = rotate(point, rot);

            ParticleDisplay.display(new Location(playerWorld, rotated.getX() + x, rotated.getY() + y, rotated.getZ() + z), XParticle.SWEEP_ATTACK.get());
//            ParticleEffect.SWEEP_ATTACK.display(3f, 3f, 3f, 1f, 0,

            point.setZ(point.getZ() * -1);
            rotated = rotate(point, rot + 3.1415f);
            point.setZ(point.getZ() * -1);

            ParticleDisplay.display(new Location(playerWorld, rotated.getX() + x, rotated.getY() + y, rotated.getZ() + z), XParticle.SWEEP_ATTACK.get());
        }
    }
}
