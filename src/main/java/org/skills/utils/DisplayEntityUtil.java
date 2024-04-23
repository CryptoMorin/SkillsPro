package org.skills.utils;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Map;

public final class DisplayEntityUtil {
    public static Entity spawnDisplay(Player player, Location spawnLoc) {
        Location noPitchLoc = spawnLoc.clone();
        noPitchLoc.setPitch(0);
        noPitchLoc.setYaw(0);

        ItemDisplay itemDisplay = (ItemDisplay) spawnLoc.getWorld().spawnEntity(noPitchLoc, EntityType.ITEM_DISPLAY);
        itemDisplay.setItemStack(XMaterial.DIAMOND_SWORD.parseItem());
        // itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
        // itemDisplay.setBillboard(Display.Billboard.CENTER); Automatic rotation
        itemDisplay.setBrightness(new Display.Brightness(10, 10));
        itemDisplay.setDisplayHeight(20);
        itemDisplay.setDisplayWidth(20);

//        itemDisplay.setTeleportDuration(50);
//        itemDisplay.setInterpolationDuration(40);
//        itemDisplay.setInterpolationDelay(-1);

        // https://github.com/JOML-CI/JOML/blob/main/src/jmh/java/org/joml/jmh/Matrix4f.java
        // https://misode.github.io/transformation/
        Vector axis = player.getEyeLocation().getDirection();
        Transformation transformation = itemDisplay.getTransformation();
        Quaternionf dest2 = transformation.getRightRotation(); // https://www.evl.uic.edu/ralph/508S98/coordinates.html

        Location location = player.getEyeLocation();

        ParticleUtil.VALUES.entrySet().stream().filter(x -> x.getKey().startsWith("trans")).map(Map.Entry::getValue).forEach(x -> {
            double[] trans = Arrays.stream(x).toArray();
            if (ParticleUtil.hasValue("perp")) {
                Vector perp = ParticleUtil.getPerpendicularVector(new Vector(trans[1], trans[2], trans[3]));
                trans[1] = perp.getX();
                trans[2] = perp.getY();
                trans[3] = perp.getZ();
            }
            transformation.getRightRotation().rotateAxis((float) trans[0], new Vector3f((float) trans[1], (float) trans[2], (float) trans[3]));
        });

        Vector direction = location.getDirection();
        if (ParticleUtil.hasValue("face")) {
            ParticleDisplay.Quaternion q = ParticleUtil.INTERNAL_CALL_LookRotation(direction, new Vector(0, 1, 0));
            transformation.getRightRotation().mul((float) q.x, (float) q.y, (float) q.z, (float) q.w);
//            transformation.getRightRotation().rotateAxis((float) Math.toRadians(location.getYaw()), new Vector3f(0f, 1f, 0f));
//            transformation.getRightRotation().rotateAxis((float) Math.toRadians(location.getPitch()), new Vector3f(1f, 0f, 0f));
        }
        if (ParticleUtil.hasValue("faf")) {
            transformation.getRightRotation().lookAlong(
                    new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ()),
                    new Vector3f(0, 1, 0));
        }

        itemDisplay.setTransformation(transformation);
        // itemDisplay.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(0.5));
//        transformation.getLeftRotation()
//                .set(new AxisAngle4f(20, (float) axis.getX(), (float) axis.getY(), (float) axis.getZ()));
//        itemDisplay.setTransformation(transformation);
        return itemDisplay;
    }

    static void ToQuaternion(Quaternionf q, double roll, double pitch, double yaw) // roll (x), pitch (y), yaw (z), angles are in radians
    {
        // Abbreviations for the various angular functions

        float cr = (float) Math.cos(roll * 0.5);
        float sr = (float) Math.sin(roll * 0.5);
        float cp = (float) Math.cos(pitch * 0.5);
        float sp = (float) Math.sin(pitch * 0.5);
        float cy = (float) Math.cos(yaw * 0.5);
        float sy = (float) Math.sin(yaw * 0.5);

        q.w = cr * cp * cy + sr * sp * sy;
        q.x = sr * cp * cy - cr * sp * sy;
        q.y = cr * sp * cy + sr * cp * sy;
        q.z = cr * cp * sy - sr * sp * cy;
    }

    public void rotate(Quaternionf rotation, float x, float y, float z) {
        // Use modulus to fix values to below 360 then convert values to radians
        float newX = (float) Math.toRadians(x);
        float newY = (float) Math.toRadians(y);
        float newZ = (float) Math.toRadians(z);

        // Create a quaternion with the delta rotation values
        Quaternionf rotationDelta = new Quaternionf();
        rotationDelta.rotationXYZ(newX, newY, newZ);

        // Calculate the inverse of the delta quaternion
        Quaternionf conjugate = rotationDelta.conjugate();

        // Multiply this transform by the rotation delta quaternion and its inverse
        rotation.mul(rotationDelta).mul(conjugate);
    }

}
