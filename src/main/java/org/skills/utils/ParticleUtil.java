package org.skills.utils;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ParticleUtil {
    public static final ParticleDisplay SPAWN_DESPAWN_PARTICLE = ParticleDisplay.of(Particle.CLOUD).withCount(50).offset(1, 1, 1);

    private static final Vector UP = new Vector(0, 1, 0);
    private static final double UP_ANGLE = Math.PI / 2;

    public static final Map<String, double[]> VALUES = new HashMap<>();

    public static boolean hasValue(String ns) {
        return VALUES.containsKey(ns);
    }

    public static double[] getValues(String ns) {
        return VALUES.get(ns);
    }

    public static final class Listen implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onChat(AsyncPlayerChatEvent event) {
            String msg = event.getMessage().toLowerCase(Locale.ENGLISH).replace(" ", "");
            String[] split = msg.split(",");
            String ns = split[0];

            if (ns.startsWith("-")) {
                if (VALUES.remove(ns.substring(1)) != null) event.getPlayer().sendMessage("removed " + ns.substring(1));
                return;
            }

            VALUES.put(ns, Arrays.stream(split).skip(1).mapToDouble(x -> {
                boolean toRad = false;
                if (x.startsWith("r")) {
                    toRad = true;
                    x = x.substring(1);
                }

                double d = Double.parseDouble(x);
                if (toRad) d = Math.toRadians(d);
                return d;
            }).toArray());
        }
    }

    public static Vector getPerpendicularVector(Vector vector) {
        // https://gamedev.stackexchange.com/questions/120980/get-perpendicular-vector-from-another-vector/120982#120982
        // Generate a uniformly-distributed unit vector in the XY plane.
        Vector inPlane = new Vector(Math.cos(UP_ANGLE), Math.sin(UP_ANGLE), 0f);

        // Rotate the vector into the plane perpendicular to v and return it.
        // https://github.com/Unity-Technologies/UnityCsReference/blob/7c95a72366b5ed9b6d9e804de8b5e869c962f5a9/Runtime/Export/Math/Quaternion.cs#L86-L94
        return INTERNAL_CALL_LookRotation(vector, UP).mul(inPlane);
    }

    public static ParticleDisplay.Quaternion INTERNAL_CALL_LookRotation(Vector forward, Vector up) {
        // https://github.com/Unity-Technologies/UnityCsReference/blob/7c95a72366b5ed9b6d9e804de8b5e869c962f5a9/Runtime/Export/Math/Math.bindings.cs#L98-L99
        // Also: public Quaternionf lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ, Quaternionf dest)
        // https://gist.github.com/HelloKitty/91b7af87aac6796c3da9

        forward = forward.normalize();
        up = up.clone();
        Vector right = up.getCrossProduct(forward).normalize();
        up = forward.getCrossProduct(right);
        double m00 = right.getX();
        double m01 = right.getY();
        double m02 = right.getZ();
        double m10 = up.getX();
        double m11 = up.getY();
        double m12 = up.getZ();
        double m20 = forward.getX();
        double m21 = forward.getY();
        double m22 = forward.getZ();


        double num8 = (m00 + m11) + m22;
        if (num8 > 0.0) {
            double num = Math.sqrt(num8 + 1.0);
            double w = num * 0.5f;
            num = 0.5f / num;
            double x = (m12 - m21) * num;
            double y = (m20 - m02) * num;
            double z = (m01 - m10) * num;
            return new ParticleDisplay.Quaternion(w, x, y, z);
        }
        if ((m00 >= m11) && (m00 >= m22)) {
            double num7 = Math.sqrt(((1.0 + m00) - m11) - m22);
            double num4 = 0.5 / num7;
            double x = 0.5 * num7;
            double y = (m01 + m10) * num4;
            double z = (m02 + m20) * num4;
            double w = (m12 - m21) * num4;
            return new ParticleDisplay.Quaternion(w, x, y, z);
        }
        if (m11 > m22) {
            double num6 = Math.sqrt(((1.0 + m11) - m00) - m22);
            double num3 = 0.5 / num6;
            double x = (m10 + m01) * num3;
            double y = 0.5 * num6;
            double z = (m21 + m12) * num3;
            double w = (m20 - m02) * num3;
            return new ParticleDisplay.Quaternion(w, x, y, z);
        }
        double num5 = Math.sqrt(((1.0 + m22) - m00) - m11);
        double num2 = 0.5 / num5;
        double x = (m20 + m02) * num2;
        double y = (m21 + m12) * num2;
        double z = 0.5 * num5;
        double w = (m01 - m10) * num2;
        return new ParticleDisplay.Quaternion(w, x, y, z);
    }

    public static void cloudParticle(Location location) {
        SPAWN_DESPAWN_PARTICLE.spawn(location);
    }
}
